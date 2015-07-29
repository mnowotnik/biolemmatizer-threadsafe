package edu.northwestern.at.morphadorner.tools.adornedtosketch;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.text.*;
import java.util.*;

import org.jdom2.*;
import org.jdom2.filter.*;
import org.jdom2.input.*;
import org.jdom2.output.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.xml.*;
import edu.northwestern.at.morphadorner.tei.*;
import edu.northwestern.at.morphadorner.tools.*;

/** Convert adorned file to input for Sketch or NoSketch engine.
 *
 *  <p>
 *  AdornedToSketch converts adorned TEI XML files to the verticalized
 *  format required as input to the Sketch or NoSketch corpus search engines.
 *  </p>
 *
 *  <p>
 *  Usage:
 *  </p>
 *
 *  <blockquote>
 *  <p>
 *  <code>
 *  java edu.northwestern.at.morphadorner.tools.adornedtosketch.AdornedToSketch sketchinput.txt corpusname adorned1.xml adorned2.xml ...
 *  </code>
 *  </p>
 *  </blockquote>
 *
 *  <p>
 *  where
 *  </p>
 *
 *  <ul>
 *  <li><strong>sketchinput.txt</strong> specifies the output filename of
 *  the verticalized representation required for input to the
 *  Sketch or NoSketch engines.
 *  </li>
 *  <li><strong>corpusname</strong> specifies the corpus name to be used
 *  when creating the Sketch engine input.
 *  </li>
 *  <li><strong>adorned1.xml adorned2.xml ...</strong> specifies the input
 *  MorphAdorned XML files from which to produce the Sketch engine
 *  input.
 *  </li>
 *  </ul>
 *
 *  <p>
 *  Known flaw: AdornedToSketch does not generate the "glue" elements which
 *  bind punctuation marks to word tokens.
 *  Searching the corpus still works fine in the Sketch or NoSketch engine,
 *  but the punctuation marks are displayed detached from any token to which
 *  they would normally be attached.
 *  </p>
 */

public class AdornedToSketch
{
    /** Number of documents to process. */

    protected static int docsToProcess      = 0;

    /** Current document. */

    protected static int currentDocNumber   = 0;

    /** Input directory. */

    protected static String inputDirectory;

    /** Output file name. */

    protected static String outputFile;

    /** Output file stream. */

    protected static PrintStream outputFileStream;

    /** Wrapper for printStream to allow utf-8 output. */

    protected static PrintStream printStream;

    /** Corpus name. */

    protected static String corpusName;

    /** # params before input file specs. */

    protected static final int INITPARAMS   = 2;

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
                                //  Process all files.

            long startTime      = System.currentTimeMillis();

            int filesProcessed  = processFiles( args );

            long processingTime =
                ( System.currentTimeMillis() - startTime + 999 ) / 1000;

                                //  Terminate.

