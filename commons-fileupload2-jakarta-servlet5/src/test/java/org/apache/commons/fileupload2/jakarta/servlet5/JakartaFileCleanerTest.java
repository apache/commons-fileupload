/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.fileupload2.jakarta.servlet5;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileCleaningTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

/**
 * Unit tests for {@link JakartaFileCleaner}.
 */
class JakartaFileCleanerTest {

    /**
     * Returns {@code true} if {@link FileCleaningTracker#exitWhenFinished()} has been called on the given tracker (i.e. the {@code exitWhenFinished} field is
     * {@code true}).
     */
    private static boolean isExitWhenFinished(final FileCleaningTracker tracker) throws Exception {
        final Field field = FileCleaningTracker.class.getDeclaredField("exitWhenFinished");
        field.setAccessible(true);
        return field.getBoolean(tracker);
    }

    /**
     * A simple {@link ServletContext} attribute store backed by a {@link Map}. Used together with Mockito to give {@code getAttribute}/{@code setAttribute}
     * real behaviour.
     */
    private Map<String, Object> attributes;

    private JakartaFileCleaner fileCleaner;

    private ServletContext servletContext;

    private ServletContextEvent servletContextEvent;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
        servletContext = mock(ServletContext.class);
        // Wire getAttribute / setAttribute / removeAttribute to the backing map.
        when(servletContext.getAttribute(any(String.class))).thenAnswer(invocation -> attributes.get(invocation.getArgument(0, String.class)));
        Mockito.doAnswer(invocation -> {
            attributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(servletContext).setAttribute(any(String.class), any());
        servletContextEvent = mock(ServletContextEvent.class);
        when(servletContextEvent.getServletContext()).thenReturn(servletContext);
        fileCleaner = new JakartaFileCleaner();
    }

    @Test
    void testConstructor() {
        assertDoesNotThrow(JakartaFileCleaner::new, "Default constructor should not throw");
    }

    @Test
    void testConstructorCreatesDistinctInstances() {
        final JakartaFileCleaner a = new JakartaFileCleaner();
        final JakartaFileCleaner b = new JakartaFileCleaner();
        assertEquals(a.getClass(), b.getClass());
        assertTrue(a != b, "Each call to new JakartaFileCleaner() must yield a distinct object");
    }

    @Test
    void testContextDestroyedCallsExitWhenFinished() throws Exception {
        fileCleaner.contextInitialized(servletContextEvent);
        final FileCleaningTracker tracker = JakartaFileCleaner.getFileCleaningTracker(servletContext);
        assertNotNull(tracker);
        fileCleaner.contextDestroyed(servletContextEvent);
        assertTrue(isExitWhenFinished(tracker), "After contextDestroyed the tracker's exitWhenFinished flag should be true");
    }

    @Test
    void testContextDestroyedDoesNotThrow() {
        fileCleaner.contextInitialized(servletContextEvent);
        assertDoesNotThrow(() -> fileCleaner.contextDestroyed(servletContextEvent), "contextDestroyed should not throw any exception");
    }

    @Test
    void testContextDestroyedUsesServletContextFromEvent() {
        fileCleaner.contextInitialized(servletContextEvent);
        fileCleaner.contextDestroyed(servletContextEvent);
        // getServletContext() must have been called at least twice: once for init, once for destroy
        verify(servletContextEvent, Mockito.atLeast(2)).getServletContext();
    }

    @Test
    void testContextInitializedCreatesNewTrackerEachTime() {
        fileCleaner.contextInitialized(servletContextEvent);
        final FileCleaningTracker first = JakartaFileCleaner.getFileCleaningTracker(servletContext);
        first.exitWhenFinished();
        // Re-initialise (simulate re-deployment)
        fileCleaner.contextInitialized(servletContextEvent);
        final FileCleaningTracker second = JakartaFileCleaner.getFileCleaningTracker(servletContext);
        assertNotNull(second);
        second.exitWhenFinished();
    }

    @Test
    void testContextInitializedStoresNewTracker() {
        fileCleaner.contextInitialized(servletContextEvent);
        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(servletContext).setAttribute(eq(JakartaFileCleaner.FILE_CLEANING_TRACKER_ATTRIBUTE), captor.capture());
        assertNotNull(captor.getValue(), "contextInitialized should store a non-null tracker");
        assertInstanceOf(FileCleaningTracker.class, captor.getValue(), "Stored object should be a FileCleaningTracker");
        ((FileCleaningTracker) captor.getValue()).exitWhenFinished();
    }

    @Test
    void testContextInitializedTrackerIsRetrievableViaStaticHelper() {
        fileCleaner.contextInitialized(servletContextEvent);
        final FileCleaningTracker tracker = JakartaFileCleaner.getFileCleaningTracker(servletContext);
        assertNotNull(tracker, "Tracker stored by contextInitialized should be retrievable via getFileCleaningTracker");
        tracker.exitWhenFinished();
    }

    @Test
    void testContextInitializedUsesServletContextFromEvent() {
        fileCleaner.contextInitialized(servletContextEvent);
        verify(servletContextEvent).getServletContext();
    }

    @Test
    void testFileCleaningTrackerAttributeNameContainsClassName() {
        assertTrue(JakartaFileCleaner.FILE_CLEANING_TRACKER_ATTRIBUTE.contains("JakartaFileCleaner"), "Attribute name should contain the class name");
    }

    @Test
    void testFileCleaningTrackerAttributeNameContainsSuffix() {
        assertTrue(JakartaFileCleaner.FILE_CLEANING_TRACKER_ATTRIBUTE.endsWith(".FileCleaningTracker"),
                "Attribute name should end with '.FileCleaningTracker'");
    }

    @Test
    void testFileCleaningTrackerAttributeNameIsNotEmpty() {
        assertNotNull(JakartaFileCleaner.FILE_CLEANING_TRACKER_ATTRIBUTE);
        assertTrue(JakartaFileCleaner.FILE_CLEANING_TRACKER_ATTRIBUTE.length() > 0);
    }

    @Test
    void testFullLifecycle() throws Exception {
        // Simulate web-application start
        fileCleaner.contextInitialized(servletContextEvent);
        final FileCleaningTracker tracker = JakartaFileCleaner.getFileCleaningTracker(servletContext);
        assertNotNull(tracker, "Tracker must be present after contextInitialized");
        // Simulate web-application stop
        fileCleaner.contextDestroyed(servletContextEvent);
        assertTrue(isExitWhenFinished(tracker), "Tracker's exitWhenFinished must be true after contextDestroyed");
    }

    @Test
    void testGetFileCleaningTrackerCallsGetAttributeOnContext() {
        JakartaFileCleaner.getFileCleaningTracker(servletContext);
        verify(servletContext).getAttribute(JakartaFileCleaner.FILE_CLEANING_TRACKER_ATTRIBUTE);
    }

    @Test
    void testGetFileCleaningTrackerReturnsNullWhenNotSet() {
        assertNull(JakartaFileCleaner.getFileCleaningTracker(servletContext), "Should return null when no tracker has been stored");
    }

    @Test
    void testSetAndGetFileCleaningTracker() {
        final FileCleaningTracker tracker = new FileCleaningTracker();
        JakartaFileCleaner.setFileCleaningTracker(servletContext, tracker);
        assertSame(tracker, JakartaFileCleaner.getFileCleaningTracker(servletContext), "getFileCleaningTracker should return the same instance that was set");
        tracker.exitWhenFinished();
    }

    @Test
    void testSetFileCleaningTrackerCallsSetAttributeOnContext() {
        final FileCleaningTracker tracker = new FileCleaningTracker();
        JakartaFileCleaner.setFileCleaningTracker(servletContext, tracker);
        verify(servletContext).setAttribute(eq(JakartaFileCleaner.FILE_CLEANING_TRACKER_ATTRIBUTE), eq(tracker));
        tracker.exitWhenFinished();
    }

    @Test
    void testSetFileCleaningTrackerCanBeOverwritten() {
        final FileCleaningTracker first = new FileCleaningTracker();
        final FileCleaningTracker second = new FileCleaningTracker();
        JakartaFileCleaner.setFileCleaningTracker(servletContext, first);
        JakartaFileCleaner.setFileCleaningTracker(servletContext, second);
        assertSame(second, JakartaFileCleaner.getFileCleaningTracker(servletContext), "Second tracker should replace the first");
        first.exitWhenFinished();
        second.exitWhenFinished();
    }
}