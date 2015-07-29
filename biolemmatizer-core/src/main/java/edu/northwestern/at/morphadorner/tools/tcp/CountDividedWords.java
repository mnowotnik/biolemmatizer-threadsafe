package edu.northwestern.at.morphadorner.tools.tcp;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;
import java.util.regex.*;

import edu.northwestern.at.morphadorner.tools.*;
import edu.northwestern.at.morphadorner.tools.compareadornedfiles.*;
import edu.northwestern.at.utils.*;

/** Count words containing divider characters.
 */

public class CountDividedWords
{
    /** # params before input file specs. */

    protected static final int INITPARAMS   = 2;

    /** Number of documents to process. */

    protected static int filesToProcess     = 0;

    /** Current document. */

    protected static int currentFileNumber  = 0;

    /** Total words found. */

    protected static int totalWords         = 0;

    /** Divided words file name. */

    protected static String dividedWordsFileName    = null;

    /** Words and counts file name. */

    protected static String wordsAndCountsFileName  = null;

    /** Wrapper for printStream to allow utf-8 output. */

    protected static PrintStream printStream;

    /** Tokens and counts. */

    protected static Map<String, Number> wordsAndCounts =
        MapFactory.createNewMap();

    /** Tokens containing break marker. */

    protected static Set<String> dividedWords   =
        SetFactory.createNewSet();

    /** Pattern to match partial word's word ID. */

    protected static Matcher partWordMatcher    =
        Pattern.compile( "\\.(\\d)$" ).matcher( "" );

    /** Main program.
     *
     *  @param  args    Program parameters.
     */

    public static void main( String[] args )
    {
                                //  Initialize.

        int filesProcessed  = 0;
        long processingTime = 0;

        try
        {
            if ( !initialize( args ) )
            {
                System.exit( 1 );
            }
                                //  Process all files.

            long startTime      = System.currentTimeMillis();

            filesProcessed      = processFiles( args );

            processingTime  =
                ( System.currentTimeMillis() - startTime + 999 ) / 1000;
        }
        catch ( Exception e )
        {
            e.printStackTrace();

            System.exit( 1 );
        }
                                //  Terminate.

        terminate( filesProcessed , processingTime );
    }

    /** Initialize.
     */

    protected static boolean initialize( String[] args )
        throws Exception
    {
                                //  Get the file to check for non-standard
                                //  spellings.

        if ( args.length < ( INITPARAMS + 1 ) )
        {
            System.err.println( "Not enough parameters." );
            return false;
        }

        dividedWordsFileName    = args[ 0 ];
        wordsAndCountsFileName  = args[ 1 ];

        return true;
    }

    /** Process one file.
     *
     *  @param  xmlFileName     XML input file name.
     */

    protected static void processOneFile( String xmlFileName )
    {
                                //  Increment count of documents
                                //  processed.
        currentFileNumber++;

        System.out.println(
            "Processing " + xmlFileName + " (" + currentFileNumber +
            "/" + filesToProcess + ")" );

                                //  Load words from next document.

        AdornedWordsLoader xmlReader    = null;

        try
        {
            xmlReader   = new AdornedWordsLoader( xmlFileName );
        }
        catch ( Exception e )
        {
            e.printStackTrace();

            System.out.println
            (
                "   *** Processing of " + xmlFileName + " failed."
            );

            return;
        }
                                //  Get list of word IDs.

        List<String> idList = xmlReader.getAdornedWordIDs();

                                //  Total number of word IDs.

        totalWords  += idList.size();

                                //  Loop over all word IDs.

        for ( int wordOrd = 0 ; wordOrd < idList.size() ; wordOrd++ )
        {
                                //  Get next word's information.

            String id   = idList.get( wordOrd );

            AdornedWordData w       =
                xmlReader.getAdornedWordData( id );

                                //  Only need information from
                                //  last part of a multipart word.

            if ( !isFirstWordPart( id ) ) continue;

                                //  Get word text.  May need to
                                //  join text across multiple word parts.

            String wordText = getWordText( xmlReader , id );

                                //  Skip punctuation and symbols.

            if  (   CharUtils.isPunctuationOrSymbol( wordText ) ||
                    id.endsWith( "-eos" )
                )
            {
                continue;
            }
                                //  Replace divider with vertical bar.

            String token    =
                StringUtils.replaceAll
                (
                    wordText ,
                    CharUtils.NONBREAKING_HYPHEN_STRING ,
                    "|"
                );

            token   =
                StringUtils.replaceAll
                (
                    token ,
                    CharUtils.NONBREAKING_HYPHEN_STRING +
                        CharUtils.NONBREAKING_HYPHEN_STRING ,
                    "|"
                );
                                //  Replace double vertical bar with
                                //  single occurrence.
            token   =
                StringUtils.replaceAll
                (
                    token ,
                    "||" ,
                    "|"
                );

            if ( token.length() == 0 )
            {
                System.out.println( "   Empty word at " + id );
            }
                                //  Increment token counts.

            CountMapUtils.updateWordCountMap
            (
                token ,
                1 ,
                wordsAndCounts
            );
                                //  Increment divided words count.

            if ( token.indexOf( "|" ) >= 0 )
            {
                dividedWords.add( token );
            }
        }
    }

