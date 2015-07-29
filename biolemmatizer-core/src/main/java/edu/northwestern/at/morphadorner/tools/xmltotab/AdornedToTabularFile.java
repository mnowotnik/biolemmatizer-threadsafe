package edu.northwestern.at.morphadorner.tools.xmltotab;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;

import edu.northwestern.at.morphadorner.*;
import edu.northwestern.at.morphadorner.tools.*;
import edu.northwestern.at.morphadorner.tools.xmltotab.*;
import edu.northwestern.at.utils.*;

/** Convert adorned files to verticalized (tabular) format.
 *
 *  <p>
 *  Usage:
 *  </p>
 *
 *  <p>
 *  java edu.northwestern.at.morphadorner.tools.xmltotab.AdornedToTabularFile
 *      outputdirectory adorned1.xml adorned2.xml ...
 *  </p>
 *
 *  <ul>
 *  <li>outputdirectory         --  Output directory for tabular files.
 *  </li>
 *  <li>adorned.xml ...          -- List of input MorphAdorned XML files.
 *  </li>
 *  </ul>
 */

public class AdornedToTabularFile
{
    /** Main program.
     *
     *  @param  args    Command line arguments.
     */

    public static void main( String[] args )
    {
        try
        {
            adornedToTabularFile( args );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /** Get tabular output for batch of adorned files.
     *
     *  @param  args    Command line arguments.
     */

    public static void adornedToTabularFile( String[] args )
        throws Exception
    {
        FileBatchProcessor processor    =
            new FileBatchProcessor()
        {
            public void processOneFile( String inputFileName )
                throws Exception
            {
                                //  Report name of file being converted.

                printStream.println( "Processing " + inputFileName );

                                //  Strip path from input file name.

                String strippedFileName =
                    FileNameUtils.stripPathName( inputFileName );

                strippedFileName    =
                    FileNameUtils.changeFileExtension( strippedFileName , "" );

                                //  Create output file name.

                String outputFileName   =
                    new File
                    (
                        outputDirectoryName ,
                        strippedFileName + ".tab"
                    ).getAbsolutePath();

                FileUtils.createPathForFile( outputFileName );

                                //  Convert adorned file to tabular
                                //  format.
                new XMLToTab
                (
                    new String[]
                    {
                        inputFileName ,
                        outputFileName
                    }
                );
            }
        };

        processor.setOutputDirectoryName( args[ 0 ] );
        processor.setInputFileNames( args , 1 );
        processor.run();
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



