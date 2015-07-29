package edu.northwestern.at.morphadorner.tools.tcp;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

import org.jdom2.*;
import org.jdom2.input.*;
import org.jdom2.filter.*;
import org.jdom2.output.*;

import edu.northwestern.at.morphadorner.tools.compareadornedfiles.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.xml.*;
import edu.northwestern.at.utils.xml.jdom.*;

/** Fix word breaks. */

public class FixWordBreaks
{
    /** TEI P5 name space. */

    public static Namespace teiNamespace    =
        Namespace.getNamespace( "http://www.tei-c.org/ns/1.0" );

    /** Fixed words map. */

    public static Map<String, String> fixedWordsMap;

    /** File batch processor. */

    public static FileBatchProcessor processor;

    /** Main program.
     *
     *  @param  args    Command line arguments.
     */

    public static void main( String[] args )
    {
        try
        {
            fixWordBreaks( args );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /** Fix word breaks.
     *
     *  @param  args    Command line arguments.
     */

    public static void fixWordBreaks( String[] args )
        throws Exception
    {
                                //  Define batch processor for
                                //  fixing word elements.

        processor   =
            new FileBatchProcessor()
            {
                public void processOneFile( String inputFileName )
                    throws Exception
                {
                    try
                    {
                        System.out.println( "Processing " + inputFileName );

                                //  Load adorned file.

                        String doc  =
                            FileUtils.readTextFile( inputFileName , "utf-8" );

                        Document document   = JDOMUtils.parseText( doc );

                                //  Filter for word elements.

                        Filter<Element> filter  =
                            new ElementsFilter( new String[]{ "w" , "pc" } );

                                //  Fix word breaking hyphens.

                        JDOMUtils.applyElementFilter
                        (
                            document ,
                            filter ,
                            new WProcessor()
                        );
                                //  Create output file name.

                        String outputFileName   =
                            new File
                            (
                                outputDirectoryName ,
                                new File( inputFileName ).getName()
                            ).getCanonicalPath();

                                //  Output adorned file with
                                //  word breaking hyphens corrected.

                        Format format   =
                            org.jdom2.output.Format.getRawFormat();

                        format.setOmitDeclaration( true );

                        FileUtils.createPathForFile( outputFileName );

                        JDOMUtils.save
                        (
                            document ,
                            outputFileName ,
                            format
                        );
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace();
                    }
                }
            };
                                //  Set output directory.

        processor.setOutputDirectoryName( args[ 0 ] );

                                //  Load fixed words map.

        fixedWordsMap   = MapUtils.loadMap( args[ 1 ] );

                                //  Set input file names.

        processor.setInputFileNames( args , 2 );

                                //  Run the gap fixer over the input
                                //  files.
        processor.run();
    }

    /** Process an adorned word.
     */

    public static class WProcessor implements ElementProcessor
    {
        public void processElement( Document document , Element w )
        {
            String wText    = w.getText();

            String id       =
                JDOMUtils.getAttributeValue( w , "xml:id" , true );

            boolean hasHyphen   = false;

            hasHyphen   =
                ( wText.indexOf( CharUtils.NONBREAKING_HYPHEN_STRING ) >= 0 ) ||
                ( wText.indexOf( "|" ) >= 0 )
                ;

            if ( hasHyphen )
            {
                if ( wText.indexOf( " " ) >= 0 )
                {
                    processor.getPrintStream().println
                    (
                        "   " + id + " contains blank, not handled."
                    );
                }
                else
                {
                    wText   =
                        StringUtils.replaceAll
                        (
                            wText ,
                            CharUtils.NONBREAKING_HYPHEN_STRING ,
                            "|"
                        );

                    wText   =
                        StringUtils.replaceAll
                        (
                            wText ,
                            "||" ,
                            "|"
                        );

                    String fixedWText   = "";

                    if ( wText.equals( "|" ) )
                    {
                        fixedWText  = CharUtils.NONBREAKING_HYPHEN_STRING;
                    }
                    else
                    {
                        fixedWText  = fixedWordsMap.get( wText );

                        if ( fixedWText == null )
                        {
                            fixedWText  =
                                StringUtils.replaceAll
                                (
                                    wText ,
                                    "|" ,
                                    ""
                                );
                        }
                    }

                    w.setText( fixedWText );
                }
            }
        }
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



