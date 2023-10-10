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
package org.apache.commons.fileupload2.javax;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload2.core.AbstractFileUpload;

/**
 * Test fixture for {@link HttpServletRequest}.
 */
public class JavaxMockHttpServletRequest implements HttpServletRequest {

    private static final class MyServletInputStream extends ServletInputStream {

        private final InputStream inputStream;
        private final int readLimit;

        /**
         * Creates a new instance, which returns the given streams data.
         */
        public MyServletInputStream(final InputStream inputStream, final int readLimit) {
            this.inputStream = inputStream;
            this.readLimit = readLimit;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            if (readLimit > 0) {
                return inputStream.read(b, off, Math.min(readLimit, len));
            }
            return inputStream.read(b, off, len);
        }

    }

    private final InputStream requestInputStream;

    private final long requestLength;

    private final String contentType;

    private final int readLimit;

    private final Map<String, String> headers = new HashMap<>();

    /**
     * Creates a new instance with the given request data and content type.
     */
    public JavaxMockHttpServletRequest(final byte[] requestData, final String contentType) {
        this(new ByteArrayInputStream(requestData), requestData.length, contentType, -1);
    }

    /**
     * Creates a new instance with the given request data and content type.
     */
    public JavaxMockHttpServletRequest(final InputStream requestInputStream, final long requestLength, final String contentType, final int readLimit) {
        this.requestInputStream = requestInputStream;
        this.requestLength = requestLength;
        this.contentType = contentType;
        this.headers.put(AbstractFileUpload.CONTENT_TYPE, contentType);
        this.readLimit = readLimit;
    }

    /**
     * @see ServletRequest#getAttribute(String)
     */
    @Override
    public Object getAttribute(final String arg0) {
        return null;
    }

    /**
     * @see ServletRequest#getAttributeNames()
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    /**
     * @see HttpServletRequest#getAuthType()
     */
    @Override
    public String getAuthType() {
        return null;
    }

    /**
     * @see ServletRequest#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {
        return null;
    }

    /**
     * @see ServletRequest#getContentLength()
     */
    @Override
    public int getContentLength() {
        return requestInputStream != null ? Math.toIntExact(requestLength) : -1;
    }

    /**
     * @see ServletRequest#getContentType()
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * @see HttpServletRequest#getContextPath()
     */
    @Override
    public String getContextPath() {
        return null;
    }

    /**
     * @see HttpServletRequest#getCookies()
     */
    @Override
    public Cookie[] getCookies() {
        return null;
    }

    /**
     * @see HttpServletRequest#getDateHeader(String)
     */
    @Override
    public long getDateHeader(final String arg0) {
        return 0;
    }

    /**
     * @see HttpServletRequest#getHeader(String)
     */
    @Override
    public String getHeader(final String headerName) {
        return headers.get(headerName);
    }

    /**
     * @see HttpServletRequest#getHeaderNames()
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        // todo - implement
        return null;
    }

    /**
     * @see HttpServletRequest#getHeaders(String)
     */
    @Override
    public Enumeration<String> getHeaders(final String arg0) {
        // todo - implement
        return null;
    }

    /**
     * @see ServletRequest#getInputStream()
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new MyServletInputStream(requestInputStream, readLimit);
    }

    /**
     * @see HttpServletRequest#getIntHeader(String)
     */
    @Override
    public int getIntHeader(final String arg0) {
        return 0;
    }

    /**
     * @see ServletRequest#getLocalAddr()
     */
    @Override
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public String getLocalAddr() {
        return null;
    }

    /**
     * @see ServletRequest#getLocale()
     */
    @Override
    public Locale getLocale() {
        return null;
    }

    /**
     * @see ServletRequest#getLocales()
     */
    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    /**
     * @see ServletRequest#getLocalName()
     */
    @Override
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public String getLocalName() {
        return null;
    }

    /**
     * @see ServletRequest#getLocalPort()
     */
    @Override
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public int getLocalPort() {
        return 0;
    }

    /**
     * @see HttpServletRequest#getMethod()
     */
    @Override
    public String getMethod() {
        return null;
    }

