package edu.northwestern.at.morphadorner.tools.compareadornedfiles;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;

import edu.northwestern.at.utils.*;

/** Get changes in tokens from one set of adorned files to another.
 *
 *  <p>
 *  Usage:
 *  </p>
 *
 *  <p>
 *  java edu.northwestern.at.morphadorner.tools.compareadornedfiles.GetTokenChanges
 *      origfilesdirectory  updatedfilesdirectory changelogfilesdirectory<br />
 *  <br />
 *  origfiledirectory       -- Directory containing original TEI XML files.<br />
 *  updatedfilesdirectory   -- Directory containing updated TEI XML files.br />
 *  changelogfilesdirectory -- Directory to receive change log files.
 *  </p>
 *
 *  <p>
 *  For each pair of matching files in (origfiledirectory, updatedfilesdirectory)
 *  a change log file is written to changelogfilesdirectory.  The change log
 *  is written in the format specified by CompareAdornedFiles.  Only
 *  token-based changes are recorded in the change log file.
 *  </p>
 */

public class GetTokenChanges
{
    /** Input directory containing old adorned files. */

    protected static String oldAdornedFilesDirectory;

    /** Input directory containing new adorned files. */

    protected static String newAdornedFilesDirectory;

    /** Output directory to receive change files. */

    protected static String changeFilesDirectory;

    /** Main program.
     *
     *  @param  args    Command line arguments.
     */

    public static void main( String[] args )
    {
        try
        {
            getTokenChanges( args );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /** Get token changes for batch of adorned files.
     *
     *  @param  args    Command line arguments.
     */

    public static void getTokenChanges( String[] args )
        throws Exception
    {
                                //  Allow utf-8 output standard output.

        PrintStream printStream     =
            new PrintStream
            (
                new BufferedOutputStream( System.out ) ,
                true ,
                "utf-8"
            );
                                //  Make sure we have enough arguments.

        if ( args.length < 3 )
        {
            printStream.println( "Not enough parameters." );
            System.exit( 1 );
        }
                                //  Get old adorned files directory.

        oldAdornedFilesDirectory    = args[ 0 ];

                                //  Get updated adorned files directory.

        newAdornedFilesDirectory    = args[ 1 ];

                                //  Get output change files directory.

        changeFilesDirectory        = args[ 2 ];

                                //  Get list of old adorned files.

        String wildcard =
            oldAdornedFilesDirectory + File.separator + "*.xml";

        String[] oldAdornedFileNames    =
            FileNameUtils.expandFileNameWildcards
            (
                new String[]{ wildcard }
            );

        int nOld    = oldAdornedFileNames.length;

        printStream.println
        (
            "There are " +
            Formatters.formatIntegerWithCommas( nOld ) +
            StringUtils.pluralize( nOld , " file " , " files " ) +
            " to process."
        );
                                //  Get changes between each old file
                                //  and the corresponding new file.

        for ( int i = 0 ; i < oldAdornedFileNames.length ; i++ )
        {
                                //  Get next old adorned file name.

            String oldAdornedFileName   = oldAdornedFileNames[ i ] ;

            String strippedFileName =
                FileNameUtils.stripPathName( oldAdornedFileName );

                                //  Get corresponding updated  adorned
                                //  file name.

            String newAdornedFileName   =
                new File
                (
                    newAdornedFilesDirectory ,
                    strippedFileName
                ).getCanonicalPath();

                                //  Get name of output token change file.

            String changeFileName       =
                new File
                (
                    changeFilesDirectory ,
                    "changes-" + strippedFileName
                ).getCanonicalPath();

                                //  Compare the old and new adorned files
                                //  and generate the changes.

            new CompareAdornedFiles
            (
                oldAdornedFileName ,
                newAdornedFileName ,
                changeFileName ,
                printStream
            );
                                //  Suggest garbage collection.
            System.gc();
        }

        printStream.println( "Finished processing." );
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



