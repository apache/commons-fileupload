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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link MultipartInput}.
 */
class MultipartStreamTest {

    static private final String BOUNDARY_TEXT = "myboundary";

    /**
     * Checks, whether the maxSize works.
     */
    @Test
    public void testPartHeaderSizeMaxLimit() throws Exception {
        final String request = "-----1234\r\n" + "Content-Disposition: form-data; name=\"file1\"; filename=\"foo1.tab\"\r\n" + "Content-Type: text/whatever\r\n"
                + "Content-Length: 10\r\n" + "\r\n" + "This is the content of the file\n" + "\r\n" + "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"file2\"; filename=\"foo2.tab\"\r\n" + "Content-Type: text/whatever\r\n" + "\r\n"
                + "This is the content of the file\n" + "\r\n" + "-----1234--\r\n";
        final String strContents = request;
        final byte[] byteContents = request.getBytes(StandardCharsets.UTF_8);
        final InputStream input = new ByteArrayInputStream(byteContents);
        final byte[] boundary = "---1234".getBytes();
        final MultipartInput mi = MultipartInput.builder().setInputStream(input).setBoundary(boundary).setPartHeaderSizeMax(100).get();
        assertNotNull(mi);
        try {
            boolean nextPart = mi.skipPreamble();
            while (nextPart) {
                final String headers = mi.readHeaders();
                System.out.print("Headers=" + headers.length() + ", " + headers);
                assertNotNull(headers);
                // process headers
                // create some output stream
                mi.readBodyData(NullOutputStream.INSTANCE);
                nextPart = mi.readBoundary();
            }
            fail("Expected Exception");
        } catch (final FileUploadSizeException fuse) {
            assertEquals(100, fuse.getPermitted());
        }
    }

    @Test
    void testSmallBuffer() {
        final var strData = "foobar";
        final var contents = strData.getBytes();
        final InputStream input = new ByteArrayInputStream(contents);
        final var boundary = BOUNDARY_TEXT.getBytes();
        final var iBufSize = 1;
        assertThrows(IllegalArgumentException.class, () -> MultipartInput.builder().setInputStream(input).setBoundary(boundary).setBufferSize(iBufSize)
                .setProgressNotifier(new MultipartInput.ProgressNotifier(null, contents.length)).get());
    }

    @Test
    void testThreeParamConstructor() throws Exception {
        final var strData = "foobar";
        final var contents = strData.getBytes();
        final InputStream input = new ByteArrayInputStream(contents);
        final var boundary = BOUNDARY_TEXT.getBytes();
        final var iBufSize = boundary.length + MultipartInput.BOUNDARY_PREFIX.length + 1;
        final var ms = MultipartInput.builder().setInputStream(input).setBoundary(boundary).setBufferSize(iBufSize)
                .setProgressNotifier(new MultipartInput.ProgressNotifier(null, contents.length)).get();
        assertNotNull(ms);
    }

    @Test
    void testTwoParamConstructor() throws Exception {
        final var strData = "foobar";
        final var contents = strData.getBytes();
        final InputStream input = new ByteArrayInputStream(contents);
        final var boundary = BOUNDARY_TEXT.getBytes();
        final var ms = MultipartInput.builder().setInputStream(input).setBoundary(boundary)
                .setProgressNotifier(new MultipartInput.ProgressNotifier(null, contents.length)).get();
        assertNotNull(ms);
    }
}
