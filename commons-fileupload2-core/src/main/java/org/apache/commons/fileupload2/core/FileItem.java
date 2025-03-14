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
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * <p>
 * This class represents a file or form item that was received within a {@code multipart/form-data} POST request.
 * </p>
 * <p>
 * After retrieving an instance of this class from a {@link AbstractFileUpload FileUpload} instance (see
 * {@code org.apache.commons.fileupload2.core.servlet.ServletFileUpload #parseRequest(javax.servlet.http.HttpServletRequest)}), you may either request all
 * contents of the file at once using {@link #get()} or request an {@link InputStream} with {@link #getInputStream()} and process the file without attempting to
 * load it into memory, which may come handy with large files.
 * </p>
 * <p>
 * While this interface does not extend {@code javax.activation.DataSource} (to avoid a seldom used dependency), several of the defined methods are specifically
 * defined with the same signatures as methods in that interface. This allows an implementation of this interface to also implement
 * {@code javax.activation.DataSource} with minimal additional work.
 * </p>
 *
 * @param <F> The FileItem type.
 */
public interface FileItem<F extends FileItem<F>> extends FileItemHeadersProvider<F> {

    /**
     * Deletes the underlying storage for a file item, including deleting any associated temporary disk file. Use this method to ensure that this is done at an
     * earlier time, to preserve resources.
     *
     * @return {@code this} instance.
     * @throws IOException if an error occurs.
     */
    F delete() throws IOException;

    /**
     * Gets the contents of the file item as a byte array.
     *
     * @return The contents of the file item as a byte array.
     * @throws IOException if an I/O error occurs
     */
    byte[] get() throws IOException;

    /**
     * Gets the content type passed by the browser or {@code null} if not defined.
     *
     * @return The content type passed by the browser or {@code null} if not defined.
     */
    String getContentType();

    /**
     * Gets the name of the field in the multipart form corresponding to this file item.
     *
     * @return The name of the form field.
     */
    String getFieldName();

    /**
     * Gets an {@link java.io.InputStream InputStream} that can be used to retrieve the contents of the file.
     *
     * @return An {@link java.io.InputStream InputStream} that can be used to retrieve the contents of the file.
     * @throws IOException if an error occurs.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Gets the original file name in the client's file system, as provided by the browser (or other client software). In most cases, this will be the base file
     * name, without path information. However, some clients, such as the Opera browser, do include path information.
     *
     * @return The original file name in the client's file system.
     * @throws InvalidPathException The file name contains a NUL character, which might be an indicator of a security attack. If you intend to use the file name
     *                              anyways, catch the exception and use InvalidFileNameException#getName().
     */
    String getName();

    /**
     * Gets an {@link java.io.OutputStream OutputStream} that can be used for storing the contents of the file.
     *
     * @return An {@link java.io.OutputStream OutputStream} that can be used for storing the contents of the file.
     * @throws IOException if an error occurs.
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Gets the size of the file item.
     *
     * @return The size of the file item, in bytes.
     */
    long getSize();

    /**
     * Gets the contents of the file item as a String, using the default character encoding. This method uses {@link #get()} to retrieve the contents of the
     * item.
     *
     * @return The contents of the item, as a string.
     *
     * @throws IOException if an I/O error occurs
     */
    String getString() throws IOException;

    /**
     * Gets the contents of the file item as a String, using the specified encoding. This method uses {@link #get()} to retrieve the contents of the item.
     *
     * @param toCharset The character encoding to use.
     * @return The contents of the item, as a string.
     * @throws IOException if an I/O error occurs
     */
    String getString(Charset toCharset) throws IOException;

    /**
     * Tests whether or not a {@code FileItem} instance represents a simple form field.
     *
     * @return {@code true} if the instance represents a simple form field; {@code false} if it represents an uploaded file.
     */
    boolean isFormField();

    /**
     * Tests a hint as to whether or not the file contents will be read from memory.
     *
     * @return {@code true} if the file contents will be read from memory; {@code false} otherwise.
     */
    boolean isInMemory();

    /**
     * Sets the field name used to reference this file item.
     *
     * @param name The name of the form field.
     * @return {@code this} instance.
     */
    F setFieldName(String name);

    /**
     * Sets whether or not a {@code FileItem} instance represents a simple form field.
     *
     * @param state {@code true} if the instance represents a simple form field; {@code false} if it represents an uploaded file.
     * @return {@code this} instance.
     */
    F setFormField(boolean state);

    /**
     * Writes an uploaded item to disk.
     * <p>
     * The client code is not concerned with whether or not the item is stored in memory, or on disk in a temporary location. They just want to write the
     * uploaded item to a file.
     * </p>
     * <p>
     * This method is not guaranteed to succeed if called more than once for the same item. This allows a particular implementation to use, for example, file
     * renaming, where possible, rather than copying all of the underlying data, thus gaining a significant performance benefit.
     * </p>
     *
     * @param file The {@code File} into which the uploaded item should be stored.
     * @throws IOException if an error occurs.
     * @return {@code this} instance.
     */
    F write(Path file) throws IOException;

}
