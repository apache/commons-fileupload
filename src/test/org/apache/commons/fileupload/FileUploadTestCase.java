package org.apache.commons.fileupload;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import junit.framework.TestCase;


/**
 * Base class for deriving test cases.
 */
public abstract class FileUploadTestCase extends TestCase {
	protected static final String CONTENT_TYPE = "multipart/form-data; boundary=---1234";

	protected List parseUpload(byte[] bytes) throws FileUploadException {
        return parseUpload(bytes, CONTENT_TYPE);
    }

	protected List parseUpload(byte[] bytes, String contentType) throws FileUploadException {
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        HttpServletRequest request = new MockHttpServletRequest(bytes, contentType);

        List fileItems = upload.parseRequest(request);
        return fileItems;
	}

	protected List parseUpload(String content)
    	throws UnsupportedEncodingException, FileUploadException
    {
		byte[] bytes = content.getBytes("US-ASCII");
		return parseUpload(bytes, CONTENT_TYPE);
    }
}
