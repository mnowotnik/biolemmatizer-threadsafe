package edu.northwestern.at.morphadorner;

/*  Please see the license information at the end of this file. */

import java.text.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.sentencemelder.*;
import edu.northwestern.at.utils.xml.*;

/** Filter to strip word elements for specified tags from adorned file.
  */

public class StripWordElementsFilter extends ExtendedXMLFilterImpl
{
    /** Set of tags from which to strip words. */

    protected Set<String> elementsToStripSet;

    /** XML sentence melder. */

    protected XMLSentenceMelder sentenceMelder  = null;

    /** Strip element stack. */

    protected QueueStack<String> stripElementStack  =
        new QueueStack<String>();

    /** True if processing word element. */

    protected boolean processingWord;

    /** Create filter.
      *
      * @param  reader          XML input reader to which filter applies.
      * @param  elementsToStrip Elements to strip separated by spaces.
      * @param  sentenceMelder  Associated sentence melder.
      */

    public StripWordElementsFilter
    (
        XMLReader reader ,
        String elementsToStrip ,
        XMLSentenceMelder sentenceMelder
    )
    {
        super( reader );

                                //  Create set of elements from which
                                //  to strip word elements.

        elementsToStripSet  = SetFactory.createNewSet();

        elementsToStripSet.addAll
        (
            Arrays.asList( StringUtils.makeTokenArray( elementsToStrip ) )
        );
                                //  Not processing word element yet.

        processingWord  = false;
    }

    /** Create filter.
     *
     *  @param  reader          XML input reader to which filter applies.
     *  @param  elementsToStrip Elements to strip separated by spaces.
     */

    public StripWordElementsFilter
    (
        XMLReader reader ,
        String elementsToStrip
    )
    {
        this( reader , elementsToStrip , null );
    }

    /** Set associated sentence melder.
     *
     *  @param  sentenceMelder  Sentence melder.
     */

    public void setSentenceMelder( XMLSentenceMelder sentenceMelder )
    {
                                //  Set XML sentence melder.

        this.sentenceMelder = sentenceMelder;
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
                                //  If this element is one from which
                                //  we're to strip word elements,
                                //  push the element onto the stack.

        if ( elementsToStripSet.contains( localName ) )
        {
            stripElementStack.push( localName );
        }
                                //  Not processing word element yet.

        processingWord  = false;

                                //  If we are in an element from which
                                //  to strip word elements ...

        if ( stripElementStack.size() > 0 )
        {
            if ( sentenceMelder != null )
            {
                sentenceMelder.setEmitXMLWrapperForBlank( false );
            }
                                //  Set flag if we're starting a word
                                //  element.  We do not emit the word
                                //  element, just its text.

            if  (   qName.equalsIgnoreCase( "w" ) ||
                    qName.equalsIgnoreCase( "pc" ) ||
                    qName.equalsIgnoreCase( "c" )
                )
            {
                processingWord  = true;
            }
            else
            {
                super.startElement( uri , localName , qName , atts );
            }
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
                                //  If we're in a descendent of an element
                                //  from which to strip word elements,
                                //  only emit character if we're processing
                                //  a word element.

        if ( stripElementStack.size() > 0 )
        {
            if ( processingWord )
            {
                super.characters( ch , start , length );
            }
        }
        else
        {
            super.characters( ch , start , length );
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
                                //  Pop stack if this is the end of an
                                //  element from which to remove word
                                //  element tags.

        if ( elementsToStripSet.contains( localName ) )
        {
            stripElementStack.pop();
        }
                                //  Do not output end element tag
                                //  for skipped word element.

        if ( stripElementStack.size() > 0 )
        {
            if ( !processingWord )
            {
                super.endElement( uri , localName , qName );
            }
        }
        else
        {
            super.endElement( uri , localName , qName );
        }
                                //  Not processing word element anymore.

        processingWord  = false;

                                //  If not in element from which to
                                //  remove word elements anymore,
                                //  set XML wrapper for blanks to TRUE
                                //  in associated sentence melder.

        if ( stripElementStack.size() == 0 )
        {
            if ( sentenceMelder != null )
            {
                sentenceMelder.setEmitXMLWrapperForBlank( true );
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



