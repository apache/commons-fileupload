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

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.file.PathUtils;

/**
 * Creates {@link FileItem} instances.
 * <p>
 * Factories can provide their own custom configuration, over and above that provided by the default file upload implementation.
 * </p>
 *
 * @param <I> The {@link FileItem} type this factory creates.
 */
public interface FileItemFactory<I extends FileItem<I>> {

    /**
     * Abstracts building for subclasses.
     *
     * @param <I> the type of {@link FileItem} to build.
     * @param <B> the type of builder subclass.
     */
    abstract class AbstractFileItemBuilder<I extends FileItem<I>, B extends AbstractFileItemBuilder<I, B>> extends AbstractStreamBuilder<I, B> {

        public static FileItemHeaders newFileItemHeaders() {
            return new FileItemHeadersImpl();
        }

        /**
         * Field name.
         */
        private String fieldName;

        /**
         * Content type.
         */
        private String contentType;

        /**
         * Is this a form field.
         */
        private boolean isFormField;

        /**
         * File name.
         */
        private String fileName;

        /**
         * File item headers.
         */
        private FileItemHeaders fileItemHeaders = newFileItemHeaders();

        /**
         * The instance of {@link FileCleaningTracker}, which is responsible for deleting temporary files.
         * <p>
         * May be null, if tracking files is not required.
         * </p>
         */
        private FileCleaningTracker fileCleaningTracker;

        public AbstractFileItemBuilder() {
            setBufferSize(DiskFileItemFactory.DEFAULT_THRESHOLD);
            setPath(PathUtils.getTempDirectory());
        }

        public String getContentType() {
            return contentType;
        }

        public String getFieldName() {
            return fieldName;
        }

        public FileCleaningTracker getFileCleaningTracker() {
            return fileCleaningTracker;
        }

        public FileItemHeaders getFileItemHeaders() {
            return fileItemHeaders;
        }

        public String getFileName() {
            return fileName;
        }

        public boolean isFormField() {
            return isFormField;
        }

        public B setContentType(final String contentType) {
            this.contentType = contentType;
            return asThis();
        }

        public B setFieldName(final String fieldName) {
            this.fieldName = fieldName;
            return asThis();
        }

        public B setFileCleaningTracker(final FileCleaningTracker fileCleaningTracker) {
            this.fileCleaningTracker = fileCleaningTracker;
            return asThis();
        }

        public B setFileItemHeaders(final FileItemHeaders fileItemHeaders) {
            this.fileItemHeaders = fileItemHeaders != null ? fileItemHeaders : newFileItemHeaders();
            return asThis();
        }

        public B setFileName(final String fileName) {
            this.fileName = fileName;
            return asThis();
        }

        public B setFormField(final boolean isFormField) {
            this.isFormField = isFormField;
            return asThis();
        }

    }

    /**
     * Creates a new AbstractFileItemBuilder.
     *
     * @param <B> The type of AbstractFileItemBuilder.
     * @return a new AbstractFileItemBuilder.
     */
    <B extends AbstractFileItemBuilder<I, B>> AbstractFileItemBuilder<I, B> fileItemBuilder();

}
