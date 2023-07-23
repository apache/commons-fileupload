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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.junit.jupiter.api.Test;

/**
 * Unit tests {@link FileItemHeaders} and {@link FileItemHeadersImpl}.
 */
public class FileItemHeadersTest {

    /**
     * @throws Exception
     */
    @Test
    public void testFileItemHeaders() throws Exception {
        final var mutableFileItemHeaders = new FileItemHeadersImpl();
        mutableFileItemHeaders.addHeader("Content-Disposition", "form-data; name=\"FileItem\"; filename=\"file1.txt\"");
        mutableFileItemHeaders.addHeader("Content-Type", "text/plain");

        mutableFileItemHeaders.addHeader("TestHeader", "headerValue1");
        mutableFileItemHeaders.addHeader("TestHeader", "headerValue2");
        mutableFileItemHeaders.addHeader("TestHeader", "headerValue3");
        mutableFileItemHeaders.addHeader("testheader", "headerValue4");

        final var headerNameIterator = mutableFileItemHeaders.getHeaderNames();
        assertEquals("content-disposition", headerNameIterator.next());
        assertEquals("content-type", headerNameIterator.next());
        assertEquals("testheader", headerNameIterator.next());
        assertFalse(headerNameIterator.hasNext());

        assertEquals(mutableFileItemHeaders.getHeader("Content-Disposition"), "form-data; name=\"FileItem\"; filename=\"file1.txt\"");
        assertEquals(mutableFileItemHeaders.getHeader("Content-Type"), "text/plain");
        assertEquals(mutableFileItemHeaders.getHeader("content-type"), "text/plain");
        assertEquals(mutableFileItemHeaders.getHeader("TestHeader"), "headerValue1");
        assertNull(mutableFileItemHeaders.getHeader("DummyHeader"));

        Iterator<String> headerValueEnumeration;

        headerValueEnumeration = mutableFileItemHeaders.getHeaders("Content-Type");
        assertTrue(headerValueEnumeration.hasNext());
        assertEquals(headerValueEnumeration.next(), "text/plain");
        assertFalse(headerValueEnumeration.hasNext());

        headerValueEnumeration = mutableFileItemHeaders.getHeaders("content-type");
        assertTrue(headerValueEnumeration.hasNext());
        assertEquals(headerValueEnumeration.next(), "text/plain");
        assertFalse(headerValueEnumeration.hasNext());

        headerValueEnumeration = mutableFileItemHeaders.getHeaders("TestHeader");
        assertTrue(headerValueEnumeration.hasNext());
        assertEquals(headerValueEnumeration.next(), "headerValue1");
        assertTrue(headerValueEnumeration.hasNext());
        assertEquals(headerValueEnumeration.next(), "headerValue2");
        assertTrue(headerValueEnumeration.hasNext());
        assertEquals(headerValueEnumeration.next(), "headerValue3");
        assertTrue(headerValueEnumeration.hasNext());
        assertEquals(headerValueEnumeration.next(), "headerValue4");
        assertFalse(headerValueEnumeration.hasNext());

        headerValueEnumeration = mutableFileItemHeaders.getHeaders("DummyHeader");
        assertFalse(headerValueEnumeration.hasNext());
    }

}
