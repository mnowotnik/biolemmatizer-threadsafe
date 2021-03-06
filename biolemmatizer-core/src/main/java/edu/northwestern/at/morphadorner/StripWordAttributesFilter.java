package edu.northwestern.at.morphadorner;

/*  Please see the license information at the end of this file. */

import java.io.*;

import java.text.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.xml.*;

/** Filter to strip word attributes in adorned file.
  */

public class StripWordAttributesFilter extends ExtendedXMLFilterImpl
{
    /** Holds abbr expan= attribute value. */

    protected String abbrExpan  = "";

    /** Holds set of word IDs of words with defined reg= attribute. */

    protected Set<String> regIDSet  = SetFactory.createNewSet();

    /** Create filter.
      *
      * @param  reader  XML input reader to which this filter applies.
      */

    public StripWordAttributesFilter( XMLReader reader )
    {
        super( reader );
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
        if ( qName.equals( "w" ) || qName.equals( "pc" ) )
        {
            AttributesImpl newAtts  = new AttributesImpl();

                                //  Keep existing xml:id, rend, label
                                //  (usually n), type, and unit attributes.

            String id   = atts.getValue( WordAttributeNames.id );

            if ( id != null )
            {
                setAttributeValue( newAtts , WordAttributeNames.id , id );
            }

            String label    = atts.getValue( WordAttributeNames.label );

            if ( label != null )
            {
                setAttributeValue( newAtts , WordAttributeNames.label , label );
            }

            String rend = atts.getValue( WordAttributeNames.rend );

            if ( rend != null )
            {
                setAttributeValue( newAtts , WordAttributeNames.rend , rend );
            }

            String type = atts.getValue( WordAttributeNames.type );

            if ( type != null )
            {
                setAttributeValue( newAtts , WordAttributeNames.type , type );
            }

            String unit = atts.getValue( WordAttributeNames.unit );

            if ( unit != null )
            {
                setAttributeValue( newAtts , WordAttributeNames.unit , unit );
            }

            String part = atts.getValue( WordAttributeNames.part );

            if ( part != null )
            {
                setAttributeValue( newAtts , WordAttributeNames.part , part );
            }

            if  (   ( abbrExpan != null ) &&
                    ( abbrExpan.length() > 0 ) &&
                    ( qName.equals( "w" ) )
                )
            {
                setAttributeValue( newAtts , WordAttributeNames.reg , abbrExpan );

                regIDSet.add( id );
            }

            super.startElement( uri , localName , qName , newAtts );
        }
        else if ( qName.equals( "c" ) )
        {
            AttributesImpl newAtts  = new AttributesImpl();

            removeAttribute( newAtts , WordAttributeNames.part );

            super.startElement( uri , localName , qName , newAtts );
        }
        else if ( qName.equals( "abbr" ) )
        {
            String expan    = atts.getValue( "expan" );

            if ( expan != null )
            {
                abbrExpan   = expan;
            }
            else
            {
                abbrExpan   = "";
            }

            super.startElement( uri , localName , qName , atts );
        }
        else
        {
            super.startElement( uri , localName , qName , atts );
        }
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
        if ( qName.equalsIgnoreCase( "abbr" ) )
        {
            abbrExpan   = "";
        }

        super.endElement( uri , localName , qName );
    }

    /** Get set of word IDs with defined reg= attribute.
     *
     *  @return     Set of word IDs for words with defined reg= attribute.
     */

    public Set<String> getRegIDSet()
    {
        return regIDSet;
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



