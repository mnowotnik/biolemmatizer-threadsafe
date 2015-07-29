package edu.northwestern.at.morphadorner.tools.punktabbreviationdetector;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.text.*;
import java.util.*;

import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.*;

import edu.northwestern.at.utils.CharUtils;
import edu.northwestern.at.utils.FileNameUtils;
import edu.northwestern.at.utils.FileUtils;
import edu.northwestern.at.utils.SetUtils;
import edu.northwestern.at.utils.StringUtils;

/** Find abbreviations in a set of texts using Punkt algorithm.
 *
 *  <p>
 *  <code>
 *  java -Xmx512m edu.northwestern.at.morphadorner.tools.punktabbreviationdetector.PunktAbbreviationDetector
 *      isolangcode abbrevs.txt text1.txt text2.txt ...
 *  </code>
 *  </p>
 *
 *  <ul>
 *  <li>
 *  <strong>isolangcode</strong> specifies the two or three character
 *  ISO language code in which the texts to be analyzed are written.
 *  </li>
 *
 *  <li>
 *  <strong>abbrevs.txt</strong> specifies the name of the output file
 *  to receive the abbreviations extracted from the texts.
 *  </li>
 *
 *  <li>
 *  <strong>text1 text2 ... </strong> specify the names of utf-8 encoded
 *  text files from which to extract potential abbreviations.
 *  </li>
 *  </ul>
 */

public class PunktAbbreviationDetector
{
    /** # params before input file specs. */

    protected static final int INITPARAMS   = 2;

    /** Wrapper for printStream to allow utf-8 output. */

    protected static PrintStream printStream;

    /** Main program. */

    public static void main( String[] args )
        throws IOException
    {
        long startTime  = System.currentTimeMillis();

                                //  Allow utf-8 output to printStream .
        printStream =
            new PrintStream
            (
                new BufferedOutputStream( System.out ) ,
                true ,
                "utf-8"
            );
                                //  Pick up language of input texts.

        String languageCode = args[ 0 ];

        printStream.println( "Language code: " + languageCode );

        Locale locale   = languageCodeToLocale( languageCode );

        printStream.println( "Language: " + locale.getDisplayLanguage() );

                                //  Create tokenizer for specified
                                //  language.

        ICU4JBreakIteratorWordTokenizer tokenizer   =
            new ICU4JBreakIteratorWordTokenizer( locale );

                                //  Treat whitespace as a token.

        tokenizer.setStoreWhitespaceTokens( true );
        tokenizer.setMergeWhitespaceTokens( true );

                                //  Don't split token around periods.

        tokenizer.setSplitAroundPeriods( false );

                                //  Pick up name of abbreviations
                                //  output file.

        String abbrevsFileName  = args[ 1 ];

                                //  Create counter to hold count
                                //  information about tokens and periods.

        PunktTokenCounter tokenCounter =
            new PunktTokenCounter( 0.3D , false );

                                //  Get file name/file wildcard specs.

        String[] wildCards  = new String[ args.length - INITPARAMS ];

        for ( int i = INITPARAMS ; i < args.length ; i++ )
        {
            wildCards[ i - INITPARAMS ] = args[ i ];
        }
                                //  Expand wildcards to list of
                                //  file names.
        String[] fileNames  =
            FileNameUtils.expandFileNameWildcards( wildCards );

        printStream.println
        (
            "There are " +
            StringUtils.formatNumberWithCommas( fileNames.length ) +
            " files to process."
        );
                                //  No tokens read yet.
        long tokensRead = 0;
                                //  Process input files.

        for ( int i = 0 ; i < fileNames.length ; i++ )
        {
                                //  Read text from next input file.
            String text =
                FileUtils.readTextFile( fileNames[ i ] , "utf-8" );

                                //  Extract tokens from text.

            List<String> tokens = tokenizer.extractWords( text );

                                //  Process each token.

            for ( int j = 0 ; j < tokens.size() ; j++ )
            {
                tokenCounter.count( makePunktToken( tokens.get( j ) ) );

                tokensRead++;
            }
        }
                                //  Report processing completed.
        long processingTime =
            ( System.currentTimeMillis() - startTime + 999 ) / 1000;

        printStream.println
        (
            "\nProcessing completed in " +
            StringUtils.formatNumberWithCommas( processingTime ) +
            " seconds."
        );

        printStream.println
        (
            "\n" + StringUtils.formatNumberWithCommas( tokensRead ) +
            " tokens extracted."
        );

        printStream.println();

        printStream.println
        (
            "There were " +
            StringUtils.formatNumberWithCommas
            (
                tokenCounter.getCandidates().size()
            ) + " candidates."
        );

        printStream.println
        (
            "There are " +
            StringUtils.formatNumberWithCommas
            (
                tokenCounter.getAbbreviations().size()
            ) + " abbreviations."
        );
                                //  Save abbreviations to specified file.
        SetUtils.saveSet
        (
            new TreeSet<String>( tokenCounter.getAbbreviations() ) ,
            abbrevsFileName ,
            "utf-8"
        );
    }

    /** Get a Java Locale from an ISO language code.
     *
     *  @param  languageCode    The ISO language code.
     *
     *  @return                 The Java locale corresponding to
     *                          the ISO language code.
     */

    public static Locale languageCodeToLocale( String languageCode )
    {
//      return new Locale.Builder().setLanguage( languageCode ).build();
        return new Locale( languageCode );
    }

    /** Create Punkt token from a string.
     *
     *  @param  token   The token.
     */

    public static PunktToken makePunktToken( String token )
    {
                                //  Determine token type.

        char ch = token.charAt( 0 );

        if ( Character.isWhitespace( ch ) )
        {
            return new PunktToken( token , PunktTokenType.WHITESPACE );
        }

        else if ( Character.isDigit( ch ) )
        {
            return new PunktToken( token , PunktTokenType.NUMBER );
        }

        else if ( CharUtils.isPunctuationOrSymbol( token ) )
        {
            return new PunktToken( token , PunktTokenType.NONWORD );
        }

        else
        {
            return new PunktToken( token , PunktTokenType.WORD );
        }
    }

    /** Allow overrides but not instantiation. */

    protected PunktAbbreviationDetector()
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



