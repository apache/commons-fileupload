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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Tests for {@link org.apache.commons.fileupload.DefaultFileItem}.
 */
@SuppressWarnings({ "deprecation" }) // unit tests for deprecated class
public class DefaultFileItemTest {

    /**
     * Content type for regular form items.
     */
    private static final String CONTENT_TYPE_TEXT = "text/plain";

    /**
     * Content type for file uploads.
     */
    private static final String CONTENT_TYPE_FILE = "application/octet-stream";

    /**
     * Very low threshold for testing memory versus disk options.
     */
    private static final int THRESHOLD = 16;

    /** Charset name. */
    static final String CHARSET_ISO_8859_1 = StandardCharsets.ISO_8859_1.name();

    /** Charset name. */
    static final String CHARSET_US_ASCII = StandardCharsets.US_ASCII.name();

    /** Charset name. */
    static final String CHARSET_UTF8 = StandardCharsets.UTF_8.name();

    /** Charset name. */
    static final String CHARSET_KOI8_R = "KOI8_R";

    /** Charset name. */
    static final String CHARSET_WIN1251 = "Cp1251";

    /** Test fixture. */
    static final int[] SWISS_GERMAN_STUFF_UNICODE = { 0x47, 0x72, 0xFC, 0x65, 0x7A, 0x69, 0x5F, 0x7A, 0xE4, 0x6D, 0xE4 };

    /** Test fixture. */
    static final int[] SWISS_GERMAN_STUFF_ISO8859_1 = { 0x47, 0x72, 0xFC, 0x65, 0x7A, 0x69, 0x5F, 0x7A, 0xE4, 0x6D, 0xE4 };

    /** Test fixture. */
    static final int[] SWISS_GERMAN_STUFF_UTF8 = { 0x47, 0x72, 0xC3, 0xBC, 0x65, 0x7A, 0x69, 0x5F, 0x7A, 0xC3, 0xA4, 0x6D, 0xC3, 0xA4 };

    /** Test fixture. */
    static final int[] RUSSIAN_STUFF_UNICODE = { 0x412, 0x441, 0x435, 0x43C, 0x5F, 0x43F, 0x440, 0x438, 0x432, 0x435, 0x442 };

    /** Test fixture. */
    static final int[] RUSSIAN_STUFF_UTF8 = { 0xD0, 0x92, 0xD1, 0x81, 0xD0, 0xB5, 0xD0, 0xBC, 0x5F, 0xD0, 0xBF, 0xD1, 0x80, 0xD0, 0xB8, 0xD0, 0xB2, 0xD0, 0xB5,
            0xD1, 0x82 };

    /** Test fixture. */
    static final int[] RUSSIAN_STUFF_KOI8R = { 0xF7, 0xD3, 0xC5, 0xCD, 0x5F, 0xD0, 0xD2, 0xC9, 0xD7, 0xC5, 0xD4 };

    /** Test fixture. */
    static final int[] RUSSIAN_STUFF_WIN1251 = { 0xC2, 0xF1, 0xE5, 0xEC, 0x5F, 0xEF, 0xF0, 0xE8, 0xE2, 0xE5, 0xF2 };

    private static String constructString(final int[] unicodeChars) {
        final StringBuilder buffer = new StringBuilder();
        if (unicodeChars != null) {
            for (final int unicodeChar : unicodeChars) {
                buffer.append((char) unicodeChar);
            }
        }
        return buffer.toString();
    }

    /**
     * Creates a new {@code FileItemFactory} and returns it, obscuring from the caller the underlying implementation of this interface.
     *
     * @param repository The directory within which temporary files will be created.
     * @return the new {@code FileItemFactory} instance.
     */
    protected FileItemFactory createFactory(final File repository) {
        return new DefaultFileItemFactory(THRESHOLD, repository);
    }

