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
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.junit.Test;


/**
 * Serialization Unit tests for
 *  {@link org.apache.commons.fileupload.disk.DiskFileItem}.
 *
 * @version $Id$
 */
public class DiskFileItemSerializeTest {

    private static final String ERRMSG_DISKFILEITEM_DESERIALIZED = "Property org.apache.commons.fileupload.disk.DiskFileItem.serializable is not true, rejecting to deserialize a DiskFileItem.";

	/**
     * Content type for regular form items.
     */
    private static final String textContentType = "text/plain";

    /**
     * Very low threshold for testing memory versus disk options.
     */
    private static final int threshold = 16;

    /**
     * Helper method to test creation of a field when a repository is used.
     */
    public void testInMemoryObject(byte[] testFieldValueBytes, File repository) {
        FileItem item = createFileItem(testFieldValueBytes, repository);

        // Check state is as expected
        assertTrue("Initial: in memory", item.isInMemory());
        assertEquals("Initial: size", item.getSize(), testFieldValueBytes.length);
        compareBytes("Initial", item.get(), testFieldValueBytes);

        // Serialize & Deserialize
        FileItem newItem =  (FileItem)serializeDeserialize(item);

        // Test deserialized content is as expected
        assertTrue("Check in memory", newItem.isInMemory());
        compareBytes("Check", testFieldValueBytes, newItem.get());

        // Compare FileItem's (except byte[])
        compareFileItems(item, newItem);
    }
    
    /**
     * Helper method to test creation of a field.
     */
    private void testInMemoryObject(byte[] testFieldValueBytes) {
        testInMemoryObject(testFieldValueBytes, null);
    }
    
    /**
     * Test creation of a field for which the amount of data falls below the
     * configured threshold.
     */
    @Test
    public void testBelowThreshold() throws Exception {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold - 1);
        testInMemoryObject(testFieldValueBytes);
    }

    /**
     * Test creation of a field for which the amount of data equals the
     * configured threshold.
     */
    @Test
    public void testThreshold() throws Exception {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold);
        testInMemoryObject(testFieldValueBytes);
    }

    /**
     * Test creation of a field for which the amount of data falls above the
     * configured threshold.
     */
    @Test
    public void testAboveThreshold() throws Exception {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold + 1);
        FileItem item = createFileItem(testFieldValueBytes);

        // Check state is as expected
        assertFalse("Initial: in memory", item.isInMemory());
        assertEquals("Initial: size", item.getSize(), testFieldValueBytes.length);
        compareBytes("Initial", item.get(), testFieldValueBytes);

        // Serialize & Deserialize
        FileItem newItem = (FileItem)serializeDeserialize(item);

        // Test deserialized content is as expected
        assertFalse("Check in memory", newItem.isInMemory());
        compareBytes("Check", testFieldValueBytes, newItem.get());

        // Compare FileItem's (except byte[])
        compareFileItems(item, newItem);
    }
    
    /**
     * Test serialization and deserialization when repository is not null.
     */
    @Test
    public void testValidRepository() throws Exception {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold);
        File repository = new File(System.getProperty("java.io.tmpdir"));
        testInMemoryObject(testFieldValueBytes, repository);
    }
    
    /**
     * Test deserialization fails when repository is not valid.
     */
    @Test(expected=IOException.class)
    public void testInvalidRepository() throws Exception {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold);
        File repository = new File(System.getProperty("java.io.tmpdir") + "file");
        FileItem item = createFileItem(testFieldValueBytes, repository);
        deserialize(serialize(item));
    }
    
    /**
     * Test deserialization fails when repository contains a null character.
     */
    @Test
    public void testInvalidRepositoryWithNullChar() throws Exception {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold);
        File repository = new File(System.getProperty("java.io.tmpdir") + "\0");
        FileItem item = createFileItem(testFieldValueBytes, repository);
        try {
        	deserialize(serialize(item));
        	fail("Expected Exception");
        } catch (IllegalStateException e) {
        	assertEquals(ERRMSG_DISKFILEITEM_DESERIALIZED, e.getMessage());
        }
        System.setProperty(DiskFileItem.SERIALIZABLE_PROPERTY, "true");
    }

    /**
     * Compare FileItem's (except the byte[] content)
     */
    private void compareFileItems(FileItem origItem, FileItem newItem) {
        assertTrue("Compare: is in Memory",   origItem.isInMemory()   == newItem.isInMemory());
        assertTrue("Compare: is Form Field",  origItem.isFormField()  == newItem.isFormField());
        assertEquals("Compare: Field Name",   origItem.getFieldName(),   newItem.getFieldName());
        assertEquals("Compare: Content Type", origItem.getContentType(), newItem.getContentType());
        assertEquals("Compare: File Name",    origItem.getName(),        newItem.getName());
    }

    /**
     * Compare content bytes.
     */
    private void compareBytes(String text, byte[] origBytes, byte[] newBytes) {
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
    private byte[] createContentBytes(int size) {
        StringBuilder buffer = new StringBuilder(size);
        byte count = 0;
        for (int i = 0; i < size; i++) {
            buffer.append(count+"");
            count++;
            if (count > 9) {
                count = 0;
            }
        }
        return buffer.toString().getBytes();
    }

    /**
     * Create a FileItem with the specfied content bytes and repository.
     */
    private FileItem createFileItem(byte[] contentBytes, File repository) {
        FileItemFactory factory = new DiskFileItemFactory(threshold, repository);
        String textFieldName = "textField";

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                "My File Name"
        );
        try {
            OutputStream os = item.getOutputStream();
            os.write(contentBytes);
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException" + e);
        }

        return item;

    }
    
    /**
     * Create a FileItem with the specfied content bytes.
     */
    private FileItem createFileItem(byte[] contentBytes) {
        return createFileItem(contentBytes, null);
    }
    
    /**
     * Do serialization
     */
    private ByteArrayOutputStream serialize(Object target) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(target);
        oos.flush();
        oos.close();
        return baos;
    }
    
    /**
     * Do deserialization
     */
    private Object deserialize(ByteArrayOutputStream baos) throws Exception {
        Object result = null;
        ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        result = ois.readObject();
        bais.close();

        return result;
    }
    
    /**
     * Do serialization and deserialization.
     */
    private Object serializeDeserialize(Object target) {
        // Serialize the test object
        ByteArrayOutputStream baos = null;
        try {
            baos = serialize(target);
        } catch (Exception e) {
            fail("Exception during serialization: " + e);
        }
        
        // Deserialize the test object
        Object result = null;
        try {
            result = deserialize(baos);
        } catch (Exception e) {
            fail("Exception during deserialization: " + e);
        }
        
        return result;
    }

}
