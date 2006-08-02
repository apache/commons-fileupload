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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.FileUploadBase.IOFileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

import junit.framework.TestCase;


/**
 * Unit test for items with varying sizes.
 */
public class StreamingTest extends TestCase
{
    /**
     * Tests a file upload with varying file sizes.
     */
    public void testFileUpload()
            throws IOException, FileUploadException
    {
    	byte[] request = newRequest();
        List fileItems = parseUpload(request);
        Iterator fileIter = fileItems.iterator();
        int add = 16;
        int num = 0;
        for (int i = 0;  i < 16384;  i += add) {
            if (++add == 32) {
                add = 16;
            }
            FileItem item = (FileItem) fileIter.next();
            assertEquals("field" + (num++), item.getFieldName());
            byte[] bytes = item.get();
            assertEquals(i, bytes.length);
            for (int j = 0;  j < i;  j++) {
                assertEquals((byte) j, bytes[j]);
            }
        }
        assertTrue(!fileIter.hasNext());
    }


    /**
     * Tests, whether an invalid request throws a proper
     * exception.
     */
    public void testFileUploadException()
    		throws IOException, FileUploadException {
    	byte[] request = newRequest();
    	byte[] invalidRequest = new byte[request.length-11];
    	System.arraycopy(request, 0, invalidRequest, 0, request.length-11);
    	try {
    		parseUpload(invalidRequest);
	        fail("Expected EndOfStreamException");
    	} catch (IOFileUploadException e) {
    		assertTrue(e.getCause() instanceof MultipartStream.MalformedStreamException);
    	}
    }

    /**
     * Tests, whether an IOException is properly delegated.
     */
    public void testIOException()
    		throws IOException, FileUploadException {
    	byte[] request = newRequest();
    	InputStream stream = new FilterInputStream(new ByteArrayInputStream(request)){
    		private int num;
    		public int read() throws IOException {
    			if (++num > 123) {
    				throw new IOException("123");
    			}
    			return super.read();
    		}
			public int read(byte[] pB, int pOff, int pLen)
					throws IOException {
				for (int i = 0;  i < pLen;  i++) {
					int res = read();
					if (res == -1) {
						return i == 0 ? -1 : i;
					}
					pB[pOff+i] = (byte) res;
				}
				return pLen;
			}
    	};
    	try {
    		parseUpload(stream, request.length);
    	} catch (IOFileUploadException e) {
    		assertTrue(e.getCause() instanceof IOException);
    		assertEquals("123", e.getCause().getMessage());
    	}
    }

    private List parseUpload(byte[] bytes) throws FileUploadException {
    	return parseUpload(new ByteArrayInputStream(bytes), bytes.length);
    }

    private List parseUpload(InputStream pStream, int pLength)
    		throws FileUploadException {
        String contentType = "multipart/form-data; boundary=---1234";

        FileUploadBase upload = new ServletFileUpload();
        upload.setFileItemFactory(new DiskFileItemFactory());
        HttpServletRequest request = new MockHttpServletRequest(pStream,
        		pLength, contentType);

        List fileItems = upload.parseRequest(new ServletRequestContext(request));
        return fileItems;
    }

    private byte[] newRequest() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int add = 16;
        int num = 0;
        for (int i = 0;  i < 16384;  i += add) {
            if (++add == 32) {
                add = 16;
            }
            String header = "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"field" + (num++) + "\"\r\n"
                + "\r\n";
            baos.write(header.getBytes("US-ASCII"));
            for (int j = 0;  j < i;  j++) {
                baos.write((byte) j);
            }
            baos.write("\r\n".getBytes("US-ASCII"));
        }
        baos.write("-----1234--\r\n".getBytes("US-ASCII"));
        return baos.toByteArray();
    }
}
