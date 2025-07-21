/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.fileupload2.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.fileupload2.core.FileItemInput.ItemSkippedException;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractOrigin;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.output.NullOutputStream;

/**
 * Low-level API for processing file uploads.
 *
 * <p>
 * This class can be used to process data streams conforming to MIME 'multipart' format as defined in <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC
 * 1867</a>. Arbitrarily large amounts of data in the stream can be processed under constant memory usage.
 * </p>
 * <p>
 * The format of the stream is defined in the following way:
 * </p>
 * <pre>
 *   multipart-body := preamble 1*encapsulation close-delimiter epilogue<br>
 *   encapsulation := delimiter body CRLF<br>
 *   delimiter := "--" boundary CRLF<br>
 *   close-delimiter := "--" boundary "--"<br>
 *   preamble := &lt;ignore&gt;<br>
 *   epilogue := &lt;ignore&gt;<br>
 *   body := header-part CRLF body-part<br>
 *   header-part := 1*header CRLF<br>
 *   header := header-name ":" header-value<br>
 *   header-name := &lt;printable ASCII characters except ":"&gt;<br>
 *   header-value := &lt;any ASCII characters except CR &amp; LF&gt;<br>
 *   body-data := &lt;arbitrary data&gt;<br>
 * </pre>
 *
 * <p>
 * Note that body-data can contain another mulipart entity. There is limited support for single pass processing of such nested streams. The nested stream is
 * <strong>required</strong> to have a boundary token of the same length as the parent stream (see {@link #setBoundary(byte[])}).
 * </p>
 * <p>
 * Here is an example of usage of this class:
 * </p>
 *
 * <pre>
 * try {
 *     MultipartInput multipartStream = MultipartInput.builder()
 *             .setBoundary(boundary)
 *             .setInputStream(input)
 *             .get();
 *     boolean nextPart = multipartStream.skipPreamble();
 *     OutputStream output;
 *     while (nextPart) {
 *         String header = multipartStream.readHeaders();
 *         // process headers
 *         // create some output stream
 *         multipartStream.readBodyData(output);
 *         nextPart = multipartStream.readBoundary();
 *     }
 * } catch (MultipartInput.MalformedStreamException e) {
 *     // the stream failed to follow required syntax
 * } catch (IOException e) {
 *     // a read or write error occurred
 * }
 * </pre>
 */
public final class MultipartInput {

    /**
     * Builds a new {@link MultipartInput} instance.
     * <p>
     * For example:
     * </p>
     *
     * <pre>{@code
     * MultipartInput factory = MultipartInput.builder().setPath(path).setBufferSize(DEFAULT_THRESHOLD).get();
     * }
     * </pre>
     */
    public static class Builder extends AbstractStreamBuilder<MultipartInput, Builder> {

        /**
         * Boundary.
         */
        private byte[] boundary;

        /**
         * Progress notifier.
         */
        private ProgressNotifier progressNotifier;

        /** The  per part size limit for headers.
         */
        private int partHeaderSizeMax = DEFAULT_PART_HEADER_SIZE_MAX;

        /**
         * Constructs a new instance.
         */
        public Builder() {
            setBufferSizeDefault(DEFAULT_BUFSIZE);
        }

        /**
         * Constructs a new instance.
         * <p>
         * This builder uses the InputStream, buffer size, boundary and progress notifier aspects.
         * </p>
         * <p>
         * You must provide an origin that can be converted to a Reader by this builder, otherwise, this call will throw an
         * {@link UnsupportedOperationException}.
         * </p>
         *
         * @return a new instance.
         * @throws IOException                   if an I/O error occurs.
         * @throws UnsupportedOperationException if the origin cannot provide a Path.
         * @see AbstractOrigin#getReader(Charset)
         */
        @Override
        public MultipartInput get() throws IOException {
            return new MultipartInput(getInputStream(), boundary, getBufferSize(), getPartHeaderSizeMax(), progressNotifier);
        }

