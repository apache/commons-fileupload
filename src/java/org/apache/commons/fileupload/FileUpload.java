package org.apache.commons.fileupload;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * <p> Files will be stored in temporary disk storage on in memory,
 * depending on request size, and will be available as {@link
 * org.apache.commons.fileupload.FileItem}s.
 *
 * <p>This class handles multiple
 * files per single html widget, sent using multipar/mixed encoding
 * type, as specified by RFC 1867.  Use {@link
 * #parseRequest(HttpServletRequest, String)} to
 * acquire a list of {@link
 * org.apache.commons.fileupload.FileItem}s associated with given
 * html widget.
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: FileUpload.java,v 1.1 2002/03/24 07:05:13 jmcnally Exp $
 */
public class FileUpload
{
    /**
     * HTTP header.
     */
    public static final String CONTENT_TYPE = "Content-type";

    /**
     * HTTP header.
     */
    public static final String CONTENT_DISPOSITION = "Content-disposition";

    /**
     * Content-disposition value.
     */
    public static final String FORM_DATA = "form-data";

    /**
     * Content-disposition value.
     */
    public static final String ATTACHMENT = "attachment";

    /**
     * HTTP header.
     */
    public static final String MULTIPART_FORM_DATA =
        "multipart/form-data";

    /**
     * HTTP header.
     */
    public static final String MULTIPART_MIXED = "multipart/mixed";

    /**
     * A maximum lenght of a single header line that will be
     * parsed. (1024 bytes).
     */
    public static final int MAX_HEADER_SIZE = 1024;

    private int sizeMax;    
    private int sizeThreshold;
    private String repositoryPath;
    

    /**
     * <p> Processes an <a href="http://rf.cx/rfc1867.html">RFC 1867</a> 
     * compliant <code>multipart/form-data</code> stream.  if files are
     * stored on disk, the path is given by getRepository()
     *
     * @param req The servlet request to be parsed.
     * @exception FileUploadException If there are problems reading/parsing
     * the request or storing files.
     */
    public List parseRequest(HttpServletRequest req)
        throws FileUploadException
    {
        return parseRequest(req, getRepositoryPath());
    }

    /**
     * <p> Processes an <a href="http://rf.cx/rfc1867.html">RFC 1867</a> 
     * compliant <code>multipart/form-data</code> stream.
     *
     * @param req The servlet request to be parsed.
     * @param path The location where the files should be stored.
     * @exception FileUploadException If there are problems reading/parsing
     * the request or storing files.
     */
    public List parseRequest(HttpServletRequest req, String path)
        throws FileUploadException
    {
        ArrayList items = new ArrayList();
        String contentType = req.getHeader(CONTENT_TYPE);

        if(!contentType.startsWith(MULTIPART_FORM_DATA))
        {
            throw new FileUploadException("the request doesn't contain a " +
                MULTIPART_FORM_DATA + " stream");
        }
        int requestSize = req.getContentLength();

        if(requestSize == -1)
        {
            throw new FileUploadException("the request was rejected because " +
                "it's size is unknown");
        }

        if(requestSize > getSizeMax())
        {
            throw new FileUploadException("the request was rejected because " +
                "it's size exceeds allowed range");
        }

        try
        {
            byte[] boundary = contentType.substring(
                contentType.indexOf("boundary=")+9).getBytes();

            InputStream input = (InputStream)req.getInputStream();

            MultipartStream multi = new MultipartStream(input, boundary);
            boolean nextPart = multi.skipPreamble();
            while(nextPart)
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
                                .indexOf("boundary=")+9).getBytes();
                        multi.setBoundary(subBoundary);
                        boolean nextSubPart = multi.skipPreamble();
                        while (nextSubPart)
                        {
                            headers = parseHeaders(multi.readHeaders());
                            if (getFileName(headers) != null)
                            {
                                FileItem item = createItem(path, headers,
                                                           requestSize);
                                OutputStream os = item.getOutputStream();
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
                            FileItem item = createItem(path, headers,
                                                       requestSize);
                            OutputStream os = item.getOutputStream();
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
                            FileItem item = createItem(path, headers,
                                                       requestSize);
                            OutputStream os = item.getOutputStream();
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
        catch(IOException e)
        {
            throw new FileUploadException(
                "Processing of " + MULTIPART_FORM_DATA +
                    " request failed. " + e.getMessage() );
        }

        return items;
    }

    /**
     * <p> Retrieves file name from <code>Content-disposition</code> header.
     *
     * @param headers The HTTP request headers.
     * @return A the file name for the current <code>encapsulation</code>.
     */
    protected String getFileName(Map headers)
    {
        String fileName = null;
        String cd = getHeader(headers, CONTENT_DISPOSITION);
        if(cd.startsWith(FORM_DATA) || cd.startsWith(ATTACHMENT))
        {
            int start = cd.indexOf("filename=\"");
            int end = cd.indexOf('"', start + 10);
            if(start != -1 && end != -1 && (start + 10) != end)
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
     * <p> Retrieves field name from <code>Content-disposition</code> header.
     *
     * @param headers The HTTP request headers.
     * @return The field name for the current <code>encapsulation</code>.
     */
    protected String getFieldName(Map headers)
    {
        String fieldName = null;
        String cd = getHeader(headers, CONTENT_DISPOSITION);
        if(cd != null && cd.startsWith(FORM_DATA))
        {
            int start = cd.indexOf("name=\"");
            int end = cd.indexOf('"', start + 6);
            if(start != -1 && end != -1)
            {
                fieldName = cd.substring(start + 6, end);
            }
        }
        return fieldName;
    }

    /**
     * <p> Creates a new instance of {@link
     * org.apache.commons.fileupload.FileItem}.
     *
     * @param path The path for the FileItem.
     * @param headers The HTTP request headers.
     * @param requestSize The size of the request.
     * @return A newly created <code>FileItem</code>.
     */
    protected FileItem createItem( String path,
                                   Map headers,
                                   int requestSize )
    {
        return DefaultFileItem.newInstance(path, getFileName(headers),
            getHeader(headers, CONTENT_TYPE), requestSize, getSizeThreshold());
    }

    /**
     * <p> Parses the <code>header-part</code> and returns as key/value
     * pairs.
     *
     * <p> If there are multiple headers of the same names, the name
     * will map to a comma-separated list containing the values.
     *
     * @param headerPart The <code>header-part</code> of the current
     * <code>encapsulation</code>.
     * @return The parsed HTTP request headers.
     */
    protected Map parseHeaders( String headerPart )
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
                i=0;
                // Copy a single line of characters into the buffer,
                // omitting trailing CRLF.
                while (i<2 || buffer[i-2] != '\r' || buffer[i-1] != '\n')
                {
                    buffer[i++] = headerPart.charAt(j++);
                }
                header = new String(buffer, 0, i-2);
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
        catch(IndexOutOfBoundsException e)
        {
            // Headers were malformed. continue with all that was
            // parsed.
        }
        return headers;
    }

    /**
     * <p> Returns a header with specified name.
     *
     * @param headers The HTTP request headers.
     * @param name The name of the header to fetch.
     * @return The value of specified header, or a comma-separated
     * list if there were multiple headers of that name.
     */
    protected final String getHeader( Map headers, String name )
    {
        return (String)headers.get(name.toLowerCase());
    }

    // -------------------------------------------------------------------
    // properties

    /**
     * The maximum allowed upload size
     */
    public int getSizeMax() 
    {
        return sizeMax;
    }
    
    /**
     * The maximum allowed upload size
     */
    public void setSizeMax(int  v) 
    {
        this.sizeMax = v;
    }
    

    /**
     * The threshold beyond which files are written directly to disk.
     */
    public int getSizeThreshold()
    {
        return sizeThreshold;
    }

    
    /**
     * The threshold beyond which files are written directly to disk.
     */
    public void setSizeThreshold(int  v) 
    {
        this.sizeThreshold = v;
    }
    
    /**
     * The location used to temporarily store files that are larger
     * than the size threshold.
     */
    public String getRepositoryPath()
    {
        return repositoryPath;
    }
    
    /**
     * The location used to temporarily store files that are larger
     * than the size threshold.
     */
    public void setRepositoryPath(String  v) 
    {
        this.repositoryPath = v;
    }
}
