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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.apache.commons.fileupload2.core.DeferrableOutputStream.Listener;
import org.apache.commons.fileupload2.core.DeferrableOutputStream.State;
import org.apache.commons.fileupload2.core.FileItemFactory.AbstractFileItemBuilder;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.build.AbstractOrigin;
import org.apache.commons.io.file.PathUtils;


/**
 * The default implementation of the {@link FileItem FileItem} interface.
 *
 * <p>After retrieving an instance of this class from a {@link DiskFileItemFactory} instance (see
 * {@code org.apache.commons.fileupload2.core.servlet.ServletFileUpload
 * #parseRequest(javax.servlet.http.HttpServletRequest)}), you may either request all contents of file at once using {@link #get()} or request an
 * {@link java.io.InputStream InputStream} with {@link #getInputStream()} and process the file without attempting to load it into memory, which may come handy
 * with large files.</p>
 *
 * <p><em>State model</em>: Instances of {@link DiskFileItem} are subject to a carefully designed state model.
 * Depending on the so-called {@link #getThreshold() threshold}, either of the three models are possible:</p>
 * <ol>
 *   <li><em>threshold = -1</em>
 *     Uploaded data is never kept in memory. Instead, a temporary file is being created immediately.
 *
 *     {@link #isInMemory()} will always return false, {@link #getPath()} will always return the path
 *     of an existing file. The temporary file may be empty.</li>
 *   <li><em>threshold = 0</em>
 *     Uploaded data is never kept in memory. (Same as threshold=-1.) However, the temporary file is
 *     only created, if data was uploaded. Or, in other words: The uploaded file will never be
 *     empty.
 *
 *     {@link #isInMemory()} will return true, if no data was uploaded, otherwise it will be false.
 *     In the former case {@link #getPath()} will return null, but in the latter case it returns
 *     the path of an existing, non-empty file.</li>
 *   <li><em>threshold &gt; 0</em>
 *     Uploaded data will be kept in memory, if the size is below the threshold. If the size
 *     is equal to, or above the threshold, then a temporary file has been created, and all
 *     uploaded data has been transferred to that file.
 *
 *     {@link #isInMemory()} returns true, if the size of the uploaded data is below the threshold.
 *     If so, {@link #getPath()} returns null. Otherwise, {@link #isInMemory()} returns false,
 *     and {@link #getPath()} returns the path of an existing, temporary file. The size
 *     of the temporary file is equal to, or above the threshold.</li>
 * </ol>
 *
 * <p>Temporary files, which are created for file items, should be deleted later on. The best way to do this is using a
 * {@link org.apache.commons.io.FileCleaningTracker}, which you can set on the {@link DiskFileItemFactory}. However, if you do use such a tracker, then you must
 * consider the following: Temporary files are automatically deleted as soon as they are no longer needed. (More precisely, when the corresponding instance of
 * {@link java.io.File} is garbage collected.) This is done by the so-called reaper thread, which is started and stopped automatically by the
 * {@link org.apache.commons.io.FileCleaningTracker} when there are files to be tracked. It might make sense to terminate that thread, for example, if your web
 * application ends. See the section on "Resource cleanup" in the users guide of Commons FileUpload.</p>
 */
public final class DiskFileItem implements FileItem<DiskFileItem> {

    /**
     * Builds a new {@link DiskFileItem} instance.
     * <p>
     * For example:
     * </p>
     *
     * <pre>{@code
     * final FileItem fileItem = fileItemFactory.fileItemBuilder()
     *   .setFieldName("FieldName")
     *   .setContentType("ContentType")
     *   .setFormField(true)
     *   .setFileName("FileName")
     *   .setFileItemHeaders(...)
     *   .get();
     * }
     * </pre>
     */
    public static class Builder extends AbstractFileItemBuilder<DiskFileItem, Builder> {

        /**
         * The threshold. We do maintain this separate from the {@link #getBufferSize()},
         * because the parent class might change the value in {@link #setBufferSize(int)}.
         */
        private int threshold;

        /**
         * Constructs a new instance.
         */
        public Builder() {
            setBufferSize(DiskFileItemFactory.DEFAULT_THRESHOLD);
            setPath(PathUtils.getTempDirectory());
            setCharset(DEFAULT_CHARSET);
            setCharsetDefault(DEFAULT_CHARSET);
        }

