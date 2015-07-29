package edu.northwestern.at.morphadorner.tools.tcp;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;

import org.jdom2.*;
import org.jdom2.filter.*;
import org.jdom2.input.*;
import org.jdom2.output.*;
import org.jdom2.xpath.*;

import edu.northwestern.at.morphadorner.tools.*;
import edu.northwestern.at.morphadorner.tools.compareadornedfiles.*;
import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.xml.*;

/** AddUnclear adds type="unclear" attribute to tokens containing character gaps.
 *
 *  <p>
 *  Usage:
 *  </p>
 *  <blockquote>
 *  <pre>
 *  java edu.northwestern.at.morphadorner.tools.tcp.AddUnclear outputdirectory input1.xml input2.xml ...
 *  </pre>
 *  </blockquote>
 *  <p>
 *  where <strong>outputdirectory</strong> is the output directory containing
 *  the resultant XML files with type="unclear" attributes added to tokens
 *  containing character gaps and input*.xml are the input tokenized XML files.
 *  Character gaps are indicated by the presence of unicode character \u25CF
 *  (the black circle) in a token.
 *  </p>
 */

public class AddUnclear
{
    /** TEI name spaces. */

    protected static Namespace teiNamespace =
        Namespace.getNamespace
        (
            "http://www.tei-c.org/ns/1.0"
        );

    protected static Namespace teiNamespace2    =
        Namespace.getNamespace
        (
            "tei" ,
            "http://www.tei-c.org/ns/1.0"
        );

    /** Main program.
     *
     *  @param  args    Program arguments.
     */

    public static void main( String[] args )
    {
        try
        {
            addUnclear( args );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /** Add type="unclear" attribute to tokens containing gap characters.
     *
     *  @param  args    Program arguments.
     *
     *  @throws     Exception   In case of error.
     */

    public static void addUnclear( String[] args )
        throws Exception
    {
                                //  Create batch processor to handle
                                //  input files.

        FileBatchProcessor processor    =
            new FileBatchProcessor()
        {
            /** Process one input XML file.
             *
             *  @param  inputFileName   The XMl input file name.
             *
             *  @throws Exception       In case of error.
             */

            public void processOneFile( String inputFileName )
                throws Exception
            {
                                //  Load adorned/tokenized file.

                MutableAdornedFile mutableAdornedFile   =
                    new MutableAdornedFile( inputFileName );

                                //  Get list of token IDs.

                List<String> adornedWordIDs =
                    mutableAdornedFile.getAdornedWordIDs();

                                //  Get adorned document.

                Document document   = mutableAdornedFile.getDocument();

                                //  Process each token.

                for ( int i = 0 ; i < adornedWordIDs.size() ; i++ )
                {
                                //  Get next token.

                    Element adornedWord =
                        mutableAdornedFile.getAdornedWord
                        (
                            adornedWordIDs.get( i )
                        );

                                //  Get token text.

                    String wordText = adornedWord.getText();

                                //  If the token contains a gap
                                //  character ...

                    if ( wordText.indexOf( CharUtils.CHAR_GAP_MARKER_STRING ) >= 0 )
                    {
                                //  Get the parent element of the token.
                                //  If it is "<unclear>", do nothing.

                        Element parentElement   =
                            adornedWord.getParentElement();

                        if ( !parentElement.getName().equals( "unclear" ) )
                        {
                                //  Parent element was not "<unclear>".
                                //  Add type="unclear" to this token.

                            String type =
                                StringUtils.safeString
                                (
                                    adornedWord.getAttributeValue( "type" )
                                );

                            if ( type.length() == 0 )
                            {
/*
                                printStream.println
                                (
                                    inputFileName + "\t" +
                                    JDOMUtils.getAttributeValue
                                    (
                                        adornedWord ,
                                        "xml:id" ,
                                        false
                                    ) +
                                    "\t" +
                                    wordText
                                );
*/
                                adornedWord.setAttribute
                                (
                                    "type" ,
                                    "unclear"
                                );
                            }
                        }
                    }
                }
                                //  Strip path from input file name.

                String strippedFileName =
                    FileNameUtils.stripPathName( inputFileName );

                strippedFileName    =
                    FileNameUtils.changeFileExtension(
                        strippedFileName , "" );

                                //  Create output file name.

                String xmlOutputFileName    =
                    new File
                    (
                        outputDirectoryName ,
                        strippedFileName + ".xml"
                    ).getAbsolutePath();

                                //  Create output file path.

                FileUtils.createPathForFile( xmlOutputFileName );

                                //  Write updated XML file.

                AdornedXMLWriter writer =
                    new AdornedXMLWriter( document , xmlOutputFileName );
            }

            /** Create an element in the TEI name space.
             *
             *  @param  name    Element name.
            */

            public Element createElement( String name )
            {
                return new Element( name , teiNamespace );
            }
        };

                                //  Set output directory.

        processor.setOutputDirectoryName( args[ 0 ] );

                                //  Get list of input file.

        processor.setInputFileNames( args , 1 );

                                //  Add type="unclear" attributes.
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



