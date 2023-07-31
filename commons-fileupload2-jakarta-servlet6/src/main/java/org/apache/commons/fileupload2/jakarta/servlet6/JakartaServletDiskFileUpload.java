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

package org.apache.commons.fileupload2.jakarta.servlet6;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;

/**
 * A JakartaServletFileUpload for {@link DiskFileItem} and {@link DiskFileItemFactory}.
 */
public class JakartaServletDiskFileUpload extends JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> {

    public JakartaServletDiskFileUpload() {
        super(DiskFileItemFactory.builder().get());
    }

    public JakartaServletDiskFileUpload(final DiskFileItemFactory fileItemFactory) {
        super(fileItemFactory);
    }

}
