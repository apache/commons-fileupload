/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//fileupload/src/test/org/apache/commons/fileupload/DefaultFileItemTest.java,v 1.2 2003/10/09 21:16:47 rdonkin Exp $
 * $Revision: 1.2 $
 * $Date: 2003/10/09 21:16:47 $
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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
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


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import junit.framework.TestCase;
import org.apache.commons.fileupload.DefaultFileItem;
import org.apache.commons.fileupload.DefaultFileItemFactory;


/**
 * Unit tests for {@link org.apache.commons.fileupload.DefaultFileItem}.
 *
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 */
public class DefaultFileItemTest extends TestCase
 {

    /**
     * Content type for regular form items.
     */
    private static final String textContentType = "text/plain";

    /**
     * Content type for file uploads.
     */
    private static final String fileContentType = "application/octet-stream";

    /**
     * Very low threshold for testing memory vs. disk options.
     */
    private static final int threshold = 16;

    /**
     * Standard JUnit test case constructor.
     *
     * @param name The name of the test case.
     */
    public DefaultFileItemTest(String name)
    {
        super(name);
    }

    /**
     * Test construction of a regular text field.
     */
    public void testTextFieldConstruction()
    {
        FileItemFactory factory = createFactory(null);
        String textFieldName = "textField";

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        assertNotNull(item);
        assertEquals(item.getFieldName(), textFieldName);
        assertEquals(item.getContentType(), textContentType);
        assertTrue(item.isFormField());
        assertNull(item.getName());
    }

    /**
     * Test construction of a file field.
     */
    public void testFileFieldConstruction()
    {
        FileItemFactory factory = createFactory(null);
        String fileFieldName = "fileField";
        String fileName = "originalFileName";

        FileItem item = factory.createItem(
                fileFieldName,
                fileContentType,
                false,
                fileName
        );
        assertNotNull(item);
        assertEquals(item.getFieldName(), fileFieldName);
        assertEquals(item.getContentType(), fileContentType);
        assertFalse(item.isFormField());
        assertEquals(item.getName(), fileName);
    }

    /**
     * Test creation of a field for which the amount of data falls below the
     * configured threshold.
     */
    public void testBelowThreshold()
    {
        FileItemFactory factory = createFactory(null);
        String textFieldName = "textField";
        String textFieldValue = "0123456789";
        byte[] testFieldValueBytes = textFieldValue.getBytes();

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        assertNotNull(item);

        try
        {
            OutputStream os = item.getOutputStream();
            os.write(testFieldValueBytes);
            os.close();
        }
        catch(IOException e)
        {
            fail("Unexpected IOException");
        }
        assertTrue(item.isInMemory());
        assertEquals(item.getSize(), testFieldValueBytes.length);
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes));
        assertEquals(item.getString(), textFieldValue);
    }

    /**
     * Test creation of a field for which the amount of data falls above the
     * configured threshold, where no specific repository is configured.
     */
    public void testAboveThresholdDefaultRepository()
    {
        doTestAboveThreshold(null);
    }

    /**
     * Test creation of a field for which the amount of data falls above the
     * configured threshold, where a specific repository is configured.
     */
    public void testAboveThresholdSpecifiedRepository()
    {
        String tempPath = System.getProperty("java.io.tmpdir");
        String tempDirName = "testAboveThresholdSpecifiedRepository";
        File tempDir = new File(tempPath, tempDirName);
        tempDir.mkdir();
        doTestAboveThreshold(tempDir);
        assertTrue(tempDir.delete());
    }

    /**
     * Common code for cases where the amount of data is above the configured
     * threshold, but the ultimate destination of the data has not yet been
     * determined.
     *
     * @param repository The directory within which temporary files will be
     *                   created.
     */
    public void doTestAboveThreshold(File repository)
    {
        FileItemFactory factory = createFactory(repository);
        String textFieldName = "textField";
        String textFieldValue = "01234567890123456789";
        byte[] testFieldValueBytes = textFieldValue.getBytes();

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        assertNotNull(item);

        try
        {
            OutputStream os = item.getOutputStream();
            os.write(testFieldValueBytes);
            os.close();
        }
        catch(IOException e)
        {
            fail("Unexpected IOException");
        }
        assertFalse(item.isInMemory());
        assertEquals(item.getSize(), testFieldValueBytes.length);
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes));
        assertEquals(item.getString(), textFieldValue);

        assertTrue(item instanceof DefaultFileItem);
        DefaultFileItem dfi = (DefaultFileItem) item;
        File storeLocation = dfi.getStoreLocation();
        assertNotNull(storeLocation);
        assertTrue(storeLocation.exists());
        assertEquals(storeLocation.length(), testFieldValueBytes.length);

        if (repository != null)
        {
            assertEquals(storeLocation.getParentFile(), repository);
        }

        item.delete();
    }


    /**
     * Creates a new <code>FileItemFactory</code> and returns it, obscuring
     * from the caller the underlying implementation of this interface.
     *
     * @param repository The directory within which temporary files will be
     *                   created.
     * @return the new <code>FileItemFactory</code> instance.
     */
    protected FileItemFactory createFactory(File repository)
    {
        return new DefaultFileItemFactory(threshold, repository);
    }
}
