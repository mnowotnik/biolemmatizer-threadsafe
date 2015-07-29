package edu.northwestern.at.morphadorner.tools.applyxslt;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import edu.northwestern.at.utils.*;

/** Apply XSLT transformation to a batch of input files.
 *
 *  <p>
 *  Usage:
 *  </p>
 *  <blockquote>
 *  <pre>
 *  java edu.northwestern.at.morphadorner.tools.applyxslt.ApplyXSLT outputdirectory script.xsl input1.xml input2.xml ...
 *  </pre>
 *  </blockquote>
 *  <table>
 *  <tr>
 *  <td>outputdirectory</td>
 *  <td>Output directory for files processed by applying the XSLT
 *      script to the input files.
 *  </td>
 *  <tr>
 *  <td>script.xsl</td><td>XSLT script file.</td>
 *  </tr>
 *  <tr>
 *  <td>input1.xml input2.xml ... </td>
 *  <td>Input xml files.</td>
 *  </tr>
 *  </table>
 */

public class ApplyXSLT
{
    /** Templates for transformer. */

    protected static Templates templates;

    /** Input source. */

    protected static Source inputXML;

    /** Result. */

    protected static Result result;

    /** Main program. */

    public static void main( String[] args )
    {
        try
        {
            applyXSLT( args );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /** Apply XSLT transformation to files. */

    public static void applyXSLT( final String[] args )
        throws Exception
    {
        FileBatchProcessor processor    = new FileBatchProcessor()
        {
            /** Transform one input file using the XSL script.
             *
             *  @param  inputFileName   Input XML file name to transform.
             */

            public void processOneFile( String inputFileName )
                throws Exception
            {
                                //  Set up input file for transformation.

                inputXML    = new StreamSource( inputFileName );

                                //  Get output file name for transformed
                                //  XML.

                String outputFileName   =
                    new File
                    (
                        outputDirectoryName ,
                        new File( inputFileName ).getName()
                    ).toString();

                                //  Skip processing if output file
                                //  exists.

                if ( FileNameUtils.fileExists( outputFileName ) )
                {
                    printStream.println( "Skipping " + inputFileName );
                }
                else
                {
                    printStream.println( "Processing " + inputFileName );

                    outputFileName  =
                        new File( outputFileName ).toURI().toURL().toString();

                    try
                    {
                                //  Get a clone of the compiled transformer.

                        Transformer transformer = templates.newTransformer();

                                //  Indent the output.

                        transformer.setOutputProperty
                        (
                            OutputKeys.INDENT ,
                            "yes"
                        );
                                //  Set up the output file.

                        result = new StreamResult( outputFileName );

                                //  Transform input to output using
                                //  the compiled XSLT style sheet.

                        transformer.transform( inputXML , result );
                    }
                    catch ( Exception e )
                    {
                        printStream.println
                        (
                            "   --- Error: " + inputFileName +
                            ": " + e.getMessage()
                        );
                    }
                }
            }
        };
                                //  Set the output directory for the
                                //  transformed files.

        processor.setOutputDirectoryName( args[ 0 ] );

                                //  Set input file names.

        processor.setInputFileNames( args , 2 );

                                //  Get a transformer factory.

        TransformerFactory factory  = TransformerFactory.newInstance();

                                //  Compile the XSLT input file.

        Source xslSource    = new StreamSource( args[ 1 ] );

        templates   = factory.newTemplates( xslSource );

                                //  Transform all the input files
                                //  using the compiled style sheet.
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



