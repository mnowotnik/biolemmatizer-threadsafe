package edu.northwestern.at.morphadorner.corpuslinguistics.sentencemelder;

/*  Please see the license information at the end of this file. */

import java.util.*;
import com.megginson.sax.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/** XML Sentence melder
 */

public class XMLSentenceMelder extends SentenceMelder
{
    /** Single blank character. */

    protected final static char[] oneBlankChar  = new char[]{ ' ' };

    /** True to emit XML wrapper element for a blank. */

    protected boolean emitXMLWrapperForBlank    = true;

    /** XML element to wrap a blank. */

    protected String xmlBlankWrapper            = "c";

    /** Create XML sentence melder.
     */

    public XMLSentenceMelder()
    {
    }

    /** Create sentence melder.
     *
     *  @param  xmlWriter   XML output data writer.
     */

    public XMLSentenceMelder( XMLWriter xmlWriter )
    {
        this.state.xmlWriter    = xmlWriter;
    }

    /** Set XML writer.
     *
     *  @param  xmlWriter   XML output data writer.
     */

    public void seWriter( XMLWriter xmlWriter )
    {
        this.state.xmlWriter    = xmlWriter;
    }

    /** Set XML wrapper flag for blanks.
     *
     *  @param  xmlBlankWrapper     Wrap blank with specified XML element.
     *
     *  <p>
     *  If the specified wrapper is empty or null, blanks are not wrapped.
     *  </p>
     */

    public void setEmitXMLWrapperForBlank( String xmlBlankWrapper )
    {
        this.xmlBlankWrapper        = xmlBlankWrapper;
        this.emitXMLWrapperForBlank =
            ( xmlBlankWrapper != null ) && ( xmlBlankWrapper.length() > 0 );
    }

    /** Set XML wrapper flag for blanks.
     *
     *  @param  emitXMLWrapperForBlank  Wrap blank with given XML element.
     *
     *  <p>
     *  If the specified wrapper is empty or null, blanks are not wrapped.
     *  </p>
     */

    public void setEmitXMLWrapperForBlank( boolean emitXMLWrapperForBlank )
    {
        this.emitXMLWrapperForBlank = emitXMLWrapperForBlank;
        this.xmlBlankWrapper        = "c";
    }

    /** Set URI for XML elements.
     *
     *  @param  elementURI      URI for XML elements,
     */

    public void setURI( String elementURI )
    {
        this.state.elementURI   = elementURI;
    }

    /** Add blank to sentence.
     */

    public void outputBlank()
    {
        try
        {
            if ( emitXMLWrapperForBlank )
            {
                state.xmlWriter.startElement
                (
                    state.elementURI ,
                    xmlBlankWrapper ,
                    xmlBlankWrapper ,
                    new AttributesImpl()
                );
            }

            state.xmlWriter.characters( oneBlankChar , 0 , 1 );

            if ( emitXMLWrapperForBlank )
            {
                state.xmlWriter.endElement
                (
                    state.elementURI ,
                    xmlBlankWrapper ,
                    xmlBlankWrapper
                );
            }
        }
        catch ( Exception e )
        {
        }
    }

    /** Add word to sentence.
     *
     *  @param  word    The word to add.
     */

    protected void outputWord( String word )
    {
    }

    /** Finish sentence.
     *
     *  @return Returned sentence.
     */

    public String endSentence()
    {
        return "";
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



