/*
 * Copyright 2001-2004 The Apache Software Foundation
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
