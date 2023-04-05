package org.apache.commons.fileupload2.impl;

import org.apache.commons.fileupload2.pub.FileUploadByteCountLimitException;
import org.apache.commons.fileupload2.pub.FileUploadSizeException;
import static java.lang.String.format;

public class FileUploadExceptionCreation {
        // my changes
        public static void FileUploadException(String fieldName, long SizeMax, long CountLength, String name)
                        throws FileUploadByteCountLimitException {
                throw new FileUploadByteCountLimitException(
                                format("The field %s exceeds its maximum permitted size of %s bytes.", fieldName,
                                                SizeMax),
                                CountLength, SizeMax, name,
                                fieldName);
        }

        public static void FileUploadException(long requestSize, long sizeMax) throws FileUploadSizeException {
                throw new FileUploadSizeException(
                                format("the request was rejected because its size (%s) exceeds the configured maximum (%s)",
                                                requestSize, sizeMax),
                                sizeMax,
                                requestSize);
        }

}
