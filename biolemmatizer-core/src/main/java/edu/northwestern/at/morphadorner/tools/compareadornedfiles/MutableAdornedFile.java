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

/** Loads an adorned XML file for updating.
 */

public class MutableAdornedFile
{
    /** Map from word ID to adorned word elements. */

    protected Map<String, Element> adornedWordMap   =
        MapFactory.createNewLinkedMap( 10000 );

    /** List of word IDs. */

    protected List<String> wordIDs  = ListFactory.createNewList( 10000 );

    /** Set of word tags (typically "w" and "pc"). */

    protected Set<String> wordTagsSet   = SetFactory.createNewSet();

    /** Adorned document. */

    protected Document document;

    /** TEI name space. */

    protected static Namespace teiNamespace =
        Namespace.getNamespace( "http://www.tei-c.org/ns/1.0" );

    /** Create mutable adorned file.
     *
     *  @param  adornedFileName     The adorned file.
     *
     *  @param  wordTags            String array of word tag element
     *                              names, typically "w" and "pc".
     *
     *  @throws Exception           in case of error.
     */

    public MutableAdornedFile( String adornedFileName , String[] wordTags )
        throws Exception
    {
                                //  Create set from word tags.

        wordTagsSet.addAll( Arrays.asList( wordTags ) );

                                //  Load adorned file to JDOM document.

        document    = JDOMUtils.parse( new File( adornedFileName ) );

                                //  Create filter to pull out word elements,
                                //  usually <w> and <pc>.

        Filter<Element> filter  = new ElementsFilter( wordTags );

                                //  Get root element of adorned file.

        Element root    = document.getRootElement();

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

                                //  Add word element to map.

            adornedWordMap.put( id , wordElement );
        }
    }

    /** Create mutable adorned file.
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

    public MutableAdornedFile( String adornedFileName )
        throws Exception
    {
        this( adornedFileName , new String[]{ "w" , "pc" } );
    }

    /** Return list of word IDs.
     *
     *  @return     list of word IDs.
     */

    public List<String> getAdornedWordIDs()
    {
        return wordIDs;
    }

    /** Get element for a specified word ID.
     *
     *  @param  id  Word ID.
     *
     *  @return     The word element, or null if the word ID does not exist.
     */

    public Element getAdornedWord( String id )
    {
        return adornedWordMap.get( id );
    }

    /** Get document.
    *
    *   @return     The updated document.
    */

    public Document getDocument()
    {
        return document;
    }

    /** Apply a change from a change log.
     *
     *  @param  change      The change to apply.
     */

    public void applyChange( WordChange change )
    {
                                //  Get ID of word to which this change
                                //  applies.

        String id   = change.id;

                                //  Select type of change.

        switch ( change.changeType )
        {
            case addition :
                if ( change.fieldType == FieldType.text )
                {
                    addWordElement
                    (
                        id ,
                        change.siblingID ,
                        change.newValue ,
                        change.blankPrecedes
                    );
                }
                else if ( change.fieldType == FieldType.attribute )
                {
                    setWordElementAttribute
                    (
                        id ,
                        change.attributeName ,
                        change.newValue
                    );
                }
                break;

            case modification :
                if ( change.fieldType == FieldType.text )
                {
                    setWordElementText( id , change.newValue );
                }
                else if ( change.fieldType == FieldType.attribute )
                {
                    setWordElementAttribute
                    (
                        id ,
                        change.attributeName ,
                        change.newValue
                    );
                }
                break;

            case deletion :
                if ( change.fieldType == FieldType.text )
                {
                    deleteWordElement( id );
                }
                else if ( change.fieldType == FieldType.attribute )
                {
                    deleteWordElementAttribute
                    (
                        id ,
                        change.attributeName
                    );
                }
                break;
        }
    }

    /** Revert a change from a change log.
     *
     *  @param  change      The change to revert.
     */

    public void revertChange( WordChange change )
    {
                                //  Get ID of word to which this change
                                //  applies.

        String id   = change.id;

                                //  Select type of change.

        switch ( change.changeType )
        {
            case deletion :
                if ( change.fieldType == FieldType.text )
                {
                    addWordElement
                    (
                        id ,
                        change.siblingID ,
                        change.oldValue ,
                        change.blankPrecedes
                    );
                }
                else if ( change.fieldType == FieldType.attribute )
                {
                    setWordElementAttribute
                    (
                        id ,
                        change.attributeName ,
                        change.oldValue
                    );
                }
                break;

            case modification :
                if ( change.fieldType == FieldType.text )
                {
                    setWordElementText( id , change.oldValue );
                }
                else if ( change.fieldType == FieldType.attribute )
                {
                    setWordElementAttribute
                    (
                        id ,
                        change.attributeName ,
                        change.oldValue
                    );
                }
                break;

            case addition :
                if ( change.fieldType == FieldType.text )
                {
                    deleteWordElement( id );
                }
                else if ( change.fieldType == FieldType.attribute )
                {
                    deleteWordElementAttribute
                    (
                        id ,
                        change.attributeName
                    );
                }
                break;
        }
    }

    /** Apply changes from a change log.
     *
     *  @param  changes     The changes to apply.
     */

    public void applyChanges( List<WordChange> changes )
    {
                                //  Apply list of changes.

        for ( int i = 0 ; i < changes.size() ; i++ )
        {
            applyChange( changes.get( i ) );
        }
                                //  Compress <c> element sequences.
        compressCElements();
    }

    /** Revert changes from a change log.
     *
     *  @param  changes     The changes to revert.
     */

    public void revertChanges( List<WordChange> changes )
    {
                                //  Revert list of changes.

        for ( int i = 0 ; i < changes.size() ; i++ )
        {
            revertChange( changes.get( i ) );
        }
                                //  Compress <c> element sequences.
        compressCElements();
    }

    /** Create an element.
     *
     *  @param  name    Element name.
     */

    protected Element createElement( String name )
    {
        return new Element( name , teiNamespace );
    }

    /** Delete a word element.
     *
     *  @param  id      Word ID of element to delete.
     *
     *  @return         true if delete, false otherwise.
     */

    protected boolean deleteWordElement( String id )
    {
        boolean result  = false;

        Element wordElement = adornedWordMap.get( id );

        if ( wordElement != null )
        {
            Element parent  = wordElement.getParentElement();

            int wordIndex   = parent.indexOf( wordElement );

            result  = parent.removeContent( wordElement );

            if ( result )
            {
                adornedWordMap.remove( id );
                wordIDs.remove( id );
            }
        }

        return result;
    }

    /** Delete a word element attribute.
     *
     *  @param  id          Word ID of element for which to delete attribute.
     *  @param  attrName    Name of attribute to delete.
     */

    public void deleteWordElementAttribute
    (
        String id ,
        String attrName
    )
    {
        Element wordElement = adornedWordMap.get( id );

        if ( ( wordElement != null ) && ( attrName != null ) )
        {
            JDOMUtils.removeAttribute( wordElement ,  attrName );
        }
    }

    /** Set word element text.
     *
     *  @param  id      Word ID of element.
     *  @param  text    The word text.
     */

    public void setWordElementText( String id , String text )
    {
        Element wordElement = adornedWordMap.get( id );

        if ( ( wordElement != null ) && ( text != null ) )
        {
            wordElement.setText( text );
        }
    }

    /** Set word element attribute value.
     *
     *  @param  id          Word ID of element.
     *  @param  attrName    The attribute name.
     *  @param  attrValue   The attribute value.
     */

    public void setWordElementAttribute
    (
        String id ,
        String attrName ,
        String attrValue
    )
    {
        Element wordElement = adornedWordMap.get( id );

        if  (   ( wordElement != null ) &&
                ( attrName != null ) &&
                ( attrValue != null )
            )
        {
            JDOMUtils.setAttributeValue
            (
                wordElement ,
                attrName ,
                attrValue
            );
        }
    }

    /** Add word element.
     *
     *  @param  id          Word ID of element to add.
     *  @param  siblingID   Word ID of sibling element.
     *  @param  text        The word text.
     */

    protected void addWordElement
    (
        String id ,
        String siblingID ,
        String text ,
        boolean blankPrecedes
    )
    {
                                //  Must have sibling ID to add word.

        if ( siblingID == null ) return;

                                //  Find parent element of sibling.
                                //  This will also be the parent for the
                                //  new word element we are adding.

        Element sibling = adornedWordMap.get( siblingID );
        Element parent  = sibling.getParentElement();

                                //  Remember the index in the parent
                                //  of the sibling element.

        int siblingIndex    = parent.indexOf( sibling );

                                //  Create a "w" or "pc" element,
                                //  depending upon the text value.
        Element wordElement;
        boolean addSpace    = false;

        if ( CharUtils.isPunctuation( text ) )
        {
            wordElement = createElement( "pc" );
        }
        else
        {
            wordElement = createElement( "w" );
//          addSpace    = true;
        }
                                //  Note if we have to add a blank
                                //  marker element before the newly
                                //  created word.

        addSpace    = blankPrecedes;

                                //  Set text value of newly created
                                //  word element.

        wordElement.setText( text );

                                //  Set ID of newly created word element.

        JDOMUtils.setAttributeValue( wordElement , "xml:id" , id );

                                //  Figure out if we're adding the new
                                //  word before or after the sibling
                                //  element.

        if ( Compare.compare( id , siblingID ) < 0 )
        {
                                //  Add new word before sibling.

            parent.addContent( siblingIndex , wordElement );

                                //  Add space element if necessary.
            if ( addSpace )
            {
                Element cElement    = createElement( "c" );

                cElement.setText( " " );

                parent.addContent( siblingIndex , cElement );
            }
        }
        else
        {
                                //  Add new word after sibling.

            parent.addContent( siblingIndex + 1 , wordElement );

                                //  Add space element if necessary.
            if ( addSpace )
            {
                Element cElement    = createElement( "c" );

                cElement.setText( " " );

                parent.addContent( siblingIndex + 1 , cElement );
            }
        }
                                //  Add new word to map and word ID list.

        adornedWordMap.put( id , wordElement );

        int index   = wordIDs.indexOf( siblingID );

        if ( index >= 0 )
        {
            if ( Compare.compare( id , siblingID ) < 0 )
            {
                wordIDs.add( index , id );
            }
            else
            {
                wordIDs.add( index + 1 , id );
            }
        }
    }

    /** Compress "<c>" elements.
     *
     *  <p>
     *  Deleting words may have left sequences of "<c> </c><c> </c> ..."
     *  elements.  Each such sequence should be compressed to a single
     *  "<c> </c>" element.
     *  </p>
     */

    protected void compressCElements()
    {
                                //  Filter to extract all elements.

        Filter<Element> filter      = Filters.element();

                                //  Get document root.

        Element root        = document.getRootElement();

                                //  Previous document element encountered.

        Element previousElement = null;

                                //  Iterator over all elements
                                //  in document.

        Iterator<Element> iterator  = root.getDescendants( filter );

                                //  Holds list of "<c>" elements
                                //  to delete.

        List<Element> cToDelete = ListFactory.createNewList();

                                //  Search for sequences of "<c> </c>"
                                //  and make list of extra ones to be
                                //  deleted.

        while ( iterator.hasNext() )
        {
                                //  Get next element.

            Element e   = iterator.next();

                                //  If this is a "c" element, and
                                //  the last element was a "c" element,
                                //  add this element to list of "c" elements
                                //  to delete.

            if ( previousElement != null )
            {
                if ( e.getName().equals( "c" ) )
                {
                    if ( e.getText().equals( " " ) )
                    {
                        if ( previousElement.getName().equals( "c" ) )
                        {
                            if ( previousElement.getText().equals( " " ) )
                            {
                                cToDelete.add( e );
                            }
                        }
                    }
                }
            }

            previousElement = e;
        }
                                //  Delete extraneous "c" elements.

        for ( int i = 0 ; i < cToDelete.size() ; i++ )
        {
            Element c       = cToDelete.get( i );

            Parent parent   = c.getParent();

            if ( parent != null )
            {
                parent.removeContent( c );
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



