/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//fileupload/src/java/org/apache/commons/fileupload/FileUpload.java,v 1.7 2002/07/22 05:00:46 martinc Exp $
 * $Revision: 1.7 $
 * $Date: 2002/07/22 05:00:46 $
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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.MethodUtils;


/**
 * <p>High level API for processing file uploads.
 *
 * <p>This class handles multiple files per single HTML widget, sent using
 * <code>multipar/mixed</code> encoding type, as specified by 
 * <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.  Use {@link
 * #parseRequest(HttpServletRequest, String)} to acquire a list of {@link
 * org.apache.commons.fileupload.FileItem}s associated with a given HTML
 * widget.
 *
 * <p> Files will be stored in temporary disk storage or in memory,
 * depending on request size, and will be available as {@link
 * org.apache.commons.fileupload.FileItem}s.
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 *
 * @version $Id: FileUpload.java,v 1.7 2002/07/22 05:00:46 martinc Exp $
 */
public class FileUpload
{

    // ----------------------------------------------------- Manifest constants


    /**
     * HTTP content type header name.
     */
    public static final String CONTENT_TYPE = "Content-type";


    /**
     * HTTP content disposition header name.
     */
    public static final String CONTENT_DISPOSITION = "Content-disposition";


    /**
     * Content-disposition value for form data.
     */
    public static final String FORM_DATA = "form-data";


    /**
     * Content-disposition value for file attachment.
     */
    public static final String ATTACHMENT = "attachment";


    /**
     * Part of HTTP content type header.
     */
    private static final String MULTIPART = "multipart/";


    /**
     * HTTP content type header for multipart forms.
     */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";


    /**
     * HTTP content type header for multiple uploads.
     */
    public static final String MULTIPART_MIXED = "multipart/mixed";


    /**
     * The maximum length of a single header line that will be parsed.
     * (1024 bytes).
     */
    public static final int MAX_HEADER_SIZE = 1024;


    // ----------------------------------------------------------- Data members


    /**
     * The maximum size permitted for an uploaded file.
     */
    private int sizeMax;


    /**
     * The threshold above which uploads will be stored on disk.
     */
    private int sizeThreshold;


    /**
     * The path to which uploaded files will be stored, if stored on disk.
     */
    private String repositoryPath;


    /**
     * The name of the class to use for <code>FileItem</code>s.
     */
    private String fileItemClassName =
            "org.apache.commons.fileupload.DefaultFileItem";


    /**
     * The cached method for obtaining a new <code>FileItem</code> instance.
     */
    private Method newInstanceMethod;
    

    // ----------------------------------------------------- Property accessors


    /**
     * Returns the maximum allowed upload size.
     *
     * @return The maximum allowed size, in bytes.
     */
    public int getSizeMax() 
    {
        return sizeMax;
    }


    /**
     * Sets the maximum allowed upload size. If negative, there is no maximum.
     *
     * @param sizeMax The maximum allowed size, in bytes, or -1 for no maximum.
     */
    public void setSizeMax(int sizeMax) 
    {
        this.sizeMax = sizeMax;
    }


    /**
     * Returns the size threshold beyond which files are written directly to
     * disk. The default value is 1024 bytes.
     *
     * @return The size threshold, in bytes.
     */
    public int getSizeThreshold()
    {
        return sizeThreshold;
    }


    /**
     * Sets the size threshold beyond which files are written directly to disk.
     *
     * @param sizeThreshold The size threshold, in bytes.
     */
    public void setSizeThreshold(int sizeThreshold) 
    {
        this.sizeThreshold = sizeThreshold;
    }


    /**
     * Returns the location used to temporarily store files that are larger
     * than the configured size threshold.
     *
     * @return The path to the temporary file location.
     */
    public String getRepositoryPath()
    {
        return repositoryPath;
    }

    /**
     * Sets the location used to temporarily store files that are larger
     * than the configured size threshold.
     *
     * @param repositoryPath The path to the temporary file location.
     */
    public void setRepositoryPath(String repositoryPath) 
    {
        this.repositoryPath = repositoryPath;
    }


