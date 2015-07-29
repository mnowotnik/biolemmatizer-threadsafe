package edu.northwestern.at.morphadorner.tei;

/*  Please see the license information at the end of this file. */

import java.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.northwestern.at.utils.MapFactory;

/** Extract selected "monkHeader" information from a MorphAdorned file.
 *
 *  <p>
 *  The "monkHeader" was a custom extension to the TEI XML format
 *  used by the Monk project.
 *  </p>
 */

public class BibadornedInfo
{
    /* File name. */

    protected String fileName;

    /* Title. */

    protected String title;

    /* Author name. */

    protected String name;

    /** Circulation year. */

    protected String circulationYear;

    /** Genre. */

    protected String genre;

    /** Subgenre. */

    protected String subgenre;

    /** Availability. */

    protected String availability;

    /** True if in Monk header. */

    protected boolean inMonkHeader;

    /** True if Monk header found. */

    protected boolean monkHeaderFound   = false;

    /** List of authors. */

    protected List<TEIHeaderAuthor> authorList  =
        new ArrayList<TEIHeaderAuthor>();

    /** Current author map. */

    protected Map<String, String> authorMap = null;

    /** Get Bibadorned bibliographic information.
     *
     *  @param  bibadornedXMLFile   The bibadorned XML file.
     */

    public BibadornedInfo( String bibadornedXMLFile )
    {
        parseXML( bibadornedXMLFile );
    }

    /** Get file name.
     *
     *  @return     The work's file name.
     */

    public String getFileName()
    {
        return fileName;
    }

    /** Get title.
     *
     *  @return     The work's title.
     */

    public String getTitle()
    {
        return title;
    }

    /** Return author list.
     *
     *  @return     authorList      List of author maps.
     */

    public List<TEIHeaderAuthor> getAuthors()
    {
        return authorList;
    }

    /** Get genre.
     *
     *  @return     Genre of work.
     */

    public String getGenre()
    {
        return genre;
    }

    /** Get subgenre.
     *
     *  @return     Subgenre of work.
     */

    public String getSubgenre()
    {
        return subgenre;
    }

    /** Get circulation yeaar.
     *
     *  @return     Circulation year.
     */

    public String getCirculationYear()
    {
        return circulationYear;
    }

    /** Get availability.
     *
     *  @return     Availability of work.
     */

    public String getAvailability()
    {
        return availability;
    }

    /** Check if Monk header found.
     *
     *  @return     true if Monk header seen, false otherwise.
     */

    public boolean getMonkHeaderFound()
    {
        return monkHeaderFound;
    }

    /** Parse input file looking for Monk header.
     *
     *  @param  xmlFile     The XML file name to parse.
     */

    public void parseXML( String xmlFile )
    {
        DefaultHandler handler =
            new DefaultHandler()
            {
                boolean inAuthor    = false;
                boolean isName      = false;
                boolean isFileName  = false;
                boolean isTitle     = false;
                boolean isGenre     = false;
                boolean isSubgenre  = false;
                boolean isCircYear  = false;

                boolean inMonkHeader        = false;

                public void startElement
                (
                    String uri ,
                    String localName ,
                    String qName ,
                    Attributes attributes
                )
                    throws SAXException
                {
                                //  If we hit the text element,
                                //  quit, as the Monk header should
                                //  already have been seen.

                    if ( qName.equals( "text" ) )
                    {
                        throw new SAXException( "monkHeader not found" );
                    }

                    if ( qName.equals( "monkHeader" ) )
                    {
                        inMonkHeader    = true;
                        monkHeaderFound = true;
                    }

                    if ( inMonkHeader )
                    {
                        isFileName  = qName.equals( "fileName" );
                        isTitle     = qName.equals( "title" );
                        isGenre     = qName.equals( "genre" );
                        isSubgenre  = qName.equals( "subgenre" );
                        isCircYear  = qName.equals( "circulationYear" );

                        if ( qName.equals( "author" ) )
                        {
                            inAuthor    = true;
                        }

                        if ( inAuthor )
                        {
                            isName  = qName.equals( "name" );
                        }
                    }
                }

                public void endElement
                (
                    String uri ,
                    String localName ,
                    String qName
                )
                    throws SAXException
                {
                    if ( qName.equals( "monkHeader" ) )
                    {
                        inMonkHeader    = false;

                        throw new SAXException( "monkHeader done" );
                    }
                    else if ( qName.equals( "author" ) )
                    {
                        inAuthor    = false;
                    }
                }

                public void characters
                (
                    char ch[] ,
                    int start ,
                    int length
                )
                    throws SAXException
                {
                    if ( isName )
                    {
                        name    = new String( ch , start , length );
                        isName  = false;

                        Map<String, String> nameMap =
                            MapFactory.createNewMap();

                        nameMap.put( "name" , name );

                        authorList.add( new TEIHeaderAuthor( nameMap ) );
                    }

                    if ( isFileName )
                    {
                        fileName    = new String( ch , start , length );
                        isFileName  = false;
                    }

                    if ( isTitle )
                    {
                        title   = new String( ch , start , length );
                        isTitle = false;
                    }

                    if ( isCircYear )
                    {
                        circulationYear = new String( ch , start , length );
                        isCircYear      = false;
                    }

                    if ( isGenre )
                    {
                        genre   = new String( ch , start , length );
                        isGenre = false;
                    }

                    if ( isSubgenre )
                    {
                        subgenre    = new String( ch , start , length );
                        isSubgenre  = false;
                    }
                }
            };

        try
        {
            SAXParserFactory factory    =
                SAXParserFactory.newInstance();

            SAXParser saxParser = factory.newSAXParser();

            monkHeaderFound = false;

            saxParser.parse( xmlFile , handler );
        }
        catch ( Exception e )
        {
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