    /**
     * @see ServletRequest#getParameter(String)
     */
    @Override
    public String getParameter(final String arg0) {
        return null;
    }

    /**
     * @see ServletRequest#getParameterMap()
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    /**
     * @see ServletRequest#getParameterNames()
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    /**
     * @see ServletRequest#getParameterValues(String)
     */
    @Override
    public String[] getParameterValues(final String arg0) {
        return null;
    }

    /**
     * @see HttpServletRequest#getPathInfo()
     */
    @Override
    public String getPathInfo() {
        return null;
    }

    /**
     * @see HttpServletRequest#getPathTranslated()
     */
    @Override
    public String getPathTranslated() {
        return null;
    }

    /**
     * @see ServletRequest#getProtocol()
     */
    @Override
    public String getProtocol() {
        return null;
    }

    /**
     * @see HttpServletRequest#getQueryString()
     */
    @Override
    public String getQueryString() {
        return null;
    }

    /**
     * @see ServletRequest#getReader()
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    /**
     * @see ServletRequest#getRealPath(String)
     * @deprecated
     */
    @Override
    @Deprecated
    public String getRealPath(final String arg0) {
        return null;
    }

    /**
     * @see ServletRequest#getRemoteAddr()
     */
    @Override
    public String getRemoteAddr() {
        return null;
    }

    /**
     * @see ServletRequest#getRemoteHost()
     */
    @Override
    public String getRemoteHost() {
        return null;
    }

    /**
     * @see ServletRequest#getRemotePort()
     */
    @Override
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public int getRemotePort() {
        return 0;
    }

    /**
     * @see HttpServletRequest#getRemoteUser()
     */
    @Override
    public String getRemoteUser() {
        return null;
    }

    /**
     * @see ServletRequest#getRequestDispatcher(String)
     */
    @Override
    public RequestDispatcher getRequestDispatcher(final String arg0) {
        return null;
    }

    /**
     * @see HttpServletRequest#getRequestedSessionId()
     */
    @Override
    public String getRequestedSessionId() {
        return null;
    }

    /**
     * @see HttpServletRequest#getRequestURI()
     */
    @Override
    public String getRequestURI() {
        return null;
    }

    /**
     * @see HttpServletRequest#getRequestURL()
     */
    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    /**
     * @see ServletRequest#getScheme()
     */
    @Override
    public String getScheme() {
        return null;
    }

    /**
     * @see ServletRequest#getServerName()
     */
    @Override
    public String getServerName() {
        return null;
    }

    /**
     * @see ServletRequest#getServerPort()
     */
    @Override
    public int getServerPort() {
        return 0;
    }

    /**
     * @see HttpServletRequest#getServletPath()
     */
    @Override
    public String getServletPath() {
        return null;
    }

    /**
     * @see HttpServletRequest#getSession()
     */
    @Override
    public HttpSession getSession() {
        return null;
    }

    /**
     * @see HttpServletRequest#getSession(boolean)
     */
    @Override
    public HttpSession getSession(final boolean arg0) {
        return null;
    }

    /**
     * @see HttpServletRequest#getUserPrincipal()
     */
    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    /**
     * @see HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    /**
     * @see HttpServletRequest#isRequestedSessionIdFromUrl()
     * @deprecated
     */
    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    /**
     * @see HttpServletRequest#isRequestedSessionIdFromURL()
     */
    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    /**
     * @see HttpServletRequest#isRequestedSessionIdValid()
     */
    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    /**
     * @see HttpServletRequest#isSecure()
     */
    @Override
    public boolean isSecure() {
        return false;
    }

    /**
     * @see HttpServletRequest#isUserInRole(String)
     */
    @Override
    public boolean isUserInRole(final String arg0) {
        return false;
    }

    /**
     * @see ServletRequest#removeAttribute(String)
     */
    @Override
    public void removeAttribute(final String arg0) {
    }

    /**
     * @see ServletRequest#setAttribute(String, Object)
     */
    @Override
    public void setAttribute(final String arg0, final Object arg1) {
    }

    /**
     * @see ServletRequest#setCharacterEncoding(String)
     */
    @Override
    public void setCharacterEncoding(final String arg0) throws UnsupportedEncodingException {
    }

}
