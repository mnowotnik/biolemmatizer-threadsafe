package edu.northwestern.at.utils.xml.jdom;

/*  Please see the license information at the end of this file. */

import org.jdom2.Element;
import org.jdom2.filter.*;
import org.jdom2.Namespace;

/** A Filter that only matches one or more {@link org.jdom2.Element} objects.
 */

public class ElementsFilter extends AbstractFilter<Element>
{
    /** JDOM2 Serialization: Default mechanism  */

    private static final long serialVersionUID = 200L;

    /** The element names */

    private String[] names;

    /** The element namespace */

    private Namespace namespace;

    /** Select only the Elements.
     */

    public ElementsFilter()
    {
    }

    /** Select only the Elements with the supplied names in any Namespace.
     *
     * @param names   The names of the Element.
     */

    public ElementsFilter( String[] names )
    {
        this.names   = names;
    }

    /** Select only the Elements with the supplied Namespace.
     *
     * @param namespace The namespace the Element lives in.
     */

    public ElementsFilter( Namespace namespace )
    {
        this.namespace = namespace;
    }

    /** Select only the Elements with the supplied names and Namespace.
     *
     * @param names    The names of the Elements.
     * @param namespace The namespace the Elements live in.
     */

    public ElementsFilter( String[] names , Namespace namespace )
    {
        this.names      = names;
        this.namespace  = namespace;
    }

    /**
     * Check to see if the object matches a predefined set of rules.
     *
     * @param content The object to verify.
     * @return <code>true</code> if the objected matched a predfined
     *           set of rules.
     */

    @Override
    public Element filter( Object content )
    {
        if ( content instanceof Element )
        {
            Element el = (Element)content;

            if ( names == null )
            {
                if ( namespace == null )
                {
                    return el;
                }

                return namespace.equals( el.getNamespace() ) ? el : null;
            }

            boolean found   = false;
            String name     = el.getName();

            for ( int i = 0 ; i < names.length ; i++ )
            {
                found   = name.equals( names[ i ] );
                if ( found ) break;
            }

            if ( !found )
            {
                return null;
            }

            if ( namespace == null )
            {
                return el;
            }

            return namespace.equals( el.getNamespace() ) ? el : null;
        }

        return null;
    }

    /** Returns whether the two filters are equivalent (i&#46;e&#46; the
     *  matching names and namespace are equivalent).
     *
     * @param   obj Object to compare against
     * @return      true if the two filters are equal
     */

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( !( obj instanceof ElementsFilter ) ) return false;

        final ElementsFilter filter = (ElementsFilter)obj;

        if
        (
            ( names != null ) ?
            !names.equals( filter.names ) :
            ( filter.names != null )
        )
        {
            return false;
        }

        if
        (
            ( namespace != null ) ?
            !namespace.equals( filter.namespace ) :
            ( filter.namespace != null )
        )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = (names != null ? names.hashCode() : 0);
        result = 29 * result + (namespace != null ? namespace.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "[ElementFilter: Names " +
            ( names == null ? "*any*" : names ) +
            " with Namespace " + namespace + "]";
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



