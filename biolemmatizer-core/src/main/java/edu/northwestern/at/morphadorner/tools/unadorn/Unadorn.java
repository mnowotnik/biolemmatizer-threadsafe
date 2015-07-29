package edu.northwestern.at.morphadorner.tools.unadorn;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.text.*;
import java.util.*;

import org.jdom2.*;
import org.jdom2.input.*;
import org.jdom2.filter.*;
import org.jdom2.output.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import edu.northwestern.at.morphadorner.*;
import edu.northwestern.at.morphadorner.tools.*;

import edu.northwestern.at.utils.*;

/** Unadorn removes word level adornments from adorned files.
 *
 *  <p>
 *  Usage:
 *  </p>
 *
 *  <p>
 *  java edu.northwestern.at.morphadorner.tools.unadorn.Unadorn
 *      outputdirectory adorned1.xml adorned2.xml ...
 *  </p>
 *
 *  <ul>
 *  <li>outputdirectory     -- Output directory for unadorned XML files.
 *  </li>
 *  <li>adorned*.xml ...    -- List of input MorphAdorned XML files.
 *  </li>
 *  </ul>
 *
 *  <p>
 *  Unadorn replaces &lt;w&gt;, &lt;pc&gt;, and &lt;c&gt; elements with
 *  their text contents.
 *  </p>
 */

public class Unadorn
{
    /** # params before input file specs. */

    protected static final int INITPARAMS   = 1;

    /** Number of documents to process. */

    protected static int docsToProcess      = 0;

    /** Current document. */

    protected static int currentDocNumber   = 0;

    /** Output directory name. */

    protected static String outputDirectoryName = "";

    /** Main program.
     *
     *  \u0040param args    Program parameters.
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
                                //  Get the file to check for non-standard
                                //  spellings.

        if ( args.length < ( INITPARAMS + 1  ) )
        {
            System.err.println( "Not enough parameters." );
            return false;
        }
                                //  Get the output directory name.

        outputDirectoryName = args[ 0 ];

        return true;
    }

    /** Process one file.
     *
     *  \u0040param xmlFileName     Input file name to check for
     *                                  part of speech/lemma mismatches..
     */

    protected static void processOneFile( String xmlFileName )
    {
        try
        {
                                //  Report document being processed.

            System.out.println
            (
                "(" + ++currentDocNumber + "/" + docsToProcess + ") " +
                "processing " + xmlFileName
            );
                                //  Create filter to strip <w> and <c>
                                //  elements.

            XMLFilter filter    =
                new StripAllWordElementsFilter
                (
                    XMLReaderFactory.createXMLReader()
                );
                                //  Strip path from input file name.

            String strippedFileName =
                FileNameUtils.stripPathName( xmlFileName );

            strippedFileName    =
                FileNameUtils.changeFileExtension( strippedFileName , "" );

                                //  Generate output file name.

            String xmlOutputFileName    =
                new File
                (
                    outputDirectoryName ,
                    strippedFileName + ".xml"
                ).getAbsolutePath();

                                //  Make sure output directory exists.

            FileUtils.createPathForFile( xmlOutputFileName );

                                //  Copy input xml to output xml,
                                //  stripping <w> and <c> elements.

            new FilterAdornedFile
            (
                xmlFileName ,
                xmlOutputFileName ,
                filter
            );
                                //  Read it back and fix spacing.

            String fixedXML =
                FileUtils.readTextFile
                (
                    xmlOutputFileName ,
                    "utf-8"
                );

            fixedXML    =
                fixedXML.replaceAll
                (
                    "(\\s+)" ,
                    " "
                );

            fixedXML    =
                fixedXML.replaceAll
                (
                    " ([\\.?!,;:\\)])" ,
                    "\u00241"
                );

            fixedXML    =
                fixedXML.replaceAll
                (
                    "\\( " ,
                    "("
                );

            fixedXML    =
                fixedXML.replaceAll
                (
                    "\u00b6 " ,
                    "\u00b6"
                );

            fixedXML    =
                fixedXML.replaceAll
                (
                    "__NS1:" ,
                    ""
                );

            fixedXML    =
                fixedXML.replaceAll
                (
                    "__NS1" ,
                    ""
                );
/*
            fixedXML    =
                fixedXML.replaceAll
                (
                    "</__NS1:" ,
                    ""
                );
*/
                                //  Emit unadorned XML.

            SAXBuilder builder = new SAXBuilder();

            Document document   =
                builder.build
                (
                    new StringReader( fixedXML )
                );

            new AdornedXMLWriter( document , xmlOutputFileName );
        }
        catch ( Exception e )
        {
            System.out.println( "   Error: " + e.getMessage() );

            e.printStackTrace();
        }
    }

    /** Process files.
     */

    protected static int processFiles( String[] args )
    {
        int result  = 0;
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

        docsToProcess       = fileNames.length;

        System.out.println
        (
            "There are " +
            Formatters.formatIntegerWithCommas
            (
                docsToProcess
            ) +
            " documents to process."
        );
                                //  Process each file.

        for ( int i = 0 ; i < fileNames.length ; i++ )
        {
            processOneFile( fileNames[ i ] );
        }

        return fileNames.length;
    }

    /** Terminate.
     *
     *  \u0040param filesProcessed  Number of files processed.
     *  \u0040param processingTime  Processing time in seconds.
     */

    protected static void terminate
    (
        int filesProcessed ,
        long processingTime
    )
    {
        System.out.println
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



