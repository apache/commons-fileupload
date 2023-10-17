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

import java.io.IOException;
import java.io.OutputStream;

/**
 */
final class QuotedPrintableDecoder {

    /**
     * The shift value required to create the upper nibble from the first of 2 byte values converted from ASCII hex.
     */
    private static final int UPPER_NIBBLE_SHIFT = Byte.SIZE / 2;

    /**
     * Decodes the encoded byte data writing it to the given output stream.
     *
     * @param data The array of byte data to decode.
     * @param out  The output stream used to return the decoded data.
     *
     * @return the number of bytes produced.
     * @throws IOException if an IO error occurs
     */
    public static int decode(final byte[] data, final OutputStream out) throws IOException {
        var off = 0;
        final var length = data.length;
        final var endOffset = off + length;
        var bytesWritten = 0;

        while (off < endOffset) {
            final var ch = data[off++];

            // space characters were translated to '_' on encode, so we need to translate them back.
            if (ch == '_') {
                out.write(' ');
            } else if (ch == '=') {
                // we found an encoded character. Reduce the 3 char sequence to one.
                // but first, make sure we have two characters to work with.
                if (off + 1 >= endOffset) {
                    throw new IOException("Invalid quoted printable encoding; truncated escape sequence");
                }

                final var b1 = data[off++];
                final var b2 = data[off++];

                // we've found an encoded carriage return. The next char needs to be a newline
                if (b1 == '\r') {
                    if (b2 != '\n') {
                        throw new IOException("Invalid quoted printable encoding; CR must be followed by LF");
                    }
                    // this was a soft linebreak inserted by the encoding. We just toss this away
                    // on decode.
                } else {
                    // this is a hex pair we need to convert back to a single byte.
                    final var c1 = hexToBinary(b1);
                    final var c2 = hexToBinary(b2);
                    out.write(c1 << UPPER_NIBBLE_SHIFT | c2);
                    // 3 bytes in, one byte out
                    bytesWritten++;
                }
            } else {
                // simple character, just write it out.
                out.write(ch);
                bytesWritten++;
            }
        }

        return bytesWritten;
    }

    /**
     * Converts a hexadecimal digit to the binary value it represents.
     *
     * @param b the ASCII hexadecimal byte to convert (0-0, A-F, a-f)
     * @return the int value of the hexadecimal byte, 0-15
     * @throws IOException if the byte is not a valid hexadecimal digit.
     */
    private static int hexToBinary(final byte b) throws IOException {
        // CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINE
        final var i = Character.digit((char) b, 16);
        if (i == -1) {
            throw new IOException("Invalid quoted printable encoding: not a valid hex digit: " + b);
        }
        return i;
    }

    /**
     * Hidden constructor, this class must not be instantiated.
     */
    private QuotedPrintableDecoder() {
        // do nothing
    }

}