        /** Returns the per part size limit for headers.
         * @return The maximum size of the headers in bytes.
         * @since 2.0.0-M4
         */
        public int getPartHeaderSizeMax() {
            return partHeaderSizeMax;
        }

        /**
         * Sets the boundary.
         *
         * @param boundary the boundary.
         * @return {@code this} instance.
         */
        public Builder setBoundary(final byte[] boundary) {
            this.boundary = boundary;
            return this;
        }

        /** Sets the per part size limit for headers.
         * @param partHeaderSizeMax The maximum size of the headers in bytes.
         * @return This builder.
         * @since 2.0.0-M4
         */
        public Builder setPartHeaderSizeMax(final int partHeaderSizeMax) {
            this.partHeaderSizeMax = partHeaderSizeMax;
            return this;
        }

        /**
             * Sets the progress notifier.
             *
             * @param progressNotifier progress notifier.
             * @return {@code this} instance.
             */
            public Builder setProgressNotifier(final ProgressNotifier progressNotifier) {
                this.progressNotifier = progressNotifier;
                return this;
            }

    }

    /**
     * Signals an attempt to set an invalid boundary token.
     */
    public static class FileUploadBoundaryException extends FileUploadException {

        /**
         * The UID to use when serializing this instance.
         */
        private static final long serialVersionUID = 2;

        /**
         * Constructs an instance with the specified detail message.
         *
         * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
         */
        public FileUploadBoundaryException(final String message) {
            super(message);
        }

    }

    /**
     * An {@link InputStream} for reading an items contents.
     */
    public class ItemInputStream extends InputStream {

        /**
         * Offset when converting negative bytes to integers.
         */
        private static final int BYTE_POSITIVE_OFFSET = 256;

        /**
         * The number of bytes, which have been read so far.
         */
        private long total;

        /**
         * The number of bytes, which must be hold, because they might be a part of the boundary.
         */
        private int pad;

        /**
         * The current offset in the buffer.
         */
        private int pos;

        /**
         * Whether the stream is already closed.
         */
        private boolean closed;

        /**
         * Creates a new instance.
         */
        ItemInputStream() {
            findSeparator();
        }

        /**
         * Returns the number of bytes, which are currently available, without blocking.
         *
         * @throws IOException An I/O error occurs.
         * @return Number of bytes in the buffer.
         */
        @Override
        public int available() throws IOException {
            if (pos == -1) {
                return tail - head - pad;
            }
            return pos - head;
        }

        private void checkOpen() throws ItemSkippedException {
            if (closed) {
                throw new FileItemInput.ItemSkippedException("checkOpen()");
            }
        }

        /**
         * Closes the input stream.
         *
         * @throws IOException An I/O error occurred.
         */
        @Override
        public void close() throws IOException {
            close(false);
        }

        /**
         * Closes the input stream.
         *
         * @param closeUnderlying Whether to close the underlying stream (hard close)
         * @throws IOException An I/O error occurred.
         */
        public void close(final boolean closeUnderlying) throws IOException {
            if (closed) {
                return;
            }
            if (closeUnderlying) {
                closed = true;
                input.close();
            } else {
                for (;;) {
                    var avail = available();
                    if (avail == 0) {
                        avail = makeAvailable();
                        if (avail == 0) {
                            break;
                        }
                    }
                    if (skip(avail) != avail) {
                        // TODO What to do?
                    }
                }
            }
            closed = true;
        }

        /**
         * Called for finding the separator.
         */
        private void findSeparator() {
            pos = MultipartInput.this.findSeparator();
            if (pos == -1) {
                if (tail - head > keepRegion) {
                    pad = keepRegion;
                } else {
                    pad = tail - head;
                }
            }
        }

        /**
         * Gets the number of bytes, which have been read by the stream.
         *
         * @return Number of bytes, which have been read so far.
         */
        public long getBytesRead() {
            return total;
        }

