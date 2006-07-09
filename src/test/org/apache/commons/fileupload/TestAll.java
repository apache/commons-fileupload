/*
 * Copyright 2002-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.fileupload;

import junit.framework.*;

/**
 *
 * @author Sean C. Sullivan
 *
 */
public class TestAll extends TestCase {
	/**
	 * Creates a new instance.
	 */
    public TestAll(String testName) {
        super(testName);
    }

    /**
     * Runs the test suite (all other test cases).
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestSuite(DefaultFileItemTest.class));
        suite.addTest(new TestSuite(DiskFileItemSerializeTest.class));
        suite.addTest(new TestSuite(ParameterParserTest.class));
        suite.addTest(new TestSuite(MultipartStreamTest.class));
        suite.addTest(new TestSuite(ServletFileUploadTest.class));
        suite.addTest(new TestSuite(StreamingTest.class));
        return suite;
    }

    /**
     * Command line interface, which invokes all tests.
     */
    public static void main(String args[]) {
        String[] testCaseName = { TestAll.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }
}
