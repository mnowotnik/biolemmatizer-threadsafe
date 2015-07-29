package edu.northwestern.at.utils;

/*  Please see the license information at the end of this file. */

import java.io.IOException;
import java.io.OutputStream;

/** A null output stream which ignores all output written to it.
 */

public class NullOutputStream extends OutputStream
{
    /** Construct NullOutputStream.
     */

    public NullOutputStream()
    {
    }

    /** Writes (actually ignores) array of bytes to output stream.
     *
     *  @param  b   The bytes to output.
     *
     *  @throws IOException     If there is an I/O error.
     *
     *  <p>
     *  Nothing is actually written.
     *  </p>
     */

    public void write( byte[] b ) throws IOException
    {
    }

    /** Write (actually, ignore) bytes from byte array to output stream.
     *
     *  @param b        The bytes to output.
     *  @param off      Starting offset in the byte array.
     *  @param len      Number of bytes to write.
     *
     *  @throws IOException     If there is an I/O error.
     *
     *  <p>
     *  Nothing is actually written.
     *  </p>
     */

    public void write( byte[] b , int off , int len )
        throws IOException
    {
    }

    /** Write (actually, ignore) integer value to output stream.
     *
     *  @param  i   Value to write.
     *
     *  @throws IOException     If there is an I/O error.
     *
     *  <p>
     *  Nothing is actually written.
     *  </p>
     */

    public void write( int i ) throws IOException
    {
    }
}

/*
Copyright (c) 2008, 2013 by Northwestern University.
All rights reserved.

Developed by:
   Academic and Research Technologies
   Northwestern University
   http://www.it.northwestern.edu/about/departments/at/

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal with the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or
sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimers.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimers in the documentation and/or other materials provided
      with the distribution.

    * Neither the names of Academic and Research Technologies,
      Northwestern University, nor the names of its contributors may be
      used to endorse or promote products derived from this Software
      without specific prior written permission.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE CONTRIBUTORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*/



