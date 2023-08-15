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
import java.nio.file.InvalidPathException;

import org.apache.commons.io.input.BoundedInputStream;

/**
 * Default implementation of {@link FileItemInput}.
 */
class FileItemInputImpl implements FileItemInput {

    /**
     * The File Item iterator implementation.
     *
     * @see FileItemInputIteratorImpl
     */
    private final FileItemInputIteratorImpl fileItemInputIteratorImpl;

    /**
     * The file items content type.
     */
    private final String contentType;

    /**
     * The file items field name.
     */
    private final String fieldName;

    /**
     * The file items file name.
     */
    private final String fileName;

    /**
     * Whether the file item is a form field.
     */
    private final boolean formField;

    /**
     * The file items input stream.
     */
    private final InputStream inputStream;

    /**
     * The file items input stream closed flag.
     */
    private boolean inputStreamClosed;

    /**
     * The headers, if any.
     */
    private FileItemHeaders headers;

    /**
     * Creates a new instance.
     *
     * @param fileItemIterator The {@link FileItemInputIteratorImpl iterator}, which returned this file item.
     * @param fileName         The items file name, or null.
     * @param fieldName        The items field name.
     * @param contentType      The items content type, or null.
     * @param formField        Whether the item is a form field.
     * @param contentLength    The items content length, if known, or -1
     * @throws IOException         Creating the file item failed.
     * @throws FileUploadException Parsing the incoming data stream failed.
     */
    FileItemInputImpl(final FileItemInputIteratorImpl fileItemIterator, final String fileName, final String fieldName, final String contentType,
            final boolean formField, final long contentLength) throws FileUploadException, IOException {
        this.fileItemInputIteratorImpl = fileItemIterator;
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.formField = formField;
        final var fileSizeMax = fileItemInputIteratorImpl.getFileSizeMax();
        if (fileSizeMax != -1 && contentLength != -1 && contentLength > fileSizeMax) {
            throw new FileUploadByteCountLimitException(String.format("The field %s exceeds its maximum permitted size of %s bytes.", fieldName, fileSizeMax),
                    contentLength, fileSizeMax, fileName, fieldName);
        }
        // OK to construct stream now
        final var itemInputStream = fileItemInputIteratorImpl.getMultiPartInput().newInputStream();
        InputStream istream = itemInputStream;
        if (fileSizeMax != -1) {
            // onMaxLength will be called when the length is greater than _or equal to_ the supplied maxLength.
            // Because we only want to throw an exception when the length is greater than fileSizeMax, we
            // increment fileSizeMax by 1.
            istream = new BoundedInputStream(istream, fileSizeMax + 1) {
                @Override
                protected void onMaxLength(final long sizeMax, final long count) throws IOException {
                    itemInputStream.close(true);
                    throw new FileUploadByteCountLimitException(
                            String.format("The field %s exceeds its maximum permitted size of %s bytes.", fieldName, fileSizeMax), count, fileSizeMax, fileName,
                            fieldName);
                }
            };
        }
        this.inputStream = istream;
    }

    /**
     * Closes the file item.
     *
     * @throws IOException An I/O error occurred.
     */
    public void close() throws IOException {
        inputStream.close();
        inputStreamClosed = true;
    }

    /**
     * Gets the content type, or null.
     *
     * @return Content type, if known, or null.
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the items field name.
     *
     * @return Field name.
     */
    @Override
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the headers.
     *
     * @return The items header object
     */
    @Override
    public FileItemHeaders getHeaders() {
        return headers;
    }

    /**
     * Gets the input stream, which may be used to read the items contents.
     *
     * @return Opened input stream.
     * @throws IOException An I/O error occurred.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (inputStreamClosed) {
            throw new FileItemInput.ItemSkippedException("getInputStream()");
        }
        return inputStream;
    }

    /**
     * Gets the file name.
     *
     * @return File name, if known, or null.
     * @throws InvalidPathException The file name is invalid, for example it contains a NUL character, which might be an indicator of a security attack. If you
     *                              intend to use the file name anyways, catch the exception and use InvalidPathException#getInput().
     */
    @Override
    public String getName() {
        return DiskFileItem.checkFileName(fileName);
    }

    /**
     * Tests whether this is a form field.
     *
     * @return True, if the item is a form field, otherwise false.
     */
    @Override
    public boolean isFormField() {
        return formField;
    }

    /**
     * Sets the file item headers.
     *
     * @param headers The items header object
     */
    @Override
    public FileItemInputImpl setHeaders(final FileItemHeaders headers) {
        this.headers = headers;
        return this;
    }

}
