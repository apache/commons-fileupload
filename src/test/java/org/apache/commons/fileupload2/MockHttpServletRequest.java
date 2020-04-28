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
package org.apache.commons.fileupload2;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ReadListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import java.util.Collection;

import org.apache.commons.fileupload2.FileUploadBase;

public class MockHttpServletRequest implements HttpServletRequest {

    private final InputStream m_requestData;

    private long length;

    private final String m_strContentType;

    private int readLimit = -1;

    private final Map<String, String> m_headers = new java.util.HashMap<String, String>();

    /**
     * Creates a new instance with the given request data and content type.
     */
    public MockHttpServletRequest(
            final byte[] requestData,
            final String strContentType) {
        this(new ByteArrayInputStream(requestData),
                requestData.length, strContentType);
    }

    /**
     * Creates a new instance with the given request data and content type.
     */
    public MockHttpServletRequest(
            final InputStream requestData,
            final long requestLength,
            final String strContentType) {
        m_requestData = requestData;
        length = requestLength;
        m_strContentType = strContentType;
        m_headers.put(FileUploadBase.CONTENT_TYPE, strContentType);
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getAuthType()
     */
    @Override
    public String getAuthType() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getCookies()
     */
    @Override
    public Cookie[] getCookies() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getDateHeader(String)
     */
    @Override
    public long getDateHeader(String arg0) {
        return 0;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getHeader(String)
     */
    @Override
    public String getHeader(String headerName) {
        return m_headers.get(headerName);
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getHeaders(String)
     */
    @Override
    public Enumeration<String> getHeaders(String arg0) {
        // todo - implement
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getHeaderNames()
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        // todo - implement
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getIntHeader(String)
     */
    @Override
    public int getIntHeader(String arg0) {
        return 0;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getMethod()
     */
    @Override
    public String getMethod() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getPathInfo()
     */
    @Override
    public String getPathInfo() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getPathTranslated()
     */
    @Override
    public String getPathTranslated() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getContextPath()
     */
    @Override
    public String getContextPath() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getQueryString()
     */
    @Override
    public String getQueryString() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getRemoteUser()
     */
    @Override
    public String getRemoteUser() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#isUserInRole(String)
     */
    @Override
    public boolean isUserInRole(String arg0) {
        return false;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    @Override
    public String getRequestedSessionId() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getRequestURI()
     */
    @Override
    public String getRequestURI() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getRequestURL()
     */
    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getServletPath()
     */
    @Override
    public String getServletPath() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getSession(boolean)
     */
    @Override
    public HttpSession getSession(boolean arg0) {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#getSession()
     */
    @Override
    public HttpSession getSession() {
        return null;
    }

    /**
     * @see jakarta.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    /**
     * @see
     * jakarta.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    /**
     * @see
     * jakarta.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    /**
     * @see
     * jakarta.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     * @deprecated
     */
    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getAttribute(String)
     */
    @Override
    public Object getAttribute(String arg0) {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getAttributeNames()
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#setCharacterEncoding(String)
     */
    @Override
    public void setCharacterEncoding(String arg0)
            throws UnsupportedEncodingException {
    }

    /**
     * @see jakarta.servlet.ServletRequest#getContentLength()
     */
    @Override
    public int getContentLength() {
        int iLength = 0;

        if (null == m_requestData) {
            iLength = -1;
        } else {
            if (length > Integer.MAX_VALUE) {
                throw new RuntimeException("Value '" + length + "' is too large to be converted to int");
            }
            iLength = (int) length;
        }
        return iLength;
    }

    /**
     * For testing attack scenarios in SizesTest.
     */
    public void setContentLength(long length) {
        this.length = length;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getContentType()
     */
    @Override
    public String getContentType() {
        return m_strContentType;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getInputStream()
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        ServletInputStream sis = new MyServletInputStream(m_requestData, readLimit);
        return sis;
    }

    /**
     * Sets the read limit. This can be used to limit the number of bytes to
     * read ahead.
     *
     * @param readLimit the read limit to use
     */
    public void setReadLimit(int readLimit) {
        this.readLimit = readLimit;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getParameter(String)
     */
    @Override
    public String getParameter(String arg0) {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getParameterNames()
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getParameterValues(String)
     */
    @Override
    public String[] getParameterValues(String arg0) {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getParameterMap()
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getProtocol()
     */
    @Override
    public String getProtocol() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getScheme()
     */
    @Override
    public String getScheme() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getServerName()
     */
    @Override
    public String getServerName() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getLocalName()
     */
    @Override
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public String getLocalName() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getServerPort()
     */
    @Override
    public int getServerPort() {
        return 0;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getLocalPort()
     */
    @Override
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public int getLocalPort() {
        return 0;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getRemotePort()
     */
    @Override
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public int getRemotePort() {
        return 0;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getReader()
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getRemoteAddr()
     */
    @Override
    public String getRemoteAddr() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getLocalAddr()
     */
    @Override
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public String getLocalAddr() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getRemoteHost()
     */
    @Override
    public String getRemoteHost() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#setAttribute(String, Object)
     */
    @Override
    public void setAttribute(String arg0, Object arg1) {
    }

    /**
     * @see jakarta.servlet.ServletRequest#removeAttribute(String)
     */
    @Override
    public void removeAttribute(String arg0) {
    }

    /**
     * @see jakarta.servlet.ServletRequest#getLocale()
     */
    @Override
    public Locale getLocale() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getLocales()
     */
    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#isSecure()
     */
    @Override
    public boolean isSecure() {
        return false;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getRequestDispatcher(String)
     */
    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return null;
    }

    /**
     * @see jakarta.servlet.ServletRequest#getRealPath(String)
     * @deprecated
     */
    @Override
    @Deprecated
    public String getRealPath(String arg0) {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {
    }

    @Override
    public void logout() throws ServletException {
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    private static class MyServletInputStream
            extends jakarta.servlet.ServletInputStream {

        private final InputStream in;
        private final int readLimit;

        /**
         * Creates a new instance, which returns the given streams data.
         */
        public MyServletInputStream(InputStream pStream, int readLimit) {
            in = pStream;
            this.readLimit = readLimit;
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            if (readLimit > 0) {
                return in.read(b, off, Math.min(readLimit, len));
            }
            return in.read(b, off, len);
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

    }

}
