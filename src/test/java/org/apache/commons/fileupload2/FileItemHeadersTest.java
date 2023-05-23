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
package org.apache.commons.fileupload2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.apache.commons.fileupload2.util.FileItemHeadersImpl;
import org.junit.jupiter.api.Test;

/**
 * Unit tests {@link FileItemHeaders} and
 * {@link FileItemHeadersImpl}.
 */
public class FileItemHeadersTest {

    /**
     * @throws Exception
     */
    @Test
    void testFileItemHeaders() throws Exception {
        final FileItemHeadersImpl aMutableFileItemHeaders = new FileItemHeadersImpl();
        aMutableFileItemHeaders.addHeader("Content-Disposition",
                "form-data; name=\"FileItem\"; filename=\"file1.txt\"");
        aMutableFileItemHeaders.addHeader("Content-Type", "text/plain");

        aMutableFileItemHeaders.addHeader("TestHeader", "headerValue1");
        aMutableFileItemHeaders.addHeader("TestHeader", "headerValue2");
        aMutableFileItemHeaders.addHeader("TestHeader", "headerValue3");
        aMutableFileItemHeaders.addHeader("testheader", "headerValue4");

        final Iterator<String> headerNameEnumeration = aMutableFileItemHeaders.getHeaderNames();
        assertEquals("content-disposition", headerNameEnumeration.next());
        assertEquals("content-type", headerNameEnumeration.next());
        assertEquals("testheader", headerNameEnumeration.next());
        assertFalse(headerNameEnumeration.hasNext());

        assertEquals("form-data; name=\"FileItem\"; filename=\"file1.txt\"",
                aMutableFileItemHeaders.getHeader("Content-Disposition"));

        assertEquals("text/plain", aMutableFileItemHeaders.getHeader("Content-Type"));
        assertEquals("text/plain", aMutableFileItemHeaders.getHeader("content-type"));
        assertEquals("headerValue1", aMutableFileItemHeaders.getHeader("TestHeader"));

        assertNull(aMutableFileItemHeaders.getHeader("DummyHeader"));

        Iterator<String> headerValueEnumeration;

        headerValueEnumeration = aMutableFileItemHeaders.getHeaders("Content-Type");
        assertTrue(headerValueEnumeration.hasNext());
        assertEquals("text/plain", headerValueEnumeration.next());
        assertFalse(headerValueEnumeration.hasNext());

        headerValueEnumeration = aMutableFileItemHeaders.getHeaders("content-type");
        assertTrue(headerValueEnumeration.hasNext());
        assertEquals("text/plain", headerValueEnumeration.next());
        assertFalse(headerValueEnumeration.hasNext());

        headerValueEnumeration = aMutableFileItemHeaders.getHeaders("TestHeader");
        assertTrue(headerValueEnumeration.hasNext());
        assertEquals("headerValue1", headerValueEnumeration.next());
        assertTrue(headerValueEnumeration.hasNext());
        assertEquals("headerValue2", headerValueEnumeration.next());
        assertTrue(headerValueEnumeration.hasNext());
        assertEquals("headerValue3", headerValueEnumeration.next());
        assertTrue(headerValueEnumeration.hasNext());
        assertEquals("headerValue4", headerValueEnumeration.next());
        assertFalse(headerValueEnumeration.hasNext());


        headerValueEnumeration = aMutableFileItemHeaders.getHeaders("DummyHeader");
        assertFalse(headerValueEnumeration.hasNext());
    }

}
