/*
 * Copyright 2001-2004 The Apache Software Foundation
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

import junit.framework.TestCase;
import javax.servlet.http.HttpServletRequest;

/**
 * Unit tests {@link org.apache.commons.fileupload.DiskFileUpload}.
 *
 * @author <a href="mailto:jmcnally@apache.org">John McNally</a>
 * @author Sean C. Sullivan
 *
 */
public class FileUploadTest extends TestCase
{
	public void testWithInvalidRequest()
    {
    	FileUploadBase fu = null;

    	fu = new DiskFileUpload();

    	HttpServletRequest req = HttpServletRequestFactory.createInvalidHttpServletRequest();


    	try
    	{
    		fu.parseRequest(req);
    		fail("testWithInvalidRequest: expected exception was not thrown");
    	}
    	catch (FileUploadException expected)
    	{
    		// this exception is expected
    	}

    }


	public void testWithNullContentType()
    {
    	FileUploadBase fu = new DiskFileUpload();

    	HttpServletRequest req = HttpServletRequestFactory.createHttpServletRequestWithNullContentType();

    	try
    	{
    		fu.parseRequest(req);
    		fail("testWithNullContentType: expected exception was not thrown");
    	}
    	catch (DiskFileUpload.InvalidContentTypeException expected)
    	{
    		// this exception is expected
    	}
        catch (FileUploadException unexpected)
        {
    		fail("testWithNullContentType: unexpected exception was thrown");
        }

    }


    public FileUploadTest(String name)
    {
        super(name);
    }

    public void testParseRequest() throws FileUploadException
    {

    	String[] fileNames =
    	{
			"filename1",
    		"filename2"
    	};

		FileUploadBase fu = new DiskFileUpload();

		HttpServletRequest req = HttpServletRequestFactory.createValidHttpServletRequest(fileNames);

		// todo java.util.List lst = fu.parseRequest(req);
		// todo assertNotNull(lst);

    }
}

