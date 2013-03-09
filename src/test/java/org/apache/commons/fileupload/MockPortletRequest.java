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

import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;

/**
 * A mock portlet request, useful for unit testing and offline utilities
 *
 * @version $Id$
 */
public class MockPortletRequest implements PortletRequest {

    MockPortletSession session = null;

    public MockPortletRequest() {
        session = new MockPortletSession();
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#isWindowStateAllowed(javax.portlet.WindowState)
     */
    public boolean isWindowStateAllowed(WindowState state) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#isPortletModeAllowed(javax.portlet.PortletMode)
     */
    public boolean isPortletModeAllowed(PortletMode mode) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getPortletMode()
     */
    public PortletMode getPortletMode() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getWindowState()
     */
    public WindowState getWindowState() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getPreferences()
     */
    public PortletPreferences getPreferences() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getPortletSession()
     */
    public PortletSession getPortletSession() {
        return session;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getPortletSession(boolean)
     */
    public PortletSession getPortletSession( boolean create ) {
        if ( session == null )
        {
            session = new MockPortletSession();
        }
        return session;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getProperty(java.lang.String)
     */
    public String getProperty( String name ) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getProperties(java.lang.String)
     */
    public Enumeration<String> getProperties( String name ) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getPropertyNames()
     */
    public Enumeration<String> getPropertyNames() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getPortalContext()
     */
    public PortalContext getPortalContext() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getAuthType()
     */
    public String getAuthType() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getContextPath()
     */
    public String getContextPath() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getRemoteUser()
     */
    public String getRemoteUser() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole( String role ) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getAttribute(java.lang.String)
     */
    public Object getAttribute( String name ) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getAttributeNames()
     */
    public Enumeration<String> getAttributeNames() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getParameter(java.lang.String)
     */
    public String getParameter( String name ) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getParameterNames()
     */
    public Enumeration<String> getParameterNames() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues( String name ) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getParameterMap()
     */
    public Map<String, String[]> getParameterMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#isSecure()
     */
    public boolean isSecure() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute( String name, Object o ) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#removeAttribute(java.lang.String)
     */
    public void removeAttribute( String name ) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getRequestedSessionId()
     */
    public String getRequestedSessionId() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getResponseContentType()
     */
    public String getResponseContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getResponseContentTypes()
     */
    public Enumeration<String> getResponseContentTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getLocale()
     */
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getScheme()
     */
    public String getScheme() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getServerName()
     */
    public String getServerName() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.portlet.PortletRequest#getServerPort()
     */
    public int getServerPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Enumeration<Locale> getLocales() {
        return null;
    }

}
