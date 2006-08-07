/*
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.commons.fileupload.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * An input stream, which limits its data size. This stream is
 * used, if the content length is unknown.
 */
public abstract class LimitedInputStream extends FilterInputStream
		implements Closeable {
	private long sizeMax;
	private long count;
	private boolean closed;

	/**
	 * Creates a new instance.
	 * @param pIn The input stream, which shall be limited.
	 * @param pSizeMax The limit; no more than this number of bytes
	 *   shall be returned by the source stream.
	 */
	public LimitedInputStream(InputStream pIn, long pSizeMax) {
		super(pIn);
		sizeMax = pSizeMax;
	}

	protected abstract void raiseError(long pSizeMax, long pCount) throws IOException;

	private void checkLimit() throws IOException {
		if (count > sizeMax) {
			raiseError(sizeMax, count);
		}
	}

	public int read() throws IOException {
		int res = super.read();
		if (res != -1) {
			count++;
			checkLimit();
		}
		return res;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int res = super.read(b, off, len);
		if (res > 0) {
			count += res;
			checkLimit();
		}
		return res;
	}

	public boolean isClosed() throws IOException {
		return closed;
	}

	public void close() throws IOException {
		closed = true;
		super.close();
	}
}
