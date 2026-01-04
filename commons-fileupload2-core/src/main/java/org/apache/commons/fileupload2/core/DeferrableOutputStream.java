/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.fileupload2.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;


/**
 * An {@link OutputStream}, which keeps its data in memory, until a configured
 * threshold is reached. If that is the case, a temporary file is being created,
 * and the in-memory data is transferred to that file. All following data will
 * be written to that file, too.
 *
 * In other words: If an uploaded file is small, then it will be kept completely
 * in memory. On the other hand, if the uploaded file's size exceeds the
 * configured threshold, it it considered a large file, and the data is kept
 * in a temporary file.
 *
 * More precisely, this output stream supports three modes of operation:
 * <ol>
 *   <li>{@code threshold=-1}: <em>Always</em> create a temporary file, even if
 *     the uploaded file is empty.</li>
 *   <li>{@code threshold=0}: Don't create empty, temporary files. (Create a
 *     temporary file, as soon as the first byte is written.)</li>
 *   <li>{@code threshold>0}: Create a temporary file, if the size exceeds the
 *     threshold, otherwise keep the file in memory.</li>
 * </ol>
 *
 * Technically, this is similar to
 * {@link org.apache.commons.io.output.DeferredFileOutputStream}, which has
 * been used in the past, except that this implementation observes
 * a precisely specified behavior, and semantics, that match the needs of the
 * {@link DiskFileItem}.
 *
 * Background: Over the various versions of commons-io, the
 * {@link org.apache.commons.io.output.DeferredFileOutputStream} has changed
 * semantics, and behavior more than once.
 * (For details, see
 * <a href="https://issues.apache.org/jira/browse/FILEUPLOAD-295">FILEUPLOAD-295</a>)
 */
public class DeferrableOutputStream extends OutputStream {

    /**
     * Interface of a listener object, that wishes to be notified about
     * state changes.
     */
    public interface Listener {

        /**
         * Called, after {@link #persist()} has been invoked,
         *   and the temporary file has been created.
         * @param path Path of the temporary file, that has been
         *   created. All in-memory data has been transferred to
         *   that file, but it is still opened.
         */
         default void persisted(final Path path) { }
    }

    /**
     * This enumeration represents the possible states of the {@link DeferrableOutputStream}.
     */
    public enum State {

        /**
         * The stream object has been created with a non-negative threshold,
         * but so far no data has been written.
         */
        initialized,

        /**
         * The stream object has been created with a non-negative threshold,
         * and some data has been written, but the threshold is not yet exceeded,
         * and the data is still kept in memory.
         */
        opened,

        /**
         * Either of the following conditions is given:
         * <ol>
         *   <li>The stream object has been created with a threshold of -1, or</li>
         *   <li>the stream object has been created with a non-negative threshold,
         *     and some data has been written. The number of bytes, that have
         *     been written, exceeds the configured threshold.</li>
         * </ol>
         * In either case, a temporary file has been created, and all data has been
         * written to the temporary file, erasing all existing data from memory.
         */
        persisted,

        /**
         * The stream has been closed, and data can no longer be written. It is
         * now valid to invoke {@link DeferrableOutputStream#getInputStream()}.
         */
        closed
    }

    /**
     * The configured threshold, as an integer. This variable isn't actually
     * used. Instead {@link #longThreshold} is used.
     * @see #longThreshold
     */
    private final int threshold;

    /**
     * The configured threshold, as a long integer. (Using a long integer
     * enables proper handling of the threshold, when the file size is
     * approaching {@link Integer#MAX_VALUE}.
     * @see #threshold
     */
    private final long longThreshold;

    /**
     * This supplier will be invoked, if the temporary file is created,
     * t
     *  determine the temporary file's location.
     * @see #path
     */
    private final Supplier<Path> pathSupplier;

    /**
     * If a temporary file has been created: Path of the temporary
     * file. Otherwise null.
     * @see #pathSupplier
     */
    private Path path;