    /**
     * Common code for cases where the amount of data is above the configured threshold, but the ultimate destination of the data has not yet been determined.
     *
     * @param repository The directory within which temporary files will be created.
     */
    private void doTestAboveThreshold(final File repository) throws IOException {
        final FileItemFactory factory = createFactory(repository);
        final String textFieldName = "textField";
        final String textFieldValue = "01234567890123456789";
        final byte[] testFieldValueBytes = textFieldValue.getBytes();
        final FileItem item = factory.createItem(textFieldName, CONTENT_TYPE_TEXT, true, null);
        assertNotNull(item);
        try (OutputStream os = item.getOutputStream()) {
            os.write(testFieldValueBytes);
        }
        assertFalse(item.isInMemory());
        assertEquals(item.getSize(), testFieldValueBytes.length);
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes));
        assertEquals(item.getString(), textFieldValue);
        assertTrue(item instanceof DefaultFileItem);
        final DefaultFileItem dfi = (DefaultFileItem) item;
        final File storeLocation = dfi.getStoreLocation();
        assertNotNull(storeLocation);
        assertTrue(storeLocation.exists());
        assertEquals(storeLocation.length(), testFieldValueBytes.length);
        if (repository != null) {
            assertEquals(storeLocation.getParentFile(), repository);
        }
        item.delete();
    }

    /**
     * Test creation of a field for which the amount of data falls above the configured threshold, where no specific repository is configured.
     */
    @Test
    public void testAboveThresholdDefaultRepository() throws IOException {
        doTestAboveThreshold(null);
    }

    /**
     * Test creation of a field for which the amount of data falls above the configured threshold, where a specific repository is configured.
     */
    @Test
    public void testAboveThresholdSpecifiedRepository() throws IOException {
        final String tempPath = System.getProperty("java.io.tmpdir");
        final String tempDirName = "testAboveThresholdSpecifiedRepository";
        final File tempDir = new File(tempPath, tempDirName);
        FileUtils.forceMkdir(tempDir);
        doTestAboveThreshold(tempDir);
        assertTrue(tempDir.delete());
    }

    /**
     * Test creation of a field for which the amount of data falls below the configured threshold.
     */
    @Test
    public void testBelowThreshold() throws IOException {
        final FileItemFactory factory = createFactory(null);
        final String textFieldName = "textField";
        final String textFieldValue = "0123456789";
        final byte[] testFieldValueBytes = textFieldValue.getBytes();
        final FileItem item = factory.createItem(textFieldName, CONTENT_TYPE_TEXT, true, null);
        assertNotNull(item);
        try (OutputStream os = item.getOutputStream()) {
            os.write(testFieldValueBytes);
        }
        assertTrue(item.isInMemory());
        assertEquals(item.getSize(), testFieldValueBytes.length);
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes));
        assertEquals(item.getString(), textFieldValue);
    }

    /**
     * Test construction of content charset.
     */
    @Test
    public void testContentCharSet() throws Exception {
        final FileItemFactory factory = createFactory(null);
        String teststr = constructString(SWISS_GERMAN_STUFF_UNICODE);
        FileItem item = factory.createItem("doesnotmatter", "text/plain; charset=" + CHARSET_ISO_8859_1, true, null);
        try (OutputStream out = item.getOutputStream()) {
            for (final int element : SWISS_GERMAN_STUFF_ISO8859_1) {
                out.write(element);
            }
        }
        assertEquals(teststr, teststr, item.getString());
        item = factory.createItem("doesnotmatter", "text/plain; charset=" + CHARSET_UTF8, true, null);
        try (OutputStream out = item.getOutputStream()) {
            for (final int element : SWISS_GERMAN_STUFF_UTF8) {
                out.write(element);
            }
        }
        assertEquals(teststr, teststr, item.getString());
        teststr = constructString(RUSSIAN_STUFF_UNICODE);
        item = factory.createItem("doesnotmatter", "text/plain; charset=" + CHARSET_KOI8_R, true, null);
        try (OutputStream out = item.getOutputStream()) {
            for (final int element : RUSSIAN_STUFF_KOI8R) {
                out.write(element);
            }
        }
        assertEquals(teststr, teststr, item.getString());
        item = factory.createItem("doesnotmatter", "text/plain; charset=" + CHARSET_WIN1251, true, null);
        try (OutputStream out = item.getOutputStream()) {
            for (final int element : RUSSIAN_STUFF_WIN1251) {
                out.write(element);
            }
        }
        assertEquals(teststr, teststr, item.getString());
        item = factory.createItem("doesnotmatter", "text/plain; charset=" + CHARSET_UTF8, true, null);
        try (OutputStream out = item.getOutputStream()) {
            for (final int element : RUSSIAN_STUFF_UTF8) {
                out.write(element);
            }
        }
        assertEquals(teststr, teststr, item.getString());
    }

    /**
     * Test construction of a file field.
     */
    @Test
    public void testFileFieldConstruction() {
        final FileItemFactory factory = createFactory(null);
        final String fileFieldName = "fileField";
        final String fileName = "originalFileName";
        final FileItem item = factory.createItem(fileFieldName, CONTENT_TYPE_FILE, false, fileName);
        assertNotNull(item);
        assertEquals(item.getFieldName(), fileFieldName);
        assertEquals(item.getContentType(), CONTENT_TYPE_FILE);
        assertFalse(item.isFormField());
        assertEquals(item.getName(), fileName);
    }

    /**
     * Test construction of a regular text field.
     */
    @Test
    public void testTextFieldConstruction() {
        final FileItemFactory factory = createFactory(null);
        final String textFieldName = "textField";
        final FileItem item = factory.createItem(textFieldName, CONTENT_TYPE_TEXT, true, null);
        assertNotNull(item);
        assertEquals(item.getFieldName(), textFieldName);
        assertEquals(item.getContentType(), CONTENT_TYPE_TEXT);
        assertTrue(item.isFormField());
        assertNull(item.getName());
    }
}
