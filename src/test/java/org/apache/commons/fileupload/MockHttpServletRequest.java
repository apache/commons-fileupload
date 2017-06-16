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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class MockHttpServletRequest implements HttpServletRequest {

    private final InputStream m_requestData;

    private long length;

    private String m_strContentType;

    private int readLimit = -1;

    private final Map<String, String> m_headers = new java.util.HashMap<String, String>();

    /**
     * Creates a new instance with the given request data
     * and content type.
     */
    public MockHttpServletRequest(
            final byte[] requestData,
            final String strContentType) {
        this(new ByteArrayInputStream(requestData),
                requestData.length, strContentType);
    }

    /**
     * Creates a new instance with the given request data
     * and content type.
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
     * @see javax.servlet.http.HttpServletRequest#getAuthType()
     */
    public String getAuthType() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     */
    public Cookie[] getCookies() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getDateHeader(String)
     */
    public long getDateHeader(String arg0) {
        return 0;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getHeader(String)
     */
    public String getHeader(String headerName) {
        return m_headers.get(headerName);
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getHeaders(String)
     */
    public Enumeration<String> getHeaders(String arg0) {
        // todo - implement
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
     */
    public Enumeration<String> getHeaderNames() {
        // todo - implement
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getIntHeader(String)
     */
    public int getIntHeader(String arg0) {
        return 0;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getMethod()
     */
    public String getMethod() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */
    public String getPathInfo() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
     */
    public String getPathTranslated() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    public String getContextPath() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    public String getQueryString() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
     */
    public String getRemoteUser() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isUserInRole(String)
     */
    public boolean isUserInRole(String arg0) {
        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    public String getRequestedSessionId() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    public String getRequestURI() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    public String getServletPath() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
     */
    public HttpSession getSession(boolean arg0) {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getSession()
     */
    public HttpSession getSession() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     * @deprecated
     */
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    /**
     * @see javax.servlet.ServletRequest#getAttribute(String)
     */
    public Object getAttribute(String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#setCharacterEncoding(String)
     */
    public void setCharacterEncoding(String arg0)
        throws UnsupportedEncodingException {
    }

    /**
     * @see javax.servlet.ServletRequest#getContentLength()
     */
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
     * @see javax.servlet.ServletRequest#getContentType()
     */
    public String getContentType() {
        return m_strContentType;
    }

    /**
     * @see javax.servlet.ServletRequest#getInputStream()
     */
    public ServletInputStream getInputStream() throws IOException {
        ServletInputStream sis = new MyServletInputStream(m_requestData, readLimit);
        return sis;
    }

    /**
     * Sets the read limit. This can be used to limit the number of bytes to read ahead.
     *
     * @param readLimit the read limit to use
     */
    public void setReadLimit(int readLimit) {
        this.readLimit = readLimit;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameter(String)
     */
    public String getParameter(String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration<String> getParameterNames() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterValues(String)
     */
    public String[] getParameterValues(String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getProtocol()
     */
    public String getProtocol() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getScheme()
     */
    public String getScheme() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getServerName()
     */
    public String getServerName() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocalName()
     */
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public String getLocalName() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    public int getServerPort() {
        return 0;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocalPort()
     */
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public int getLocalPort() {
        return 0;
    }

    /**
     * @see javax.servlet.ServletRequest#getRemotePort()
     */
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public int getRemotePort() {
        return 0;
    }

    /**
     * @see javax.servlet.ServletRequest#getReader()
     */
    public BufferedReader getReader() throws IOException {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocalAddr()
     */
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public String getLocalAddr() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    public String getRemoteHost() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#setAttribute(String, Object)
     */
    public void setAttribute(String arg0, Object arg1) {
    }

    /**
     * @see javax.servlet.ServletRequest#removeAttribute(String)
     */
    public void removeAttribute(String arg0) {
    }

    /**
     * @see javax.servlet.ServletRequest#getLocale()
     */
    public Locale getLocale() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocales()
     */
    public Enumeration<Locale> getLocales() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#isSecure()
     */
    public boolean isSecure() {
        return false;
    }

    /**
     * @see javax.servlet.ServletRequest#getRequestDispatcher(String)
     */
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getRealPath(String)
     * @deprecated
     */
    @Deprecated
    public String getRealPath(String arg0) {
        return null;
    }

    private static class MyServletInputStream
        extends javax.servlet.ServletInputStream {

        private final InputStream in;
        private final int readLimit;

        /**
         * Creates a new instance, which returns the given
         * streams data.
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

    }

}
