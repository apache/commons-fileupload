package org.apache.commons.fileupload;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Low level api for processing file uploads.
 *
 * This class can be used to process data streams conforming to MIME
 * 'multipart' format as defined in
 * <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.  Arbitrary
 * large amouns of data in the stream can be processed under constant
 * memory usage.
 *
 * <p>The format of the stream is defined in the following way:<br>
 *
 * <code>
 *   multipart-body := preamble 1*encapsulation close-delimiter epilogue<br>
 *   encapsulation := delimiter body CRLF<br>
 *   delimiter := "--" boundary CRLF<br>
 *   close-delimiter := "--" boudary "--"<br>
 *   preamble := &lt;ignore&gt;<br>
 *   epilogue := &lt;ignore&gt;<br>
 *   body := header-part CRLF body-part<br>
 *   header-part := 1*header CRLF<br>
 *   header := header-name ":" header-value<br>
 *   header-name := &lt;printable ascii characters except ":"&gt;<br>
 *   header-value := &lt;any ascii characters except CR & LF&gt;<br>
 *   body-data := &lt;arbitrary data&gt;<br>
 * </code>
 *
 * <p>Note that body-data can contain another mulipart entity.  There
 * is limited support for single pass processing of such nested
 * streams.  The nested stream is <strong>required</strong> to have a
 * boundary token of the same length as the parent stream (see {@link
 * #setBoundary(byte[])}).
 *
 * <p>Here is an exaple of usage of this class.<br>
 *
 * <pre>
 *    try {
 *        MultipartStream multipartStream = new MultipartStream(input,
 *                                                              boundary);
 *        boolean nextPart = malitPartStream.skipPreamble();
 *        OutputStream output;
 *        while(nextPart) {
 *            header = chunks.readHeader();
 *            // process headers
 *            // create some output stream
 *            multipartStream.readBodyPart(output);
 *            nextPart = multipartStream.readBoundary();
 *        }
 *    } catch(MultipartStream.MalformedStreamException e) {
 *          // the stream failed to follow required syntax
 *    } catch(IOException) {
 *          // a read or write error occurred
 *    }
 *
 * </pre>
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @version $Id: MultipartStream.java,v 1.2 2002/04/11 06:03:19 jmcnally Exp $
 */
public class MultipartStream
{
    /**
     * The maximum lenght of <code>header-part</code> that will be
     * processed (10 kilobytes = 10240 bytes.
     )*/
    public static final int HEADER_PART_SIZE_MAX = 10240;

    /** The stream were data is read from. */
    protected InputStream input;

    /**
     * The lenght of boundary token plus leading <code>CRLF--</code>.
     */
    protected int boundaryLength;

    /**
     * The amount of data that must be kept in the buffer in order to
     * detect delimiters reliably.
     */
    protected int keepRegion;

    /** A byte sequence that partitions the stream. */
    protected byte[] boundary;

    /** The lenght of the buffer used for processing. */
    protected int bufSize;

    /** The default lenght of the buffer used for processing. */
    protected static final int DEFAULT_BUFSIZE = 4096;

    /** The buffer used for processing. */
    protected byte[] buffer;

    /**
     * The index of first valid character in the buffer.
     *
     * 0 <= head < bufSize
     */
    protected int head;

    /**
     * The index of last valid characer in the buffer + 1.
     *
     * 0 <= tail <= bufSize
     */
    protected int tail;

    /**
     * A byte sequence that marks the end of <code>header-part</code>
     * (<code>CRLFCRLF</code>).
     */
    protected static final byte[] HEADER_SEPARATOR = {0x0D, 0x0A, 0x0D, 0x0A};

    /**
     * A byte sequence that that follows a delimiter that will be
     * followed by an encapsulation (<code>CRLF</code>).
     */
    protected static final byte[] FIELD_SEPARATOR = { 0x0D, 0x0A };

    /**
     * A byte sequence that that follows a delimiter of the last
     * encapsulation in the stream (<code>--</code>).
     */
    protected static final byte[] STREAM_TERMINATOR = { 0x2D, 0x2D };

    /**
     * Required by the proxy.
     */
    public MultipartStream()
    {
    }

    /**
     * Constructs a MultipartStream with a custom size buffer.
     *
     * <p>Note that the buffer must be at least big enough to contain
     * the boundary string, plus 4 characters for CR/LF and double
     * dash, plus at least one byte of data.  Too small buffer size
     * setting will degrade performance.
     *
     * @param input The <code>InputStream</code> to serve as a data
     * source.
     * @param boundary The token used for dividing the stream into
     * <code>encapsulations</code>.
     * @param bufSize The size of the buffer to be used in bytes.
     * @exception MalformedStreamException.
     * @exception IOException.
     */
    public MultipartStream( InputStream input,
                            byte[] boundary,
                            int bufSize )
        throws MalformedStreamException,
               IOException
    {
        this.input = input;
        this.bufSize = bufSize;
        this.buffer = new byte[bufSize];

        // We prepend CR/LF to the boundary to chop trailng CR/LF from
        // body-data tokens.
        this.boundary = new byte[boundary.length+4];
        this.boundaryLength = boundary.length+4;
        this.keepRegion = boundary.length+3;
        this.boundary[0] = 0x0D;
        this.boundary[1] = 0x0A;
        this.boundary[2] = 0x2D;
        this.boundary[3] = 0x2D;
        System.arraycopy(boundary, 0, this.boundary, 4, boundary.length);

        head = 0;
        tail = 0;
    }

    /**
     * Constructs a MultipartStream with a defalut size buffer.
     *
     * @param input The <code>InputStream</code> to serve as a data
     * source.
     * @param boundary The token used for dividing the stream into
     * <code>encapsulations</code>.
     * @exception IOException.
     */
    public MultipartStream( InputStream input,
                            byte[] boundary )
        throws IOException
    {
        this(input, boundary, DEFAULT_BUFSIZE);
    }

    /**
     * Reads a byte from the <code>buffer</code>, and refills it as
     * neccessary.
     *
     * @return Next byte from the input stream.
     * @exception IOException, if there isn't any more data available.
     */
    public byte readByte()
        throws IOException
    {
        // Buffer depleted ?
        if(head == tail)
        {
            head = 0;
            // Refill.
            tail = input.read(buffer, head, bufSize);
            if(tail == -1)
            {
                // No more data available.
                throw new IOException("No more data is available");
            }
        }
        return buffer[head++];
    }

    /**
     * Skips a <code>boundary</code> token, and checks wether more
     * <code>encapsulations</code> are contained in the stream.
     *
     * @return <code>True</code> if there are more encapsulations in
     * this stream.
     * @exception MalformedStreamException if the stream ends
     * unexpecetedly or fails to follow required syntax.
     */
    public boolean readBoundary()
        throws MalformedStreamException
    {
        byte[] marker = new byte[2];
        boolean nextChunk = false;

        head += boundaryLength;
        try
        {
            marker[0] = readByte();
            marker[1] = readByte();
            if (arrayequals(marker, STREAM_TERMINATOR, 2))
            {
                nextChunk = false;
            }
            else if(arrayequals(marker, FIELD_SEPARATOR, 2))
            {
                nextChunk = true;
            }
            else
            {
                throw new MalformedStreamException("Unexpected characters follow a boundary");
            }
        }
        catch(IOException e)
        {
            throw new MalformedStreamException("Stream ended unexpectedly");
        }
        return nextChunk;
    }

    /**
     * Changes the boundary token used for partitioning the stream.
     *
     * <p>This method allows single pass processing of nested
     * multipart streams.
     *
     * <p>The boundary token of the nested stream is
     * <code>required</code> to be of the same length as the boundary
     * token in parent stream.
     *
     * <p>Restoring parent stream boundary token after processing of a
     * nested stream is left ot the application. <br>
     *
     * @param boundary A boundary to be used for parsing of the nested
     * stream.
     * @exception IllegalBoundaryException, if <code>boundary</code>
     * has diffrent lenght than the one being currently in use.
     */
    public void setBoundary( byte[] boundary )
        throws IllegalBoundaryException
    {
        if (boundary.length != boundaryLength-4)
        {
            throw new IllegalBoundaryException("The length of a boundary token can not be changed");
        }
        System.arraycopy(boundary, 0, this.boundary, 4, boundary.length);
    }

    /**
     * <p>Reads <code>header-part</code> of the current
     * <code>encapsulation</code>
     *
     * <p>Headers are returned verbatim to the input stream, including
     * traling <code>CRLF</code> marker. Parsing is left to the
     * application.
     *
     * <p><strong>TODO</strong> allow limiting maximum header size to
     * protect against abuse.<br>
     *
     * @return <code>header-part</code> of the current encapsulation.
     * @exception MalformedStreamException, if the stream ends
     * unexpecetedly.
     */
    public String readHeaders()
        throws MalformedStreamException
    {
        int i = 0;
        byte b[] = new byte[1];
        StringBuffer buf = new StringBuffer();
        int sizeMax = HEADER_PART_SIZE_MAX;
        int size = 0;
        while(i<4)
        {
            try
            {
                b[0] = readByte();
            }
            catch(IOException e)
            {
                throw new MalformedStreamException("Stream ended unexpectedly");
            }
            size++;
            if(b[0] == HEADER_SEPARATOR[i])
            {
                i++;
            }
            else
            {
                i = 0;
            }
            if(size <= sizeMax)
            {
                buf.append(new String(b));
            }
        }
        return buf.toString();
    }

    /**
     * Reads <code>body-data</code> from the current
     * <code>encapsulation</code> and writes its contents into the
     * output <code>Stream</code>.
     *
     * <p>Arbitrary large amouts of data can be processed by this
     * method using a constant size buffer. (see {@link
     * #MultipartStream(InputStream,byte[],int) constructor}).
     *
     * @param output The <code>Stream</code> to write data into.
     * @return the amount of data written.
     * @exception MalformedStreamException
     * @exception IOException
     */
    public int readBodyData( OutputStream output )
        throws MalformedStreamException,
               IOException
    {
        boolean done = false;
        int pad;
        int pos;
        int bytesRead;
        int total = 0;
        while(!done)
        {
            // Is boundary token present somewere in the buffer?
            pos = findSeparator();
            if(pos != -1)
            {
                // Write the rest of the data before the boundary.
                output.write(buffer, head, pos-head);
                total += pos-head;
                head = pos;
                done = true;
            }
            else
            {
                // Determine how much data should be kept in the
                // buffer.
                if(tail-head>keepRegion)
                {
                    pad = keepRegion;
                }
                else
                {
                    pad = tail-head;
                }
                // Write out the data belonging to the body-data.
                output.write(buffer, head, tail-head-pad);

                // Move the data to the beging of the buffer.
                total += tail-head-pad;
                System.arraycopy(buffer, tail-pad, buffer, 0, pad);

                // Refill buffer with new data.
                head = 0;
                bytesRead = input.read(buffer, pad, bufSize-pad);

                // [pprrrrrrr]
                if(bytesRead != -1)
                {
                    tail = pad+bytesRead;
                }
                else
                {
                    // The last pad amount is left in the buffer.
                    // Boundary can't be in there so write out the
                    // data you have and signal an error condition.
                    output.write(buffer,0,pad);
                    output.flush();
                    total += pad;
                    throw new MalformedStreamException("Stream ended unexpectedly");
                }
            }
        }
        output.flush();
        return total;
    }

    /**
     * Reads <code>body-data</code> from the current
     * <code>encapsulation</code> and discards it.
     *
     * <p>Use this method to skip encapsulations you don't need or
     * don't understand.
     *
     * @return The amount of data discarded.
     * @exception MalformedStreamException
     * @exception IOException
     */
    public int discardBodyData()
        throws MalformedStreamException,
               IOException
    {
        boolean done = false;
        int pad;
        int pos;
        int bytesRead;
        int total = 0;
        while(!done)
        {
            // Is boundary token present somewere in the buffer?
            pos = findSeparator();
            if(pos != -1)
            {
                // Write the rest of the data before the boundary.
                total += pos-head;
                head = pos;
                done = true;
            }
            else
            {
                // Determine how much data should be kept in the
                // buffer.
                if(tail-head>keepRegion)
                {
                    pad = keepRegion;
                }
                else
                {
                    pad = tail-head;
                }
                total += tail-head-pad;

                // Move the data to the beging of the buffer.
                System.arraycopy(buffer, tail-pad, buffer, 0, pad);

                // Refill buffer with new data.
                head = 0;
                bytesRead = input.read(buffer, pad, bufSize-pad);

                // [pprrrrrrr]
                if(bytesRead != -1)
                {
                    tail = pad+bytesRead;
                }
                else
                {
                    // The last pad amount is left in the buffer.
                    // Boundary can't be in there so signal an error
                    // condition.
                    total += pad;
                    throw new MalformedStreamException("Stream ended unexpectedly");
                }
            }
        }
        return total;
    }

    /**
     * Finds the beginning of the first <code>encapsulation</code>.
     *
     * @return <code>True</code> if an <code>encapsulation</code> was
     * found in the stream.
     * @exception IOException
     */
    public boolean skipPreamble()
        throws IOException
    {
        // First delimiter may be not preceeded with a CRLF.
        System.arraycopy(boundary, 2, boundary, 0, boundary.length-2);
        boundaryLength = boundary.length-2;
        try
        {
            // Discard all data up to the delimiter.
            discardBodyData();

            // Read boundary - if succeded, the stream contains an
            // encapsulation.
            return readBoundary();
        }
        catch(MalformedStreamException e)
        {
            return false;
        }
        finally
        {
            // Restore delimiter.
            System.arraycopy(boundary,0, boundary, 2, boundary.length-2);
            boundaryLength = boundary.length;
            boundary[0] = 0x0D;
            boundary[1] = 0x0A;
        }
    }

    /**
     * Compares <code>count</code> first bytes in the arrays
     * <code>a</code> and <code>b</code>.
     *
     * @param a The first array to compare.
     * @param b The second array to compare.
     * @param count How many bytes should be compared.
     * @return <code>true</code> if <code>count</code> first bytes in
     * arrays <code>a</code> and <code>b</code> are equal.
     */
    public static boolean arrayequals( byte[] a,
                                       byte[] b,
                                       int count )
    {
        for(int i=0; i<count; i++)
        {
            if(a[i] != b[i])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Searches a byte of specified value in the <code>buffer</code>
     * starting at specified <code>position</code>.
     *
     * @param value the value to find.
     * @param pos The starting position for searching.
     * @return The position of byte found, counting from beginning of
     * the <code>buffer</code>, or <code>-1</code> if not found.
     */
    protected int findByte(byte value,
                           int pos)
    {
        for (int i = pos; i < tail; i++)
            if(buffer[i] == value)
                return i;

            return -1;
    }

    /**
     * Searches the <code>boundary</code> in <code>buffer</code>
     * region delimited by <code>head</code> and <code>tail</code>.
     *
     * @return The position of the boundary found, counting from
     * beginning of the <code>buffer</code>, or <code>-1</code> if not
     * found.
     */
    protected int findSeparator()
    {
        int first;
        int match = 0;
        int maxpos = tail - boundaryLength;
        for(first = head;
            (first <= maxpos) && (match != boundaryLength);
            first++)
        {
            first = findByte(boundary[0], first);
            if(first == -1 || (first > maxpos))
                return -1;
            for(match = 1; match<boundaryLength; match++)
            {
                if(buffer[first+match] != boundary[match])
                    break;
            }
        }
        if(match == boundaryLength)
        {
            return first-1;
        }
        return -1;
    }


    /**
     * Thrown to indicate that the input stream fails to follow the
     * required syntax.
     */
    public class MalformedStreamException
        extends IOException
    {
        /**
         * Constructs a <code>MalformedStreamException</code> with no
         * detail message.
         */
        public MalformedStreamException()
        {
            super();
        }

        /**
         * Constructs an <code>MalformedStreamException</code> with
         * the specified detail message.
         *
         * @param message The detail message.
         */
        public MalformedStreamException(String message)
        {
            super(message);
        }
    }

    /**
     * Thrown upon attempt of setting an invalid boundary token.
     */
    public class IllegalBoundaryException
        extends IOException
    {
        /**
         * Constructs an <code>IllegalBoundaryException</code> with no
         * detail message.
         */
        public IllegalBoundaryException()
        {
            super();
        }

        /**
         * Constructs an <code>IllegalBoundaryException</code> with
         * the specified detail message.
         *
         * @param message The detail message.
         */
        public IllegalBoundaryException(String message)
        {
            super(message);
        }
    }

    /*-------------------------------------------------------------

    // These are the methods that were used to debug this stuff.

    // Dump data.
    protected void dump()
    {
        System.out.println("01234567890");
        byte[] temp = new byte[buffer.length];
        for(int i=0; i<buffer.length; i++)
        {
            if(buffer[i] == 0x0D || buffer[i] == 0x0A)
            {
                temp[i] = 0x21;
            }
            else
            {
                temp[i] = buffer[i];
            }
        }
        System.out.println(new String(temp));
        int i;
        for(i=0; i<head; i++)
            System.out.print(" ");
        System.out.println("h");
        for(i=0; i<tail; i++)
            System.out.print(" ");
        System.out.println("t");
        System.out.flush();
    }

    // Main routine, for testing purposes only.
    //
    // @param args A String[] with the command line arguments.
    // @exception Exception, a generic exception.
    public static void main( String[] args )
        throws Exception
    {
        File boundaryFile = new File("boundary.dat");
        int boundarySize = (int)boundaryFile.length();
        byte[] boundary = new byte[boundarySize];
        FileInputStream input = new FileInputStream(boundaryFile);
        input.read(boundary,0,boundarySize);

        input = new FileInputStream("multipart.dat");
        MultipartStream chunks = new MultipartStream(input, boundary);

        int i = 0;
        String header;
        OutputStream output;
        boolean nextChunk = chunks.skipPreamble();
        while(nextChunk)
        {
            header = chunks.readHeaders();
            System.out.println("!"+header+"!");
            System.out.println("wrote part"+i+".dat");
            output = new FileOutputStream("part"+(i++)+".dat");
            chunks.readBodyData(output);
            nextChunk = chunks.readBoundary();
        }
    }

    */
}
