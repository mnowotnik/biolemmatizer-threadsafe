package edu.northwestern.at.morphadorner;

/*  Please see the license information at the end of this file. */

import java.io.*;

import java.text.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.xml.*;

import edu.northwestern.at.morphadorner.tools.*;

/** Filter to add word attributes to adorned file.
  */

public class AddWordAttributesFilter extends ExtendedXMLFilterImpl
{
    /** ExtendedAdornedWordFilter providing word attribute information. */

    protected ExtendedAdornedWordFilter wordInfoFilter;

    /** True to output non-redundant attributes only. */

    protected boolean outputNonredundantAttributesOnly  = false;

    /** True to output non-redundant token attributes only. */

    protected boolean outputNonredundantTokenAttribute  = false;

    /** True to output non-redundant part attributes only. */

    protected boolean outputNonredundantPartAttribute   = false;

    /** True to output non-redundant eos attributes only. */

    protected boolean outputNonredundantEosAttribute    = false;

    /** True to output eos attribute. */

    protected boolean outputEosAttribute    = false;

    /** True to output whitespace elements. */

    protected boolean outputWhitespace  = true;

    /** True to output word number attributes. */

    protected boolean outputWordNumber  = false;

    /** True to output sentence number attributes. */

    protected boolean outputSentenceNumber  = false;

    /** True to output word ordinal attributes. */

    protected boolean outputWordOrdinal = false;

    /** ID attribute name. */

    protected String idAttrName = WordAttributeNames.id;

    /** MorphAdorner settings. */

    protected MorphAdornerSettings morphAdornerSettings = null;

    /** Create filter.
      *
      * @param  reader                  XML input reader to which this
      *                                 filter applies.
      * @param  wordInfoFilter          ExtendedAdornedWordFilter with
      *                                 word information.
      * @param  morphAdornerSettings    MorphAdorner settings.
      */

