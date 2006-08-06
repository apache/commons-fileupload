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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Exception for errors encountered while processing the request.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class FileUploadException extends Exception {
	private static final long serialVersionUID = 8881893724388807504L;
	private final Throwable cause;

    /**
     * Constructs a new <code>FileUploadException</code> without message.
     */
    public FileUploadException() {
    	this(null, null);
    }

    /**
     * Constructs a new <code>FileUploadException</code> with specified detail
     * message.
     *
     * @param msg the error message.
     */
    public FileUploadException(final String msg) {
    	this(msg, null);
    }

    /**
     * Creates a new <code>FileUploadException</code> with the given
     * detail message and cause.
     */
    public FileUploadException(String msg, Throwable cause) {
    	super(msg);
    	this.cause = cause;
    }

    public void printStackTrace(PrintStream stream) {
    	super.printStackTrace(stream);
    	if (cause != null) {
    		stream.println("Caused by:");
    		cause.printStackTrace(stream);
    	}
    }

    public void printStackTrace(PrintWriter writer) {
    	super.printStackTrace(writer);
    	if (cause != null) {
    		writer.println("Caused by:");
    		cause.printStackTrace(writer);
    	}
    }
}
