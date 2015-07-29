package edu.northwestern.at.utils.xml;

/*  Please see the license information at the end of this file. */

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Parse an XML fragment for use with a JDOM document.
 *
 *  @author Per Norrman.
 *
 *  <p>
 *  Minor modifications by Pib.
 *  </p>
 */

public class JDOMFragmentParser
{
    protected String _xml;
    protected SAXBuilder _builder;
    protected String _fragment = null;

    public JDOMFragmentParser( Namespace[] namespaces )
    {
        StringBuffer buf = new StringBuffer();

        buf.append
        (
            "<!DOCTYPE root [<!ENTITY fragment  SYSTEM 'file:fragment.xml' >]><root "
        );

        for ( int i = 0 ; i < namespaces.length ; i++ )
        {
            buf.append( "xmlns:" );
            buf.append( namespaces[ i ].getPrefix() );
            buf.append( "=\"" );
            buf.append( namespaces[ i ].getURI() );
            buf.append( "\" " );
        }

        buf.append( ">&fragment;</root>" );

        _xml = buf.toString();

        _builder = new SAXBuilder();

        _builder.setEntityResolver
        (
            new EntityResolver()
            {
                public InputSource resolveEntity
                (
                    String publicId ,
                    String systemId
                )
                    throws SAXException, IOException
                {
                    if ( _fragment != null )
                    {
                        return
                            new InputSource( new StringReader( _fragment ) );
                    }
                    else
                    {
                        return null;
                    }
                }
            }
        );
    }

    public List<Element> parseFragment( String fragment )
        throws Exception
    {
        try
        {
            _fragment = fragment;

            Document doc = _builder.build( new StringReader(_xml) );

            @SuppressWarnings("unchecked")
            List<Element> list =
                new ArrayList<Element>
                (
                    ((List<Element>)doc.getRootElement().getChildren())
                );

            for ( Iterator<Element> i = list.iterator(); i.hasNext(); )
            {
                i.next().detach();
            }

            _fragment = null;

            return list;
        }
        catch ( Exception e )
        {
            throw e;
        }
        finally
        {
            _fragment = null;
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



