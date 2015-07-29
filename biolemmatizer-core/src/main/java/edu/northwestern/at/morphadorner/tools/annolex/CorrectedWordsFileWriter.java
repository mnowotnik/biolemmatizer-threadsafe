package edu.northwestern.at.morphadorner.tools.annolex;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.text.*;
import java.util.*;

import edu.northwestern.at.utils.Env;

/** Write word correction information file.
*/

public class CorrectedWordsFileWriter
{
    /** Create corrected words file writer.
     *
     *  @param  correctedWords  List of corrected words to output.
     *  @param  outputFileName  Output file name.
     *  @param  append          True to append to existing contents.
     */

    public CorrectedWordsFileWriter
    (
        List<CorrectedWord> correctedWords ,
        String outputFileName ,
        boolean append
    )
        throws java.io.IOException
    {
                                //  Write corrections.

        writeCorrectedWords( correctedWords , outputFileName , append );
    }

    /** Writes corrected words to file.
     *
     *  @param  correctedWords  List of corrected words to output.
     *  @param  outputFileName  The output file name.
     *  @param  append          True to append to existing contents.
     */

    protected void writeCorrectedWords
    (
        List<CorrectedWord> correctedWords ,
        String outputFileName ,
        boolean append
    )
        throws java.io.IOException
    {
        FileOutputStream outputStream           =
            new FileOutputStream( new File( outputFileName ) , append );

        BufferedOutputStream bufferedStream =
            new BufferedOutputStream( outputStream );

        OutputStreamWriter outputWriter =
            new OutputStreamWriter
            (
                bufferedStream ,
                "utf-8"
            );

        if ( correctedWords != null )
        {
            for ( int i = 0 ; i < correctedWords.size() ; i++ )
            {
                CorrectedWord correctedWord =
                    correctedWords.get( i );

                outputWriter.write( correctedWord.toString() );
                outputWriter.write( Env.LINE_SEPARATOR );
            }
        }

        outputWriter.close();
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



