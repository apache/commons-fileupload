/*
 * Copyright 2001-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.fileupload;


/** 
 * An iterator, as returned by
 * {@link FileUploadBase#getItemRequest(RequestContext)}
 */
public interface FileItemIterator {
    /** 
     * Returns, whether another instance of {@link FileItemStream}
     * is available.
     */
    boolean hasNext() throws FileUploadException;

    /** 
     * Returns the next available {@link FileItemStream}.
     * @throws NoSuchElementException No more items are available. Use
     * {@link #hasNext()} to prevent this exception.
     */
    FileItemStream next() throws FileUploadException;
}
