package org.apache.commons.fileupload;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 *
 * 
 * 
 * 
 * 
 */
final class HttpServletRequestFactory
{
	static public HttpServletRequest createHttpServletRequestWithNullContentType()
	{
		byte[] requestData = "foobar".getBytes();
		return new MyHttpServletRequest(
							requestData,
							null);
	}

	static public HttpServletRequest createValidHttpServletRequest(
		final String[] strFileNames)
	{
		byte[] requestData = "todo".getBytes();
		return new MyHttpServletRequest(
							requestData,
							FileUpload.MULTIPART_FORM_DATA);
	}

	static public HttpServletRequest createInvalidHttpServletRequest()
	{
		byte[] requestData = "foobar".getBytes();
		return new MyHttpServletRequest(
							requestData,
							FileUpload.MULTIPART_FORM_DATA);
	}
}
