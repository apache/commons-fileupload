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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.fileupload2.core.FileItemFactory.AbstractFileItemBuilder;
import org.apache.commons.io.IOUtils;

/**
 * High level API for processing file uploads.
 * <p>
 * This class handles multiple files per single HTML widget, sent using {@code multipart/mixed} encoding type, as specified by
 * <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>. Use {@link #parseRequest(RequestContext)} to acquire a list of {@link FileItem}s associated with
 * a given HTML widget.
 * </p>
 * <p>
 * How the data for individual parts is stored is determined by the factory used to create them; a given part may be in memory, on disk, or somewhere else.
 * </p>
 *
 * @param <R> The request context type.
 * @param <I> The FileItem type.
 * @param <F> the FileItemFactory type.
 */
public abstract class AbstractFileUpload<R, I extends FileItem<I>, F extends FileItemFactory<I>> {

    /**
     * Boundary parameter key.
     */
    private static final String BOUNDARY_KEY = "boundary";

    /**
     * Name parameter key.
     */
    private static final String NAME_KEY = "name";

    /**
     * File name parameter key.
     */
    private static final String FILENAME_KEY = "filename";

    /**
     * HTTP content type header name.
     */
    public static final String CONTENT_TYPE = "Content-type";

    /**
     * HTTP content disposition header name.
     */
    public static final String CONTENT_DISPOSITION = "Content-disposition";

    /**
     * HTTP content length header name.
     */
    public static final String CONTENT_LENGTH = "Content-length";

    /**
     * Content-disposition value for form data.
     */
    public static final String FORM_DATA = "form-data";

    /**
     * Content-disposition value for file attachment.
     */
    public static final String ATTACHMENT = "attachment";

    /**
     * Part of HTTP content type header.
     */
    public static final String MULTIPART = "multipart/";

    /**
     * HTTP content type header for multipart forms.
     */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    /**
     * HTTP content type header for multiple uploads.
     */
    public static final String MULTIPART_MIXED = "multipart/mixed";

    /**
     * Utility method that determines whether the request contains multipart content.
     * <p>
     * <strong>NOTE:</strong> This method will be moved to the {@code ServletFileUpload} class after the FileUpload 1.1 release. Unfortunately, since this
     * method is static, it is not possible to provide its replacement until this method is removed.
     * </p>
     *
     * @param ctx The request context to be evaluated. Must be non-null.
     * @return {@code true} if the request is multipart; {@code false} otherwise.
     */
    public static final boolean isMultipartContent(final RequestContext ctx) {
        final var contentType = ctx.getContentType();
        if (contentType == null) {
            return false;
        }
        return contentType.toLowerCase(Locale.ENGLISH).startsWith(MULTIPART);
    }

    /**
     * The maximum size permitted for the complete request, as opposed to {@link #fileSizeMax}. A value of -1 indicates no maximum.
     */
    private long sizeMax = -1;

    /**
     * The maximum size permitted for a single uploaded file, as opposed to {@link #sizeMax}. A value of -1 indicates no maximum.
     */
    private long fileSizeMax = -1;

    /**
     * The maximum permitted number of files that may be uploaded in a single request. A value of -1 indicates no maximum.
     */
    private long fileCountMax = -1;

    /**
     * The content encoding to use when reading part headers.
     */
    private Charset headerCharset;

    /**
     * The progress listener.
     */
    private ProgressListener progressListener = ProgressListener.NOP;

    /**
     * The factory to use to create new form items.
     */
    private F fileItemFactory;

    /**
     * Gets the boundary from the {@code Content-type} header.
     *
     * @param contentType The value of the content type header from which to extract the boundary value.
     * @return The boundary, as a byte array.
     */
    public byte[] getBoundary(final String contentType) {
        final var parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        final var params = parser.parse(contentType, new char[] { ';', ',' });
        final var boundaryStr = params.get(BOUNDARY_KEY);
        return boundaryStr != null ? boundaryStr.getBytes(StandardCharsets.ISO_8859_1) : null;
    }

    /**
     * Gets the field name from the {@code Content-disposition} header.
     *
     * @param headers A {@code Map} containing the HTTP request headers.
     * @return The field name for the current {@code encapsulation}.
     */
    public String getFieldName(final FileItemHeaders headers) {
        return getFieldName(headers.getHeader(CONTENT_DISPOSITION));
    }

