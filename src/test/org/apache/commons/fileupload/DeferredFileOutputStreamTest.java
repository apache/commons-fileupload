/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//fileupload/src/test/org/apache/commons/fileupload/Attic/DeferredFileOutputStreamTest.java,v 1.1 2003/04/27 17:30:06 martinc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/04/27 17:30:06 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */


package org.apache.commons.fileupload;

import junit.framework.TestCase;
import org.apache.commons.fileupload.DeferredFileOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Unit tests for the <code>DeferredFileOutputStream</code> class.
 *
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 *
 * @version $Id: DeferredFileOutputStreamTest.java,v 1.1 2003/04/27 17:30:06 martinc Exp $
 */
public class DeferredFileOutputStreamTest extends TestCase
 {

    /**
     * The test data as a string (which is the simplest form).
     */
    private String testString = "0123456789";

    /**
     * The test data as a byte array, derived from the string.
     */
    private byte[] testBytes = testString.getBytes();

    /**
     * Standard JUnit test case constructor.
     *
     * @param name The name of the test case.
     */
    public DeferredFileOutputStreamTest(String name)
    {
        super(name);
    }

    /**
     * Tests the case where the amount of data falls below the threshold, and
     * is therefore confined to memory.
     */
    public void testBelowThreshold()
    {
        DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length + 42, null);
        try
        {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        }
        catch (IOException e) {
            fail("Unexpected IOException");
        }
        assertTrue(dfos.isInMemory());

        byte[] resultBytes = dfos.getData();
        assertTrue(resultBytes.length == testBytes.length);
        assertTrue(Arrays.equals(resultBytes, testBytes));
    }

    /**
     * Tests the case where the amount of data is exactly the same as the
     * threshold. The behavior should be the same as that for the amount of
     * data being below (i.e. not exceeding) the threshold.
     */
    public void testAtThreshold() {
        DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length, null);
        try
        {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        }
        catch (IOException e) {
            fail("Unexpected IOException");
        }
        assertTrue(dfos.isInMemory());

        byte[] resultBytes = dfos.getData();
        assertTrue(resultBytes.length == testBytes.length);
        assertTrue(Arrays.equals(resultBytes, testBytes));
    }

    /**
     * Tests the case where the amount of data exceeds the threshold, and is
     * therefore written to disk. The actual data written to disk is verified,
     * as is the file itself.
     */
    public void testAboveThreshold() {
        File testFile = new File("testAboveThreshold.dat");

        // Ensure that the test starts from a clean base.
        testFile.delete();

        DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length - 5, testFile);
        try
        {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        }
        catch (IOException e) {
            fail("Unexpected IOException");
        }
        assertFalse(dfos.isInMemory());
        assertNull(dfos.getData());

        // Verify that the data is consistent.
        try
        {
            FileInputStream fis = new FileInputStream(testFile);
            assertTrue(fis.available() == testBytes.length);

            byte[] resultBytes = new byte[testBytes.length];
            assertTrue(fis.read(resultBytes) == testBytes.length);

            assertTrue(Arrays.equals(resultBytes, testBytes));
            assertTrue(fis.read(resultBytes) == -1);

            try
            {
                fis.close();
            }
            catch (IOException e) {
                // Ignore an exception on close
            }
        }
        catch (FileNotFoundException e) {
            fail("Unexpected FileNotFoundException");
        }
        catch (IOException e) {
            fail("Unexpected IOException");
        }

        // Ensure that the test starts from a clean base.
        testFile.delete();
    }
}
