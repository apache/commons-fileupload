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

import java.util.Iterator;

import org.apache.commons.fileupload.util.FileItemHeadersImpl;

import junit.framework.TestCase;

/**
 * Unit tests {@link FileItemHeaders} and
 * {@link FileItemHeadersImpl}.
 *
 * @author Michael C. Macaluso
 */
public class FileItemHeadersTest extends TestCase {
    /**
     * @throws Exception
     */
    public void testFileItemHeaders() throws Exception {
    	FileItemHeadersImpl aMutableFileItemHeaders = new FileItemHeadersImpl();
    	aMutableFileItemHeaders.addHeader("Content-Disposition", "form-data; name=\"FileItem\"; filename=\"file1.txt\"");
    	aMutableFileItemHeaders.addHeader("Content-Type", "text/plain");

    	aMutableFileItemHeaders.addHeader("TestHeader", "headerValue1");
    	aMutableFileItemHeaders.addHeader("TestHeader", "headerValue2");
    	aMutableFileItemHeaders.addHeader("TestHeader", "headerValue3");
    	aMutableFileItemHeaders.addHeader("testheader", "headerValue4");

    	Iterator headerNameEnumeration = aMutableFileItemHeaders.getHeaderNames();
    	assertEquals("content-disposition", (String) headerNameEnumeration.next());
    	assertEquals("content-type", (String) headerNameEnumeration.next());
    	assertEquals("testheader", (String) headerNameEnumeration.next());
    	assertFalse(headerNameEnumeration.hasNext());

    	assertEquals(aMutableFileItemHeaders.getHeader("Content-Disposition"), "form-data; name=\"FileItem\"; filename=\"file1.txt\"");
    	assertEquals(aMutableFileItemHeaders.getHeader("Content-Type"), "text/plain");
    	assertEquals(aMutableFileItemHeaders.getHeader("content-type"), "text/plain");
    	assertEquals(aMutableFileItemHeaders.getHeader("TestHeader"), "headerValue1");
    	assertNull(aMutableFileItemHeaders.getHeader("DummyHeader"));

    	Iterator headerValueEnumeration;

    	headerValueEnumeration = aMutableFileItemHeaders.getHeaders("Content-Type");
    	assertTrue(headerValueEnumeration.hasNext());
    	assertEquals(headerValueEnumeration.next(), "text/plain");
    	assertFalse(headerValueEnumeration.hasNext());

    	headerValueEnumeration = aMutableFileItemHeaders.getHeaders("content-type");
    	assertTrue(headerValueEnumeration.hasNext());
    	assertEquals(headerValueEnumeration.next(), "text/plain");
    	assertFalse(headerValueEnumeration.hasNext());

    	headerValueEnumeration = aMutableFileItemHeaders.getHeaders("TestHeader");
    	assertTrue(headerValueEnumeration.hasNext());
    	assertEquals(headerValueEnumeration.next(), "headerValue1");
    	assertTrue(headerValueEnumeration.hasNext());
    	assertEquals(headerValueEnumeration.next(), "headerValue2");
    	assertTrue(headerValueEnumeration.hasNext());
    	assertEquals(headerValueEnumeration.next(), "headerValue3");
    	assertTrue(headerValueEnumeration.hasNext());
    	assertEquals(headerValueEnumeration.next(), "headerValue4");
    	assertFalse(headerValueEnumeration.hasNext());

    	headerValueEnumeration = aMutableFileItemHeaders.getHeaders("DummyHeader");
    	assertFalse(headerValueEnumeration.hasNext());
    }

}
