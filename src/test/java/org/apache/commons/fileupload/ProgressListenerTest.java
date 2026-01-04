/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.fileupload;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link ProgressListener}.
 */
public class ProgressListenerTest {

    private static class ProgressListenerImpl implements ProgressListener {

        /** Expected content length. */
        private final long expectedContentLength;

        /** Expected item count. */
        private final int expectedItems;

        /** Bytes read count. */
        private Long bytesRead;

        /** Item count. */
        private Integer items;

        ProgressListenerImpl(final long expectedContentLength, final int expectedItems) {
            this.expectedContentLength = expectedContentLength;
            this.expectedItems = expectedItems;
        }

        void checkFinished() {
            assertEquals(expectedContentLength, bytesRead.longValue());
            assertEquals(expectedItems, items.intValue());
        }

        @Override
        public void update(final long actualBytesRead, final long actualContentLength, final int actualItems) {
            assertTrue(actualBytesRead >= 0 && actualBytesRead <= expectedContentLength);
            assertTrue(actualContentLength == -1 || actualContentLength == expectedContentLength);
            assertTrue(actualItems >= 0 && actualItems <= expectedItems);
            assertTrue(this.bytesRead == null || actualBytesRead >= this.bytesRead.longValue());
            this.bytesRead = Long.valueOf(actualBytesRead);
            assertTrue(items == null || actualItems >= items.intValue());
            this.items = Integer.valueOf(actualItems);
        }
    }

    private void runTest(final int numItems, final long contentLength, final MockHttpServletRequest request) throws FileUploadException, IOException {
        final ServletFileUpload upload = new ServletFileUpload();
        final ProgressListenerImpl listener = new ProgressListenerImpl(contentLength, numItems);
        upload.setProgressListener(listener);
        final FileItemIterator iter = upload.getItemIterator(request);
        for (int i = 0; i < numItems; i++) {
            final FileItemStream stream = iter.next();
            try (InputStream istream = stream.openStream()) {
                final int maxIn = 16384;
                for (int j = 0; j < maxIn + i; j++) {
                    //
                    // This used to be assertEquals((byte) j, (byte) istream.read()); but this seems to trigger a bug in JRockit, so we express the same like
                    // this:
                    //
                    final byte b1 = (byte) j;
                    final byte b2 = (byte) istream.read();
                    if (b1 != b2) {
                        fail("Expected " + b1 + ", got " + b2);
                    }
                }
                assertEquals(-1, istream.read());
            }
        }
        assertTrue(!iter.hasNext());
        listener.checkFinished();
    }

    /**
     * Parse a very long file upload by using a progress listener.
     */
    @Test
    public void testProgressListener() throws Exception {
        final int numItems = 512;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < numItems; i++) {
            final String header = "-----1234\r\n" + "Content-Disposition: form-data; name=\"field" + (i + 1) + "\"\r\n" + "\r\n";
            baos.write(header.getBytes("US-ASCII"));
            final int maxOut = 16384;
            for (int j = 0; j < maxOut + i; j++) {
                baos.write((byte) j);
            }
            baos.write("\r\n".getBytes("US-ASCII"));
        }
        baos.write("-----1234--\r\n".getBytes("US-ASCII"));
        final byte[] contents = baos.toByteArray();
        MockHttpServletRequest request = new MockHttpServletRequest(contents, Constants.CONTENT_TYPE);
        runTest(numItems, contents.length, request);
        request = new MockHttpServletRequest(contents, Constants.CONTENT_TYPE) {

            @Override
            public int getContentLength() {
                return -1;
            }
        };
        runTest(numItems, contents.length, request);
    }
}