    /**
     * If no temporary file was created: A stream, to which the
     * incoming data is being written, until the threshold is reached.
     * Otherwise null.
     */
    private ByteArrayOutputStream baos;

    /**
     * If no temporary file was created, and the stream is closed:
     * The in-memory data, that was written to the stream. Otherwise null.
     */
    private byte[] bytes;

    /**
     * If a temporary file has been created: An open stream
     * for writing to that file. Otherwise null.
     */
    private OutputStream out;

    /**
     * The streams current state.
     */
    private State state;

    /**
     * True, if the stream has ever been in state {@link State#persisted}.
     * Or, in other words: True, if a temporary file has been created.
     */
    private boolean wasPersisted;

    /**
     * Number of bytes, that have been written to this stream so far.
     */
    private long size;

    /**
     * The configured {@link Listener}, if any, or null.
     */
    private final Listener listener;

    /**
     * Creates a new instance with the given threshold, and the given supplier for a
     * temporary files path.
     * If the threshold is -1, then the temporary file will be created immediately, and
     * no in-memory data will be kept, at all.
     * If the threshold is 0, then the temporary file will be created, as soon as the
     * first byte will be written, but no in-memory data will be kept.
     * If the threshold is &gt; 0, then the temporary file will be created, as soon as that
     * number of bytes have been written. Up to that point, data will be kept in an
     * in-memory buffer.
     *
     * @param threshold Either of -1 (Create the temporary file immediately), 0 (Create
     *   the temporary file, as soon as data is being written for the first time), or &gt;0
     *   (Keep data in memory, as long as the given number of bytes is reached, then
     *   create a temporary file, and continue using that).
     * @param pathSupplier A supplier for the temporary files path. This supplier must
     *   not return null. The file's directory will be created, if necessary, by
     *   invoking {@link Files#createDirectories(Path, java.nio.file.attribute.FileAttribute...)}.
     * @param listener An optional listener, which is being notified about important state
     *   changes.
     * @throws IOException Creating the temporary file (in the case of threshold -1)
     *   has failed.
     */
    public DeferrableOutputStream(final int threshold, final Supplier<Path> pathSupplier, final Listener listener) throws IOException {
        if (threshold < 0) {
            this.threshold = -1;
        } else {
            this.threshold = threshold;
        }
        longThreshold = (long) threshold;
        this.pathSupplier = pathSupplier;
        this.listener = listener;
        checkThreshold(0);
    }

    /**
     * Called to check, whether the threshold will be exceeded, if the given number
     * of bytes are written to the stream. If so, persists the in-memory data by
     * creating a new, temporary file, and writing the in-memory data to the file.
     * @param numberOfIncomingBytes The number of bytes, which are about to be written.
     * @return The actual output stream, to which the incoming data may be written.
     *   If the threshold is not yet exceeded, then this will be an internal
     *   {@link ByteArrayOutputStream}, otherwise a stream, which is writing to the
     *   temporary output file.
     * @throws IOException Persisting the in-memory data to a temporary file
     *   has failed.
     */
    protected OutputStream checkThreshold(final int numberOfIncomingBytes) throws IOException {
        if (state == null) {
            // Called from the constructor, state is unspecified.
            if (threshold == -1) {
                return persist();
            } else {
                baos = new ByteArrayOutputStream();
                bytes = null;
                state = State.initialized;
                return baos;
            }
        } else {
            switch (state) {
            case initialized:
            case opened:
                final int bytesWritten = baos.size();
                if ((long) bytesWritten + (long) numberOfIncomingBytes >= longThreshold) {
                    return persist();
                }
                if (numberOfIncomingBytes > 0) {
                    state = State.opened;
                }
                return baos;
            case persisted:
                // Do nothing, we're staying in the current state.
                return out;
            case closed:
                // Do nothing, we're staying in the current state.
                return null;
            default:
                throw illegalStateError();
            }
        }
    }

