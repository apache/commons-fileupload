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
package org.apache.commons.fileupload2.disk;

import java.nio.charset.Charset;
import java.nio.file.Path;

import org.apache.commons.fileupload2.FileItem;
import org.apache.commons.fileupload2.FileItemFactory;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.build.AbstractOrigin;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.file.PathUtils;

/**
 * The default {@link FileItemFactory} implementation.
 * <p>
 * This implementation creates {@link FileItem} instances which keep their content either in memory, for smaller items, or in a
 * temporary file on disk, for larger items. The size threshold, above which content will be stored on disk, is configurable, as is the directory in which
 * temporary files will be created.
 * </p>
 * <p>
 * If not otherwise configured, the default configuration values are as follows:
 * </p>
 * <ul>
 * <li>Size threshold is 10 KB.</li>
 * <li>Repository is the system default temporary directory, as returned by {@code System.getProperty("java.io.tmpdir")}.</li>
 * </ul>
 * <p>
 * <b>NOTE</b>: Files are created in the system default temporary directory with predictable names. This means that a local attacker with write access to that
 * directory can perform a TOUTOC attack to replace any uploaded file with a file of the attackers choice. The implications of this will depend on how the
 * uploaded file is used but could be significant. When using this implementation in an environment with local, untrusted users, {@link #setRepository(Path)}
 * MUST be used to configure a repository location that is not publicly writable. In a Servlet container the location identified by the ServletContext attribute
 * {@code javax.servlet.context.tempdir} may be used.
 * </p>
 * <p>
 * Temporary files, which are created for file items, should be deleted later on. The best way to do this is using a {@link FileCleaningTracker}, which you can
 * set on the {@link DiskFileItemFactory}. However, if you do use such a tracker, then you must consider the following: Temporary files are automatically
 * deleted as soon as they are no longer needed. (More precisely, when the corresponding instance of {@link java.io.File} is garbage collected.) This is done by
 * the so-called reaper thread, which is started and stopped automatically by the {@link FileCleaningTracker} when there are files to be tracked. It might make
 * sense to terminate that thread, for example, if your web application ends. See the section on "Resource cleanup" in the users guide of Commons FileUpload.
 * </p>
 * @see Builder
 * @see Builder#get()
 */
public final class DiskFileItemFactory implements FileItemFactory {

    /**
     * Builds a new {@link DiskFileItemFactory} instance.
     * <p>
     * For example:
     * </p>
     *
     * <pre>{@code
     * DiskFileItemFactory factory = DiskFileItemFactory.builder()
     *    .setPath(path)
     *    .setBufferSize(DEFAULT_THRESHOLD)
     *    .get();
     * }
     * </pre>
     *
     * @since 2.12.0
     */
    public static class Builder extends AbstractStreamBuilder<DiskFileItemFactory, Builder> {

        public Builder() {
            setBufferSize(DEFAULT_THRESHOLD);
            setPath(PathUtils.getTempDirectory());
        }

        /**
         * Constructs a new instance.
         * <p>
         * This builder use the aspects Path and buffer size.
         * </p>
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
        public DiskFileItemFactory get() {
            return new DiskFileItemFactory(getOrigin().getPath(), getBufferSize());
        }

    }

    /**
     * The default threshold in bytes above which uploads will be stored on disk.
     */
    public static final int DEFAULT_THRESHOLD = 10_240;

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The directory in which uploaded files will be stored, if stored on disk.
     */
    private Path repository;

    /**
     * The threshold above which uploads will be stored on disk.
     */
    private int threshold = DEFAULT_THRESHOLD;

    /**
     * The instance of {@link FileCleaningTracker}, which is responsible for deleting temporary files.
     * <p>
     * May be null, if tracking files is not required.
     * </p>
     */
    private FileCleaningTracker fileCleaningTracker;

    /**
     * Default content charset to be used when no explicit charset parameter is provided by the sender.
     */
    private Charset defaultCharset = DiskFileItem.DEFAULT_CHARSET;

    /**
     * Constructs a preconfigured instance of this class.
     * @param repository The data repository, which is the directory in which files will be created, should the item size exceed the threshold.
     * @param threshold  The threshold, in bytes, below which items will be retained in memory and above which they will be stored as a file.
     */
    private DiskFileItemFactory(final Path repository, final int threshold) {
        this.threshold = threshold;
        this.repository = repository;
    }

    /**
     * Creates a new {@link org.apache.commons.fileupload2.disk.DiskFileItem} instance from the supplied parameters and the local factory configuration.
     *
     * @param fieldName   The name of the form field.
     * @param contentType The content type of the form field.
     * @param isFormField {@code true} if this is a plain form field; {@code false} otherwise.
     * @param fileName    The name of the uploaded file, if any, as supplied by the browser or other client.
     * @return The newly created file item.
     */
    @Override
    public FileItem createFileItem(final String fieldName, final String contentType, final boolean isFormField, final String fileName) {
        final DiskFileItem result = new DiskFileItem(fieldName, contentType, isFormField, fileName, threshold, repository);
        result.setDefaultCharset(defaultCharset);
        final FileCleaningTracker tracker = getFileCleaningTracker();
        if (tracker != null) {
            tracker.track(result.getTempFile().toFile(), result);
        }
        return result;
    }

    /**
     * Gets the default charset for use when no explicit charset parameter is provided by the sender.
     *
     * @return the default charset
     */
    public Charset getDefaultCharset() {
        return defaultCharset;
    }

    /**
     * Gets the tracker, which is responsible for deleting temporary files.
     *
     * @return An instance of {@link FileCleaningTracker}, or null (default), if temporary files aren't tracked.
     */
    public FileCleaningTracker getFileCleaningTracker() {
        return fileCleaningTracker;
    }

    /**
     * Gets the directory used to temporarily store files that are larger than the configured size threshold.
     *
     * @return The directory in which temporary files will be located.
     * @see #setRepository(Path)
     */
    public Path getRepository() {
        return repository;
    }

    /**
     * Gets the size threshold beyond which files are written directly to disk. The default value is {@value #DEFAULT_THRESHOLD} bytes.
     *
     * @return The size threshold in bytes.
     * @see #setThreshold(int)
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Sets the default charset for use when no explicit charset parameter is provided by the sender.
     *
     * @param charset the default charset
     */
    public void setDefaultCharset(final Charset charset) {
        defaultCharset = charset;
    }

    /**
     * Sets the tracker, which is responsible for deleting temporary files.
     *
     * @param tracker An instance of {@link FileCleaningTracker}, which will from now on track the created files, or null (default), to disable tracking.
     */
    public void setFileCleaningTracker(final FileCleaningTracker tracker) {
        fileCleaningTracker = tracker;
    }

    /**
     * Sets the directory used to temporarily store files that are larger than the configured size threshold.
     *
     * @param repository The directory in which temporary files will be located.
     * @see #getRepository()
     */
    public void setRepository(final Path repository) {
        this.repository = repository;
    }

    /**
     * Sets the size threshold beyond which files are written directly to disk. The default value is {@value #DEFAULT_THRESHOLD} bytes.
     *
     * @param threshold The size threshold in bytes.
     * @see #getThreshold()
     */
    public void setThreshold(final int threshold) {
        this.threshold = threshold;
    }
}