        /**
         * Constructs a new instance.
         * <p>
         * You must provide an origin that can be converted to a Reader by this builder, otherwise, this call will throw an
         * {@link UnsupportedOperationException}.
         * </p>
         *
         * @return a new instance.
         * @throws UnsupportedOperationException if the origin cannot provide a Path.
         * @see AbstractOrigin#getReader(Charset)
         */
        @Override
        public DiskFileItem get() {
            final var diskFileItem = new DiskFileItem(getFieldName(), getContentType(), isFormField(), getFileName(), getBufferSize(), getPath(),
                    getFileItemHeaders(), getCharset());
            final var tracker = getFileCleaningTracker();
            if (tracker != null) {
                diskFileItem.setFileCleaningTracker(tracker);
            }
            return diskFileItem;
        }

        /**
         * Equivalent to {@link #getThreshold()}.
         * @return The threshold, which is being used.
         * @see #getThreshold()
         * @deprecated Since 2.0.0, use {@link #getThreshold()} instead.
         */
        public int getBufferSize() {
            return getThreshold();
        }

        /**
         * Returns the threshold.
         * @return The threshold.
         */
        public int getThreshold() {
            return threshold;
        }

        /**
         * Eqivalent to {@link #setThreshold(int)}.
         * @param bufferSize The threshold, which is being used.
         * @see #setThreshold(int)
         * @return This builder.
         * @deprecated Since 2.0.0, use {@link #setThreshold(int)} instead.
         */
        @Override
        public Builder setBufferSize(final int bufferSize) {
            return setThreshold(bufferSize);
        }

        /**
         * Sets the threshold. The uploaded data is typically kept in memory, until
         * a certain number of bytes (the threshold) is reached. At this point, the
         * incoming data is transferred to a temporary file, and the in-memory data
         * is removed.
         * @param threshold The threshold, which is being used.
         * @return This builder.
         */
        public Builder setThreshold(final int threshold) {
            this.threshold = threshold;
            return this;
        }
    }

    /**
     * Default content charset to be used when no explicit charset parameter is provided by the sender. Media subtypes of the "text" type are defined to have a
     * default charset value of "ISO-8859-1" when received via HTTP.
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;

    /**
     * UID used in unique file name generation.
     */
    private static final String UID = UUID.randomUUID().toString().replace('-', '_');

    /**
     * Counter used in unique identifier generation.
     */
    private static final AtomicInteger COUNTER = new AtomicInteger();

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Tests if the file name is valid. For example, if it contains a NUL characters, it's invalid. If the file name is valid, it will be returned without any
     * modifications. Otherwise, throw an {@link InvalidPathException}.
     *
     * @param fileName The file name to check
     * @return Unmodified file name, if valid.
     * @throws InvalidPathException The file name is invalid.
     */
    public static String checkFileName(final String fileName) {
        if (fileName != null) {
            // Specific NUL check to build a better exception message.
            final var indexOf0 = fileName.indexOf(0);
            if (indexOf0 != -1) {
                final var sb = new StringBuilder();
                for (var i = 0; i < fileName.length(); i++) {
                    final var c = fileName.charAt(i);
                    switch (c) {
                    case 0:
                        sb.append("\\0");
                        break;
                    default:
                        sb.append(c);
                        break;
                    }
                }
                throw new InvalidPathException(fileName, sb.toString(), indexOf0);
            }
            // Throws InvalidPathException on invalid file names
            Paths.get(fileName);
        }
        return fileName;
    }

    /**
     * Gets an identifier that is unique within the class loader used to load this class, but does not have random-like appearance.
     *
     * @return A String with the non-random looking instance identifier.
     */
    private static String getUniqueId() {
        final var limit = 100_000_000;
        final var current = COUNTER.getAndIncrement();
        var id = Integer.toString(current);

        // If you manage to get more than 100 million of ids, you'll
        // start getting ids longer than 8 characters.
        if (current < limit) {
            id = ("00000000" + id).substring(id.length());
        }
        return id;
    }

    /**
     * The name of the form field as provided by the browser.
     */
    private String fieldName;

    /**
     * The content type passed by the browser, or {@code null} if not defined.
     */
    private final String contentType;

    /**
     * Whether or not this item is a simple form field.
     */
    private volatile boolean isFormField;

    /**
     * The original file name in the user's file system.
     */
    private final String fileName;

