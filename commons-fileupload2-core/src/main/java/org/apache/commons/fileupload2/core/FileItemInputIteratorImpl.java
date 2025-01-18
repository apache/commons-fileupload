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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;

/**
 * The iterator returned by {@link AbstractFileUpload#getItemIterator(RequestContext)}.
 */
class FileItemInputIteratorImpl implements FileItemInputIterator {

    private final AbstractFileUpload<?, ?, ?> fileUpload;
    private final RequestContext requestContext;
    private long sizeMax;
    private long fileSizeMax;
    private MultipartInput multiPartInput;
    private MultipartInput.ProgressNotifier progressNotifier;
    private byte[] multiPartBoundary;
    private FileItemInputImpl currentItem;
    private String currentFieldName;
    private boolean skipPreamble;
    private boolean itemValid;
    private boolean eof;
    private final boolean multipartRelated;

    FileItemInputIteratorImpl(final AbstractFileUpload<?, ?, ?> fileUploadBase, final RequestContext requestContext) throws IOException {
        this.fileUpload = fileUploadBase;
        this.sizeMax = fileUploadBase.getSizeMax();
        this.fileSizeMax = fileUploadBase.getFileSizeMax();
        this.requestContext = Objects.requireNonNull(requestContext, "requestContext");
        this.multipartRelated = this.requestContext.isMultipartRelated();
        this.skipPreamble = true;
        findNextItem();
    }

    private boolean findNextItem() throws IOException {
        if (eof) {
            return false;
        }
        if (currentItem != null) {
            currentItem.close();
            currentItem = null;
        }
        final var multi = getMultiPartInput();
        for (;;) {
            final boolean nextPart;
            if (skipPreamble) {
                nextPart = multi.skipPreamble();
            } else {
                nextPart = multi.readBoundary();
            }
            if (!nextPart) {
                if (currentFieldName == null) {
                    eof = true;
                    return false;
                }
                multi.setBoundary(multiPartBoundary);
                currentFieldName = null;
                continue;
            }
            final var headers = fileUpload.getParsedHeaders(multi.readHeaders());
            if (multipartRelated) {
                currentFieldName = "";
                currentItem = new FileItemInputImpl(
                        this, null, null, headers.getHeader(AbstractFileUpload.CONTENT_TYPE),
                        false, getContentLength(headers));
                currentItem.setHeaders(headers);
                progressNotifier.noteItem();
                itemValid = true;
                return true;
            } else if (currentFieldName == null) {
                final var fieldName = fileUpload.getFieldName(headers);
                if (fieldName != null) {
                    final var subContentType = headers.getHeader(AbstractFileUpload.CONTENT_TYPE);
                    if (subContentType != null && subContentType.toLowerCase(Locale.ROOT).startsWith(AbstractFileUpload.MULTIPART_MIXED)) {
                        currentFieldName = fieldName;
                        final var subBoundary = fileUpload.getBoundary(subContentType);
                        multi.setBoundary(subBoundary);
                        skipPreamble = true;
                        continue;
                    }
                    final var fileName = fileUpload.getFileName(headers);
                    currentItem = new FileItemInputImpl(this, fileName, fieldName, headers.getHeader(AbstractFileUpload.CONTENT_TYPE), fileName == null,
                            getContentLength(headers));
                    currentItem.setHeaders(headers);
                    progressNotifier.noteItem();
                    itemValid = true;
                    return true;
                }
            } else {
                final var fileName = fileUpload.getFileName(headers);
                if (fileName != null) {
                    currentItem = new FileItemInputImpl(this, fileName, currentFieldName, headers.getHeader(AbstractFileUpload.CONTENT_TYPE), false,
                            getContentLength(headers));
                    currentItem.setHeaders(headers);
                    progressNotifier.noteItem();
                    itemValid = true;
                    return true;
                }
            }
            multi.discardBodyData();
        }
    }

    private long getContentLength(final FileItemHeaders headers) {
        try {
            return Long.parseLong(headers.getHeader(AbstractFileUpload.CONTENT_LENGTH));
        } catch (final Exception e) {
            return -1;
        }
    }

    @Override
    public long getFileSizeMax() {
        return fileSizeMax;
    }

    public MultipartInput getMultiPartInput() throws IOException {
        if (multiPartInput == null) {
            init(fileUpload, requestContext);
        }
        return multiPartInput;
    }

    @Override
    public long getSizeMax() {
        return sizeMax;
    }

    @Override
    public boolean hasNext() throws IOException {
        if (eof) {
            return false;
        }
        if (itemValid) {
            return true;
        }
        return findNextItem();
    }

    protected void init(final AbstractFileUpload<?, ?, ?> fileUploadBase, final RequestContext initContext) throws IOException {
        final var contentType = requestContext.getContentType();
        if (null == contentType || !contentType.toLowerCase(Locale.ROOT).startsWith(AbstractFileUpload.MULTIPART)) {
            throw new FileUploadContentTypeException(String.format("the request doesn't contain a %s or %s stream, content type header is %s",
                    AbstractFileUpload.MULTIPART_FORM_DATA, AbstractFileUpload.MULTIPART_MIXED, contentType), contentType);
        }
        final var contentLengthInt = requestContext.getContentLength();
        final var requestSize = RequestContext.class.isAssignableFrom(requestContext.getClass())
                ? requestContext.getContentLength()
                : contentLengthInt;
        final InputStream inputStream;
        if (sizeMax >= 0) {
            if (requestSize != -1 && requestSize > sizeMax) {
                throw new FileUploadSizeException(
                        String.format("the request was rejected because its size (%s) exceeds the configured maximum (%s)", requestSize, sizeMax), sizeMax,
                        requestSize);
            }
            inputStream = new BoundedInputStream(requestContext.getInputStream(), sizeMax) {
                @Override
                protected void onMaxLength(final long maxLen, final long count) throws IOException {
                    throw new FileUploadSizeException(
                            String.format("The request was rejected because its size (%s) exceeds the configured maximum (%s)", count, maxLen), maxLen, count);
                }
            };
        } else {
            inputStream = requestContext.getInputStream();
        }
        final var charset = Charsets.toCharset(fileUploadBase.getHeaderCharset(), requestContext.getCharset());
        multiPartBoundary = fileUploadBase.getBoundary(contentType);
        if (multiPartBoundary == null) {
            IOUtils.closeQuietly(inputStream);
            throw new FileUploadException("the request was rejected because no multipart boundary was found");
        }
        progressNotifier = new MultipartInput.ProgressNotifier(fileUploadBase.getProgressListener(), requestSize);
        try {
            multiPartInput = MultipartInput.builder().setInputStream(inputStream).setBoundary(multiPartBoundary).setProgressNotifier(progressNotifier).get();
        } catch (final IllegalArgumentException e) {
            IOUtils.closeQuietly(inputStream);
            throw new FileUploadContentTypeException(String.format("The boundary specified in the %s header is too long", AbstractFileUpload.CONTENT_TYPE), e);
        }
        multiPartInput.setHeaderCharset(charset);
    }

    @Override
    public FileItemInput next() throws IOException {
        if (eof || !itemValid && !hasNext()) {
            throw new NoSuchElementException();
        }
        itemValid = false;
        return currentItem;
    }

    @Override
    public void setFileSizeMax(final long fileSizeMax) {
        this.fileSizeMax = fileSizeMax;
    }

    @Override
    public void setSizeMax(final long sizeMax) {
        this.sizeMax = sizeMax;
    }

    @Override
    public Iterator<FileItemInput> unwrap() {
        return (Iterator<FileItemInput>) this;
    }
}
