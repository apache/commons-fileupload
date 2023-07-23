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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * Unit tests {@link MultipartInput}.
 */
public class MultipartStreamTest {

    static private final String BOUNDARY_TEXT = "myboundary";

    @Test
    public void testSmallBuffer() {
        final var strData = "foobar";
        final var contents = strData.getBytes();
        final InputStream input = new ByteArrayInputStream(contents);
        final var boundary = BOUNDARY_TEXT.getBytes();
        final var iBufSize = 1;
        assertThrows(IllegalArgumentException.class, () -> MultipartInput.builder().setInputStream(input).setBoundary(boundary).setBufferSize(iBufSize)
                .setProgressNotifier(new MultipartInput.ProgressNotifier(null, contents.length)).get());
    }

    @Test
    public void testThreeParamConstructor() throws Exception {
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
    public void testTwoParamConstructor() throws Exception {
        final var strData = "foobar";
        final var contents = strData.getBytes();
        final InputStream input = new ByteArrayInputStream(contents);
        final var boundary = BOUNDARY_TEXT.getBytes();
        final var ms = MultipartInput.builder().setInputStream(input).setBoundary(boundary)
                .setProgressNotifier(new MultipartInput.ProgressNotifier(null, contents.length)).get();
        assertNotNull(ms);
    }

}
