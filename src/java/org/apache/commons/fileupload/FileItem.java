/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//fileupload/src/java/org/apache/commons/fileupload/FileItem.java,v 1.3 2002/07/17 01:17:06 martinc Exp $
 * $Revision: 1.3 $
 * $Date: 2002/07/17 01:17:06 $
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
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * <p> This class represents a file that was received by Turbine using
 * <code>multipart/form-data</code> POST request.
 *
 * <p> After retrieving an instance of this class from the {@link
 * org.apache.commons.fileupload.FileUpload FileUpload} (see
 * {@link org.apache.commons.fileupload.FileUpload
 * #parseRequest(javax.servlet.http.HttpServletRequest, String)
 * You may either request all
 * contents of file at once using {@link #get()} or request an {@link
 * java.io.InputStream InputStream} with {@link #getStream()} and
 * process the file without attempting to load it into memory, which
 * may come handy with large files.
 *
 * Implements the javax.activation.DataSource interface (which allows
 * for example the adding of a FileItem as an attachment to a multipart
 * email).
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:sean@informage.net">Sean Legassick</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: FileItem.java,v 1.3 2002/07/17 01:17:06 martinc Exp $
 */
public interface FileItem
    extends DataSource
{
    /**
     * The maximal size of request that will have it's elements stored
     * in memory.
     */
    public static final int DEFAULT_UPLOAD_SIZE_THRESHOLD = 10240;

    /**
     * Returns the original filename in the user's filesystem.
     * (implements DataSource method)
     *
     * @return The original filename in the user's filesystem.
     */
    public String getName();

    /**
     * Returns the original filename in the user's filesystem.
     *
     * @return The original filename in the user's filesystem.
     */
    public String getFileName();

    /**
    * Returns the content type passed by the browser or
    * <code>null</code> if not defined. (implements
    * DataSource method).
    *
    * @return The content type passed by the browser or
    * <code>null</code> if not defined.
    */
    public String getContentType();

    /**
     * Provides a hint if the file contents will be read from memory.
     *
     * @return <code>True</code> if the file contents will be read
     * from memory.
     */
    public boolean inMemory();

    /**
     * Returns the size of the file.
     *
     * @return The size of the file.
     */
    public long getSize();

    /**
     * Returns the contents of the file as an array of bytes.  If the
     * contents of the file were not yet cached int the memory, they
     * will be loaded from the disk storage and chached.
     *
     * @return The contents of the file as an array of bytes.
     */
    public byte[] get();

    /**
     * Returns the contents of the file as a String, using specified
     * encoding.  This method uses {@link #get()} to retireve the
     * contents of the file.<br>
     *
     * @param encoding The encoding to use.
     * @return The contents of the file.
     * @exception UnsupportedEncodingException.
     */
    public String getString( String encoding )
        throws UnsupportedEncodingException;

    public String getString();

    /**
     * Returns an {@link java.io.InputStream InputStream} that can be
     * used to retrieve the contents of the file. (implements DataSource
     * method)
     *
     * @return An {@link java.io.InputStream InputStream} that can be
     * used to retrieve the contents of the file.
     * @exception Exception, a generic exception.
     */
    public InputStream getInputStream()
        throws IOException;

    /**
     * Returns an {@link java.io.InputStream InputStream} that can be
     * used to retrieve the contents of the file.
     *
     * @return An {@link java.io.InputStream InputStream} that can be
     * used to retrieve the contents of the file.
     * @exception Exception, a generic exception.
     */
    public InputStream getStream()
        throws IOException;

    /**
     * Returns the {@link java.io.File} objects for the DefaultFileItems's
     * data temporary location on the disk.  Note that for
     * <code>DefaultFileItems</code> that have their data stored in memory
     * this method will return <code>null</code>.  When handling large
     * files, you can use {@link java.io.File#renameTo(File)} to
     * move the file to new location without copying the data, if the
     * source and destination locations reside within the same logical
     * volume.
     *
     * @return A File.
     */
    public File getStoreLocation();

    /**
     * A convenience method to write an uploaded
     * file to disk. The client code is not concerned
     * whether or not the file is stored in memory,
     * or on disk in a temporary location. They just
     * want to write the uploaded file to disk.
     *
     * @param String full path to location where uploaded
     *               should be stored.
     */
    public void write(String file) throws Exception;

    public String getFieldName();

    public void setFieldName(String name);

    public boolean isFormField();
    public void setIsFormField(boolean state);
}