    /**
     * The threshold above which uploads will be stored on disk.
     */
    private final int threshold;

    /**
     * The directory in which uploaded files will be stored, if stored on disk.
     */
    private final Path repository;

    /**
     * Output stream for this item.
     */
    private DeferrableOutputStream dos;

    /**
     * The file items headers.
     */
    private FileItemHeaders fileItemHeaders;

    /**
     * Default content Charset to be used when no explicit Charset parameter is provided by the sender.
     */
    private Charset charsetDefault = DEFAULT_CHARSET;

    /**
     * The {@link FileCleaningTracker}, which is being used to remove
     * temporary files.
     */
    private FileCleaningTracker fileCleaningTracker;

    /**
     * Constructs a new {@code DiskFileItem} instance.
     *
     * @param fieldName       The name of the form field.
     * @param contentType     The content type passed by the browser or {@code null} if not specified.
     * @param isFormField     Whether or not this item is a plain form field, as opposed to a file upload.
     * @param fileName        The original file name in the user's file system, or {@code null} if not specified.
     * @param threshold       The threshold, in bytes, below which items will be retained in memory and above which they will be stored as a file.
     * @param repository      The data repository, which is the directory in which files will be created, should the item size exceed the threshold.
     * @param fileItemHeaders The file item headers.
     * @param defaultCharset  The default Charset.
     */
    private DiskFileItem(final String fieldName, final String contentType, final boolean isFormField, final String fileName, final int threshold,
            final Path repository, final FileItemHeaders fileItemHeaders, final Charset defaultCharset) {
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.charsetDefault = defaultCharset;
        this.isFormField = isFormField;
        this.fileName = fileName;
        this.fileItemHeaders = fileItemHeaders;
        this.threshold = threshold;
        this.repository = repository != null ? repository : PathUtils.getTempDirectory();
    }

    /**
     * Deletes the underlying storage for a file item, including deleting any associated temporary disk file. This method can be used to ensure that this is
     * done at an earlier time, thus preserving system resources.
     *
     * @throws IOException if an error occurs.
     */
    @Override
    public DiskFileItem delete() throws IOException {
        if (dos != null) {
            final Path path = dos.getPath();
            if (path != null) {
                Files.deleteIfExists(path);
            }
        }
        return this;
    }

    /**
     * Gets the contents of the file as an array of bytes. If the contents of the file were not yet cached in memory, they will be loaded from the disk storage
     * and cached.
     *
     * @return The contents of the file as an array of bytes or {@code null} if the data cannot be read.
     * @throws IOException if an I/O error occurs.
     * @throws OutOfMemoryError     See {@link Files#readAllBytes(Path)}: If an array of the required size cannot be allocated, for example the file is larger
     *                              than {@code 2GB}. If so, you should use {@link #getInputStream()}.
     * @see #getInputStream()
     * @deprecated Since 2.0.0, use {@link #getInputStream()}, or {@link #getReader()}, instead.
     */
    @Override
    public byte[] get() throws IOException {
        if (dos != null) {
            final byte[] bytes = dos.getBytes();
            if (bytes != null) {
                return bytes;
            }
            final Path path = dos.getPath();
            if (path != null  &&  dos.getState() == State.closed) {
                return Files.readAllBytes(path);
            }
        }
        return null;
    }

    /**
     * Gets the content charset passed by the agent or {@code null} if not defined.
     *
     * @return The content charset passed by the agent or {@code null} if not defined.
     */
    public Charset getCharset() {
        final var parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        final var params = parser.parse(getContentType(), ';');
        return Charsets.toCharset(params.get("charset"), charsetDefault);
    }

    /**
     * Gets the default charset for use when no explicit charset parameter is provided by the sender.
     *
     * @return the default charset
     */
    public Charset getCharsetDefault() {
        return charsetDefault;
    }

    /**
     * Gets the content type passed by the agent or {@code null} if not defined.
     *
     * @return The content type passed by the agent or {@code null} if not defined.
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the name of the field in the multipart form corresponding to this file item.
     *
     * @return The name of the form field.
     * @see #setFieldName(String)
     */
    @Override
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns the {@link FileCleaningTracker}, which is being used to remove
     * temporary files.
     * @return The {@link FileCleaningTracker}, which is being used to remove
     * temporary files.
     */
    public FileCleaningTracker getFileCleaningTracker() {
        return fileCleaningTracker;
    }