    /**
     * Gets the field name, which is given by the content-disposition header.
     *
     * @param contentDisposition The content-dispositions header value.
     * @return The field name.
     */
    private String getFieldName(final String contentDisposition) {
        String fieldName = null;
        if (contentDisposition != null && contentDisposition.toLowerCase(Locale.ENGLISH).startsWith(FORM_DATA)) {
            final var parser = new ParameterParser();
            parser.setLowerCaseNames(true);
            // Parameter parser can handle null input
            final var params = parser.parse(contentDisposition, ';');
            fieldName = params.get(NAME_KEY);
            if (fieldName != null) {
                fieldName = fieldName.trim();
            }
        }
        return fieldName;
    }

    /**
     * Gets the maximum number of files allowed in a single request.
     *
     * @return The maximum number of files allowed in a single request.
     */
    public long getFileCountMax() {
        return fileCountMax;
    }

    /**
     * Gets the factory class used when creating file items.
     *
     * @return The factory class for new file items.
     */
    public F getFileItemFactory() {
        return fileItemFactory;
    }

    /**
     * Gets the file name from the {@code Content-disposition} header.
     *
     * @param headers The HTTP headers object.
     *
     * @return The file name for the current {@code encapsulation}.
     */
    public String getFileName(final FileItemHeaders headers) {
        return getFileName(headers.getHeader(CONTENT_DISPOSITION));
    }

    /**
     * Gets the given content-disposition headers file name.
     *
     * @param contentDisposition The content-disposition headers value.
     * @return The file name
     */
    private String getFileName(final String contentDisposition) {
        String fileName = null;
        if (contentDisposition != null) {
            final var cdl = contentDisposition.toLowerCase(Locale.ENGLISH);
            if (cdl.startsWith(FORM_DATA) || cdl.startsWith(ATTACHMENT)) {
                final var parser = new ParameterParser();
                parser.setLowerCaseNames(true);
                // Parameter parser can handle null input
                final var params = parser.parse(contentDisposition, ';');
                if (params.containsKey(FILENAME_KEY)) {
                    fileName = params.get(FILENAME_KEY);
                    if (fileName != null) {
                        fileName = fileName.trim();
                    } else {
                        // Even if there is no value, the parameter is present,
                        // so we return an empty file name rather than no file
                        // name.
                        fileName = "";
                    }
                }
            }
        }
        return fileName;
    }

    /**
     * Gets the maximum allowed size of a single uploaded file, as opposed to {@link #getSizeMax()}.
     *
     * @see #setFileSizeMax(long)
     * @return Maximum size of a single uploaded file.
     */
    public long getFileSizeMax() {
        return fileSizeMax;
    }

    /**
     * Gets the character encoding used when reading the headers of an individual part. When not specified, or {@code null}, the request encoding is used. If
     * that is also not specified, or {@code null}, the platform default encoding is used.
     *
     * @return The encoding used to read part headers.
     */
    public Charset getHeaderCharset() {
        return headerCharset;
    }

    /**
     * Gets a file item iterator.
     *
     * @param request The servlet request to be parsed.
     * @return An iterator to instances of {@code FileItemInput} parsed from the request, in the order that they were transmitted.
     * @throws FileUploadException if there are problems reading/parsing the request or storing files.
     * @throws IOException         An I/O error occurred. This may be a network error while communicating with the client or a problem while storing the
     *                             uploaded content.
     */
    public abstract FileItemInputIterator getItemIterator(R request) throws FileUploadException, IOException;

    /**
     * Gets an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant {@code multipart/form-data} stream.
     *
     * @param requestContext The context for the request to be parsed.
     * @return An iterator to instances of {@code FileItemInput} parsed from the request, in the order that they were transmitted.
     * @throws FileUploadException if there are problems reading/parsing the request or storing files.
     * @throws IOException         An I/O error occurred. This may be a network error while communicating with the client or a problem while storing the
     *                             uploaded content.
     */
    public FileItemInputIterator getItemIterator(final RequestContext requestContext) throws FileUploadException, IOException {
        return new FileItemInputIteratorImpl(this, requestContext);
    }

