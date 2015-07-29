package edu.northwestern.at.morphadorner.tools.compareadornedfiles;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;

import org.jdom2.*;
import org.jdom2.filter.*;
import org.jdom2.input.*;
import org.jdom2.output.*;
import org.jdom2.xpath.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.xml.*;
import edu.northwestern.at.utils.xml.jdom.*;

/** Loads word elements in an adorned XML file.
 */

public class AdornedWordsLoader
{
    /** Map from word ID to adorned word data. */

    protected Map<String, AdornedWordData> adornedWordDataMap   =
        MapFactory.createNewLinkedMap( 10000 );

    /** List of word IDs. */

    protected List<String> wordIDs  = ListFactory.createNewList( 10000 );

    /** Set of word tags (typically "w" and "pc"). */

    protected Set<String> wordTagsSet   = SetFactory.createNewSet();

    /** Adorned XML file as JDOM document. */

    protected Document adornedXMLDocument   = null;

    /** Create adorned words loader.
     *
     *  @param  adornedFileName     The adorned file from which to load
     *                              word data.
     *
     *  @param  wordTags            String array of word tag element
     *                              names, typically "w" and "pc".
     *
     *  @throws Exception           in case of error.
     */

    public AdornedWordsLoader( String adornedFileName , String[] wordTags )
        throws Exception
    {
                                //  Create set from word tags.

        wordTagsSet.addAll( Arrays.asList( wordTags ) );

                                //  Load adorned file to JDOM document.

        adornedXMLDocument  = JDOMUtils.parse( new File( adornedFileName ) );

                                //  Create filter to pull out word elements,
                                //  usually <w> and <pc>.

        Filter<Element> filter  = new ElementsFilter( wordTags );

                                //  Get root element of adorned file.

        Element root    = adornedXMLDocument.getRootElement();

                                //  Create iterator over word elements
                                //  using filter.

        Iterator<Element> iterator  = root.getDescendants( filter );

                                //  For each adorned word element,
                                //  pull out the xml:id, word text,
                                //  and element attributes.

        while ( iterator.hasNext() )
        {
                                //  Get next word element.

            Element wordElement = iterator.next();

                                //  Get word text.

            String wordText     = wordElement.getText();

                                //  Get word ID.
            String id   =
                JDOMUtils.getAttributeValue
                (
                    wordElement ,
                    "xml:id" ,
                    false
                );
                                //  Add this ID to list of word IDs.

            wordIDs.add( id );

                                //  Get attributes list.

            List<Attribute> attributeList   = wordElement.getAttributes();

                                //  Create attributes map from list.

            Map<String, String> attributeMap    =
                MapFactory.createNewSortedMap();

            for ( int i = 0 ; i < attributeList.size() ; i++ )
            {
                Attribute attribute = attributeList.get( i );

                attributeMap.put
                (
                    attribute.getQualifiedName() ,
                    attribute.getValue()
                );
            }
                                //  Get word ID of sibling word with
                                //  the same parent.

            String siblingID    = findSiblingID( wordElement , id );

                                //  Note if blank (<c>) precedes this word.

            boolean blankPrecedes   = ifBlankPrecedes( wordElement );

                                //  Add word data to map.

            AdornedWordData adornedWordData =
                new AdornedWordData(
                    wordText , attributeMap , siblingID , blankPrecedes );

            adornedWordDataMap.put( id , adornedWordData );
        }
    }

    /** Create adorned words loader.
     *
     *  @param  adornedFileName     The adorned file from which to load
     *                              word data.
     *
     *  @throws Exception           in case of error.
     *
     *  <p>
     *  The word elements are assumed to be tagged as <w> and <pc>.
     *  </p>
     */

    public AdornedWordsLoader( String adornedFileName )
        throws Exception
    {
        this( adornedFileName , new String[]{ "w" , "pc" } );
    }

    /** Return list of words IDs.
     *
     *  @return     list of word IDs.
     */

    public List<String> getAdornedWordIDs()
    {
        return wordIDs;
    }

    /** Get data for a specified word ID.
     *
     *  @param  id  Word ID.
     *
     *  @return     The word data, or null if the word ID does not exist.
     */

    public AdornedWordData getAdornedWordData( String id )
    {
        return adornedWordDataMap.get( id );
    }

    /** Get sibling ID for a given word ID.
     *
     *  @param  wordElement     The word element.
     *  @param  id              The word ID.
     *
     *  @return     Word ID of either the nearest previous sibling,
     *              if the given element is not the first child of
     *              its parent, or else the nearest following sibling.
     *              Returns null if a sibling cannot be found.
     */

    public String findSiblingID( Element wordElement , String id )
    {
        String result   = null;

        if ( wordElement != null )
        {
            Element parent = wordElement.getParentElement();

            List<Element> children = parent.getChildren();

            int index = children.indexOf( wordElement );

                                //  Get previous sibling if available.
            int i = index - 1;

            while ( i >= 0 )
            {
                Element prevElement = children.get( i );

                if ( wordTagsSet.contains( prevElement.getName() ) )
                {
                    result  =
                        JDOMUtils.getAttributeValue
                        (
                            prevElement ,
                            "xml:id" ,
                            false
                        );

                    break;
                }
                else
                {
                    i--;
                }
            }
                                //  Otherwise get following sibling.

            if ( result == null )
            {
                i   = index + 1;

                while ( i < children.size() )
                {
                    Element nextElement = children.get( i );

                    if ( wordTagsSet.contains( nextElement.getName() ) )
                    {
                        result  =
                            JDOMUtils.getAttributeValue
                            (
                                nextElement ,
                                "xml:id" ,
                                false
                            );

                        break;
                    }
                    else
                    {
                        i++;
                    }
                }
            }
        }

        return result;
    }

    /** Determine if blank marker element (<c> </c>) precedes a word.
     *
     *  @param  wordElement     The word element.
     *
     *  @return     true if word is preceded by a blank marker element.
     */

    public boolean ifBlankPrecedes( Element wordElement )
    {
                                //  Assume there isn't a preceding
                                //  blank element.

        boolean result  = false;

                                //  If the word element is not null ...

        if ( wordElement != null )
        {
                                //  Get parent element of word element.

            Element parent = wordElement.getParentElement();

                                //  Get children of the parent element.

            List<Element> children = parent.getChildren();

                                //  Find the index of the parent element.

            int index = children.indexOf( wordElement );

                                //  See if previous sibling is a
                                //  "<c>" element.

            if ( index > 0 )
            {
                Element prevElement = children.get( index - 1 );

                result  = prevElement.getName().equals( "c" );
            }
        }

        return result;
    }

    /** Get adorned XML document.
     *
     *  @return     Adorned XML document.
     */

    public Document getDocument()
    {
        return adornedXMLDocument;
    }

    /** Release adorned XML document.
     */

    public void releaseDocument()
    {
        adornedXMLDocument  = null;

        System.gc();
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