    public AddWordAttributesFilter
    (
        XMLReader reader ,
        ExtendedAdornedWordFilter wordInfoFilter ,
        MorphAdornerSettings morphAdornerSettings
    )
    {
        super( reader );

        this.wordInfoFilter         = wordInfoFilter;
        this.morphAdornerSettings   = morphAdornerSettings;

                                //  Save ID attribute name.
        this.idAttrName =
            morphAdornerSettings.xgOptions.getIdArgumentName();

                                //  Output non-redundant attributes
                                //  only.

        this.outputNonredundantAttributesOnly   =
            morphAdornerSettings.outputNonredundantAttributesOnly;

                                //  Output non-redundant token attribute
                                //  only.

        this.outputNonredundantTokenAttribute   =
            morphAdornerSettings.outputNonredundantTokenAttribute;

                                //  Output non-redundant part attribute
                                //  only.

        this.outputNonredundantPartAttribute    =
            morphAdornerSettings.outputNonredundantPartAttribute;

                                //  Output eos attribute.

        this.outputEosAttribute = morphAdornerSettings.outputEOSFlag;

                                //  Output non-redundant eos attribute
                                //  only.

        this.outputNonredundantEosAttribute =
            morphAdornerSettings.outputNonredundantEosAttribute;

                                //  Save output whitespace option.

        this.outputWhitespace   =
            morphAdornerSettings.outputWhitespaceElements;

        this.outputSentenceNumber   =
            morphAdornerSettings.outputSentenceNumber;

        this.outputWordNumber       =
            morphAdornerSettings.outputWordNumber;

        this.outputWordOrdinal      =
            morphAdornerSettings.outputWordOrdinal;
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
                                //  Update word or punctuation element.

        if ( qName.equalsIgnoreCase( "w" ) || qName.equalsIgnoreCase( "pc" ) )
        {
                                //  Holds updated attributes.

            Map<String, String> newAtts = MapFactory.createNewSortedMap();

                                //  Get word ID.

            String id   = atts.getValue( idAttrName );

                                //  Always add word ID to output
                                //  attributes.

            newAtts.put( idAttrName , id );

                                //  Get adorned word info for this ID.

            ExtendedAdornedWord wordInfo    =
                wordInfoFilter.getExtendedAdornedWord( id );

                                //  Get word attribute values.

            String tok      = wordInfo.getToken();
            String spe      = wordInfo.getSpelling();
            String pos      = wordInfo.getPartsOfSpeech();
            boolean eos     = wordInfo.getEOS();
            String lem      = wordInfo.getLemmata();
            String reg      = wordInfo.getStandardSpelling();
            int ord         = wordInfo.getOrd();
            String part     = wordInfo.getPart();
            String wordText = wordInfo.getWordText();

                                //  Copy any existing unit attribute.

            String unit     = atts.getValue( "unit" );

            if ( unit != null )
            {
                newAtts.put( "unit" , unit );
            }
                                //  Copy any existing rend attribute.

            String rend     = atts.getValue( "rend" );

            if ( ( rend != null ) && ( rend.length() > 0 ) )
            {
                newAtts.put( "rend" , rend );
            }
                                //  Copy any existing type attribute.

            String type     = atts.getValue( "type" );

            if ( ( type != null ) && ( type.length() > 0 ) )
            {
                newAtts.put( "type" , type );
            }
                                //  Copy any existing label attribute.

            String label        = atts.getValue( WordAttributeNames.label );

            if ( ( label != null ) && ( label.length() > 0 ) )
            {
                newAtts.put( WordAttributeNames.label , label );
            }
                                //  Set updated attribute values.

            if ( outputSentenceNumber )
            {
                newAtts.put
                (
                    WordAttributeNames.sn ,
                    wordInfo.getSentenceNumber() + ""
                );
            }

            if ( outputWordNumber )
            {
                newAtts.put
                (
                    WordAttributeNames.wn ,
                    wordInfo.getWordNumber() + ""
                );
            }

            if ( morphAdornerSettings.outputEOSFlag )
            {
                newAtts.put( WordAttributeNames.eos , eos ? "1" : "0" );
            }

            if ( morphAdornerSettings.outputLemma )
            {
                newAtts.put( WordAttributeNames.lem , lem );
            }

            if ( morphAdornerSettings.outputPartOfSpeech )
            {
                newAtts.put( WordAttributeNames.pos , pos );
            }

            if ( morphAdornerSettings.outputStandardSpelling )
            {
                newAtts.put( WordAttributeNames.reg , reg );
            }

            if ( morphAdornerSettings.outputSpelling )
            {
                newAtts.put( WordAttributeNames.spe , spe );
            }

            if ( morphAdornerSettings.outputOriginalToken )
            {
                newAtts.put( WordAttributeNames.tok , tok );
            }

            newAtts.put( WordAttributeNames.part , part );

                                //  Remove redundant attributes
                                //  if requested.  Also remove
                                //  word-related attributes if
                                //  the word text is empty (should
                                //  only occur for end-of-sentence
                                //  <pc> markers).

            if ( outputNonredundantAttributesOnly || ( tok.length() == 0 ) )
            {
                if ( !eos || unit.equals( "sentence" ) )
                {
                    newAtts.remove( WordAttributeNames.eos );
                }

                if ( spe.equals( tok ) )
                {
                    newAtts.remove( WordAttributeNames.spe );
                }

                if ( lem.equals( spe ) )
                {
                    newAtts.remove( WordAttributeNames.lem );
                }

                if ( pos.equals( spe ) || ( tok.length() == 0 ) )
                {
                    newAtts.remove( WordAttributeNames.pos );
                }

                if ( reg.equals( spe ) )
                {
                    newAtts.remove( WordAttributeNames.reg );
                }

                if ( ( part != null ) && part.equals( "N" ) )
                {
                    newAtts.remove( WordAttributeNames.part );
                }

                if ( tok.equals( wordText ) )
                {
                    newAtts.remove( WordAttributeNames.tok );
                }
            }
            else
            {
                                //  If the word token is the same as
                                //  the word text, and we are
                                //  outputting abbreviated attributes,
                                //  remove the redundant token
                                //  attribute.

                if ( outputNonredundantTokenAttribute )
                {
                    if ( tok.equals( wordText ) )
                    {
                        newAtts.remove( WordAttributeNames.tok );
                    }
                }
                                //  Remove part attribute if we're only
                                //  outputting non-redundant part
                                //  attributes and part = "N".

                if ( outputNonredundantPartAttribute )
                {
                    if ( part.equals( "N" ) )
                    {
                        newAtts.remove( WordAttributeNames.part );
                    }
                }
                                //  Remove eos attribute if we're only
                                //  outputting non-redundant eos
                                //  attributes and eos = "0".

                if ( outputNonredundantEosAttribute )
                {
                    if ( !eos || ( unit != null ) )
                    {
                        newAtts.remove( WordAttributeNames.eos );
                    }
                }
            }

            AttributesImpl newAttributes    = new AttributesImpl();

            for ( String attName : newAtts.keySet() )
            {
                setAttributeValue(
                    newAttributes , attName , newAtts.get( attName ) );
            }

            super.startElement( uri , localName , qName , newAttributes );
        }
                                //  Remove part attribute from blank
                                //  wrapper element.

        else if ( qName.equalsIgnoreCase( "c" ) )
        {
            AttributesImpl newAtts  = new AttributesImpl( atts );

            removeAttribute( newAtts , WordAttributeNames.part );

            if ( outputWhitespace )
            {
                super.startElement( uri , localName , qName , newAtts );
            }
        }
                                //  Pass through remaining elements.
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
        if ( qName.equalsIgnoreCase( "c" ) )
        {
            if ( outputWhitespace )
            {
                super.endElement( uri , localName , qName );
            }
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



