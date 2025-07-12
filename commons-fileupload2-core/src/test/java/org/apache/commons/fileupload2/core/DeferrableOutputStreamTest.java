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
package org.apache.commons.fileupload2.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.commons.fileupload2.core.DeferrableOutputStream.State;


/** Test suite for the {@link DeferrableOutputStream}.
 */
class DeferrableOutputStreamTest {
	private static final Path testDir = Paths.get("target/unit-tests/DeferrableOutputStreamTest");
	private static Path tempTestDir;
	private Supplier<Path> testFileSupplier = () -> {
		try {
			return Files.createTempFile(tempTestDir, "testFile", ".bin");
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	};

	@BeforeAll
	static void setUpTestDirs() throws IOException {
		Files.createDirectories(testDir);
		tempTestDir = Files.createTempDirectory(testDir, "testDir");
	}

	/** Tests using the {@link DeferrableOutputStream} with a positive threshold.
	 */
	@Test
	void testExceedPositiveThreshold() {
		DeferrableOutputStream[] streams = new DeferrableOutputStream[1];
		final Consumer<Consumer<OutputStream>> tester = (consumer) -> {
			try (final DeferrableOutputStream dos = new DeferrableOutputStream(5, testFileSupplier)) {
				streams[0] = dos;
				assertTrue(dos.isInMemory());
				assertNull(dos.getPath());
				assertNull(dos.getBytes());
				assertSame(State.initialized, dos.getState());
				for (int i = 0;  i < 4;  i++) {
					try {
						dos.write('.');
					} catch (IOException ioe) {
						throw new UncheckedIOException(ioe);
					}
					assertSame(State.opened, dos.getState());
					assertTrue(dos.isInMemory());
					assertNull(dos.getPath());
					assertNull(dos.getBytes());
				}
				consumer.accept(dos);
				assertFalse(dos.isInMemory());
				assertNotNull(dos.getPath());
				assertNull(dos.getBytes());
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}

			final DeferrableOutputStream dos = streams[0];
			assertFalse(dos.isInMemory());
			assertNotNull(dos.getPath());
			assertTrue(Files.isRegularFile(dos.getPath()));
			final byte[] actual;
			try (InputStream is = dos.getInputStream()) {
				actual = read(is);
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
			final byte[] expect = "....,".getBytes(StandardCharsets.UTF_8);
			assertArrayEquals(expect, actual);
		};

		// Break the threshold using OutputStream.write(int);
		tester.accept((os) -> {
			try {
				os.write(',');
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		});
		// Break the threshold using OutputStream.write(byte[]);
		tester.accept((os) -> {
			final byte[] buffer = new byte[] {','};
			try {
				os.write(buffer);
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		});
		// Break the threshold using OutputStream.write(byte[], int, int);
		tester.accept((os) -> {
			final byte[] buffer = new byte[] {',', '-'};
			try {
				os.write(buffer, 0, 1);
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		});
	}

	/** Tests using the {@link DeferrableOutputStream} with threshold 0.
	 */
	@Test
	void testThresholdZero() {
		DeferrableOutputStream[] streams = new DeferrableOutputStream[1];
		final Consumer<Consumer<OutputStream>> tester = (consumer) -> {
			try (final DeferrableOutputStream dos = new DeferrableOutputStream(0, testFileSupplier)) {
				streams[0] = dos;
				assertTrue(dos.isInMemory());
				assertNull(dos.getPath());
				assertNull(dos.getBytes());
				assertSame(State.initialized, dos.getState());
				consumer.accept(dos);
				assertFalse(dos.isInMemory());
				assertNotNull(dos.getPath());
				assertNull(dos.getBytes());
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}

			final DeferrableOutputStream dos = streams[0];
			assertFalse(dos.isInMemory());
			assertNotNull(dos.getPath());
			assertTrue(Files.isRegularFile(dos.getPath()));
			final byte[] actual;
			try (InputStream is = dos.getInputStream()) {
				actual = read(is);
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
			final byte[] expect = ",".getBytes(StandardCharsets.UTF_8);
			assertArrayEquals(expect, actual);
		};
		// Break the threshold using OutputStream.write(int);
		tester.accept((os) -> {
			try {
				os.write(',');
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		});
		// Break the threshold using OutputStream.write(byte[]);
		tester.accept((os) -> {
			final byte[] buffer = new byte[] {','};
			try {
				os.write(buffer);
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		});
		// Break the threshold using OutputStream.write(byte[], int, int);
		tester.accept((os) -> {
			final byte[] buffer = new byte[] {',', '-'};
			try {
				os.write(buffer, 0, 1);
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		});
	}

	/** Tests using the {@link DeferrableOutputStream} with threshold -1.
	 */
	@Test
	void testThresholdMinusOne() {
		DeferrableOutputStream[] streams = new DeferrableOutputStream[1];
		final Runnable tester = () -> {
			try (final DeferrableOutputStream dos = new DeferrableOutputStream(-1, testFileSupplier)) {
				streams[0] = dos;
				assertFalse(dos.isInMemory());
				assertNotNull(dos.getPath());
				assertNull(dos.getBytes());
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}

			final DeferrableOutputStream dos = streams[0];
			assertFalse(dos.isInMemory());
			assertNotNull(dos.getPath());
			assertTrue(Files.isRegularFile(dos.getPath()));
			final byte[] actual;
			try (InputStream is = dos.getInputStream()) {
				actual = read(is);
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
			final byte[] expect = "".getBytes(StandardCharsets.UTF_8);
			assertArrayEquals(expect, actual);
		};
		tester.run();
	}

	protected byte[] read(InputStream pIs) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[8192];
		for (;;) {
			final int res = pIs.read(buffer);
			if (res == -1) {
				return baos.toByteArray();
			} else if (res > 0) {
				baos.write(buffer, 0, res);
			}
		}
	}
}