    /**
     * Returns the fully qualified name of the class which will be used to
     * instantiate <code>FileItem</code> instances when a request is parsed.
     *
     * @return The fully qualified name of the Java class.
     */
    public String getFileItemClassName()
    {
        return fileItemClassName;
    }


    /**
     * Sets the fully qualified name of the class which will be used to
     * instantiate <code>FileItem</code> instances when a request is parsed.
     *
     * @param fileItemClassName The fully qualified name of the Java class.
     */
    public void setFileItemClassName(String fileItemClassName)
    {
        this.fileItemClassName = fileItemClassName;
        this.newInstanceMethod = null;
    }


    // --------------------------------------------------------- Public methods


    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream. If files are stored
     * on disk, the path is given by <code>getRepositoryPath()</code>.
     *
     * @param req The servlet request to be parsed.
     *
     * @return A list of <code>FileItem</code> instances parsed from the
     *         request, in the order that they were transmitted.
     *
     * @exception FileUploadException if there are problems reading/parsing
     *                                the request or storing files.
     */
    public List /* FileItem */ parseRequest(HttpServletRequest req)
        throws FileUploadException
    {
        return parseRequest(req, getSizeThreshold(), getSizeMax(), 
                            getRepositoryPath());
    }


    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream. If files are stored
     * on disk, the path is given by <code>getRepositoryPath()</code>.
     *
     * @param req           The servlet request to be parsed.
     * @param sizeThreshold The max size in bytes to be stored in memory.
     * @param sizeMax       The maximum allowed upload size, in bytes.
     * @param path          The location where the files should be stored.
     *
     * @return A list of <code>FileItem</code> instances parsed from the
     *         request, in the order that they were transmitted.
     *
     * @exception FileUploadException if there are problems reading/parsing
     *                                the request or storing files.
     */
    public List /* FileItem */ parseRequest(HttpServletRequest req,
                                            int sizeThreshold, 
                                            int sizeMax, String path)
        throws FileUploadException
    {
        ArrayList items = new ArrayList();
        String contentType = req.getHeader(CONTENT_TYPE);

        if (!contentType.startsWith(MULTIPART))
        {
            throw new FileUploadException("the request doesn't contain a " +
                MULTIPART_FORM_DATA + " or " + MULTIPART_MIXED + " stream");
        }
        int requestSize = req.getContentLength();

        if (requestSize == -1)
        {
            throw new FileUploadException("the request was rejected because " +
                "it's size is unknown");
        }

        if (sizeMax >= 0 && requestSize > sizeMax)
        {
            throw new FileUploadException("the request was rejected because " +
                "it's size exceeds allowed range");
        }

        try
        {
            byte[] boundary = contentType.substring(
                contentType.indexOf("boundary=") + 9).getBytes();

            InputStream input = (InputStream) req.getInputStream();

            MultipartStream multi = new MultipartStream(input, boundary);
            boolean nextPart = multi.skipPreamble();
            while (nextPart)
            {
                Map headers = parseHeaders(multi.readHeaders());
                String fieldName = getFieldName(headers);
                if (fieldName != null)
                {
                    String subContentType = getHeader(headers, CONTENT_TYPE);
                    if (subContentType != null && subContentType
                                                .startsWith(MULTIPART_MIXED))
                    {
                        // Multiple files.
                        byte[] subBoundary =
                            subContentType.substring(
                                subContentType
                                .indexOf("boundary=") + 9).getBytes();
                        multi.setBoundary(subBoundary);
                        boolean nextSubPart = multi.skipPreamble();
                        while (nextSubPart)
                        {
                            headers = parseHeaders(multi.readHeaders());
                            if (getFileName(headers) != null)
                            {
                                FileItem item = 
                                    createItem(sizeThreshold, path,
                                               headers, requestSize);
                                OutputStream os = 
                                    ((DefaultFileItem) item).getOutputStream();
                                try
                                {
                                    multi.readBodyData(os);
                                }
                                finally
                                {
                                    os.close();
                                }
                                item.setFieldName(getFieldName(headers));
                                items.add(item);
                            }
                            else
                            {
                                // Ignore anything but files inside
                                // multipart/mixed.
                                multi.discardBodyData();
                            }
                            nextSubPart = multi.readBoundary();
                        }
                        multi.setBoundary(boundary);
                    }
                    else
                    {
                        if (getFileName(headers) != null)
                        {
                            // A single file.
                            FileItem item = createItem(sizeThreshold, 
                                                       path, headers,
                                                       requestSize);
                            OutputStream os = 
                                ((DefaultFileItem) item).getOutputStream();
                            try
                            {
                                multi.readBodyData(os);
                            }
                            finally
                            {
                                os.close();
                            }
                            item.setFieldName(getFieldName(headers));
                            items.add(item);
                        }
                        else
                        {
                            // A form field.
                            FileItem item = createItem(sizeThreshold,
                                                       path, headers,
                                                       requestSize);
                            OutputStream os = 
                                ((DefaultFileItem) item).getOutputStream();
                            try
                            {
                                multi.readBodyData(os);
                            }
                            finally
                            {
                                os.close();
                            }
                            item.setFieldName(getFieldName(headers));
                            item.setIsFormField(true);
                            items.add(item);
                        }
                    }
                }
                else
                {
                    // Skip this part.
                    multi.discardBodyData();
                }
                nextPart = multi.readBoundary();
            }
        }
        catch (IOException e)
        {
            throw new FileUploadException(
                "Processing of " + MULTIPART_FORM_DATA +
                    " request failed. " + e.getMessage());
        }

        return items;
    }


