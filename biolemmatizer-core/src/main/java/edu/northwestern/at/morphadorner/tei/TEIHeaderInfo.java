package edu.northwestern.at.morphadorner.tei;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;
import javax.xml.stream.*;

import edu.northwestern.at.utils.*;

/** Extract selected "teiHeader" information from a TEI file.
 */

public class TEIHeaderInfo
{
    /** File name. */

    String fileName = null;

    /** Title. */

    String title    = null;

    /** List of authors. */

    protected List<TEIHeaderAuthor> authorList  =
        new ArrayList<TEIHeaderAuthor>();

    /** Current author map. */

    protected Map<String, String> authorMap = null;

    /** Get bibliographic information from TEI header section.
     *
     *  @param  teiXMLFileName  The TEI XML file name.
     */

    public TEIHeaderInfo( String teiXMLFileName )
    {
        this.fileName   =
            FileNameUtils.stripPathName( teiXMLFileName );

        this.fileName   =
            FileNameUtils.changeFileExtension( this.fileName , "" );

        parseXML( teiXMLFileName );
    }

    /** Get file name.
     *
     *  @return     The file name.
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
        return safeString( title );
    }

    /** Return author list.
     *
     *  @return     authorList      List of author maps.
     */

    public List<TEIHeaderAuthor> getAuthors()
    {
        return authorList;
    }

    /** Parse adorned file.
     *
     *  @param  xmlFile     Bibadorned XML file name.
     */

    protected void parseXML( String xmlFile )
    {
        try
        {
            FileInputStream fileInputStream =
                new FileInputStream( xmlFile );

            XMLStreamReader parser =
                XMLInputFactory.newInstance().createXMLStreamReader(
                    fileInputStream );

            int inBiblFull          = 0;
            int inTitleStmt         = 0;
            int inAuthor            = 0;
            int inTitle             = 0;
            boolean seenBiblFull    = false;
            boolean done            = false;
            String currentElement   = "";

            for (   int event = parser.next() ;
                    !done && ( event != XMLStreamConstants.END_DOCUMENT ) ;
                    event = parser.next()
                )
            {
                switch ( event )
                {
                    case XMLStreamConstants.START_ELEMENT:
                    {
                        currentElement  = parser.getLocalName();

                        if ( isBiblFull( currentElement ) )
                        {
                            inBiblFull++;
                            seenBiblFull    = true;
                        }
                        else if ( isTitleStmt( currentElement ) )
                        {
                            if ( inBiblFull > 0 )
                            {
                                inTitleStmt++;
                            }
                        }
                        else if ( isTitle( currentElement ) )
                        {
                            if ( inTitleStmt > 0 )
                            {
                                title   = "";
                                inTitle++;
                            }
                        }
                        else if ( isAuthor( currentElement ) )
                        {
                            if ( inTitleStmt > 0 )
                            {
                                authorMap   = new TreeMap<String, String>();
                                authorMap.put( "name" , "" );
                                inAuthor++;
                            }
                        }
                    }
                    break;

                    case XMLStreamConstants.END_ELEMENT:
                    {
                        String localName    = parser.getLocalName();

                        if ( isBiblFull( localName ) )
                        {
                            inBiblFull--;
                        }

                        if ( isTitleStmt( localName ) && ( inBiblFull > 0 ) )
                        {
                            inTitleStmt--;
                        }

                        if ( inBiblFull <= 0 )
                        {
                            done = seenBiblFull;
                        }
                        else
                        {
                            if ( isAuthor( localName ) )
                            {
                                inAuthor--;

                                if ( authorMap != null )
                                {
                                    authorList.add
                                    (
                                        new TEIHeaderAuthor( authorMap )
                                    );
                                }

                                authorMap   = null;
                            }
                            else if ( isTitle( localName ) )
                            {
                                inTitle--;
                            }
                        }

                        currentElement  = "";
                    }
                    break;

                    case XMLStreamConstants.CDATA:
                    case XMLStreamConstants.CHARACTERS:
                    {
                        if  (   ( inBiblFull > 0 ) &&
                                ( currentElement.length() > 0 )
                            )
                        {
                            if ( inAuthor > 0 )
                            {
                                authorMap.put
                                (
                                    "name" ,
                                    authorMap.get( "name" ) +
                                        parser.getText()
                                );
                            }
                            else if ( inTitle > 0 )
                            {
                                title   += parser.getText();
                            }
                        }
                    }
                    break;
                }
            }

            parser.close();
        }
        catch ( java.io.IOException e )
        {
            System.err.println( e );
        }
        catch ( Exception e )
        {
            System.err.println( e );
        }
    }

    /** Determine if this is a biblFull element or not.
     *
     *  @param  name    tag name
     *  @return         true if tag name is "biblFull".
     */

    protected static boolean isBiblFull( String name )
    {
//      return name.equals( "biblFull" );
        return name.equals( "fileDesc" );
    }

    /** Determine if this is a titleStmt element or not.
     *
     *  @param  name    tag name
     *  @return         true if tag name is "titleStmt".
     */

    protected static boolean isTitleStmt( String name )
    {
        return name.equals( "titleStmt" );
    }

    /** Determine if this is a title element or not.
     *
     *  @param  name    tag name
     *  @return         true if tag name is "title".
     */

    protected static boolean isTitle( String name )
    {
        return name.equals( "title" );
    }

    /** Determine if this is an author element or not.
     *
     *  @param  name    tag name
     *  @return         true if tag name is "author".
     */

    protected static boolean isAuthor( String name )
    {
        return name.equals( "author" );
    }

    /** Return string ensuring null is set to empty string.
     *
     *  @param  s   String to check.
     *
     *  @return     Original value of "s" if s is not null,
     *              or empty string if "s" is null.
     */

    protected String safeString( String s )
    {
        return ( s == null ) ? "" : s;
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



