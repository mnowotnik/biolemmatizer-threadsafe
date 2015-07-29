package edu.northwestern.at.morphadorner.tools.tcp;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.text.*;
import java.util.*;

import edu.northwestern.at.utils.*;

/** Filter hyphenated words.
  */

public class ExtractSoftHyphens
{
    /** Wrapper for printStream to allow utf-8 output. */

    protected static PrintStream printStream;

    /** File containing divided words. */

    protected static String dividedWordsFileName;

    /** Fixed hyphenated words. */

    protected static String filteredDividedWordsFileName;

    /** Map from divided word to matching undivided word. */

    protected static Map<String, String> dividedWords;

    /** Filtered divided words map. */

    protected static Map<String, Number> filteredDividedWords;

    /** Main program.
     *
     *  @param  args    Program parameters.
     */

    public static void main( String[] args )
    {
                                //  Initialize.
        try
        {
            if ( !initialize( args ) )
            {
                System.exit( 1 );
            }
                                //  Process divided words.

            long startTime      = System.currentTimeMillis();

            Map<String, String> filteredDividedWords    = processWords();

                                //  Save filtered words.
            MapUtils.saveMap
            (
                filteredDividedWords ,
                filteredDividedWordsFileName ,
                "\t" ,
                "" ,
                "utf-8"
            );

            long processingTime =
                ( System.currentTimeMillis() - startTime + 999 ) / 1000;

                                //  Terminate.

            terminate( filteredDividedWords.size() , processingTime );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.out.println( e.getMessage() );
        }
    }

    /** Initialize.
     */

    protected static boolean initialize( String[] args )
        throws Exception
    {
                                //  Check if we have enough parameters.

        if ( args.length < 2 )
        {
            System.err.println( "Not enough parameters." );
            return false;
        }
                                //  Allow utf-8 output to printStream .
        printStream =
            new PrintStream
            (
                new BufferedOutputStream( System.out ) ,
                true ,
                "utf-8"
            );
                                //  Get arguments.

        dividedWordsFileName        = args[ 0 ];

                                //  Get divided words.
        dividedWords    =
            MapUtils.loadMap( dividedWordsFileName );

        System.err.println
        (
            "Loaded " +
            Formatters.formatIntegerWithCommas
            (
                dividedWords.size()
            ) +
            " divided words."
        );

        filteredDividedWordsFileName    = args[ 1 ];

        return true;
    }

    /** Process words.
     *
     *  <p>
     *  Output the following for each word.
     *  </p>
     *
     *  <ul>
     *      <li>The divided word.</li>
     *      <li>Probable corrected spelling.</li>
     *      <li>Count of hyphenated word appearances.</li>
     *      <li>Count of unhyphenated word appearances.</li>
     *      <li>True/false if word appears unhyphenated in standard
     *          spellings list.</li>
     *      <li>True/false if word appears hyphenated in standard
     *          spellings list.</li>
     *  </ul>
     */

    protected static Map<String, String> processWords()
    {
                                //  Process each divided word.

        Map<String, String> filteredDividedWords    =
            MapFactory.createNewSortedMap();

        Iterator<String> iterator   = dividedWords.keySet().iterator();

        while ( iterator.hasNext() )
        {
            String dividedWord  = iterator.next();
            String fixedWord    = dividedWords.get( dividedWord );

            String fixedDividedWord =
                StringUtils.replaceAll( dividedWord , "|" , "" );

            if ( !fixedDividedWord.equals( fixedWord ) )
            {
                filteredDividedWords.put( dividedWord , fixedWord );
            }
        }

        return filteredDividedWords;
    }

    /** Terminate.
     *
     *  @param  wordsProcessed  Number of words processed.
     *  @param  processingTime  Processing time in seconds.
     */

    protected static void terminate
    (
        int wordsProcessed ,
        long processingTime
    )
    {
        System.err.println
        (
            "Emitted " +
            Formatters.formatIntegerWithCommas
            (
                wordsProcessed
            ) +
            " filtered divided words in " +
            Formatters.formatLongWithCommas
            (
                processingTime
            ) +
            " seconds."
        );
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



