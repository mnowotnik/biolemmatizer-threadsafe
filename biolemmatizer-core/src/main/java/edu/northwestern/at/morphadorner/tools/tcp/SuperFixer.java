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

/** SuperFixer marks "^" characters with special tags.
 *
 *  <p>
 *  Usage:
 *  </p>
 *  <blockquote>
 *  <pre>
 *  java edu.northwestern.at.morphadorner.tools.tcp.SuperFixer outputdirectory input1.xml input2.xml ...
 *  </pre>
 *  </blockquote>
 *  <p>
 *  where <strong>outputdirectory</strong> is the output directory containing
 *  the resultant XML files with &lt;zzzzlj&gt;text&lt;/zzzzlj&gt; added
 *  to surround tokens containing "^" superscript markers, and input*.xml
 *  are the input tokenized XML files.
 *  </p>
 *
 *  <p>
 *  Tokens which end in ^d where "d" is a single digit are converted
 *  to the token followed by a <ref rend="superscript">d</ref>.  This
 *  provides for inserting the missing targets of these apparent
 *  note references at a later editing stage.
 *  </p>
 */

public class SuperFixer
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

    /** Superscript number replacers. */

    protected static PatternReplacer replacer1  =
        new PatternReplacer
        (
            "(\\w)(\\^)([1|2|3|4|5|6|7|8|9|0])(\\.|\\?|!)" ,
            "<zzzzlj>$1</zzzzlj> <ref rend=\"superscript\">$3</ref> $4"
        );

    protected static PatternReplacer replacer2  =
        new PatternReplacer
        (
            "(\\w)(\\^)([1|2|3|4|5|6|7|8|9|0])(\\W)" ,
            "$1 <ref rend=\"superscript\"><zzzzlj>$3</zzzzlj></ref><zzzzbl/> $4"
        );

    /** Main ^ character replacer. */

//  protected static Pattern pattern    = Pattern.compile( "((\\^.)+)" );
    protected static Pattern pattern    =
        Pattern.compile( "((\\^<hi>.+</hi>)+|(\\^.)+)" );

    protected static Matcher matcher    = pattern.matcher( "" );

    /** Main program. */

    public static void main( String[] args )
    {
        try
        {
            superFixer( args );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public static void superFixer( String[] args )
        throws Exception
    {
        FileBatchProcessor processor    =
            new FileBatchProcessor()
        {
            public void processOneFile( String inputFileName )
                throws Exception
            {
                                //  Read XML file.

                String xml  = FileUtils.readTextFile( inputFileName );

                                //  Perform superscript number replacements.

//              xml = replacer1.replace( xml );
//              xml = replacer2.replace( xml );

                                //  Replace other superscript patterns.

                matcher.reset( xml );

                StringBuffer sb = new StringBuffer();

                while ( matcher.find() )
                {
                    String rep  = matcher.group( 1 );
                    rep         = "<zzzzlj>" + rep + "</zzzzlj>";

                    matcher.appendReplacement( sb , rep );
                }

                matcher.appendTail( sb );

                xml = sb.toString();

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

                FileUtils.writeTextFile
                (
                    xmlOutputFileName ,
                    false ,
                    xml ,
                    "utf-8"
                );
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