    /**
     * Parses the {@code header-part} and returns as key/value pairs.
     * <p>
     * If there are multiple headers of the same names, the name will map to a comma-separated list containing the values.
     * </p>
     *
     * @param headerPart The {@code header-part} of the current {@code encapsulation}.
     * @return A {@code Map} containing the parsed HTTP request headers.
     */
    public FileItemHeaders getParsedHeaders(final String headerPart) {
        final var len = headerPart.length();
        final var headers = newFileItemHeaders();
        var start = 0;
        for (;;) {
            var end = parseEndOfLine(headerPart, start);
            if (start == end) {
                break;
            }
            final var header = new StringBuilder(headerPart.substring(start, end));
            start = end + 2;
            while (start < len) {
                var nonWs = start;
                while (nonWs < len) {
                    final var c = headerPart.charAt(nonWs);
                    if (c != ' ' && c != '\t') {
                        break;
                    }
                    ++nonWs;
                }
                if (nonWs == start) {
                    break;
                }
                // Continuation line found
                end = parseEndOfLine(headerPart, nonWs);
                header.append(' ').append(headerPart, nonWs, end);
                start = end + 2;
            }
            parseHeaderLine(headers, header.toString());
        }
        return headers;
    }

    /**
     * Gets the progress listener.
     *
     * @return The progress listener, if any, or null.
     */
    public ProgressListener getProgressListener() {
        return progressListener;
    }

    /**
     * Gets the maximum allowed size of a complete request, as opposed to {@link #getFileSizeMax()}.
     *
     * @return The maximum allowed size, in bytes. The default value of -1 indicates, that there is no limit.
     * @see #setSizeMax(long)
     *
     */
    public long getSizeMax() {
        return sizeMax;
    }

    /**
     * Creates a new instance of {@link FileItemHeaders}.
     *
     * @return The new instance.
     */
    protected FileItemHeaders newFileItemHeaders() {
        return AbstractFileItemBuilder.newFileItemHeaders();
    }

    /**
     * Skips bytes until the end of the current line.
     *
     * @param headerPart The headers, which are being parsed.
     * @param end        Index of the last byte, which has yet been processed.
     * @return Index of the \r\n sequence, which indicates end of line.
     */
    private int parseEndOfLine(final String headerPart, final int end) {
        var index = end;
        for (;;) {
            final var offset = headerPart.indexOf('\r', index);
            if (offset == -1 || offset + 1 >= headerPart.length()) {
                throw new IllegalStateException("Expected headers to be terminated by an empty line.");
            }
            if (headerPart.charAt(offset + 1) == '\n') {
                return offset;
            }
            index = offset + 1;
        }
    }

    /**
     * Parses the next header line.
     *
     * @param headers String with all headers.
     * @param header  Map where to store the current header.
     */
    private void parseHeaderLine(final FileItemHeaders headers, final String header) {
        final var colonOffset = header.indexOf(':');
        if (colonOffset == -1) {
            // This header line is malformed, skip it.
            return;
        }
        final var headerName = header.substring(0, colonOffset).trim();
        final var headerValue = header.substring(colonOffset + 1).trim();
        headers.addHeader(headerName, headerValue);
    }

    /**
     * Parses an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant {@code multipart/form-data} stream.
     *
     * @param request The servlet request to be parsed.
     * @return A map of {@code FileItem} instances parsed from the request.
     * @throws FileUploadException if there are problems reading/parsing the request or storing files.
     */
    public abstract Map<String, List<I>> parseParameterMap(R request) throws FileUploadException;

    /**
     * Parses an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant {@code multipart/form-data} stream.
     *
     * @param ctx The context for the request to be parsed.
     * @return A map of {@code FileItem} instances parsed from the request.
     * @throws FileUploadException if there are problems reading/parsing the request or storing files.
     */
    public Map<String, List<I>> parseParameterMap(final RequestContext ctx) throws FileUploadException {
        final var items = parseRequest(ctx);
        final Map<String, List<I>> itemsMap = new HashMap<>(items.size());

        for (final I fileItem : items) {
            final var fieldName = fileItem.getFieldName();
            final var mappedItems = itemsMap.computeIfAbsent(fieldName, k -> new ArrayList<>());
            mappedItems.add(fileItem);
        }

        return itemsMap;
    }

    /**
     * Parses an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant {@code multipart/form-data} stream.
     *
     * @param request The servlet request to be parsed.
     * @return A list of {@code FileItem} instances parsed from the request, in the order that they were transmitted.
     * @throws FileUploadException if there are problems reading/parsing the request or storing files.
     */
    public abstract List<I> parseRequest(R request) throws FileUploadException;