        /**
         * Tests whether this instance is closed.
         *
         * @return whether this instance is closed.
         */
        public boolean isClosed() {
            return closed;
        }

        /**
         * Attempts to read more data.
         *
         * @return Number of available bytes
         * @throws IOException An I/O error occurred.
         */
        private int makeAvailable() throws IOException {
            if (pos != -1) {
                return 0;
            }

            // Move the data to the beginning of the buffer.
            total += tail - head - pad;
            System.arraycopy(buffer, tail - pad, buffer, 0, pad);

            // Refill buffer with new data.
            head = 0;
            tail = pad;

            for (;;) {
                final var bytesRead = input.read(buffer, tail, bufSize - tail);
                if (bytesRead == -1) {
                    // The last pad amount is left in the buffer.
                    // Boundary can't be in there so signal an error
                    // condition.
                    final var msg = "Stream ended unexpectedly";
                    throw new MalformedStreamException(msg);
                }
                if (notifier != null) {
                    notifier.noteBytesRead(bytesRead);
                }
                tail += bytesRead;

                findSeparator();
                final var av = available();

                if (av > 0 || pos != -1) {
                    return av;
                }
            }
        }

        /**
         * Reads the next byte in the stream.
         *
         * @return The next byte in the stream, as a non-negative integer, or -1 for EOF.
         * @throws IOException An I/O error occurred.
         */
        @Override
        public int read() throws IOException {
            checkOpen();
            if (available() == 0 && makeAvailable() == 0) {
                return -1;
            }
            ++total;
            final int b = buffer[head++];
            if (b >= 0) {
                return b;
            }
            return b + BYTE_POSITIVE_OFFSET;
        }

        /**
         * Reads bytes into the given buffer.
         *
         * @param b   The destination buffer, where to write to.
         * @param off Offset of the first byte in the buffer.
         * @param len Maximum number of bytes to read.
         * @return Number of bytes, which have been actually read, or -1 for EOF.
         * @throws IOException An I/O error occurred.
         */
        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            checkOpen();
            if (len == 0) {
                return 0;
            }
            var res = available();
            if (res == 0) {
                res = makeAvailable();
                if (res == 0) {
                    return -1;
                }
            }
            res = Math.min(res, len);
            System.arraycopy(buffer, head, b, off, res);
            head += res;
            total += res;
            return res;
        }

