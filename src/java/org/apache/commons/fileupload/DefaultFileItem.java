/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//fileupload/src/java/org/apache/commons/fileupload/DefaultFileItem.java,v 1.9 2002/07/19 03:56:51 martinc Exp $
 * $Revision: 1.9 $
 * $Date: 2002/07/19 03:56:51 $
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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;


/**
 * <p> The default mplementation of the
 * {@link org.apache.commons.fileupload.FileItem FileItem} interface.
 *
 * <p> After retrieving an instance of this class from a {@link
 * org.apache.commons.fileupload.FileUpload FileUpload} instance (see
 * {@link org.apache.commons.fileupload.FileUpload
 * #parseRequest(javax.servlet.http.HttpServletRequest, String)), you may
 * either request all contents of file at once using {@link #get()} or
 * request an {@link java.io.InputStream InputStream} with {@link #getStream()}
 * and process the file without attempting to load it into memory, which
 * may come handy with large files.
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:sean@informage.net">Sean Legassick</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jmcnally@apache.org">John McNally</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 *
 * @version $Id: DefaultFileItem.java,v 1.9 2002/07/19 03:56:51 martinc Exp $
 */
public class DefaultFileItem
    implements FileItem
{

    // ----------------------------------------------------------- Data members


    /**
     * Counter used in unique identifier generation.
     */
    private static int counter = 0;


    /**
     * The original filename in the user's filesystem.
     */
    private String fileName;


    /**
     * The content type passed by the browser, or <code>null</code> if
     * not defined.
     */
    private String contentType;


    /**
     * Cached contents of the file.
     */
    private byte[] content;


    /**
     * Temporary storage location on disk.
     */
    private File storeLocation;


    /**
     * Temporary storage for in-memory files.
     */
    private ByteArrayOutputStream byteStream;


    /**
     * The name of the form field as provided by the browser.
     */
    private String fieldName;


    /**
     * Whether or not this item is a simple form field.
     */
    private boolean isFormField;


    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor.
     */
    public DefaultFileItem()
    {
    }


    /**
     * Constructs a new <code>DefaultFileItem</code> instance.
     *
     * <p>Use {@link #newInstance(String,String,String,int,int)} to
     * instantiate <code>DefaultFileItem</code>s.
     *
     * @param fileName The original filename in the user's filesystem.
     * @param contentType The content type passed by the browser or
     * <code>null</code> if not defined.
     */
    protected DefaultFileItem(String fileName, String contentType)
    {
        this.fileName = fileName;
        this.contentType = contentType;
    }


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
    public InputStream getInputStream()
        throws IOException
    {
        if (content == null)
        {
            if (storeLocation != null)
            {
                return new FileInputStream(storeLocation);
            }
            else
            {
                content = byteStream.toByteArray();
                byteStream = null;
            }
        }
        return new ByteArrayInputStream(content);
    }


    /**
     * Returns the content type passed by the browser or <code>null</code> if
     * not defined.
     *
     * @return The content type passed by the browser or <code>null</code> if
     *         not defined.
     */
    public String getContentType()
    {
        return contentType;
    }


    /**
     * Returns the original filename in the client's filesystem.
     *
     * @return The original filename in the client's filesystem.
     */
    public String getName()
    {
        return fileName;
    }


    // ------------------------------------------------------- FileItem methods


    /**
     * Provides a hint as to whether or not the file contents will be read
     * from memory.
     *
     * @return <code>true</code> if the file contents will be read
     *         from memory.
     */
    public boolean isInMemory()
    {
        return (content != null || byteStream != null);
    }


    /**
     * Returns the size of the file.
     *
     * @return The size of the file, in bytes.
     */
    public long getSize()
    {
        if (storeLocation != null)
        {
            return storeLocation.length();
        }
        else if (byteStream != null)
        {
            return byteStream.size();
        }
        else
        {
            return content.length;
        }
    }


    /**
     * Returns the contents of the file as an array of bytes.  If the
     * contents of the file were not yet cached in memory, they will be
     * loaded from the disk storage and cached.
     *
     * @return The contents of the file as an array of bytes.
     */
    public byte[] get()
    {
        if (content == null)
        {
            if (storeLocation != null)
            {
                content = new byte[(int) getSize()];
                try
                {
                    FileInputStream fis = new FileInputStream(storeLocation);
                    fis.read(content);
                }
                catch (Exception e)
                {
                    content = null;
                }
            }
            else
            {
                content = byteStream.toByteArray();
                byteStream = null;
            }
        }

        return content;
    }


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
    public String getString(String encoding)
        throws UnsupportedEncodingException
    {
        return new String(get(), encoding);
    }


    /**
     * Returns the contents of the file as a String, using the default
     * character encoding.  This method uses {@link #get()} to retrieve the
     * contents of the file.
     *
     * @return The contents of the file, as a string.
     */
    public String getString()
    {
        return new String(get());
    }


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
    public File getStoreLocation()
    {
        return storeLocation;
    }


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
    public void write(String file) throws Exception
    {
        if (isInMemory())
        {
            FileOutputStream fout = null;
            try
            {
                fout = new FileOutputStream(file);
                fout.write(get());
            }
            finally
            {
                if (fout != null)
                {
                    fout.close();
                }
            }
        }
        else if (storeLocation != null)
        {
            /*
             * The uploaded file is being stored on disk
             * in a temporary location so move it to the
             * desired file.
             */
            if (storeLocation.renameTo(new File(file)) == false)
            {
                BufferedInputStream in = null;
                BufferedOutputStream out = null;
                try
                {
                    in = new BufferedInputStream(
                        new FileInputStream(storeLocation));
                    out = new BufferedOutputStream(new FileOutputStream(file));
                    byte[] bytes = new byte[2048];
                    int s = 0;
                    while ((s = in.read(bytes)) != -1)
                    {
                        out.write(bytes, 0, s);
                    }
                }
                finally
                {
                    try
                    {
                        in.close();
                    }
                    catch (Exception e)
                    {
                        // ignore
                    }
                    try
                    {
                        out.close();
                    }
                    catch (Exception e)
                    {
                        // ignore
                    }
                }
            }
        }
        else
        {
            /*
             * For whatever reason we cannot write the
             * file to disk.
             */
            throw new FileUploadException(
                "Cannot write uploaded file to disk!");
        }
    }


    /**
     * Deletes the underlying storage for a file item, including deleting any
     * associated temporary disk file. Although this storage will be deleted
     * automatically when the <code>FileItem</code> instance is garbage
     * collected, this method can be used to ensure that this is done at an
     * earlier time, thus preserving system resources.
     */
    public void delete()
    {
        byteStream = null;
        content = null;
        if (storeLocation != null && storeLocation.exists())
        {
            storeLocation.delete();
        }
    }


    /**
     * Returns the name of the field in the multipart form corresponding to
     * this file item.
     *
     * @return The name of the form field.
     */
    public String getFieldName()
    {
        return fieldName;
    }


    /**
     * Sets the field name used to reference this file item.
     *
     * @param fieldName The name of the form field.
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }


    /**
     * Determines whether or not a <code>FileItem</code> instance represents
     * a simple form field.
     *
     * @return <code>true</code> if the instance represents a simple form
     *         field; <code>false</code> if it represents an uploaded file.
     */
    public boolean isFormField()
    {
        return isFormField;
    }


    /**
     * Specifies whether or not a <code>FileItem</code> instance represents
     * a simple form field.
     *
     * @param state <code>true</code> if the instance represents a simple form
     *              field; <code>false</code> if it represents an uploaded file.
     */
    public void setIsFormField(boolean state)
    {
        isFormField = state;
    }


    // --------------------------------------------------------- Public methods


    /**
     * Removes the file contents from the temporary storage.
     */
    protected void finalize()
    {
        if (storeLocation != null && storeLocation.exists())
        {
            storeLocation.delete();
        }
    }


    /**
     * Returns an {@link java.io.OutputStream OutputStream} that can
     * be used for storing the contents of the file.
     *
     * @return An {@link java.io.OutputStream OutputStream} that can be used
     *         for storing the contensts of the file.
     *
     * @exception IOException if an error occurs.
     */
    public OutputStream getOutputStream()
        throws IOException
    {
        if (storeLocation == null)
        {
            return byteStream;
        }
        else
        {
            return new FileOutputStream(storeLocation);
        }
    }


    /**
     * Instantiates a DefaultFileItem. It uses <code>requestSize</code> to
     * decide what temporary storage approach the new item should take.
     *
     * @param path        The path under which temporary files should be
     *                    created.
     * @param name        The original filename in the client's filesystem.
     * @param contentType The content type passed by the browser, or
     *                    <code>null</code> if not defined.
     * @param requestSize The total size of the POST request this item
     *                    belongs to.
     * @param threshold   The maximum size to store in memory.
     *
     * @return A <code>DefaultFileItem</code> instance.
     */
    public static FileItem newInstance(String path,
                                       String name,
                                       String contentType,
                                       int requestSize,
                                       int threshold)
    {
        DefaultFileItem item = new DefaultFileItem(name, contentType);
        if (requestSize > threshold)
        {
            String fileName = getUniqueId();
            fileName = "upload_" + fileName + ".tmp";
            fileName = path + "/" + fileName;
            File f = new File(fileName);
            f.deleteOnExit();
            item.storeLocation = f;
        }
        else
        {
            item.byteStream = new ByteArrayOutputStream();
        }
        return item;
    }


    // -------------------------------------------------------- Private methods


    /**
     * Returns an identifier that is unique within the class loader used to 
     * load this class, but does not have random-like apearance.
     *
     * @return A String with the non-random looking instance identifier.
     */
    private static String getUniqueId()
    {
        int current;
        synchronized (DefaultFileItem.class)
        {
            current = counter++;
        }
        String id = Integer.toString(current);

        // If you manage to get more than 100 million of ids, you'll
        // start getting ids longer than 8 characters.
        if (current < 100000000)
        {
            id = ("00000000" + id).substring(id.length());
        }
        return id;
    }

}
