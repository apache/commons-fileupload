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
package org.apache.commons.fileupload;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Base class for deriving test cases.
 *
 * @version $Id$
 */
public abstract class FileUploadTestCase {

    protected static final String CONTENT_TYPE = "multipart/form-data; boundary=---1234";

    protected List<FileItem> parseUpload(byte[] bytes) throws FileUploadException {
        return parseUpload(bytes, CONTENT_TYPE);
    }

    protected List<FileItem> parseUpload(byte[] bytes, String contentType) throws FileUploadException {
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        HttpServletRequest request = new MockHttpServletRequest(bytes, contentType);

        List<FileItem> fileItems = upload.parseRequest(request);
        return fileItems;
    }

    protected List<FileItem> parseUpload(String content)
        throws UnsupportedEncodingException, FileUploadException {
        byte[] bytes = content.getBytes("US-ASCII");
        return parseUpload(bytes, CONTENT_TYPE);
    }

}
