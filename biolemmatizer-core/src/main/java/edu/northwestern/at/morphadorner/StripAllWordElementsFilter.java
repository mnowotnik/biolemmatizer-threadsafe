package edu.northwestern.at.morphadorner;

/*  Please see the license information at the end of this file. */

import java.io.*;

import java.text.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.xml.*;

/** Filter to strip word elements from adorned file.
  */

public class StripAllWordElementsFilter extends ExtendedXMLFilterImpl
{
    /** Div count. */

    protected int divCount = 0;

    /** Line within div count. */

    protected int lineCount = 0;

    /** True if processing w or c element. */

    protected boolean processingWorC;

    /** Create filter.
      *
      * @param  reader          XML input reader to which filter applies.
      */

    public StripAllWordElementsFilter( XMLReader reader )
    {
        super( reader );
                                //  Not processing <w> or <c> yet.

        processingWorC  = false;
    }

    /** Handle start of an XML element.
      *
      * @param  uri         The XML element's URI.
      * @param  localName   The XML element's local name.
      * @param  qName       The XML element's qname.
      * @param  atts        The XML element's attributes.
      */

    public void startElement
    (
        String uri ,
        String localName ,
        String qName ,
        Attributes atts
    )
        throws SAXException
    {
                                //  Not processing <w> or <c> yet.

        processingWorC  = false;

                                //  Set flag if we're starting a <w>,
                                //  <pc>, or <c> element.  We do not emit
                                //  the <w>, <pc>, or <c> element, just its
                                //  text.

        if ( qName.equals( "w" ) )
        {
            processingWorC  = true;
        }
        else if ( qName.equals( "pc" ) )
        {
            processingWorC  = true;
        }
        else if ( qName.equals( "c" ) )
        {
            processingWorC  = true;
        }
        else if ( qName.equals( "div" ) )
        {
            AttributesImpl newAtts  = new AttributesImpl( atts );

            if ( atts.getIndex( "xml:id" ) < 0 )
            {
                divCount++;
                lineCount   = 0;

                setAttributeValue( newAtts , "xml:id" , divCount );
            }

            super.startElement( uri , localName , qName , newAtts );
        }
        else if ( qName.equals( "l" ) )
        {
            AttributesImpl newAtts  = new AttributesImpl( atts );

            if ( atts.getIndex( "n" ) < 0 )
            {
                lineCount++;

                String n    = StringUtils.intToString( lineCount );

                setAttributeValue( newAtts , "n" , lineCount );
            }

            super.startElement( uri , localName , qName , newAtts );
        }
        else
        {
            super.startElement( uri , localName , qName , atts );
        }
    }

    /** Handle character data.
     *
     *  @param  ch      Array of characters.
     *  @param  start   The starting position in the array.
     *  @param  length  The number of characters.
     *
     *  @throws org.xml.sax.SAXException If there is an error.
     */

    public void characters( char ch[] , int start , int length )
        throws SAXException
    {
        super.characters( ch , start , length );
    }

    /** Handle end of an element.
     *
     *  @param  uri         The XML element's URI.
     *  @param  localName   The XML element's local name.
     *  @param  qName       The XML element's qname.
     */

    public void endElement
    (
        String uri ,
        String localName ,
        String qName
    )
        throws SAXException
    {
                                //  Not processing <w> or <c> anymore.

        processingWorC  = false;

                                //  Do not output end element tag
                                //  for skipped <w> or <c>.

        if ( qName.equals( "w" ) )
        {
        }
        else if ( qName.equals( "pc" ) )
        {
        }
        else if ( qName.equals( "c" ) )
        {
        }
        else
        {
            super.endElement( uri , localName , qName );
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