    @Override
    public void close() throws IOException {
        switch (state) {
        case initialized:
        case opened:
            bytes = baos.toByteArray();
            baos = null;
            state = State.closed;
            break;
        case persisted:
            bytes = null;
            out.close();
            state = State.closed;
            break;
        case closed:
            // Already closed, do nothing.
            break;
        default:
            throw illegalStateError();
        }
    }

    /**
     * Returns the data, that has been written, if the stream has
     * been closed, and the stream is still in memory
     * ({@link #isInMemory()} returns true). Otherwise, returns null.
     * @return If the stream is closed (no more data can be written),
     *   and the data is still in memory (no temporary file has been
     *   created), returns the data, that has been written. Otherwise,
     *   returns null.
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * If the stream is closed: Returns an {@link InputStream} on the
     * data, that has been written to this stream. Otherwise, throws
     * an {@link IllegalStateException}.
     * @return An {@link InputStream} on the data, that has been
     * written. Never null.
     * @throws IllegalStateException The stream has not yet been
     *   closed.
     * @throws IOException Creating the {@link InputStream} has
     *   failed.
     */
    public InputStream getInputStream() throws IOException {
        if (state == State.closed) {
            if (bytes != null) {
                return new ByteArrayInputStream(bytes);
            } else {
                return Files.newInputStream(path);
            }
        } else {
            throw new IllegalStateException("This stream isn't yet closed.");
        }
    }

    /**
     * Returns the output file, that has been created, if any, or null.
     * The latter is the case, if {@link #isInMemory()} returns true.
     * @return The output file, that has been created, if any, or null.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Returns the number of bytes, that have been written to this stream.
     * @return The number of bytes, that have been written to this stream.
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns the streams current state.
     * @return The streams current state.
     */
    public State getState() {
        return state;
    }

    /**
     * Returns the streams configured threshold.
     * @return The streams configured threshold.
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Returns the path of the output file, if such a file has
     * been created. That is the case, if {@link #isInMemory()}
     * returns false. Otherwise, returns null.
     * @return Path of the created output file, if any, or null.
     */
    private IllegalStateException illegalStateError() {
        throw new IllegalStateException("Expected state initialized|opened|persisted|closed, got " + state.name());
    }

    /**
     * Returns true, if this stream was never persisted,
     * and no output file has been created.
     * @return True, if the stream was never in state
     *   {@link State#persisted}, otherwise false.
     */
    public boolean isInMemory() {
        switch (state) {
        case initialized:
        case opened:
            return true;
        case persisted:
            return false;
        case closed:
            return !wasPersisted;
        default:
            throw illegalStateError();
        }
    }

    /**
     * Create the output file, change the state to {@code persisted}, and
     * return an {@link OutputStream}, which is writing to that file.
     * @return The {@link OutputStream}, which is writing to the created,
     * temporary file.
     * @throws IOException Creating the temporary file has failed.
     */
    protected OutputStream persist() throws IOException {
        final Path p = pathSupplier.get();
        final Path dir = p.getParent();
        if (dir != null) {
            Files.createDirectories(dir);
        }
        final OutputStream os = Files.newOutputStream(p);
        if (baos != null) {
            baos.writeTo(os);
        }

        /**
         * At this point, the output file has been successfully created,
         * and we can safely switch state.
         */
        state = State.persisted;
        wasPersisted = true;
        path = p;
        out = os;
        baos = null;
        bytes = null;
        if (listener != null) {
            listener.persisted(p);
        }
        return os;
    }

    @Override
    public void write(final byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int len) throws IOException {
        if (len > 0) {
            final OutputStream os = checkThreshold(len);
            if (os == null) {
                throw new IOException("This stream has already been closed.");
            }
            bytes = null;
            os.write(buffer, offset, len);
            size += len;
        }
    }

    @Override
    public void write(final int b) throws IOException {
        final OutputStream os = checkThreshold(1);
        if (os == null) {
            throw new IOException("This stream has already been closed.");
        }
        bytes = null;
        os.write(b);
        size++;
    }
}