    /**
     * Gets the file item headers.
     *
     * @return The file items headers.
     */
    @Override
    public FileItemHeaders getHeaders() {
        return fileItemHeaders;
    }

    /**
     * Gets an {@link java.io.InputStream InputStream} that can be used to retrieve the contents of the file.
     *
     * @return An {@link java.io.InputStream InputStream} that can be used to retrieve the contents of the file.
     * @throws IOException if an error occurs.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (dos != null  &&  dos.getState() == State.closed) {
            return dos.getInputStream();
        }
        throw new IllegalStateException("The file item has not been fully read.");
    }

    /**
     * Gets the original file name in the client's file system.
     *
     * @return The original file name in the client's file system.
     * @throws InvalidPathException The file name contains a NUL character, which might be an indicator of a security attack. If you intend to use the file name
     *                              anyways, catch the exception and use {@link InvalidPathException#getInput()}.
     */
    @Override
    public String getName() {
        return checkFileName(fileName);
    }

    /**
     * Gets an {@link java.io.OutputStream OutputStream} that can be used for storing the contents of the file.
     *
     * @return An {@link java.io.OutputStream OutputStream} that can be used for storing the contents of the file.
     */
    @Override
    public OutputStream getOutputStream() {
        if (dos == null) {
            final Supplier<Path> pathSupplier =
                    () -> this.repository.resolve(String.format("upload_%s_%s.tmp", UID, getUniqueId()));
            try {
                final Listener persistenceListener = new Listener() {
                    @Override
                    public void persisted(final Path pPath) {
                        // TODO Auto-generated method stub
                        Listener.super.persisted(pPath);
                        final FileCleaningTracker fct = getFileCleaningTracker();
                        if (fct != null) {
                            fct.track(getPath(), this);
                        }
                        }
                };
                dos = new DeferrableOutputStream(threshold, pathSupplier, persistenceListener);
            } catch (final IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }
        return dos;
    }

    /**
     * Gets the {@link Path} for the {@code FileItem}'s data's temporary location on the disk. Note that for {@code FileItem}s that have their data stored in
     * memory, this method will return {@code null}. When handling large files, you can use {@link Files#move(Path,Path,CopyOption...)} to move the file to new
     * location without copying the data, if the source and destination locations reside within the same logical volume.
     *
     * @return The data file, or {@code null} if the data is stored in memory.
     */
    public Path getPath() {
        if (dos != null) {
            return dos.getPath();
        }
        return null;
    }

    /**
     * Returns the contents of the file as a {@link Reader}, using the specified
     * {@link #getCharset()}. If the contents are not yet available, returns null.
     * This is the case, for example, if the underlying output stream has not yet
     * been closed.
     * @return The contents of the file as a {@link Reader}
     * @throws UnsupportedEncodingException The chararacter set, which is
     *   specified in the files "content-type" header, is invalid.
     * @throws IOException An I/O error occurred, while the
     *   underlying {@link #getInputStream() input stream} was created.
     */
    public Reader getReader() throws IOException, UnsupportedEncodingException {
        final InputStream is = getInputStream();
        final var parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        final var params = parser.parse(getContentType(), ';');
        final Charset cs = Charsets.toCharset(params.get("charset"), charsetDefault);
        return new InputStreamReader(is, cs);
    }

    /**
     * Gets the size of the file.
     *
     * @return The size of the file, in bytes.
     */
    @Override
    public long getSize() {
        if (dos != null) {
            return dos.getSize();
        }
        return 0L;
    }

    /**
     * Gets the contents of the file as a String, using the default character encoding. This method uses {@link #get()} to retrieve the contents of the file.
     *
     * @return The contents of the file, as a string, if available, or null.
     * @throws IOException if an I/O error occurs
     * @throws OutOfMemoryError See {@link Files#readAllBytes(Path)}: If a string of the required size cannot be allocated,
     *   for example the file is larger than {@code 2GB}. If so, you should use {@link #getReader()}.
     * @throws UnsupportedEncodingException The chararacter set, which is
     *   specified in the files "content-type" header, is invalid.
     * @deprecated Since 2.0.0, use {@link #getReader()} instead.
     */
    @Override
    public String getString() throws IOException, UnsupportedEncodingException, OutOfMemoryError {
        final byte[] bytes = get();
        if (bytes == null) {
            return null;
        }
        return new String(bytes, getCharset());
    }

    /**
     * Gets the contents of the file as a String, using the specified encoding. This method uses {@link #get()} to retrieve the contents of the file.
     *
     * @param charset The charset to use.
     * @return The contents of the file, as a string.
     * @throws IOException if an I/O error occurs
     */
    @Override
    public String getString(final Charset charset) throws IOException {
        return new String(get(), Charsets.toCharset(charset, charsetDefault));
    }

    /**
     * Returns the file items threshold.
     * @return The threshold.
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Tests whether or not a {@code FileItem} instance represents a simple form field.
     *
     * @return {@code true} if the instance represents a simple form field; {@code false} if it represents an uploaded file.
     * @see #setFormField(boolean)
     */
    @Override
    public boolean isFormField() {
        return isFormField;
    }

    /**
     * Provides a hint as to whether or not the file contents will be read from memory.
     *
     * @return {@code true} if the file contents will be read from memory; {@code false} otherwise.
     */
    @Override
    public boolean isInMemory() {
        if (dos != null) {
            return dos.isInMemory();
        }
        return true;
    }

    /**
     * Sets the default charset for use when no explicit charset parameter is provided by the sender.
     *
     * @param charset the default charset
     * @return {@code this} instance.
     */
    public DiskFileItem setCharsetDefault(final Charset charset) {
        charsetDefault = charset;
        return this;
    }

    /**
     * Sets the field name used to reference this file item.
     *
     * @param fieldName The name of the form field.
     * @see #getFieldName()
     */
    @Override
    public DiskFileItem setFieldName(final String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    /**
     * Sets the {@link FileCleaningTracker}, which is being used to remove
     * temporary files.
     * @param fileCleaningTracker The {@link FileCleaningTracker}, which is being used to
     * remove temporary files.
     */
    public void setFileCleaningTracker(final FileCleaningTracker fileCleaningTracker) {
        this.fileCleaningTracker = fileCleaningTracker;
    }

    /**
     * Specifies whether or not a {@code FileItem} instance represents a simple form field.
     *
     * @param state {@code true} if the instance represents a simple form field; {@code false} if it represents an uploaded file.
     * @see #isFormField()
     */
    @Override
    public DiskFileItem setFormField(final boolean state) {
        isFormField = state;
        return this;
    }

    /**
     * Sets the file item headers.
     *
     * @param headers The file items headers.
     */
    @Override
    public DiskFileItem setHeaders(final FileItemHeaders headers) {
        this.fileItemHeaders = headers;
        return this;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object.
     */
    @Override
    public String toString() {
        return String.format("name=%s, StoreLocation=%s, size=%s bytes, isFormField=%s, FieldName=%s", getName(), getPath(), getSize(), isFormField(),
                getFieldName());
    }

    /**
     * Writes an uploaded item to disk.
     * <p>
     * The client code is not concerned with whether or not the item is stored in memory, or on disk in a temporary location. They just want to write the
     * uploaded item to a file.
     * </p>
     * <p>
     * This implementation first attempts to rename the uploaded item to the specified destination file, if the item was originally written to disk. Otherwise,
     * the data will be copied to the specified file.
     * </p>
     * <p>
     * This method is only guaranteed to work <em>once</em>, the first time it is invoked for a particular item. This is because, in the event that the method
     * renames a temporary file, that file will no longer be available to copy or rename again at a later time.
     * </p>
     *
     * @param file The {@code File} into which the uploaded item should be stored.
     * @throws IOException if an error occurs.
     */
    @Override
    public DiskFileItem write(final Path file) throws IOException {
        if (isInMemory()) {
            try (var fout = Files.newOutputStream(file)) {
                fout.write(get());
            } catch (final IOException e) {
                throw new IOException("Unexpected output data", e);
            }
        } else {
            final var outputFile = getPath();
            if (outputFile == null) {
                /*
                 * For whatever reason we cannot write the file to disk.
                 */
                throw new FileUploadException("Cannot write uploaded file to disk.");
            }
            //
            // The uploaded file is being stored on disk in a temporary location so move it to the desired file.
            //
            Files.move(outputFile, file, StandardCopyOption.REPLACE_EXISTING);
        }
        return this;
    }
}
