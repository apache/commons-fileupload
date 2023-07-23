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
package org.apache.commons.fileupload2.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.SimplePathVisitor;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Serialization Unit tests for {@link DiskFileItem}.
 */
public class DiskFileItemSerializeTest {

    /**
     * Use a private repository to catch any files left over by tests.
     */
    private static final Path REPOSITORY = PathUtils.getTempDirectory().resolve("DiskFileItemRepo");

    /**
     * Content type for regular form items.
     */
    private static final String TEXT_CONTENT_TYPE = "text/plain";

    /**
     * Very low threshold for testing memory versus disk options.
     */
    private static final int THRESHOLD = 16;

    /**
     * Compare content bytes.
     */
    private void compareBytes(final String text, final byte[] origBytes, final byte[] newBytes) {
        assertNotNull(origBytes, "origBytes must not be null");
        assertNotNull(newBytes, "newBytes must not be null");
        assertEquals(origBytes.length, newBytes.length, text + " byte[] length");
        for (var i = 0; i < origBytes.length; i++) {
            assertEquals(origBytes[i], newBytes[i], text + " byte[" + i + "]");
        }
    }

    /**
     * Create content bytes of a specified size.
     */
    private byte[] createContentBytes(final int size) {
        final var buffer = new StringBuilder(size);
        byte count = 0;
        for (var i = 0; i < size; i++) {
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
     */
    private DiskFileItem createFileItem(final byte[] contentBytes) throws IOException {
        return createFileItem(contentBytes, REPOSITORY);
    }

    /**
     * Create a FileItem with the specfied content bytes and repository.
     */
    private DiskFileItem createFileItem(final byte[] contentBytes, final Path repository) throws IOException {
        // @formatter:off
        final FileItemFactory<DiskFileItem> factory = DiskFileItemFactory.builder()
                .setBufferSize(THRESHOLD)
                .setPath(repository)
                .get();
        // @formatter:on
        // @formatter:off
        final var item = factory.fileItemBuilder()
                .setFieldName("textField")
                .setContentType(TEXT_CONTENT_TYPE)
                .setFormField(true)
                .setFileName("My File Name")
                .get();
        // @formatter:on

        try (var os = item.getOutputStream()) {
            os.write(contentBytes);
        }
        return item;
    }

    /**
     * Deserializes.
     */
    private Object deserialize(final ByteArrayOutputStream baos) {
        return SerializationUtils.deserialize(baos.toByteArray());
    }

    /**
     * Serializes.
     */
    private ByteArrayOutputStream serialize(final Object target) throws IOException {
        try (final var baos = new ByteArrayOutputStream();
                final var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(target);
            oos.flush();
            return baos;
        }
    }

    @BeforeEach
    public void setUp() throws IOException {
        if (Files.exists(REPOSITORY)) {
            PathUtils.deleteDirectory(REPOSITORY);
        } else {
            Files.createDirectories(REPOSITORY);
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (Files.exists(REPOSITORY)) {
            PathUtils.visitFileTree(new SimplePathVisitor() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    System.out.println("Found leftover file " + file);
                    return FileVisitResult.CONTINUE;
                }

            }, REPOSITORY);
            PathUtils.deleteDirectory(REPOSITORY);
        }
    }

    /**
     * Test creation of a field for which the amount of data falls above the configured threshold.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testAboveThreshold() throws IOException {
        // Create the FileItem
        final var testFieldValueBytes = createContentBytes(THRESHOLD + 1);
        final var item = createFileItem(testFieldValueBytes);

        // Check state is as expected
        assertFalse(item.isInMemory(), "Initial: in memory");
        assertEquals(item.getSize(), testFieldValueBytes.length, "Initial: size");
        compareBytes("Initial", item.get(), testFieldValueBytes);

        testWritingToFile(item, testFieldValueBytes);
        item.delete();
    }

    /**
     * Test creation of a field for which the amount of data falls below the configured threshold.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testBelowThreshold() throws IOException {
        // Create the FileItem
        final var testFieldValueBytes = createContentBytes(THRESHOLD - 1);
        testInMemoryObject(testFieldValueBytes);
    }

    @Test
    public void testCheckFileName() {
        assertThrows(InvalidPathException.class, () -> DiskFileItem.checkFileName("\0"));
    }

    /**
     * Helper method to test creation of a field.
     */
    private void testInMemoryObject(final byte[] testFieldValueBytes) throws IOException {
        testInMemoryObject(testFieldValueBytes, REPOSITORY);
    }

    /**
     * Helper method to test creation of a field when a repository is used.
     */
    private void testInMemoryObject(final byte[] testFieldValueBytes, final Path repository) throws IOException {
        final var item = createFileItem(testFieldValueBytes, repository);

        // Check state is as expected
        assertTrue(item.isInMemory(), "Initial: in memory");
        assertEquals(item.getSize(), testFieldValueBytes.length, "Initial: size");
        compareBytes("Initial", item.get(), testFieldValueBytes);
        testWritingToFile(item, testFieldValueBytes);
        item.delete();
    }

    /**
     * Test deserialization fails when repository is not valid.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testInvalidRepository() throws IOException {
        // Create the FileItem
        final var testFieldValueBytes = createContentBytes(THRESHOLD);
        final var repository = PathUtils.getTempDirectory().resolve("file");
        final var item = createFileItem(testFieldValueBytes, repository);
        assertThrows(IOException.class, () -> deserialize(serialize(item)));
    }

    /**
     * Test creation of a field for which the amount of data equals the configured threshold.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testThreshold() throws IOException {
        // Create the FileItem
        final var testFieldValueBytes = createContentBytes(THRESHOLD);
        testInMemoryObject(testFieldValueBytes);
    }

    /**
     * Test serialization and deserialization when repository is not null.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testValidRepository() throws IOException {
        // Create the FileItem
        final var testFieldValueBytes = createContentBytes(THRESHOLD);
        testInMemoryObject(testFieldValueBytes, REPOSITORY);
    }

    /**
     * Helper method to test writing item contents to a file.
     */
    private void testWritingToFile(final DiskFileItem item, final byte[] testFieldValueBytes) throws IOException {
        final var temp = Files.createTempFile("fileupload", null);
        // Note that the file exists and is initially empty;
        // write() must be able to handle that.
        item.write(temp);
        compareBytes("Initial", Files.readAllBytes(temp), testFieldValueBytes);
    }
}
