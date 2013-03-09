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

import junit.framework.TestCase;
import java.io.*;

/**
 * Unit tests {@link org.apache.commons.fileupload.MultipartStream}.
 *
 * @version $Id$
 */
public class MultipartStreamTest extends TestCase {

    static private final String BOUNDARY_TEXT = "myboundary";

    public void testThreeParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        int iBufSize = boundary.length;
        MultipartStream ms = new MultipartStream(
                input,
                boundary,
                iBufSize,
                new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
    }

    public void testTwoParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        MultipartStream ms = new MultipartStream(
                input,
                boundary,
                new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
    }

}