        /**
         * Skips the given number of bytes.
         *
         * @param bytes Number of bytes to skip.
         * @return The number of bytes, which have actually been skipped.
         * @throws IOException An I/O error occurred.
         */
        @Override
        public long skip(final long bytes) throws IOException {
            checkOpen();
            var available = available();
            if (available == 0) {
                available = makeAvailable();
                if (available == 0) {
                    return 0;
                }
            }
            // Fix "Implicit narrowing conversion in compound assignment"
            // https://github.com/apache/commons-fileupload/security/code-scanning/118
            // Math.min always returns an int because available is an int.
            final var res = Math.toIntExact(Math.min(available, bytes));
            head += res;
            return res;
        }

    }

    /**
     * Signals that the input stream fails to follow the required syntax.
     */
    public static class MalformedStreamException extends FileUploadException {

        /**
         * The UID to use when serializing this instance.
         */
        private static final long serialVersionUID = 2;

        /**
         * Constructs an {@code MalformedStreamException} with the specified detail message.
         *
         * @param message The detail message.
         */
        public MalformedStreamException(final String message) {
            super(message);
        }

        /**
         * Constructs an {@code MalformedStreamException} with the specified detail message.
         *
         * @param message The detail message.
         * @param cause   The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is permitted, and indicates that the
         *                cause is nonexistent or unknown.)
         */
        public MalformedStreamException(final String message, final Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * Internal class, which is used to invoke the {@link ProgressListener}.
     */
    public static class ProgressNotifier {

        /**
         * The listener to invoke.
         */
        private final ProgressListener progressListener;

        /**
         * Number of expected bytes, if known, or -1.
         */
        private final long contentLength;

        /**
         * Number of bytes, which have been read so far.
         */
        private long bytesRead;

        /**
         * Number of items, which have been read so far.
         */
        private int items;

        /**
         * Creates a new instance with the given listener and content length.
         *
         * @param progressListener The listener to invoke.
         * @param contentLength    The expected content length.
         */
        public ProgressNotifier(final ProgressListener progressListener, final long contentLength) {
            this.progressListener = progressListener != null ? progressListener : ProgressListener.NOP;
            this.contentLength = contentLength;
        }

        /**
         * Called to indicate that bytes have been read.
         *
         * @param byteCount Number of bytes, which have been read.
         */
        void noteBytesRead(final int byteCount) {
            //
            // Indicates, that the given number of bytes have been read from the input stream.
            //
            bytesRead += byteCount;
            notifyListener();
        }

        /**
         * Called to indicate, that a new file item has been detected.
         */
        public void noteItem() {
            ++items;
            notifyListener();
        }

        /**
         * Called for notifying the listener.
         */
        private void notifyListener() {
            progressListener.update(bytesRead, contentLength, items);
        }

    }

    /**
     * The Carriage Return ASCII character value.
     */
    public static final byte CR = 0x0D;

    /**
     * The Line Feed ASCII character value.
     */
    public static final byte LF = 0x0A;

    /**
     * The dash (-) ASCII character value.
     */
    public static final byte DASH = 0x2D;

    /**
     * The default length of the buffer used for processing a request.
     */
    static final int DEFAULT_BUFSIZE = 4096;

    /**
     * Default per part header size limit in bytes.
     * @since 2.0.0-M4
     */
    public static final int DEFAULT_PART_HEADER_SIZE_MAX = 512;

    /**
     * A byte sequence that marks the end of {@code header-part} ({@code CRLFCRLF}).
     */
    static final byte[] HEADER_SEPARATOR = { CR, LF, CR, LF };

    /**
     * A byte sequence that that follows a delimiter that will be followed by an encapsulation ({@code CRLF}).
     */
    static final byte[] FIELD_SEPARATOR = { CR, LF };

    /**
     * A byte sequence that that follows a delimiter of the last encapsulation in the stream ({@code --}).
     */
    static final byte[] STREAM_TERMINATOR = { DASH, DASH };

    /**
     * A byte sequence that precedes a boundary ({@code CRLF--}).
     */
    static final byte[] BOUNDARY_PREFIX = { CR, LF, DASH, DASH };

    /**
     * Compares {@code count} first bytes in the arrays {@code a} and {@code b}.
     *
     * @param a     The first array to compare.
     * @param b     The second array to compare.
     * @param count How many bytes should be compared.
     * @return {@code true} if {@code count} first bytes in arrays {@code a} and {@code b} are equal.
     */
    static boolean arrayEquals(final byte[] a, final byte[] b, final int count) {
        for (var i = 0; i < count; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The input stream from which data is read.
     */
    private final InputStream input;

    /**
     * The length of the boundary token plus the leading {@code CRLF--}.
     */
    private int boundaryLength;

    /**
     * The amount of data, in bytes, that must be kept in the buffer in order to detect delimiters reliably.
     */
    private final int keepRegion;

    /**
     * The byte sequence that partitions the stream.
     */
    private final byte[] boundary;

    /**
     * The table for Knuth-Morris-Pratt search algorithm.
     */
    private final int[] boundaryTable;

    /**
     * The length of the buffer used for processing the request.
     */
    private final int bufSize;

    /**
     * The buffer used for processing the request.
     */
    private final byte[] buffer;

    /**
     * The index of first valid character in the buffer. <br>
     * 0 <= head < bufSize
     */
    private int head;

    /**
     * The index of last valid character in the buffer + 1. <br>
     * 0 <= tail <= bufSize
     */
    private int tail;

    /**
     * The content encoding to use when reading headers.
     */
    private Charset headerCharset;

    /**
     * The progress notifier, if any, or null.
     */
    private final ProgressNotifier notifier;

    /**
     * The maximum size of the headers in bytes.
     */
    private final int partHeaderSizeMax;

    /**
     * Constructs a {@code MultipartInput} with a custom size buffer.
     * <p>
     * Note that the buffer must be at least big enough to contain the boundary string, plus 4 characters for CR/LF and double dash, plus at least one byte of
     * data. Too small a buffer size setting will degrade performance.
     * </p>
     *
     * @param input      The {@code InputStream} to serve as a data source.
     * @param boundary   The token used for dividing the stream into {@code encapsulations}.
     * @param bufferSize The size of the buffer to be used, in bytes.
     * @param notifier   The notifier, which is used for calling the progress listener, if any.
     * @throws IllegalArgumentException If the buffer size is too small.
     */
    private MultipartInput(final InputStream input, final byte[] boundary, final int bufferSize, final int partHeaderSizeMax, final ProgressNotifier notifier) {
        if (boundary == null) {
            throw new IllegalArgumentException("boundary may not be null");
        }
        // We prepend CR/LF to the boundary to chop trailing CR/LF from
        // body-data tokens.
        this.boundaryLength = boundary.length + BOUNDARY_PREFIX.length;
        if (bufferSize < this.boundaryLength + 1) {
            throw new IllegalArgumentException("The buffer size specified for the MultipartInput is too small");
        }

        this.input = input;
        this.bufSize = Math.max(bufferSize, boundaryLength * 2);
        this.buffer = new byte[this.bufSize];
        this.notifier = notifier;
        this.partHeaderSizeMax = partHeaderSizeMax;

        this.boundary = new byte[this.boundaryLength];
        this.boundaryTable = new int[this.boundaryLength + 1];
        this.keepRegion = this.boundary.length;

        System.arraycopy(BOUNDARY_PREFIX, 0, this.boundary, 0, BOUNDARY_PREFIX.length);
        System.arraycopy(boundary, 0, this.boundary, BOUNDARY_PREFIX.length, boundary.length);
        computeBoundaryTable();

        head = 0;
        tail = 0;
    }

    /**
     * Computes the table used for Knuth-Morris-Pratt search algorithm.
     */
    private void computeBoundaryTable() {
        var position = 2;
        var candidate = 0;

        boundaryTable[0] = -1;
        boundaryTable[1] = 0;

        while (position <= boundaryLength) {
            if (boundary[position - 1] == boundary[candidate]) {
                boundaryTable[position] = candidate + 1;
                candidate++;
                position++;
            } else if (candidate > 0) {
                candidate = boundaryTable[candidate];
            } else {
                boundaryTable[position] = 0;
                position++;
            }
        }
    }

    /**
     * Reads {@code body-data} from the current {@code encapsulation} and discards it.
     * <p>
     * Use this method to skip encapsulations you don't need or don't understand.
     * </p>
     *
     * @return The amount of data discarded.
     * @throws MalformedStreamException if the stream ends unexpectedly.
     * @throws IOException              if an i/o error occurs.
     */
    public long discardBodyData() throws MalformedStreamException, IOException {
        return readBodyData(NullOutputStream.INSTANCE);
    }

    /**
     * Searches for a byte of specified value in the {@code buffer}, starting at the specified {@code position}.
     *
     * @param value The value to find.
     * @param pos   The starting position for searching.
     * @return The position of byte found, counting from beginning of the {@code buffer}, or {@code -1} if not found.
     */
    protected int findByte(final byte value, final int pos) {
        for (var i = pos; i < tail; i++) {
            if (buffer[i] == value) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Searches for the {@code boundary} in the {@code buffer} region delimited by {@code head} and {@code tail}.
     *
     * @return The position of the boundary found, counting from the beginning of the {@code buffer}, or {@code -1} if not found.
     */
    protected int findSeparator() {
        var bufferPos = this.head;
        var tablePos = 0;
        while (bufferPos < this.tail) {
            while (tablePos >= 0 && buffer[bufferPos] != boundary[tablePos]) {
                tablePos = boundaryTable[tablePos];
            }
            bufferPos++;
            tablePos++;
            if (tablePos == boundaryLength) {
                return bufferPos - boundaryLength;
            }
        }
        return -1;
    }

    /**
     * Gets the character encoding used when reading the headers of an individual part. When not specified, or {@code null}, the platform default encoding is
     * used.
     *
     * @return The encoding used to read part headers.
     */
    public Charset getHeaderCharset() {
        return headerCharset;
    }

    /** Returns the per part size limit for headers.
     *
     * @return The maximum size of the headers in bytes.
     * @since 2.0.0-M4
     */
    public int getPartHeaderSizeMax() {
        return partHeaderSizeMax;
    }

    /**
     * Creates a new {@link ItemInputStream}.
     *
     * @return A new instance of {@link ItemInputStream}.
     */
    public ItemInputStream newInputStream() {
        return new ItemInputStream();
    }

    /**
     * Reads {@code body-data} from the current {@code encapsulation} and writes its contents into the output {@code Stream}.
     * <p>
     * Arbitrary large amounts of data can be processed by this method using a constant size buffer. (see {@link MultipartInput#builder()}).
     * </p>
     *
     * @param output The {@code Stream} to write data into. May be null, in which case this method is equivalent to {@link #discardBodyData()}.
     * @return the amount of data written.
     * @throws MalformedStreamException if the stream ends unexpectedly.
     * @throws IOException              if an i/o error occurs.
     */
    public long readBodyData(final OutputStream output) throws MalformedStreamException, IOException {
        try (var inputStream = newInputStream()) {
            return IOUtils.copyLarge(inputStream, output);
        }
    }

    /**
     * Skips a {@code boundary} token, and checks whether more {@code encapsulations} are contained in the stream.
     *
     * @return {@code true} if there are more encapsulations in this stream; {@code false} otherwise.
     * @throws FileUploadSizeException  if the bytes read from the stream exceeded the size limits
     * @throws MalformedStreamException if the stream ends unexpectedly or fails to follow required syntax.
     */
    public boolean readBoundary() throws FileUploadSizeException, MalformedStreamException {
        final var marker = new byte[2];
        final boolean nextChunk;
        head += boundaryLength;
        try {
            marker[0] = readByte();
            if (marker[0] == LF) {
                // Work around IE5 Mac bug with input type=image.
                // Because the boundary delimiter, not including the trailing
                // CRLF, must not appear within any file (RFC 2046, section
                // 5.1.1), we know the missing CR is due to a buggy browser
                // rather than a file containing something similar to a
                // boundary.
                return true;
            }

            marker[1] = readByte();
            if (arrayEquals(marker, STREAM_TERMINATOR, 2)) {
                nextChunk = false;
            } else if (arrayEquals(marker, FIELD_SEPARATOR, 2)) {
                nextChunk = true;
            } else {
                throw new MalformedStreamException("Unexpected characters follow a boundary");
            }
        } catch (final FileUploadSizeException e) {
            throw e;
        } catch (final IOException e) {
            throw new MalformedStreamException("Stream ended unexpectedly", e);
        }
        return nextChunk;
    }

    /**
     * Reads a byte from the {@code buffer}, and refills it as necessary.
     *
     * @return The next byte from the input stream.
     * @throws IOException if there is no more data available.
     */
    public byte readByte() throws IOException {
        // Buffer depleted ?
        if (head == tail) {
            head = 0;
            // Refill.
            tail = input.read(buffer, head, bufSize);
            if (tail == -1) {
                // No more data available.
                throw new IOException("No more data is available");
            }
            if (notifier != null) {
                notifier.noteBytesRead(tail);
            }
        }
        return buffer[head++];
    }

    /**
     * Reads the {@code header-part} of the current {@code encapsulation}.
     * <p>
     * Headers are returned verbatim to the input stream, including the trailing {@code CRLF} marker. Parsing is left to the application.
     * </p>
     * <p>
     * <strong>TODO</strong> allow limiting maximum header size to protect against abuse.
     * </p>
     *
     * @return The {@code header-part} of the current encapsulation.
     * @throws FileUploadSizeException  if the bytes read from the stream exceeded the size limits.
     * @throws MalformedStreamException if the stream ends unexpectedly.
     */
    public String readHeaders() throws FileUploadSizeException, MalformedStreamException {
        var i = 0;
        byte b;
        // to support multi-byte characters
        final var baos = new ByteArrayOutputStream();
        var size = 0;
        while (i < HEADER_SEPARATOR.length) {
            try {
                b = readByte();
            } catch (final FileUploadSizeException e) {
                // wraps a FileUploadSizeException, re-throw as it will be unwrapped later
                throw e;
            } catch (final IOException e) {
                throw new MalformedStreamException("Stream ended unexpectedly", e);
            }
            final int phsm = getPartHeaderSizeMax();
            if (phsm != -1 && ++size > phsm) {
                throw new FileUploadSizeException(
                        String.format("Header section has more than %s bytes (maybe it is not properly terminated)", Integer.valueOf(phsm)), phsm, size);
            }
            if (b == HEADER_SEPARATOR[i]) {
                i++;
            } else {
                i = 0;
            }
            baos.write(b);
        }
        try {
            return baos.toString(Charsets.toCharset(headerCharset, Charset.defaultCharset()).name());
        } catch (final UnsupportedEncodingException e) {
            // not possible
            throw new IllegalStateException(e);
        }
    }

    /**
     * Changes the boundary token used for partitioning the stream.
     * <p>
     * This method allows single pass processing of nested multipart streams.
     * </p>
     * <p>
     * The boundary token of the nested stream is {@code required} to be of the same length as the boundary token in parent stream.
     * </p>
     * <p>
     * Restoring the parent stream boundary token after processing of a nested stream is left to the application.
     * </p>
     *
     * @param boundary The boundary to be used for parsing of the nested stream.
     * @throws FileUploadBoundaryException if the {@code boundary} has a different length than the one being currently parsed.
     */
    public void setBoundary(final byte[] boundary) throws FileUploadBoundaryException {
        if (boundary.length != boundaryLength - BOUNDARY_PREFIX.length) {
            throw new FileUploadBoundaryException("The length of a boundary token cannot be changed");
        }
        System.arraycopy(boundary, 0, this.boundary, BOUNDARY_PREFIX.length, boundary.length);
        computeBoundaryTable();
    }

    /**
     * Sets the character encoding to be used when reading the headers of individual parts. When not specified, or {@code null}, the platform default encoding
     * is used.
     *
     * @param headerCharset The encoding used to read part headers.
     */
    public void setHeaderCharset(final Charset headerCharset) {
        this.headerCharset = headerCharset;
    }

    /**
     * Finds the beginning of the first {@code encapsulation}.
     *
     * @return {@code true} if an {@code encapsulation} was found in the stream.
     * @throws IOException if an i/o error occurs.
     */
    public boolean skipPreamble() throws IOException {
        // First delimiter may be not preceded with a CRLF.
        System.arraycopy(boundary, 2, boundary, 0, boundary.length - 2);
        boundaryLength = boundary.length - 2;
        computeBoundaryTable();
        try {
            // Discard all data up to the delimiter.
            discardBodyData();

            // Read boundary - if succeeded, the stream contains an
            // encapsulation.
            return readBoundary();
        } catch (final MalformedStreamException e) {
            return false;
        } finally {
            // Restore delimiter.
            System.arraycopy(boundary, 0, boundary, 2, boundary.length - 2);
            boundaryLength = boundary.length;
            boundary[0] = CR;
            boundary[1] = LF;
            computeBoundaryTable();
        }
    }

}