    // ------------------------------------------------------ Protected methods


    /**
     * Retrieves the file name from the <code>Content-disposition</code>
     * header.
     *
     * @param headers A <code>Map</code> containing the HTTP request headers.
     *
     * @return The file name for the current <code>encapsulation</code>.
     */
    protected String getFileName(Map /* String, String */ headers)
    {
        String fileName = null;
        String cd = getHeader(headers, CONTENT_DISPOSITION);
        if (cd.startsWith(FORM_DATA) || cd.startsWith(ATTACHMENT))
        {
            int start = cd.indexOf("filename=\"");
            int end = cd.indexOf('"', start + 10);
            if (start != -1 && end != -1 && (start + 10) != end)
            {
                String str = cd.substring(start + 10, end).trim();
                if (str.length() > 0)
                {
                    fileName = str;
                }
            }            
        }
        return fileName;
    }


    /**
     * Retrieves the field name from the <code>Content-disposition</code>
     * header.
     *
     * @param headers A <code>Map</code> containing the HTTP request headers.
     *
     * @return The field name for the current <code>encapsulation</code>.
     */
    protected String getFieldName(Map /* String, String */ headers)
    {
        String fieldName = null;
        String cd = getHeader(headers, CONTENT_DISPOSITION);
        if (cd != null && cd.startsWith(FORM_DATA))
        {
            int start = cd.indexOf("name=\"");
            int end = cd.indexOf('"', start + 6);
            if (start != -1 && end != -1)
            {
                fieldName = cd.substring(start + 6, end);
            }
        }
        return fieldName;
    }


    /**
     * Creates a new {@link org.apache.commons.fileupload.FileItem} instance.
     *
     * @param sizeThreshold The max size in bytes to be stored in memory.
     * @param path          The path for the FileItem.
     * @param headers       A <code>Map</code> containing the HTTP request
     *                      headers.
     * @param requestSize   The total size of the request, in bytes.
     *
     * @return A newly created <code>FileItem</code> instance.
     *
     * @exception FileUploadException if an error occurs.
     */
    protected FileItem createItem(int sizeThreshold,
                                  String path,
                                  Map /* String, String */ headers,
                                  int requestSize)
        throws FileUploadException
    {
        Method newInstanceMethod = getNewInstanceMethod();
        Object[] args = new Object[] {
                path, getFileName(headers), getHeader(headers, CONTENT_TYPE),
                new Integer(requestSize), new Integer(sizeThreshold) };
        FileItem fileItem = null;

        try
        {
            fileItem = (FileItem) newInstanceMethod.invoke(null, args);
        }
        catch (Exception e)
        {
            throw new FileUploadException(e.toString());
        }

        return fileItem;
    }