            terminate( filesProcessed , processingTime );
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
        }
    }

    /** Initialize.
     */

    protected static boolean initialize( String[] args )
        throws Exception
    {
                                //  Allow utf-8 output to printStream .
        printStream =
            new PrintStream
            (
                new BufferedOutputStream( System.out ) ,
                true ,
                "utf-8"
            );
                                //  Get the file to check for non-standard
                                //  spellings.

        if ( args.length < ( INITPARAMS + 1  ) )
        {
            System.err.println( "Not enough parameters." );
            return false;
        }
                                //  Get output file name.

        outputFile  = args[ 0 ];

                                //  Get corpus name.

        corpusName  = args[ 1 ];

        return true;
    }

    /** Split word path into separate tags.
     *
     *  @param  path    The word path.
     *
     *  @return         String array of split XML tags.
     *                  The trailing "w[]" element is removed.
     */

    protected static String[] splitPathFull( String path )
    {
                                //  Split path into tags at backslashes.

        String[] tags   = path.split( "\\\\" );

        String[] result = new String[ tags.length - 1 ];

        int j   = 0;
                                //  Skip the trailing word tag.

        for ( int i = 0 ; i < tags.length - 1 ; i++ )
        {
            result[ j++ ]   = tags[ i ];
        }

        return result;
    }

    /** Split word path into separate tags.
     *
     *  @param  path    The word path.
     *
     *  @return         String array of split XML tags.
     *                  Both the leading document name tag and
     *                  the trailing word (w[]) tag are removed.
     */

    protected static String[] splitPath( String path )
    {
                                //  Get split tags with leading
                                //  document name included.

        String[] tags   = splitPathFull( path );

                                //  Now remove the leading slash and
                                //  document name.

        String[] result = new String[ tags.length - 2 ];

        int j   = 0;
                                //  Skip the leading slash and
                                //  work name, as well as the trailing
                                //  word tag.

        for ( int i = 2 ; i < tags.length ; i++ )
        {
            result[ j++ ]   = tags[ i ];
        }

        return result;
    }

    /** Process one file.
     *
     *  @param  xmlFileName     Adorned XML file name to reformat for CWB.
     */

    protected static void processOneFile( String xmlFileName )
    {
        String outputXmlFileName    = "";

        try
        {
                                //  Strip path from file name.

            String shortInputXmlFileName    =
                FileNameUtils.stripPathName( xmlFileName );

                                //  Load bibliographic information.

            BibadornedInfo bibadornedInfo   =
                new BibadornedInfo( xmlFileName );

                                //  Remember if Monk header found.

            boolean monkHeaderFound = bibadornedInfo.getMonkHeaderFound();

                                //  Get standard TEI header information.

            TEIHeaderInfo teiadornedInfo    =
                new TEIHeaderInfo( xmlFileName );

                                //  Emit work tag.

            outputFileStream.print
            (
                "<work filename=\""
            );

            if ( monkHeaderFound )
            {
                outputFileStream.print
                (
                    bibadornedInfo.getFileName()
                );
            }
            else
            {
                outputFileStream.print
                (
                    teiadornedInfo.getFileName()
                );
            }

            outputFileStream.print
            (
                "\" title=\""
            );

            outputFileStream.print
            (
                teiadornedInfo.getTitle()
            );

            outputFileStream.print
            (
                "\" author=\""
            );

            List<TEIHeaderAuthor> authors;

            if ( monkHeaderFound )
            {
                authors = bibadornedInfo.getAuthors();
            }
            else
            {
                authors = teiadornedInfo.getAuthors();
            }

            boolean first   = true;

            Set<String> prevAuthors = SetFactory.createNewSet();

            for ( TEIHeaderAuthor author : authors )
            {
                String authorName   = author.getName();

                if ( !prevAuthors.contains( authorName ) )
                {
                    if ( !first )
                    {
                        outputFileStream.print( "|" );
                    }

                    outputFileStream.print( authorName );

                    first   = false;
                }

                prevAuthors.add( authorName );
            }

            outputFileStream.print
            (
                "\""
            );

            if ( monkHeaderFound )
            {
                outputFileStream.print
                (
                    " circulationYear=\""
                );

                outputFileStream.print
                (
                    bibadornedInfo.getCirculationYear()
                );

                outputFileStream.print
                (
                    "\" genre=\""
                );

                outputFileStream.print
                (
                    bibadornedInfo.getGenre()
                );

                outputFileStream.print
                (
                    "\" subgenre=\""
                );

                outputFileStream.print
                (
                    bibadornedInfo.getSubgenre()
                );

                outputFileStream.print
                (
                    "\""
                );
            }

            outputFileStream.print
            (
                ">"
            );

            outputFileStream.println();

                                //  Load words from input files.

            AdornedXMLReader xmlReader  =
                new AdornedXMLReader( xmlFileName );

                                //  Emit sentences.

            List<List<ExtendedAdornedWord>> sentences   =
                xmlReader.getSentences();

            for ( int i = 0 ; i < sentences.size() ; i++ )
            {
                outputFileStream.println
                (
                    "<s>"
                );

                List<ExtendedAdornedWord> sentence  =
                    sentences.get( i );

                for ( int j = 0 ; j < sentence.size() ; j++ )
                {
                    ExtendedAdornedWord word    = sentence.get( j );

                    outputFileStream.print( word.getSpelling() );
                    outputFileStream.print( "\t" );

                    outputFileStream.print( word.getPartsOfSpeech() );
                    outputFileStream.print( "\t" );

                    outputFileStream.print( word.getLemmata() );
                    outputFileStream.print( "\t" );

                    outputFileStream.print( word.getStandardSpelling() );
                    outputFileStream.print( "\t" );

                    outputFileStream.println( word.getID() );
                }

                outputFileStream.println
                (
                    "</s>"
                );
            }
                                //  Close work tag.

            outputFileStream.println
            (
                "</work>"
            );
                                //  Generate CWB input from words.

            printStream.println
            (
                "Processed " + xmlFileName
            );
        }
        catch ( Exception e )
        {
            printStream.println
            (
                "Problem converting " + xmlFileName + " to " +
                outputXmlFileName +
                ": " + e.getMessage()
            );
        }
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

        docsToProcess       = fileNames.length;

                                //  Open output file.
        outputFileStream    =
            new PrintStream
            (
                new BufferedOutputStream
                (
                    new FileOutputStream( outputFile )
                ) ,
                true ,
                "utf-8"
            );

        outputFileStream.println
        (
            "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>"
        );

        outputFileStream.print
        (
            "<corpus name=\""
        );

        outputFileStream.print
        (
            corpusName
        );

        outputFileStream.println
        (
            "\">"
        );
                                //  Process each file.

        for ( int i = 0 ; i < fileNames.length ; i++ )
        {
            processOneFile( fileNames[ i ] );
        }
                                //  Close corpus tag.

        outputFileStream.println
        (
            "</corpus>"
        );
                                //  Close output file.

        outputFileStream.close();

                                //  Return # of files processed.

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
        printStream.println
        (
            "Processed " +
            Formatters.formatIntegerWithCommas
            (
                filesProcessed
            ) +
            " files in " +
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



