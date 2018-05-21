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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import static org.apache.commons.fileupload.util.EncodingConstants.US_ASCII_CHARSET;
import org.junit.Test;

/**
 * Tests the {@link ProgressListener}.
 */
public class ProgressListenerTest {

    /**
     * The test implementation.
     */
    private class ProgressListenerImpl implements ProgressListener {

        /**
         * The expected content length.
         */
        private final long expectedContentLength;
        /**
         * The number of expected items.
         */
        private final int expectedItems;
        /**
         * The number of bytes read.
         */
        private Long bytesRead;
        /**
         * The number of items.
         */
        private Integer items;

        ProgressListenerImpl(long pContentLength, int pItems) {
            expectedContentLength = pContentLength;
            expectedItems = pItems;
        }

        @Override
        public void update(long pBytesRead, long pContentLength, int pItems) {
            assertTrue(pBytesRead >= 0  &&  pBytesRead <= expectedContentLength);
            assertTrue(pContentLength == -1  ||  pContentLength == expectedContentLength);
            assertTrue(pItems >= 0  &&  pItems <= expectedItems);

            assertTrue(bytesRead == null  ||  pBytesRead >= bytesRead.longValue());
            bytesRead = new Long(pBytesRead);
            assertTrue(items == null  ||  pItems >= items.intValue());
            items = new Integer(pItems);
        }

        void checkFinished() {
            assertEquals(expectedContentLength, bytesRead.longValue());
            assertEquals(expectedItems, items.intValue());
        }

    }

    /**
     * Parse a very long file upload by using a progress listener.
     * @throws UnsupportedEncodingException if
     * {@link EncodingConstants#US_ASCII_CHARSET} is not supported
     * @throws FileUploadException if the file upload fails
     * @throws java.io.IOException if an I/O exception occurs
     */
    @Test
    public void testProgressListener() throws UnsupportedEncodingException,
            IOException,
            FileUploadException {
        final int numItems = 512;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0;  i < numItems;  i++) {
            String header = "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"field" + (i + 1) + "\"\r\n"
                + "\r\n";
            baos.write(header.getBytes(US_ASCII_CHARSET));
            final int iterationLimit = 16384;
            for (int j = 0;  j < iterationLimit + i;  j++) {
                baos.write((byte) j);
            }
            baos.write("\r\n".getBytes(US_ASCII_CHARSET));
        }
        baos.write("-----1234--\r\n".getBytes(US_ASCII_CHARSET));
        byte[] contents = baos.toByteArray();

        HttpServletRequestMock request = new HttpServletRequestMock(contents, Constants.CONTENT_TYPE);
        runTest(numItems, contents.length, request);
        request = new HttpServletRequestMock(contents, Constants.CONTENT_TYPE) {
            @Override
            public int getContentLength() {
                return -1;
            }
        };
        runTest(numItems, contents.length, request);
    }

    private void runTest(final int numItems,
            long pContentLength,
            HttpServletRequestMock request) throws FileUploadException, IOException {
        ServletFileUpload upload = new ServletFileUpload();
        ProgressListenerImpl listener = new ProgressListenerImpl(pContentLength, numItems);
        upload.setProgressListener(listener);
        FileItemIterator iter = upload.getItemIterator(request);
        for (int i = 0;  i < numItems;  i++) {
            FileItemStream stream = iter.next();
            InputStream istream = stream.openStream();
            final int iterationLimit = 16384;
            for (int j = 0;  j < iterationLimit + i;  j++) {
                /**
                 * This used to be
                 *     assertEquals((byte) j, (byte) istream.read());
                 * but this seems to trigger a bug in JRockit, so
                 * we express the same like this:
                 */
                byte b1 = (byte) j;
                byte b2 = (byte) istream.read();
                if (b1 != b2) {
                    fail("Expected " + b1 + ", got " + b2);
                }
            }
            assertEquals(-1, istream.read());
            istream.close();
        }
        assertTrue(!iter.hasNext());
        listener.checkFinished();
    }

}
