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
package org.apache.commons.fileupload2;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * High level API for processing file uploads.
 * <p>
 * This class handles multiple files per single HTML widget, sent using {@code multipart/mixed} encoding type, as specified by
 * <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>. Use {@link #parseRequest(RequestContext)} to acquire a list of
 * {@link org.apache.commons.fileupload2.FileItem} associated with a given HTML widget.
 * </p>
 * <p>
 * How the data for individual parts is stored is determined by the factory used to create them; a given part may be in memory, on disk, or somewhere else.
 * </p>
 * @param <T> the context type
 */
public abstract class FileUpload<T> extends AbstractFileUpload {

    /**
     * The factory to use to create new form items.
     */
    private FileItemFactory fileItemFactory;

    /**
     * Constructs an uninitialized instance of this class.
     *
     * A factory must be configured, using {@code setFileItemFactory()}, before attempting to parse requests.
     *
     * @see #FileUpload(FileItemFactory)
     */
    public FileUpload() {
    }

    /**
     * Constructs an instance of this class which uses the supplied factory to create {@code FileItem} instances.
     *
     * @see #FileUpload()
     * @param fileItemFactory The factory to use for creating file items.
     */
    public FileUpload(final FileItemFactory fileItemFactory) {
        this.fileItemFactory = fileItemFactory;
    }

    /**
     * Gets the factory class used when creating file items.
     *
     * @return The factory class for new file items.
     */
    @Override
    public FileItemFactory getFileItemFactory() {
        return fileItemFactory;
    }

    /**
     * Gets a file item iterator.
     *
     * @param request The servlet request to be parsed.
     * @return An iterator to instances of {@code FileItemStream} parsed from the request, in the order that they were transmitted.
     * @throws FileUploadException if there are problems reading/parsing the request or storing files.
     * @throws IOException         An I/O error occurred. This may be a network error while communicating with the client or a problem while storing the
     *                             uploaded content.
     */
    public abstract FileItemIterator getItemIterator(T request) throws FileUploadException, IOException;

    /**
     * Parses an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant {@code multipart/form-data} stream.
     *
     * @param request The servlet request to be parsed.
     * @return A map of {@code FileItem} instances parsed from the request.
     * @throws FileUploadException if there are problems reading/parsing the request or storing files.
     */
    public abstract Map<String, List<FileItem>> parseParameterMap(T request) throws FileUploadException;

    /**
     * Parses an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant {@code multipart/form-data} stream.
     *
     * @param request The servlet request to be parsed.
     * @return A list of {@code FileItem} instances parsed from the request, in the order that they were transmitted.
     * @throws FileUploadException if there are problems reading/parsing the request or storing files.
     */
    public abstract List<FileItem> parseRequest(T request) throws FileUploadException;

    /**
     * Sets the factory class to use when creating file items.
     *
     * @param factory The factory class for new file items.
     */
    @Override
    public void setFileItemFactory(final FileItemFactory factory) {
        this.fileItemFactory = factory;
    }

}
