package edu.northwestern.at.morphadorner.tools.tcp;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.jdom2.*;
import org.jdom2.filter.*;
import org.jdom2.input.*;
import org.jdom2.output.*;
import org.jdom2.xpath.*;

import edu.northwestern.at.morphadorner.tools.*;
import edu.northwestern.at.morphadorner.tools.compareadornedfiles.*;
import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.xml.*;

/** Remove long s, brace-enclosed entities, superscripts, etc. */

public class RemoveCruft
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

    /** Pattern to match _Cap */

    protected static Pattern underlineCapPattern            =
        Pattern.compile( "^_\\p{Lu}" );

    protected static final Matcher underlineCapMatcher  =
        underlineCapPattern.matcher( "" );

    /** Map from encoded superscripts to replacements. */

    protected static Map<String, String> superscriptsMap;

    /** Main program. */

    public static void main( String[] args )
    {
        try
        {
            removeCruft( args );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public static void removeCruft( String[] args )
        throws Exception
    {
        FileBatchProcessor processor    =
            new FileBatchProcessor()
        {
            public void processOneFile( String inputFileName )
                throws Exception
            {
                MutableAdornedFile mutableAdornedFile   =
                    new MutableAdornedFile( inputFileName );

                List<String> adornedWordIDs =
                    mutableAdornedFile.getAdornedWordIDs();

                Document document   = mutableAdornedFile.getDocument();

                for ( int i = 0 ; i < adornedWordIDs.size() ; i++ )
                {
                    Element adornedWord =
                        mutableAdornedFile.getAdornedWord
                        (
                            adornedWordIDs.get( i )
                        );

                    String wordText = adornedWord.getText();

                    if ( wordText.indexOf( CharUtils.LONG_S_STRING ) >= 0 )
                    {
                        wordText    =
                            StringUtils.replaceAll
                            (
                                wordText ,
                                CharUtils.LONG_S_STRING ,
                                "s"
                            );
                    }

                    if ( wordText.indexOf( "^" ) >= 0 )
                    {
                        wordText    =
                            StringUtils.replaceAll
                            (
                                wordText ,
                                "^" ,
                                ""
                            );

                        JDOMUtils.setAttributeValue
                        (
                            adornedWord ,
                            "rend" ,
                            "superscript"
                        );
                    }

                    if ( superscriptsMap.containsKey( wordText ) )
                    {
                        wordText    = superscriptsMap.get( wordText );
                    }

                    wordText    =
                        StringUtils.replaceAll
                        (
                            wordText ,
                            "\u0153" ,
                            "oe"
                        );

                    wordText    =
                        StringUtils.replaceAll
                        (
                            wordText ,
                            "\u00e6" ,
                            "ae"
                        );

                    wordText    =
                        StringUtils.replaceAll
                        (
                            wordText ,
                            "{" ,
                            ""
                        );

                    wordText    =
                        StringUtils.replaceAll
                        (
                            wordText ,
                            "}" ,
                            ""
                        );
                                //  Replace initial _ followed by
                                //  capital letter with just the letter.
                                //  If word does not already have
                                //  a rend= attribute, add
                                //  rend="initialchardecorated" .

                    underlineCapMatcher.reset( wordText );

                    if ( underlineCapMatcher.find() )
                    {
                        wordText    = wordText.substring( 1 );

                        String rend =
                            JDOMUtils.getAttributeValue
                            (
                                adornedWord ,
                                "rend" ,
                                false
                            );

                        if  (   ( rend == null ) ||
                                ( rend.trim().length() == 0 )
                            )
                        {
                            JDOMUtils.setAttributeValue
                            (
                                adornedWord ,
                                "rend" ,
                                "initialcharacterdecorated"
                            );
                        }
                    }

                    adornedWord.setText( wordText );
                }
                                //  Strip path from input file name.

                String strippedFileName =
                    FileNameUtils.stripPathName( inputFileName );

                strippedFileName    =
                    FileNameUtils.changeFileExtension( strippedFileName , "" );

                                //  Create output file name.

                String xmlOutputFileName    =
                    new File
                    (
                        outputDirectoryName ,
                        strippedFileName + ".xml"
                    ).getAbsolutePath();

                FileUtils.createPathForFile( xmlOutputFileName );

                AdornedXMLWriter writer =
                    new AdornedXMLWriter( document , xmlOutputFileName );
            }

            /** Create an element.
             *
             *  @param  name    Element name.
            */

            public Element createElement( String name )
            {
                Element element = new Element( name , teiNamespace );
                return element;
            }
        };

        processor.setOutputDirectoryName( args[ 0 ] );

        superscriptsMap = MapUtils.loadMap( args[ 1 ] );

        processor.setInputFileNames( args , 2 );
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



