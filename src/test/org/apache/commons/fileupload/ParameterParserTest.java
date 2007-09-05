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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.Map;

/**
 * Unit tests for {@link ParameterParser}.
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 */
public class ParameterParserTest extends TestCase
{

    // ------------------------------------------------------------ Constructor
    public ParameterParserTest(String testName)
    {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[])
    {
        String[] testCaseName = { ParameterParserTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite()
    {
        return new TestSuite(ParameterParserTest.class);
    }

    public void testParsing()
    {
        String s =
            "test; test1 =  stuff   ; test2 =  \"stuff; stuff\"; test3=\"stuff";
        ParameterParser parser = new ParameterParser();
        Map params = parser.parse(s, ';');
        assertEquals(null, params.get("test"));
        assertEquals("stuff", params.get("test1"));
        assertEquals("stuff; stuff", params.get("test2"));
        assertEquals("\"stuff", params.get("test3"));

        params = parser.parse(s, new char[] { ',', ';' });
        assertEquals(null, params.get("test"));
        assertEquals("stuff", params.get("test1"));
        assertEquals("stuff; stuff", params.get("test2"));
        assertEquals("\"stuff", params.get("test3"));

        s = "  test  , test1=stuff   ,  , test2=, test3, ";
        params = parser.parse(s, ',');
        assertEquals(null, params.get("test"));
        assertEquals("stuff", params.get("test1"));
        assertEquals(null, params.get("test2"));
        assertEquals(null, params.get("test3"));

        s = "  test";
        params = parser.parse(s, ';');
        assertEquals(null, params.get("test"));

        s = "  ";
        params = parser.parse(s, ';');
        assertEquals(0, params.size());

        s = " = stuff ";
        params = parser.parse(s, ';');
        assertEquals(0, params.size());
    }

    public void testContentTypeParsing()
    {
        String s = "text/plain; Charset=UTF-8";
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        Map params = parser.parse(s, ';');
        assertEquals("UTF-8", params.get("charset"));
    }

    public void testParsingEscapedChars()
    {
        String s = "param = \"stuff\\\"; more stuff\"";
        ParameterParser parser = new ParameterParser();
        Map params = parser.parse(s, ';');
        assertEquals(1, params.size());
        assertEquals("stuff\\\"; more stuff", params.get("param"));

        s = "param = \"stuff\\\\\"; anotherparam";
        params = parser.parse(s, ';');
        assertEquals(2, params.size());
        assertEquals("stuff\\\\", params.get("param"));
        assertNull(params.get("anotherparam"));
    }

    // See: http://issues.apache.org/jira/browse/FILEUPLOAD-139
    public void testFileUpload139() 
    {
        ParameterParser parser = new ParameterParser();
        String s = "Content-type: multipart/form-data , boundary=AaB03x";
        Map params = parser.parse(s, new char[] { ',', ';' });
        assertEquals("AaB03x", params.get("boundary"));

        s = "Content-type: multipart/form-data, boundary=AaB03x";
        params = parser.parse(s, new char[] { ';', ',' });
        assertEquals("AaB03x", params.get("boundary"));

        s = "Content-type: multipart/mixed, boundary=BbC04y";
        params = parser.parse(s, new char[] { ',', ';' });
        assertEquals("BbC04y", params.get("boundary"));
    }
}