    /**
     * Parses an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant {@code multipart/form-data} stream.
     *
     * @param requestContext The context for the request to be parsed.
     * @return A list of {@code FileItem} instances parsed from the request, in the order that they were transmitted.
     * @throws FileUploadException if there are problems reading/parsing the request or storing files.
     */
    public List<I> parseRequest(final RequestContext requestContext) throws FileUploadException {
        final List<I> itemList = new ArrayList<>();
        var successful = false;
        try {
            final var fileItemFactory = Objects.requireNonNull(getFileItemFactory(), "No FileItemFactory has been set.");
            final var buffer = new byte[IOUtils.DEFAULT_BUFFER_SIZE];
            getItemIterator(requestContext).forEachRemaining(fileItemInput -> {
                if (itemList.size() == fileCountMax) {
                    // The next item will exceed the limit.
                    throw new FileUploadFileCountLimitException(ATTACHMENT, getFileCountMax(), itemList.size());
                }
                // Don't use getName() here to prevent an InvalidFileNameException.
                // @formatter:off
                final var fileItem = fileItemFactory.fileItemBuilder()
                    .setFieldName(fileItemInput.getFieldName())
                    .setContentType(fileItemInput.getContentType())
                    .setFormField(fileItemInput.isFormField())
                    .setFileName(fileItemInput.getName())
                    .setFileItemHeaders(fileItemInput.getHeaders())
                    .get();
                // @formatter:on
                itemList.add(fileItem);
                try (var inputStream = fileItemInput.getInputStream();
                        var outputStream = fileItem.getOutputStream()) {
                    IOUtils.copyLarge(inputStream, outputStream, buffer);
                } catch (final FileUploadException e) {
                    throw e;
                } catch (final IOException e) {
                    throw new FileUploadException(String.format("Processing of %s request failed. %s", MULTIPART_FORM_DATA, e.getMessage()), e);
                }
            });
            successful = true;
            return itemList;
        } catch (final FileUploadException e) {
            throw e;
        } catch (final IOException e) {
            throw new FileUploadException(e.getMessage(), e);
        } finally {
            if (!successful) {
                for (final I fileItem : itemList) {
                    try {
                        fileItem.delete();
                    } catch (final Exception ignored) {
                        // ignored TODO perhaps add to tracker delete failure list somehow?
                    }
                }
            }
        }
    }

    /**
     * Sets the maximum number of files allowed per request.
     *
     * @param fileCountMax The new limit. {@code -1} means no limit.
     */
    public void setFileCountMax(final long fileCountMax) {
        this.fileCountMax = fileCountMax;
    }

    /**
     * Sets the factory class to use when creating file items.
     *
     * @param factory The factory class for new file items.
     */
    public void setFileItemFactory(final F factory) {
        this.fileItemFactory = factory;
    }

    /**
     * Sets the maximum allowed size of a single uploaded file, as opposed to {@link #getSizeMax()}.
     *
     * @see #getFileSizeMax()
     * @param fileSizeMax Maximum size of a single uploaded file.
     */
    public void setFileSizeMax(final long fileSizeMax) {
        this.fileSizeMax = fileSizeMax;
    }

    /**
     * Specifies the character encoding to be used when reading the headers of individual part. When not specified, or {@code null}, the request encoding is
     * used. If that is also not specified, or {@code null}, the platform default encoding is used.
     *
     * @param headerCharset The encoding used to read part headers.
     */
    public void setHeaderCharset(final Charset headerCharset) {
        this.headerCharset = headerCharset;
    }

    /**
     * Sets the progress listener.
     *
     * @param progressListener The progress listener, if any. Defaults to null.
     */
    public void setProgressListener(final ProgressListener progressListener) {
        this.progressListener = progressListener != null ? progressListener : ProgressListener.NOP;
    }

    /**
     * Sets the maximum allowed size of a complete request, as opposed to {@link #setFileSizeMax(long)}.
     *
     * @param sizeMax The maximum allowed size, in bytes. The default value of -1 indicates, that there is no limit.
     * @see #getSizeMax()
     */
    public void setSizeMax(final long sizeMax) {
        this.sizeMax = sizeMax;
    }

}
