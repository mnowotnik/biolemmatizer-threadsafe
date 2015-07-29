package edu.northwestern.at.morphadorner.tools.compareadornedfiles;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;

import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.XStream;

import edu.northwestern.at.morphadorner.tools.*;
import edu.northwestern.at.utils.*;

/** Update word elements in an adorned file using a change log.
 *  <p>
 *  Usage:
 *  </p>
 *
 *  <pre>
 *  java edu.northwestern.at.morphadorner.tools.compareadornedfiles.UpdateAdornedFile operation oldadorned.xml changelog.xml newadorned.xml
 *  </pre>
 *
 *  <p>
 *  operation       --  Update operation, either "update" to apply<br />
 *                      updates in changelog.xml to srcadorned.xml to<br />
 *                      produce destadorned.xml, or<br />
 *                      "revert" to undo updates in changelog.xml to<br />
 *                      srcadorned.xml to produce destadorned.xml .<br />
 *  srcadorned.xml  --  Source adorned file.<br />
 *  changelog.xml   --  Word differences between two adorned files.<br />
 *  destadorned.xml --  Destination adorned file.
 *  </p>
 */

public class UpdateAdornedFile
{
    /** Create UpdateAdornedFile object.
     *
     *  @param  revert                  true to revert changes, false to
     *                                  apply changes.
     *  @param  srcAdornedFileName      Source adorned file to update/revert.
     *  @param  changeLogFileName       Change log file.
     *  @param  destAdornedFileName     Destination adorned file.
     *  @param  printStream             Status output file stream.
     *                                  May be null to suppress output.
     *
     *  @throws Exception               If any error occurs.
     */

    public UpdateAdornedFile
    (
        boolean revert ,
        String srcAdornedFileName ,
        String changeLogFileName ,
        String destAdornedFileName ,
        PrintStream printStream
    )
        throws Exception
    {
        long startTime  = System.currentTimeMillis();

                                //  Load source adorned file to update/revert.

        MutableAdornedFile mutableAdornedFile   =
            new MutableAdornedFile( srcAdornedFileName );

                                //  Load change log.

        XStream xstream = new XStream( new DomDriver() );

        xstream.alias( "change" , WordChange.class );
        xstream.alias( "ChangeLog" , WordChangeLog.class );

        String changeLogText    =
            FileUtils.readTextFile( changeLogFileName , "utf-8" );

        WordChangeLog changeLog =
            (WordChangeLog)xstream.fromXML( changeLogText );

        long endTime    =
            ( System.currentTimeMillis() - startTime + 999 ) / 1000;

                                //  Extract word ID list from source
                                //  adorned file.

        List<String> wordIDs    = mutableAdornedFile.getAdornedWordIDs();

                                //  Report count of words read in source.

        if ( printStream != null )
        {
            printStream.println
            (
                "Read " +
                Formatters.formatIntegerWithCommas( wordIDs.size() ) +
                " words from " + srcAdornedFileName + "."
            );
                                //  Report count of changes to apply.

            printStream.println
            (
                "Read " +
                Formatters.formatIntegerWithCommas
                (
                    changeLog.getNumberOfChanges()
                ) +
                " changes from " + changeLogFileName + "."
            );
        }

        startTime   = System.currentTimeMillis();

                                //  Apply changes.
        if ( revert )
        {
            mutableAdornedFile.revertChanges( changeLog.getChanges() );
        }
        else
        {
            mutableAdornedFile.applyChanges( changeLog.getChanges() );
        }

        endTime = ( System.currentTimeMillis() - startTime + 999 ) / 1000;

        if ( printStream != null )
        {
            printStream.println
            (
                "Changes applied in " +
                Formatters.formatLongWithCommas( endTime ) +
                ( ( endTime == 1 ) ? " second." : "seconds." )
            );
        }
                                //  Output updated adorned file.

        new AdornedXMLWriter
        (
            mutableAdornedFile.getDocument() ,
            destAdornedFileName
        );
    }

    /** Main program. */

    public static void main( String[] args )
    {
        try
        {
                                //  Allow utf-8 output standard output.

            PrintStream printStream     =
                new PrintStream
                (
                    new BufferedOutputStream( System.out ) ,
                    true ,
                    "utf-8"
                );
                                //  Make sure we have enough
                                //  parameters.  Display help and
                                //  quit if not.

            if ( args.length < 4 )
            {
                displayHelp( printStream );

                System.exit( 1 );
            }
                                //  Get operation.  Must be either
                                //  "update" or "revert".

            String operation    = args[ 0 ].toLowerCase();

            boolean revert  = false;

            if ( operation.equals( "revert" ) )
            {
                revert  = true;
            }
            else if ( operation.equals( "update" ) )
            {
            }
            else
            {
                printStream.println
                (
                    "Operation must be either \"update\" or \"revert\"."
                );

                System.exit( 1 );
            }
                                //  Perform update/revert.

            new UpdateAdornedFile(
                revert , args[ 1 ] , args[ 2 ] , args[ 3 ] , printStream );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /** Display usage/help.
     *
     *  @param  printStream     Output stream on which to display help.
     *                          May be null to suppress output.
     */

    public static void displayHelp( PrintStream printStream )
    {
        if ( printStream == null )
        {
            return;
        }

        printStream.println
        (
            "java edu.northwestern.at.morphadorner.tools.compareadornedfiles.UpdateAdornedFile operation srcadorned.xml changelog.xml destadorned.xml"
        );

        printStream.println();

        printStream.println
        (
            "operation -- Update operation, either \"update\" to apply 4pdates"
        );

        printStream.println
        (
            "in changelog.xml to srcadorned.xml to produce destadorned.xml,"
        );

        printStream.println
        (
            "or \"revert\" to undo updates in changelog.xml to srcadorned.xml"

        );

        printStream.println
        (
            " to produce destadorned.xml ."
        );

        printStream.println();

        printStream.println
        (
            "srcadorned.xml -- Source adorned file."
        );

        printStream.println();

        printStream.println
        (
            "changelog.xml -- Word differences between two adorned files."
        );

        printStream.println();

        printStream.println
        (
            "destadorned.xml -- Destination adorned file."
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