    /**
     * <p> Returns the <code>Method</code> object to be used to obtain a new
     * <code>FileItem</code> instance.
     *
     * <p> For performance reasons, we cache the method once it has been
     * looked up, since method lookup is one of the more expensive aspects
     * of reflection.
     *
     * @return The <code>newInstance()</code> method to be invoked.
     *
     * @exception FileUploadException if an error occurs.
     */
    protected Method getNewInstanceMethod()
        throws FileUploadException
    {
        // If the method is already cached, just return it.
        if (this.newInstanceMethod != null)
        {
            return this.newInstanceMethod;
        }

        // Load the FileUpload implementation class.
        ClassLoader classLoader =
                Thread.currentThread().getContextClassLoader();
        Class fileItemClass = null;

        if (classLoader == null)
        {
            classLoader = getClass().getClassLoader();
        }

        try
        {
            fileItemClass = classLoader.loadClass(fileItemClassName);
        }
        catch (Exception e)
        {
            throw new FileUploadException(e.toString());
        }

        if (fileItemClass == null)
        {
            throw new FileUploadException(
                    "Failed to load FileItem class: " + fileItemClassName);
        }

        // Find the newInstance() method.
        Class[] parameterTypes = new Class[] {
                String.class, String.class, String.class,
                Integer.TYPE, Integer.TYPE };
        Method newInstanceMethod = MethodUtils.getAccessibleMethod(
                fileItemClass, "newInstance", parameterTypes);

        if (newInstanceMethod == null)
        {
            throw new FileUploadException(
                    "Failed find newInstance() method in FileItem class: "
                            + fileItemClassName);
        }

        // Cache the method so that all this only happens once.
        this.newInstanceMethod = newInstanceMethod;
        return newInstanceMethod;
    }


    /**
     * <p> Parses the <code>header-part</code> and returns as key/value
     * pairs.
     *
     * <p> If there are multiple headers of the same names, the name
     * will map to a comma-separated list containing the values.
     *
     * @param headerPart The <code>header-part</code> of the current
     *                   <code>encapsulation</code>.
     *
     * @return A <code>Map</code> containing the parsed HTTP request headers.
     */
    protected Map /* String, String */ parseHeaders(String headerPart)
    {
        Map headers = new HashMap();
        char buffer[] = new char[MAX_HEADER_SIZE];
        boolean done = false;
        int j = 0;
        int i;
        String header, headerName, headerValue;
        try
        {
            while (!done)
            {
                i = 0;
                // Copy a single line of characters into the buffer,
                // omitting trailing CRLF.
                while (i < 2 || buffer[i - 2] != '\r' || buffer[i - 1] != '\n')
                {
                    buffer[i++] = headerPart.charAt(j++);
                }
                header = new String(buffer, 0, i - 2);
                if (header.equals(""))
                {
                    done = true;
                }
                else
                {
                    if (header.indexOf(':') == -1)
                    {
                        // This header line is malformed, skip it.
                        continue;
                    }
                    headerName = header.substring(0, header.indexOf(':'))
                        .trim().toLowerCase();
                    headerValue =
                        header.substring(header.indexOf(':') + 1).trim();
                    if (getHeader(headers, headerName) != null)
                    {
                        // More that one heder of that name exists,
                        // append to the list.
                        headers.put(headerName,
                                    getHeader(headers, headerName) + ',' +
                                    headerValue);
                    }
                    else
                    {
                        headers.put(headerName, headerValue);
                    }
                }
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            // Headers were malformed. continue with all that was
            // parsed.
        }
        return headers;
    }


    /**
     * Returns the header with the specified name from the supplied map. The
     * header lookup is case-insensitive.
     *
     * @param headers A <code>Map</code> containing the HTTP request headers.
     * @param name    The name of the header to return.
     *
     * @return The value of specified header, or a comma-separated list if
     *         there were multiple headers of that name.
     */
    protected final String getHeader(Map /* String, String */ headers,
                                     String name)
    {
        return (String) headers.get(name.toLowerCase());
    }

}
