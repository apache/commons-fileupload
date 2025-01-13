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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Common tests for implementations of {@link AbstractFileUpload}. This is a parameterized test. Tests must be valid and common to all implementations of
 * FileUpload added as parameter in this class.
 *
 * @param <AFU> The {@link AbstractFileUpload} type.
 * @param <R>   The FileUpload request type.
 * @param <I>   The FileItem type.
 * @param <F>   The FileItemFactory type.
 */
public abstract class AbstractFileUploadTest<AFU extends AbstractFileUpload<R, I, F>, R, I extends FileItem<I>, F extends FileItemFactory<I>>
        extends AbstractFileUploadWrapper<AFU, R, I, F> {

    protected AbstractFileUploadTest(final AFU fileUpload) {
        super(fileUpload);
    }

    private void assertHeaders(final String[] headerNames, final String[] headerValues, final I fileItems, final int index) {
        for (var i = 0; i < headerNames.length; i++) {
            final var value = fileItems.getHeaders().getHeader(headerNames[i]);
            if (i == index) {
                assertEquals(headerValues[i], value);
            } else {
                assertNull(value);
            }
        }
    }

    /**
     * Tests <a href="https://issues.apache.org/jira/browse/FILEUPLOAD-239">FILEUPLOAD-239</a>
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testContentTypeAttachment() throws IOException {
        // @formatter:off
        final var fileItems = parseUpload(upload,
                "-----1234\r\n" +
                "content-disposition: form-data; name=\"field1\"\r\n" +
                "\r\n" +
                "Joe Blow\r\n" +
                "-----1234\r\n" +
                "content-disposition: form-data; name=\"pics\"\r\n" +
                "Content-type: multipart/mixed, boundary=---9876\r\n" +
                "\r\n" +
                "-----9876\r\n" +
                "Content-disposition: attachment; filename=\"file1.txt\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "... contents of file1.txt ...\r\n" +
                "-----9876--\r\n" +
                "-----1234--\r\n");
        // @formatter:on
        assertEquals(2, fileItems.size());

        final var field = fileItems.get(0);
        assertEquals("field1", field.getFieldName());
        assertTrue(field.isFormField());
        assertEquals("Joe Blow", field.getString());

        final var fileItem = fileItems.get(1);
        assertEquals("pics", fileItem.getFieldName());
        assertFalse(fileItem.isFormField());
        assertEquals("... contents of file1.txt ...", fileItem.getString());
        assertEquals("text/plain", fileItem.getContentType());
        assertEquals("file1.txt", fileItem.getName());
    }

    /**
     * This is what the browser does if you submit the form without choosing a file.
     *
     * @throws FileUploadException Test failure.
     */
    @Test
    public void testEmptyFile() throws IOException {
        // @formatter:off
        final var fileItems = parseUpload (upload,
                                                "-----1234\r\n" +
                                                "Content-Disposition: form-data; name=\"file\"; filename=\"\"\r\n" +
                                                "\r\n" +
                                                "\r\n" +
                                                "-----1234--\r\n");
        // @formatter:on
        assertEquals(1, fileItems.size());

        final var file = fileItems.get(0);
        assertFalse(file.isFormField());
        assertEquals("", file.getString());
        assertEquals("", file.getName());
    }

    @Test
    public void testFileNameCaseSensitivity() throws IOException {
        // @formatter:off
        final var fileItems = parseUpload(upload,
                                               "-----1234\r\n" +
                                               "Content-Disposition: form-data; "
                                             + "name=\"FiLe\"; filename=\"FOO.tab\"\r\n" +
                                               "Content-Type: text/whatever\r\n" +
                                               "\r\n" +
                                               "This is the content of the file\n" +
                                               "\r\n" +
                                               "-----1234--\r\n");
        // @formatter:on
        assertEquals(1, fileItems.size());

        final var file = fileItems.get(0);
        assertEquals("FiLe", file.getFieldName());
        assertEquals("FOO.tab", file.getName());
    }

    @Test
    public void testFileUpload() throws IOException {
        // @formatter:off
        final var fileItems = parseUpload(upload,
                                               "-----1234\r\n" +
                                               "Content-Disposition: "
                                               + "form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
                                               "Content-Type: text/whatever\r\n" +
                                               "\r\n" +
                                               "This is the content of the file\n" +
                                               "\r\n" +
                                               "-----1234\r\n" +
                                               "Content-Disposition: form-data; name=\"field\"\r\n" +
                                               "\r\n" +
                                               "fieldValue\r\n" +
                                               "-----1234\r\n" +
                                               "Content-Disposition: form-data; name=\"multi\"\r\n" +
                                               "\r\n" +
                                               "value1\r\n" +
                                               "-----1234\r\n" +
                                               "Content-Disposition: form-data; name=\"multi\"\r\n" +
                                               "\r\n" +
                                               "value2\r\n" +
                                               "-----1234--\r\n");
        // @formatter:on
        assertEquals(4, fileItems.size());

        final var file = fileItems.get(0);
        assertEquals("file", file.getFieldName());
        assertFalse(file.isFormField());
        assertEquals("This is the content of the file\n", file.getString());
        assertEquals("text/whatever", file.getContentType());
        assertEquals("foo.tab", file.getName());

        final var field = fileItems.get(1);
        assertEquals("field", field.getFieldName());
        assertTrue(field.isFormField());
        assertEquals("fieldValue", field.getString());

        final var multi0 = fileItems.get(2);
        assertEquals("multi", multi0.getFieldName());
        assertTrue(multi0.isFormField());
        assertEquals("value1", multi0.getString());

        final var multi1 = fileItems.get(3);
        assertEquals("multi", multi1.getFieldName());
        assertTrue(multi1.isFormField());
        assertEquals("value2", multi1.getString());
    }

    /**
     * Test case for <a href="https://issues.apache.org/jira/browse/FILEUPLOAD-130">FILEUPLOAD-130</a>.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testFileUpload130() throws IOException {
        final String[] headerNames = { "SomeHeader", "OtherHeader", "YetAnotherHeader", "WhatAHeader" };
        final String[] headerValues = { "present", "Is there", "Here", "Is That" };
        // @formatter:off
        final var fileItems = parseUpload(upload,
                                               "-----1234\r\n" +
                                               "Content-Disposition: form-data; name=\"file\"; "
                                             + "filename=\"foo.tab\"\r\n" +
                                               "Content-Type: text/whatever\r\n" +
                                               headerNames[0] + ": " + headerValues[0] + "\r\n" +
                                               "\r\n" +
                                               "This is the content of the file\n" +
                                               "\r\n" +
                                               "-----1234\r\n" +
                                               "Content-Disposition: form-data; \r\n" +
                                               "\tname=\"field\"\r\n" +
                                               headerNames[1] + ": " + headerValues[1] + "\r\n" +
                                               "\r\n" +
                                               "fieldValue\r\n" +
                                               "-----1234\r\n" +
                                               "Content-Disposition: form-data;\r\n" +
                                               "     name=\"multi\"\r\n" +
                                               headerNames[2] + ": " + headerValues[2] + "\r\n" +
                                               "\r\n" +
                                               "value1\r\n" +
                                               "-----1234\r\n" +
                                               "Content-Disposition: form-data; name=\"multi\"\r\n" +
                                               headerNames[3] + ": " + headerValues[3] + "\r\n" +
                                               "\r\n" +
                                               "value2\r\n" +
                                               "-----1234--\r\n");
        // @formatter:on
        assertEquals(4, fileItems.size());

        final var file = fileItems.get(0);
        assertHeaders(headerNames, headerValues, file, 0);

        final var field = fileItems.get(1);
        assertHeaders(headerNames, headerValues, field, 1);

        final var multi0 = fileItems.get(2);
        assertHeaders(headerNames, headerValues, multi0, 2);

        final var multi1 = fileItems.get(3);
        assertHeaders(headerNames, headerValues, multi1, 3);
    }

    /**
     * Test for <a href="https://issues.apache.org/jira/browse/FILEUPLOAD-62">FILEUPLOAD-62</a>
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testFILEUPLOAD62() throws IOException {
        // @formatter:off
        final var contentType = "multipart/form-data; boundary=AaB03x";
        final var request =
            "--AaB03x\r\n" +
            "content-disposition: form-data; name=\"field1\"\r\n" +
            "\r\n" +
            "Joe Blow\r\n" +
            "--AaB03x\r\n" +
            "content-disposition: form-data; name=\"pics\"\r\n" +
            "Content-type: multipart/mixed; boundary=BbC04y\r\n" +
            "\r\n" +
            "--BbC04y\r\n" +
            "Content-disposition: attachment; filename=\"file1.txt\"\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "... contents of file1.txt ...\r\n" +
            "--BbC04y\r\n" +
            "Content-disposition: attachment; filename=\"file2.gif\"\r\n" +
            "Content-type: image/gif\r\n" +
            "Content-Transfer-Encoding: binary\r\n" +
            "\r\n" +
            "...contents of file2.gif...\r\n" +
            "--BbC04y--\r\n" +
            "--AaB03x--";
        // @formatter:on
        final var fileItems = parseUpload(upload, request.getBytes(StandardCharsets.US_ASCII), contentType);
        assertEquals(3, fileItems.size());
        final var item0 = fileItems.get(0);
        assertEquals("field1", item0.getFieldName());
        assertNull(item0.getName());
        assertEquals("Joe Blow", new String(item0.get()));
        final var item1 = fileItems.get(1);
        assertEquals("pics", item1.getFieldName());
        assertEquals("file1.txt", item1.getName());
        assertEquals("... contents of file1.txt ...", new String(item1.get()));
        final var item2 = fileItems.get(2);
        assertEquals("pics", item2.getFieldName());
        assertEquals("file2.gif", item2.getName());
        assertEquals("...contents of file2.gif...", new String(item2.get()));
    }

    /**
     * Test for <a href="https://issues.apache.org/jira/browse/FILEUPLOAD-111">FILEUPLOAD-111</a>
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testFoldedHeaders() throws IOException {
        // @formatter:off
        final var fileItems = parseUpload(upload, "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
                "Content-Type: text/whatever\r\n" +
                "\r\n" +
                "This is the content of the file\n" +
                "\r\n" +
                "-----1234\r\n" +
                "Content-Disposition: form-data; \r\n" +
                "\tname=\"field\"\r\n" +
                "\r\n" +
                "fieldValue\r\n" +
                "-----1234\r\n" +
                "Content-Disposition: form-data;\r\n" +
                "     name=\"multi\"\r\n" +
                "\r\n" +
                "value1\r\n" +
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"multi\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n");
        // @formatter:on
        assertEquals(4, fileItems.size());

        final var file = fileItems.get(0);
        assertEquals("file", file.getFieldName());
        assertFalse(file.isFormField());
        assertEquals("This is the content of the file\n", file.getString());
        assertEquals("text/whatever", file.getContentType());
        assertEquals("foo.tab", file.getName());

        final var field = fileItems.get(1);
        assertEquals("field", field.getFieldName());
        assertTrue(field.isFormField());
        assertEquals("fieldValue", field.getString());

        final var multi0 = fileItems.get(2);
        assertEquals("multi", multi0.getFieldName());
        assertTrue(multi0.isFormField());
        assertEquals("value1", multi0.getString());

        final var multi1 = fileItems.get(3);
        assertEquals("multi", multi1.getFieldName());
        assertTrue(multi1.isFormField());
        assertEquals("value2", multi1.getString());
    }

    /**
     * Internet Explorer 5 for the Mac has a bug where the carriage return is missing on any boundary line immediately preceding an input with type=image.
     * (type=submit does not have the bug.)
     *
     * @throws FileUploadException Test failure.
     */
    @Test
    public void testIE5MacBug() throws IOException {
        final var fileItems = parseUpload(upload,
        // @formatter:off
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"field1\"\r\n" +
                "\r\n" +
                "fieldValue\r\n" +
                "-----1234\n" + // NOTE \r missing
                "Content-Disposition: form-data; name=\"submitName.x\"\r\n" +
                "\r\n" +
                "42\r\n" +
                "-----1234\n" + // NOTE \r missing
                "Content-Disposition: form-data; name=\"submitName.y\"\r\n" +
                "\r\n" +
                "21\r\n" +
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"field2\"\r\n" +
                "\r\n" +
                "fieldValue2\r\n" +
                "-----1234--\r\n");
        //@formatter:on

        assertEquals(4, fileItems.size());

        final var field1 = fileItems.get(0);
        assertEquals("field1", field1.getFieldName());
        assertTrue(field1.isFormField());
        assertEquals("fieldValue", field1.getString());

        final var submitX = fileItems.get(1);
        assertEquals("submitName.x", submitX.getFieldName());
        assertTrue(submitX.isFormField());
        assertEquals("42", submitX.getString());

        final var submitY = fileItems.get(2);
        assertEquals("submitName.y", submitY.getFieldName());
        assertTrue(submitY.isFormField());
        assertEquals("21", submitY.getString());

        final var field2 = fileItems.get(3);
        assertEquals("field2", field2.getFieldName());
        assertTrue(field2.isFormField());
        assertEquals("fieldValue2", field2.getString());
    }

    /**
     * Test for multipart/related without any content-disposition Header.
     * This kind of Content-Type is commonly used by SOAP-Requests with Attachments (MTOM)
     */
    @Test
    public void testMultipleRelated() throws Exception {
        final String soapEnvelope =
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\r\n" +
                "  <soap:Header></soap:Header>\r\n" +
                "  <soap:Body>\r\n" +
                "    <ns1:Test xmlns:ns1=\"http://www.test.org/some-test-namespace\">\r\n" +
                "      <ns1:Attachment>\r\n" +
                "        <xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\"" +
                " href=\"ref-to-attachment%40some.domain.org\"/>\r\n" +
                "      </ns1:Attachment>\r\n" +
                "    </ns1:Test>\r\n" +
                "  </soap:Body>\r\n" +
                "</soap:Envelope>";

        final String text =
                "-----1234\r\n" +
                "content-type: application/xop+xml; type=\"application/soap+xml\"\r\n" +
                "\r\n" +
                soapEnvelope + "\r\n" +
                "-----1234\r\n" +
                "Content-type: text/plain\r\n" +
                "content-id: <ref-to-attachment@some.domain.org>\r\n" +
                "\r\n" +
                "some text/plain content\r\n" +
                "-----1234--\r\n";

        final var bytes = text.getBytes(StandardCharsets.US_ASCII);
        final var fileItems = parseUpload(upload, bytes, "multipart/related; boundary=---1234;" +
                " type=\"application/xop+xml\"; start-info=\"application/soap+xml\"");
        assertEquals(2, fileItems.size());

        final var part1 = fileItems.get(0);
        assertNull(part1.getFieldName());
        assertFalse(part1.isFormField());
        assertEquals(soapEnvelope, part1.getString());

        final var part2 = fileItems.get(1);
        assertNull(part2.getFieldName());
        assertFalse(part2.isFormField());
        assertEquals("some text/plain content", part2.getString());
        assertEquals("text/plain", part2.getContentType());
        assertNull(part2.getName());
    }
}
