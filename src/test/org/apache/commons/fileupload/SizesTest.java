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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import junit.framework.TestCase;


/**
 * Unit test for items with varying sizes.
 */
public class SizesTest extends TestCase
{
    public void testFileUpload()
            throws IOException, FileUploadException
    {
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

        List fileItems = parseUpload(baos.toByteArray());
        Iterator fileIter = fileItems.iterator();
        add = 16;
        num = 0;
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

    private List parseUpload(byte[] bytes) throws FileUploadException {
        String contentType = "multipart/form-data; boundary=---1234";

        FileUploadBase upload = new DiskFileUpload();
        HttpServletRequest request = new MockHttpServletRequest(bytes, contentType);

        List fileItems = upload.parseRequest(request);
        return fileItems;
    }

}
