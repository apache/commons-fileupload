/*
 * Copyright 2001-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.fileupload;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import junit.framework.TestCase;

/**
 * Unit tests {@link org.apache.commons.fileupload.DiskFileUpload}.
 *
 * @author <a href="mailto:jmcnally@apache.org">John McNally</a>
 * @author Sean C. Sullivan
 *
 */
public class FileUploadTest extends TestCase
{
	public void testWithInvalidRequest()
    {
    	FileUploadBase fu = null;

    	fu = new DiskFileUpload();

    	HttpServletRequest req = HttpServletRequestFactory.createInvalidHttpServletRequest();


    	try
    	{
    		fu.parseRequest(req);
    		fail("testWithInvalidRequest: expected exception was not thrown");
    	}
    	catch (FileUploadException expected)
    	{
    		// this exception is expected
    	}

    }


	public void testWithNullContentType()
    {
    	FileUploadBase fu = new DiskFileUpload();

    	HttpServletRequest req = HttpServletRequestFactory.createHttpServletRequestWithNullContentType();

    	try
    	{
    		fu.parseRequest(req);
    		fail("testWithNullContentType: expected exception was not thrown");
    	}
    	catch (DiskFileUpload.InvalidContentTypeException expected)
    	{
    		// this exception is expected
    	}
        catch (FileUploadException unexpected)
        {
    		fail("testWithNullContentType: unexpected exception was thrown");
        }

    }


    public FileUploadTest(String name)
    {
        super(name);
    }


    public void testFileUpload() throws IOException, FileUploadException {
        List fileItems = parseUpload("-----1234\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
                        "Content-Type: text/whatever\r\n" +
                        "\r\n" +
                        "This is the content of the file\n" +
                        "\r\n" +
                        "-----1234\r\n" +
                        "Content-Disposition: form-data; name=\"field\"\r\n" +
                        "\r\n" +
                        "fieldValue\r\n" +
                        "-----1234\r\n" +
                        "Content-Disposition: form-data; name=\"multi\"\r\n" +
                        "\r\n" +
                        "value1\r\n" +
                        "-----1234\r\n" +
                        "Content-Disposition: form-data; name=\"multi\"\r\n" +
                        "\r\n" +
                        "value2\r\n" +
                        "-----1234--\r\n");
        assertEquals(4, fileItems.size());

        FileItem file = (FileItem) fileItems.get(0);
        assertEquals("file", file.getFieldName());
        assertFalse(file.isFormField());
        assertEquals("This is the content of the file\n", file.getString());
        assertEquals("text/whatever", file.getContentType());
        assertEquals("foo.tab", file.getName());

        FileItem field = (FileItem) fileItems.get(1);
        assertEquals("field", field.getFieldName());
        assertTrue(field.isFormField());
        assertEquals("fieldValue", field.getString());

        FileItem multi0 = (FileItem) fileItems.get(2);
        assertEquals("multi", multi0.getFieldName());
        assertTrue(multi0.isFormField());
        assertEquals("value1", multi0.getString());

        FileItem multi1 = (FileItem) fileItems.get(3);
        assertEquals("multi", multi1.getFieldName());
        assertTrue(multi1.isFormField());
        assertEquals("value2", multi1.getString());
    }

    /**
     * This is what the browser does if you submit the form without choosing a file.
     */
    public void testEmptyFile() throws UnsupportedEncodingException, FileUploadException {
        List fileItems = parseUpload ("-----1234\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"\"\r\n" +
                "\r\n" +
                "\r\n" +
                "-----1234--\r\n");
        assertEquals(1, fileItems.size());

        FileItem file = (FileItem) fileItems.get(0);
        assertFalse(file.isFormField());
        assertEquals("", file.getString());
        assertEquals("", file.getName());
    }

    private List parseUpload(String content) throws UnsupportedEncodingException, FileUploadException {
        byte[] bytes = content.getBytes("US-ASCII");

        String contentType = "multipart/form-data; boundary=---1234";

        FileUploadBase upload = new DiskFileUpload();
        HttpServletRequest request = new MyHttpServletRequest(bytes, contentType);

        List fileItems = upload.parseRequest(request);
        return fileItems;
    }

}

