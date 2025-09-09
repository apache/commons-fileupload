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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import org.apache.commons.fileupload2.core.DeferrableOutputStream.State;
import org.apache.commons.fileupload2.core.FileItemFactory.AbstractFileItemBuilder;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DiskFileItem}.
 */
class DiskFileItemTest {
    @SuppressWarnings("deprecation")
    protected void assertState(final DiskFileItem dfi, final State state, final boolean inMemory, final Path parentDir, String testString) throws IOException {
        final DeferrableOutputStream dos = (DeferrableOutputStream) dfi.getOutputStream();
        assertEquals(state, dos.getState());
        assertEquals(inMemory, dfi.isInMemory());
        assertEquals(inMemory, dos.isInMemory());
        if (parentDir == null) {
            assertNull(dos.getPath());
        } else {
            assertNotNull(dos.getPath());
            assertEquals(parentDir, dos.getPath().getParent());
        }
        if (testString != null) {
            assertEquals(testString.length(), dfi.getSize());
            assertEquals(testString.length(), dos.getSize());
            assertEquals(testString, dfi.getString());
            assertArrayEquals(testString.getBytes(), dfi.get());
        } else {
            assertNull(dfi.get());
            assertNull(dfi.getString());
        }
    }

    @Test
    void testBuilderHeaders() {
        final var builder = DiskFileItem.builder();
        assertNotNull(builder.getFileItemHeaders());
        final var fileItem = builder.get();
        assertNotNull(fileItem.getHeaders(), "Missing default headers (empty)");
        assertFalse(fileItem.getHeaders().getHeaderNames().hasNext());
        assertNotNull(fileItem.getHeaders());
        final var fileItemHeaders = AbstractFileItemBuilder.newFileItemHeaders();
        assertNotNull(fileItemHeaders);
        fileItem.setHeaders(fileItemHeaders);
        assertSame(fileItemHeaders, fileItem.getHeaders());
    }

    /**
     * Test for <a href="https://issues.apache.org/jira/browse/FILEUPLOAD-295">FILEUPLOAD-29</a>:
     * A {@link #DiskFileItem} with threshold 0 must always create a file, as soon as data comes in.
     */
    @Test
    void testStateModelWithPositiveThreshold() {
        final IntConsumer tester = (numBytes) -> {
            try {
                final Path testDir = Paths.get("target/unit-tests/" + DiskFileItemTest.class.getSimpleName());
                Files.createDirectories(testDir);
                final Path tempTestDir = Files.createTempDirectory(testDir, "testDir");
                final DiskFileItemFactory dfif = DiskFileItemFactory.builder()
                        .setThreshold(numBytes)
                        .setPath(tempTestDir)
                        .setCharset(StandardCharsets.UTF_8)
                        .get();
                assertEquals(numBytes, dfif.getThreshold());
                final DiskFileItem dfi = dfif.fileItemBuilder()
                        .get();
                // Make sure, that the threshold has not been tampered with.
                assertEquals(numBytes, dfi.getThreshold());
                // We haven't written any data. So, the output file is null.
                assertState(dfi, State.initialized, true, null, null);
                // Write some data.
                final StringBuilder sb = new StringBuilder();
                try (OutputStream os = dfi.getOutputStream()) {
                	for (int i = 0;  i < numBytes-1;  i++) {
                		os.write('.');
                		sb.append('.');
                		assertState(dfi, State.opened, true, null, null);
                	}
                	/*
                	 * Write another byte. This should hit the threshold,
                	 * thus trigger persisting the in memory data.
                	 */
            		os.write(',');
            		sb.append(',');
                    assertState(dfi, State.persisted, false, tempTestDir, null);
                }
                // The output stream is closed now, so the state has changed again.
                assertState(dfi, State.closed, false, tempTestDir, sb.toString());
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        };
        tester.accept(5);
        tester.accept(8193); // Typical buffer size +1
    }

    /**
     * Test for <a href="https://issues.apache.org/jira/browse/FILEUPLOAD-295">FILEUPLOAD-29</a>:
     * A {@link #DiskFileItem} with threshold -1 must always create a (possibly empty) file.
     */
    @Test
    void testStateModelWithThresholdMinusOne() {
        final Consumer<String> tester = (ts) -> {
            try {
                final Path testDir = Paths.get("target/unit-tests/" + DiskFileItemTest.class.getSimpleName());
                Files.createDirectories(testDir);
                final Path tempTestDir = Files.createTempDirectory(testDir, "testDir");
                final DiskFileItemFactory dfif = DiskFileItemFactory.builder()
                        .setBufferSize(-1)
                        .setPath(tempTestDir)
                        .setCharset(StandardCharsets.UTF_8)
                        .get();
                // Make sure, that the threshold has not been tampered with.
                assertEquals(-1, dfif.getThreshold());
                final DiskFileItem dfi = dfif.fileItemBuilder()
                        .get();
                // Make sure, that the threshold has not been tampered with.
                assertEquals(-1, dfi.getThreshold());
                // We haven't written any data. Yet, the output file already exists (threshold=-1)
                assertState(dfi, State.persisted, false, tempTestDir, null);
                try (OutputStream out = dfi.getOutputStream()) {
                    out.write(ts.getBytes());
                }
                // After writing some data, the output file does still exist, except that the size has changed.
                assertState(dfi, State.closed, false, tempTestDir, ts);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        };
        tester.accept("abcdef");
        tester.accept("aAbBcCdDeEfF012345");
    }

    /**
     * Test for <a href="https://issues.apache.org/jira/browse/FILEUPLOAD-295">FILEUPLOAD-29</a>:
     * A {@link #DiskFileItem} with threshold 0 must always create a file, as soon as data comes in.
     */
    @Test
    void testStateModelWithThresholdZero() {
        final IntConsumer tester = (numBytes) -> {
            try {
                final Path testDir = Paths.get("target/unit-tests/" + DiskFileItemTest.class.getSimpleName());
                Files.createDirectories(testDir);
                final Path tempTestDir = Files.createTempDirectory(testDir, "testDir");
                final DiskFileItemFactory dfif = DiskFileItemFactory.builder()
                        .setBufferSize(0)
                        .setPath(tempTestDir)
                        .setCharset(StandardCharsets.UTF_8)
                        .get();
                assertEquals(0, dfif.getThreshold());
                final DiskFileItem dfi = dfif.fileItemBuilder()
                        .get();
                // Make sure, that the threshold has not been tampered with.
                assertEquals(0, dfi.getThreshold());
                // We haven't written any data. So, the output file is null.
                assertState(dfi, State.initialized, true, null, null);
                // Write some data.
                final StringBuilder sb = new StringBuilder();
                try (OutputStream os = dfi.getOutputStream()) {
                	for (int i = 0;  i < numBytes;  i++) {
                		os.write('.');
                		sb.append('.');
                		assertState(dfi, State.persisted, false, tempTestDir, null);
                	}
            		os.write(',');
            		sb.append(',');
                }
                // The output stream is closed now, so the state has changed again.
                assertState(dfi, State.closed, false, tempTestDir, sb.toString());
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        };
        tester.accept(5);
        tester.accept(8193); // Typical buffer size +1
    }
}