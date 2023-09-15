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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.commons.fileupload2.core.FileItemFactory.AbstractFileItemBuilder;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DiskFileItem}.
 */
public class DiskFileItemTest {

    @Test
    public void testBuilderHeaders() {
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
     * Tests FILEUPLOAD-352 NullPointerException in DiskFileItem#toString
     */
    @Test
    public void testToStringDefault() {
        new DiskFileItem.Builder().get().toString();
    }
}
