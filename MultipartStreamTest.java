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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import junit.framework.Assert;
import org.junit.Test;
import java.io.*;
/**
 * Unit tests {@link org.apache.commons.fileupload.MultipartStream}.
 *
 * @version $Id: MultipartStreamTest.java 1565190 2014-02-06 12:01:48Z markt $
 */
public class MultipartStreamTest {
 
    static private final String BOUNDARY_TEXT = "myboundary";

    @Test
    public void testThreeParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        int iBufSize =
                boundary.length + MultipartStream.BOUNDARY_PREFIX.length + 1;
        MultipartStream ms = new MultipartStream(
                input,
                boundary,
                iBufSize,
                new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
        Assert.assertEquals(10240, ms.HEADER_PART_SIZE_MAX);
    }

    @SuppressWarnings("unused")
    @Test(expected=IllegalArgumentException.class)
    public void testSmallBuffer() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        int iBufSize = 1;
        new MultipartStream(
                input,
                boundary,
                iBufSize,
                new MultipartStream.ProgressNotifier(null, contents.length));
    }

    @Test
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
    
    public void testUpdateHeaderPartSizeMax() throws Exception {
      System.setProperty("HEADER_PART_SIZE_MAX", "9999999");
      final String strData = "foobar";
      final byte[] contents = strData.getBytes();
      InputStream input = new ByteArrayInputStream(contents);
      byte[] boundary = BOUNDARY_TEXT.getBytes();
      int iBufSize = boundary.length;
      MultipartStream ms = new MultipartStream(input, boundary, iBufSize, new MultipartStream.ProgressNotifier(null,
          contents.length));

      Assert.assertEquals(9999999, ms.HEADER_PART_SIZE_MAX);
    }

}
