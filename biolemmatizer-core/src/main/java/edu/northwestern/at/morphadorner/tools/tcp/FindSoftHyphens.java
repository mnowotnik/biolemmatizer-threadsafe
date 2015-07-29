package edu.northwestern.at.morphadorner.tools.tcp;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.text.*;
import java.util.*;

import org.jdom2.*;
import org.jdom2.filter.*;
import org.jdom2.input.*;
import org.jdom2.output.*;

import edu.northwestern.at.morphadorner.tools.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.adornedword.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.namerecognizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.spellingstandardizer.*;
import edu.northwestern.at.utils.math.*;
import edu.northwestern.at.utils.xml.*;

/** Determine which words containing soft hyphens should actually be hyphenated.
  */

public class FindSoftHyphens
{
    /** Wrapper for printStream to allow utf-8 output. */

    protected static PrintStream printStream;

    /** File containing words with a word break. */

    protected static String dividedWordsFileName;

    /** File containing word counts. */

    protected static String wordCountsFileName;

    /** File containing standard spellings. */

    protected static String standardSpellingsFileName;

    /** File containing fixed words. */

    protected static String fixedWordsFileName;

    /** Standard words. */

    protected static Set<String> standardSpellings;

    /** Divided words. */

    protected static Set<String> dividedWords;

    /** Word counts. */

    protected static Map<String, Number> wordCounts;

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

            Map<String, String> correctedSpellings  = processWords();

                                //  Save file words.
            MapUtils.saveMap
            (
                correctedSpellings ,
                fixedWordsFileName ,
                "\t" ,
                "" ,
                "utf-8"
            );

            long processingTime =
                ( System.currentTimeMillis() - startTime + 999 ) / 1000;

                                //  Terminate.

            terminate( correctedSpellings.size() , processingTime );
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

        if ( args.length < 4 )
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
        wordCountsFileName          = args[ 1 ];
        standardSpellingsFileName   = args[ 2 ];
        fixedWordsFileName          = args[ 3 ];

                                //  Get divided words.
        dividedWords        =
            SetUtils.loadSortedSet
            (
                dividedWordsFileName ,
                "utf-8"
            );

        System.err.println
        (
            "Loaded " +
            Formatters.formatIntegerWithCommas
            (
                dividedWords.size()
            ) +
            " divided words."
        );
                                //  Get word counts.
        wordCounts  =
            CountMapUtils.loadCountMapFromFile
            (
                new File( wordCountsFileName ) ,
                "utf-8"
            );

        System.err.println
        (
            "Loaded " +
            Formatters.formatIntegerWithCommas
            (
                wordCounts.size()
            ) +
            " word counts."
        );
                                //  Get standard spellings list.
        standardSpellings   =
            SetUtils.loadSortedSet
            (
                standardSpellingsFileName ,
                "utf-8"
            );

        System.err.println
        (
            "Loaded " +
            Formatters.formatIntegerWithCommas
            (
                standardSpellings.size()
            ) +
            " standard spellings."
        );

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

        Map<String, String> correctedSpellings  =
            MapFactory.createNewSortedMap();

        Names names = new Names();

        Iterator<String> iterator   = dividedWords.iterator();

        while ( iterator.hasNext() )
        {
            String token    = iterator.next();

            String unhyphenated =
                StringUtils.replaceAll( token , "|" , "" );

            String unhyphenatedLower    = unhyphenated.toLowerCase();

            String hyphenated   =
                StringUtils.replaceAll( token , "|" , "-" );

            String hyphenatedLower  = hyphenated.toLowerCase();

            String correctedSpelling    = unhyphenated;

            int unhyphenatedCount   =
                getWordCount( unhyphenated ) +
                getWordCount( unhyphenatedLower );

            int hyphenatedCount     =
                getWordCount( hyphenated ) +
                getWordCount( hyphenatedLower );

            if ( unhyphenatedCount == 0 )
            {
                if ( hyphenatedCount == 0 )
                {
                    if ( standardSpellings.contains( unhyphenated ) )
                    {
                        correctedSpelling   = unhyphenated;
                    }
                    else if ( standardSpellings.contains( unhyphenatedLower ) )
                    {
                        correctedSpelling   = unhyphenated;
                    }
                    else if ( standardSpellings.contains( hyphenated ) )
                    {
                        correctedSpelling   = hyphenated;
                    }
                    else if ( standardSpellings.contains( hyphenatedLower ) )
                    {
                        correctedSpelling   = hyphenated;
                    }
                    else if ( names.isNameOrPlace( unhyphenated ) )
                    {
                        correctedSpelling   = unhyphenated;
                    }
                    else if ( names.isNameOrPlace( hyphenated ) )
                    {
                        correctedSpelling   = hyphenated;
                    }
                    else
                    {
//                      correctedSpelling   = "*****";
                    }
                }
                else
                {
                    correctedSpelling   = hyphenated;
                }
            }
            else /* unhyphenated count > 0 */
            {
                if ( hyphenatedCount == 0 )
                {
                    correctedSpelling   = unhyphenated;
                }
                else if ( unhyphenatedCount > hyphenatedCount )
                {
                    correctedSpelling   = unhyphenated;
                }
                else
                {
                    correctedSpelling   = hyphenated;
                }
            }

            printStream.print( token );
            printStream.print( "\t" );
            printStream.print( correctedSpelling );
            printStream.print( "\t" );
            printStream.print( unhyphenatedCount );
            printStream.print( "\t" );
            printStream.print( hyphenatedCount );
            printStream.println();

            correctedSpellings.put( token , correctedSpelling );
        }

        return correctedSpellings;
    }

    /** Get word count for a word.
     *
     *  @param  word    The word for which to get the count.
     *
     *  @return         The word count.
     */

    protected static int getWordCount( String word )
    {
        int result      = 0;

        Number count    = wordCounts.get( word );

        if ( count != null )
        {
            result  = count.intValue();
        }

        return result;
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
            "Processed " +
            Formatters.formatIntegerWithCommas
            (
                wordsProcessed
            ) +
            " words in " +
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



