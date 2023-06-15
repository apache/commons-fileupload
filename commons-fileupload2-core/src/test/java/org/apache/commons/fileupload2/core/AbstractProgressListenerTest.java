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
package org.apache.commons.fileupload2.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link ProgressListener}.
 *
 * @param <F> The subclass of FileUpload.
 * @param <R> The type of FileUpload request.
 */
public abstract class AbstractProgressListenerTest<F extends AbstractFileUpload<R>, R> extends AbstractTest<F, R> {

    protected static class ProgressListenerImpl implements ProgressListener {

        private final long expectedContentLength;

        private final int expectedItems;

        private Long bytesRead;

        private Integer items;

        ProgressListenerImpl(final long contentLength, final int itemCount) {
            expectedContentLength = contentLength;
            expectedItems = itemCount;
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

            assertTrue(bytesRead == null || actualBytesRead >= bytesRead.longValue());
            bytesRead = Long.valueOf(actualBytesRead);
            assertTrue(items == null || actualItems >= items.intValue());
            items = Integer.valueOf(actualItems);
        }

    }

    protected void runTest(final int itemCount, final long contentLength, final R request) throws FileUploadException, IOException {
        final F upload = newFileUpload();
        final ProgressListenerImpl listener = new ProgressListenerImpl(contentLength, itemCount);
        upload.setProgressListener(listener);
        final FileItemInputIterator iter = upload.getItemIterator(request);
        for (int i = 0; i < itemCount; i++) {
            final int idxI = i;
            final FileItemInput fileItemInput = iter.next();
            try (final InputStream inputStream = fileItemInput.getInputStream()) {
                for (int j = 0; j < 16_384 + i; j++) {
                    final int idxJ = j;
                    //
                    // This used to be assertEquals((byte) j, (byte) istream.read()); but this seems to trigger a bug in JRockit, so we express the same like
                    // this:
                    //
                    final byte b1 = (byte) j;
                    final byte b2 = (byte) inputStream.read();
                    assertEquals(b1, b2, () -> String.format("itemCount = %,d, i = %,d, j = %,d", itemCount, idxI, idxJ));
                }
                assertEquals(-1, inputStream.read());
            }
        }
        assertTrue(!iter.hasNext());
        listener.checkFinished();
    }

    /**
     * Parse a very long file upload by using a progress listener.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testProgressListener() throws IOException {
        final int numItems = 512;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < numItems; i++) {
            final String header = "-----1234\r\n" + "Content-Disposition: form-data; name=\"field" + (i + 1) + "\"\r\n" + "\r\n";
            baos.write(header.getBytes(StandardCharsets.US_ASCII));
            for (int j = 0; j < 16384 + i; j++) {
                baos.write((byte) j);
            }
            baos.write("\r\n".getBytes(StandardCharsets.US_ASCII));
        }
        baos.write("-----1234--\r\n".getBytes(StandardCharsets.US_ASCII));
        final byte[] requestBytes = baos.toByteArray();

        R request = newMockHttpServletRequest(requestBytes, null, Constants.CONTENT_TYPE, null);
        runTest(numItems, requestBytes.length, request);
        request = newMockHttpServletRequest(requestBytes, -1L, Constants.CONTENT_TYPE, null);
        runTest(numItems, requestBytes.length, request);
    }

}
