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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.InvalidPathException;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Serialization Unit tests for {@link org.apache.commons.fileupload.disk.DiskFileItem}.
 */
public class DiskFileItemSerializeTest {

    // Use a private repo to catch any files left over by tests
    private static final File REPO = new File(SystemProperties.getJavaIoTmpdir(), "diskfileitemrepo");

    /**
     * Content type for regular form items.
     */
    private static final String CONTENT_TYPE_TEXT = "text/plain";

    /**
     * Very low threshold for testing memory versus disk options.
     */
    private static final int THRESHOLD = 16;

    /**
     * Compare content bytes.
     */
    private void compareBytes(final String text, final byte[] origBytes, final byte[] newBytes) {
        assertNotNull("origBytes must not be null", origBytes);
        assertNotNull("newBytes must not be null", newBytes);
        assertEquals(text + " byte[] length", origBytes.length, newBytes.length);
        for (int i = 0; i < origBytes.length; i++) {
            assertEquals(text + " byte[" + i + "]", origBytes[i], newBytes[i]);
        }
    }

    /**
     * Create content bytes of a specified size.
     */
    private byte[] createContentBytes(final int size) {
        final StringBuilder buffer = new StringBuilder(size);
        byte count = 0;
        for (int i = 0; i < size; i++) {
            buffer.append(count + "");
            count++;
            if (count > 9) {
                count = 0;
            }
        }
        return buffer.toString().getBytes();
    }

    /**
     * Create a FileItem with the specfied content bytes.
     *
     * @throws IOException test failure.
     */
    private FileItem createFileItem(final byte[] contentBytes) throws IOException {
        return createFileItem(contentBytes, REPO);
    }

    /**
     * Create a FileItem with the specfied content bytes and repository.
     *
     * @throws IOException test failure.
     */
    private FileItem createFileItem(final byte[] contentBytes, final File repository) throws IOException {
        final FileItemFactory factory = new DiskFileItemFactory(THRESHOLD, repository);
        final String textFieldName = "textField";
        final FileItem item = factory.createItem(textFieldName, CONTENT_TYPE_TEXT, true, "My File Name");
        try (OutputStream os = item.getOutputStream()) {
            os.write(contentBytes);
        }
        return item;
    }

    /**
     * Tests deserialization.
     */
    private Object deserialize(final ByteArrayOutputStream baos) throws Exception {
        Object result = null;
        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        result = ois.readObject();
        bais.close();
        return result;
    }

    /**
     * Tests serialization.
     */
    private ByteArrayOutputStream serialize(final Object target) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(target);
            oos.flush();
        }
        return baos;
    }

    @Before
    public void setUp() throws Exception {
        if (REPO.exists()) {
            FileUtils.deleteDirectory(REPO);
        }
        FileUtils.forceMkdir(REPO);
    }

    @After
    public void tearDown() throws IOException {
        for (final File file : FileUtils.listFiles(REPO, null, true)) {
            System.out.println("Found leftover file " + file);
        }
        FileUtils.deleteDirectory(REPO);
    }

    /**
     * Test creation of a field for which the amount of data falls above the configured threshold.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testAboveThreshold() throws IOException {
        // Create the FileItem
        final byte[] testFieldValueBytes = createContentBytes(THRESHOLD + 1);
        final FileItem item = createFileItem(testFieldValueBytes);
        // Check state is as expected
        assertFalse("Initial: in memory", item.isInMemory());
        assertEquals("Initial: size", item.getSize(), testFieldValueBytes.length);
        compareBytes("Initial", item.get(), testFieldValueBytes);
        item.delete();
    }

    /**
     * Test creation of a field for which the amount of data falls below the configured threshold.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testBelowThreshold() throws IOException {
        // Create the FileItem
        final byte[] testFieldValueBytes = createContentBytes(THRESHOLD - 1);
        testInMemoryObject(testFieldValueBytes);
    }

    /**
     * Helper method to test creation of a field.
     *
     * @throws IOException test failure.
     */
    private void testInMemoryObject(final byte[] testFieldValueBytes) throws IOException {
        testInMemoryObject(testFieldValueBytes, REPO);
    }

    /**
     * Helper method to test creation of a field when a repository is used.
     *
     * @throws IOException test failure.
     */
    private void testInMemoryObject(final byte[] testFieldValueBytes, final File repository) throws IOException {
        final FileItem item = createFileItem(testFieldValueBytes, repository);
        // Check state is as expected
        assertTrue("Initial: in memory", item.isInMemory());
        assertEquals("Initial: size", item.getSize(), testFieldValueBytes.length);
        compareBytes("Initial", item.get(), testFieldValueBytes);
        item.delete();
    }

    /**
     * Test deserialization fails when repository is not valid.
     */
    @Test(expected = IOException.class)
    public void testInvalidRepository() throws Exception {
        // Create the FileItem
        final byte[] testFieldValueBytes = createContentBytes(THRESHOLD);
        final File repository = new File(SystemProperties.getJavaIoTmpdir(), "file");
        final FileItem item = createFileItem(testFieldValueBytes, repository);
        deserialize(serialize(item));
    }

    /**
     * Test deserialization fails when repository contains a null character.
     */
    @Test(expected = InvalidPathException.class)
    public void testInvalidRepositoryWithNullChar() throws Exception {
        // Create the FileItem
        final byte[] testFieldValueBytes = createContentBytes(THRESHOLD);
        final File repository = new File(SystemProperties.getJavaIoTmpdir(), "\0");
        final FileItem item = createFileItem(testFieldValueBytes, repository);
        deserialize(serialize(item));
    }

    /**
     * Test creation of a field for which the amount of data equals the configured threshold.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testThreshold() throws IOException {
        // Create the FileItem
        final byte[] testFieldValueBytes = createContentBytes(THRESHOLD);
        testInMemoryObject(testFieldValueBytes);
    }

    /**
     * Test serialization and deserialization when repository is not null.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testValidRepository() throws IOException {
        // Create the FileItem
        final byte[] testFieldValueBytes = createContentBytes(THRESHOLD);
        testInMemoryObject(testFieldValueBytes, REPO);
    }
}
