/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//fileupload/src/java/org/apache/commons/fileupload/FileItem.java,v 1.10 2002/12/20 04:09:07 dion Exp $
 * $Revision: 1.10 $
 * $Date: 2002/12/20 04:09:07 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */


package org.apache.commons.fileupload;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;


/**
 * <p> This class represents a file or form item that was received within a
 * <code>multipart/form-data</code> POST request.
 *
 * <p> After retrieving an instance of this class from a {@link
 * org.apache.commons.fileupload.FileUpload FileUpload} instance (see
 * {@link org.apache.commons.fileupload.FileUpload
 * #parseRequest(javax.servlet.http.HttpServletRequest)}), you may
 * either request all contents of file at once using {@link #get()} or
 * request an {@link java.io.InputStream InputStream} with
 * {@link #getInputStream()} and process the file without attempting to load
 * it into memory, which may come handy with large files.
 *
 * <p> While this interface does not extend
 * <code>javax.activation.DataSource</code> per se (to avoid a seldom used
 * dependency), several of the defined methods are specifically defined with
 * the same signatures as methods in that interface. This allows an
 * implementation of this interface to also implement
 * <code>javax.activation.DataSource</code> with minimal additional work.
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:sean@informage.net">Sean Legassick</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 *
 * @version $Id: FileItem.java,v 1.10 2002/12/20 04:09:07 dion Exp $
 */
public interface FileItem extends Serializable
{


    // ------------------------------- Methods from javax.activation.DataSource


    /**
     * Returns an {@link java.io.InputStream InputStream} that can be
     * used to retrieve the contents of the file.
     *
     * @return An {@link java.io.InputStream InputStream} that can be
     *         used to retrieve the contents of the file.
     *
     * @exception IOException if an error occurs.
     */
    InputStream getInputStream()
        throws IOException;


    /**
     * Returns the content type passed by the browser or <code>null</code> if
     * not defined.
     *
     * @return The content type passed by the browser or <code>null</code> if
     *         not defined.
     */
    String getContentType();


    /**
     * Returns the original filename in the client's filesystem.
     *
     * @return The original filename in the client's filesystem.
     */
    String getName();


    // ----------------------------------------------------- Manifest constants


    /**
     * The maximal size of request that will have it's elements stored
     * in memory.
     */
    public static final int DEFAULT_UPLOAD_SIZE_THRESHOLD = 10240;


    // ------------------------------------------------------- FileItem methods


    /**
     * Provides a hint as to whether or not the file contents will be read
     * from memory.
     *
     * @return <code>true</code> if the file contents will be read
     *         from memory.
     */
    boolean isInMemory();


    /**
     * Returns the size of the file.
     *
     * @return The size of the file, in bytes.
     */
    long getSize();


    /**
     * Returns the contents of the file as an array of bytes.  If the
     * contents of the file were not yet cached in memory, they will be
     * loaded from the disk storage and cached.
     *
     * @return The contents of the file as an array of bytes.
     */
    byte[] get();


    /**
     * Returns the contents of the file as a String, using the specified
     * encoding.  This method uses {@link #get()} to retrieve the
     * contents of the file.
     *
     * @param encoding The character encoding to use.
     *
     * @return The contents of the file, as a string.
     *
     * @exception UnsupportedEncodingException if the requested character
     *                                         encoding is not available.
     */
    String getString(String encoding)
        throws UnsupportedEncodingException;


    /**
     * Returns the contents of the file as a String, using the default
     * character encoding.  This method uses {@link #get()} to retrieve the
     * contents of the file.
     *
     * @return The contents of the file, as a string.
     */
    String getString();


    /**
     * Returns the {@link java.io.File} object for the <code>FileItem</code>'s
     * data's temporary location on the disk. Note that for
     * <code>FileItem</code>s that have their data stored in memory,
     * this method will return <code>null</code>. When handling large
     * files, you can use {@link java.io.File#renameTo(File)} to
     * move the file to new location without copying the data, if the
     * source and destination locations reside within the same logical
     * volume.
     *
     * @return The data file, or <code>null</code> if the data is stored in
     *         memory.
     */
    File getStoreLocation();


    /**
     * A convenience method to write an uploaded file to disk. The client code
     * is not concerned whether or not the file is stored in memory, or on disk
     * in a temporary location. They just want to write the uploaded file to
     * disk.
     *
     * @param file The full path to location where the uploaded file should
     *             be stored.
     *
     * @exception Exception if an error occurs.
     */
    void write(String file) throws Exception;


    /**
     * Deletes the underlying storage for a file item, including deleting any
     * associated temporary disk file. Although this storage will be deleted
     * automatically when the <code>FileItem</code> instance is garbage
     * collected, this method can be used to ensure that this is done at an
     * earlier time, thus preserving system resources.
     */
    void delete();


    /**
     * Returns the name of the field in the multipart form corresponding to
     * this file item.
     *
     * @return The name of the form field.
     */
    String getFieldName();


    /**
     * Sets the field name used to reference this file item.
     *
     * @param name The name of the form field.
     */
    void setFieldName(String name);


    /**
     * Determines whether or not a <code>FileItem</code> instance represents
     * a simple form field.
     *
     * @return <code>true</code> if the instance represents a simple form
     *         field; <code>false</code> if it represents an uploaded file.
     */
    boolean isFormField();


    /**
     * Specifies whether or not a <code>FileItem</code> instance represents
     * a simple form field.
     *
     * @param state <code>true</code> if the instance represents a simple form
     *              field; <code>false</code> if it represents an uploaded file.
     */
    void setIsFormField(boolean state);

}