    /** Is word first part of split word?
     *
     *  @param  wordID  Word ID of possibly split word part.
     *
     *  @return         True if word part first part of split word or
     *                  only part of unsplit word.
     */

    protected static boolean isFirstWordPart( String wordID )
    {
        boolean result  = true ;

        partWordMatcher.reset( wordID );

        if ( partWordMatcher.find() )
        {
            String wordPart = partWordMatcher.group( 1 );

            result  = wordPart.equals( "1" );
        }

        return result;
    }

    /** Get word text.
     *
     *  @param  wordID  Word ID of possibly split word.
     *
     *  @return         Word text.  Word parts are joined together
     *                  if the word is split
     */

    protected static String getWordText
    (
        AdornedWordsLoader adornedWordsLoader ,
        String wordID
    )
    {
        List<String> wordPartIDs    =
            getWordPartIDs( adornedWordsLoader , wordID );

        String result   = "";

        for ( int i = 0 ; i < wordPartIDs.size() ; i++ )
        {
            result  +=
                adornedWordsLoader.getAdornedWordData
                (
                    wordPartIDs.get( i )
                ).getWordText();
        }

        return result;
    }

    /** Get all word IDs for a split word.
     *
     *  @param  wordID  Word ID of possibly split word.
     *
     *  @return         String list of word IDs for all parts of split
     *                  word.
     *
     *  <p>
     *  If the word is not split, the result list contains the
     *  single word ID specified by the input value wordID.
     *  </p>
     */

    protected static List<String> getWordPartIDs
    (
        AdornedWordsLoader adornedWordsLoader ,
        String wordID
    )
    {
        List<String> result = ListFactory.createNewList();

        partWordMatcher.reset( wordID );

        if ( partWordMatcher.find() )
        {
            int dotIndex    = wordID.lastIndexOf( "." );

            String wordIDBase   = wordID.substring( 0 , dotIndex );

            for ( int i = 1 ; i < 101 ; i++ )
            {
                String wordPart = wordIDBase + "." + i;

                if ( adornedWordsLoader.getAdornedWordData( wordPart ) != null )
                {
                    result.add( wordPart );
                }
                else
                {
                    break;
                }
            }
        }
        else
        {
            result.add( wordID );
        }

        return result;
    }

    /** Process files.
     */

    protected static int processFiles( String[] args )
        throws Exception
    {
        int result  = 0;
                                //  Get file name/file wildcard specs.

        String[] wildCards  = new String[ args.length - INITPARAMS ];

        for ( int i = INITPARAMS ; i < args.length ; i++ )
        {
            wildCards[ i - INITPARAMS ] = args[ i ];
        }
                                //  Expand wildcards to list of
                                //  file names,
        String[] fileNames  =
            FileNameUtils.expandFileNameWildcards( wildCards );

        filesToProcess      = fileNames.length;

                                //  Process each file.

        for ( int i = 0 ; i < fileNames.length ; i++ )
        {
            processOneFile( fileNames[ i ] );
        }
                                //  Output results.
        SetUtils.saveSortedSet
        (
            dividedWords ,
            dividedWordsFileName ,
            "utf-8"
        );

        MapUtils.saveSortedMap
        (
            wordsAndCounts ,
            wordsAndCountsFileName ,
            "\t" ,
            "" ,
            "utf-8"
        );
                                //  Return count of files processed.

        return fileNames.length;
    }

    /** Terminate.
     *
     *  @param  filesProcessed  Number of files processed.
     *  @param  processingTime  Processing time in seconds.
     */

    protected static void terminate
    (
        int filesProcessed ,
        long processingTime
    )
    {
                                //  Display number of words processed.
        System.out.println
        (
            "Processed " +
            Formatters.formatLongWithCommas
            (
                totalWords
            ) +
            StringUtils.pluralize
            (
                totalWords ,
                " word in " ,
                " words in "
            ) +
            Formatters.formatIntegerWithCommas
            (
                filesProcessed
            ) +
            StringUtils.pluralize
            (
                filesProcessed ,
                " file in " ,
                " files in "
            ) +
            Formatters.formatLongWithCommas
            (
                processingTime
            ) +
            StringUtils.pluralize
            (
                processingTime ,
                " second." ,
                " seconds."
            )
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



