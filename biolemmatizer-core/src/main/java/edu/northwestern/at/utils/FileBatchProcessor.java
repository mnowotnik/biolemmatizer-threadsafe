package edu.northwestern.at.utils;

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

/** Process batch of files.
 */

abstract public class FileBatchProcessor
{
    /** # params before input file specs. */

    protected int initParams    = 1;

    /** Number of documents to process. */

    protected int filesToProcess        = 0;

    /** Current document. */

    protected int currentFileNumber = 0;

    /** Output directory name. */

    protected String outputDirectoryName    = "";

    /** Input file names.  May contain wildcards. */

    protected String[] fileNames    = null;

    /** Wrapper for printStream to allow utf-8 output. */

    protected PrintStream printStream;

    /** Create file batch processor.
     */

    public FileBatchProcessor()
        throws Exception
    {
        initialize();
    }

    /** Run file batch processor.
     */

    public void run()
    {
                                //  Initialize.
        try
        {
                                //  Process all files.

            long startTime      = System.currentTimeMillis();

            int filesProcessed  = processFiles();

            long processingTime =
                ( System.currentTimeMillis() - startTime + 999 ) / 1000;

                                //  Terminate.

            terminate( filesProcessed , processingTime );
        }
        catch ( Exception e )
        {
            printStream.println( e.getMessage() );
        }
    }

    /** Initialize.
     */

    public boolean initialize()
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

        return true;
    }

    /** Process one input file.
     *
     *  @param  inputFileName   Input file name.
     */

    abstract public void processOneFile( String inputFileName )
        throws Exception;

    /** Process files. */

    public int processFiles()
        throws Exception
    {
        int result  = 0;
                                //  If file names not set, do nothing.

        if ( fileNames != null )
        {
                                //  Number of files to process.

            filesToProcess      = fileNames.length;

                                //  Process each file.

            for ( int i = 0 ; i < fileNames.length ; i++ )
            {
                processOneFile( fileNames[ i ] );
            }
        }

        return fileNames.length;
    }

    /** Terminate.
     *
     *  @param  filesProcessed  Number of files processed.
     *  @param  processingTime  Processing time in seconds.
     */

    public void terminate
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

    /** Set output directory name.
     *
     *  @param  outputDirectoryName     Output directory name.
     */

    public void setOutputDirectoryName( String outputDirectoryName )
    {
        this.outputDirectoryName    = outputDirectoryName;
    }

    /** Get output directory name.
     *
     *  @return     Output directory name.
     */

    public String getOutputDirectoryName()
    {
        return outputDirectoryName;
    }

    /** Set input file names.
     *
     *  @param  inputFileNames          String array of input file names.
     *                                  May contain wildcards.
     *
     *  @param  startIndex              Position in inputFileNames at
     *                                  which file names start.
     */

    public void setInputFileNames
    (
        String[] inputFileNames ,
        int startIndex
    )
    {
                                //  Get file name/file wildcard specs.

        String[] wildcards  =
            new String[ inputFileNames.length - startIndex ];

        for ( int i = startIndex ; i < inputFileNames.length ; i++ )
        {
            wildcards[ i - startIndex ] = inputFileNames[ i ];
        }
                                //  Expand wildcards to list of
                                //  file names.
        this.fileNames  =
            FileNameUtils.expandFileNameWildcards( wildcards );
    }

    /** Get number of files to process.
     *
     *  @return     Number of files to process.
     */

    public int getNumberOfFilesToProcess()
    {
        return filesToProcess;
    }

    /** Get index of file currently being processed.
     *
     *  @return     Index of file currently being processed.
     */

    public int getCurrentFileNumber()
    {
        return currentFileNumber;
    }

    /** Set index of file currently being processed.
     *
     *  @param  currentFileNumber   Index of file currently being processed.
     */

    public void setCurrentFileNumber( int currentFileNumber )
    {
        this.currentFileNumber  = currentFileNumber;
    }

    /** Increment index of file currently being processed.
     */

    public void incrementCurrentFileNumber()
    {
        this.currentFileNumber++;
    }

    /** Get print stream.
     *
     *  @return     Print stream.
     */

    public PrintStream getPrintStream()
    {
        return printStream;
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


