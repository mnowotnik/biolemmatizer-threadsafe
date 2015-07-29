package edu.northwestern.at.morphadorner.tei;

/*  Please see the license information at the end of this file. */

import java.util.*;

/** Holds selected "TEIHeader" author information from an adorned TEI XML file.
 */

public class TEIHeaderAuthor implements Comparable
{
    /** Author map. */

    protected Map<String, String> authorMap = null;

    /** Create author map.
     *
     *  @param  authorMap   The author map.
     */

    public TEIHeaderAuthor
    (
        Map<String, String> authorMap
    )
    {
        this.authorMap  = authorMap;
    }

    /** Get author name.
     *
     *  @return     The name of the work's author.
     */

    public String getName()
    {
        return safeString( authorMap.get( "name" ) );
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

    /** Return author name as string.
     *
     *  @return     Author name.
     */

    public String toString()
    {
        return getName();
    }

    /** Compare this object with another.
     *
     *  @param  object  The other object.
     *
     *  @return         < 0 if the other object is less than this one,
     *                  = 0 if the two objects are equal,
     *                  > 0 if the other object is greater than this one.
     */

    public int compareTo( Object object )
    {
        int result  = 0;

        if ( ( object == null ) ||
            !( object instanceof TEIHeaderAuthor ) )
        {
            result  = Integer.MIN_VALUE;
        }
        else
        {
            result  = compare( this , (TEIHeaderAuthor)object );
        }

        return result;
    }

    /** Compare two authors for order.
     *
     *  @param  a1  First author.
     *  @param  a2  Second author.
     *
     *  @return     < 0 if the other object is less than this one,
     *              = 0 if the two objects are equal,
     *              > 0 if the other object is greater than this one.
     */

    public int compare( TEIHeaderAuthor a1 , TEIHeaderAuthor a2 )
    {
        int result  = a1.getName().compareTo( a2.getName() );

        return result;
    }

    /** Two authors are the same if all their details agree.
     *
     *  @param  obj     Object to compare for equality to this one,
     *
     *  @return         True if the two authors are the same.
     *
     *  <p>Two authors are assumed to be the same if all their
     *  details agree.</p>
     */

    public boolean equals( Object obj )
    {
        boolean result  = false;

        if ( obj instanceof TEIHeaderAuthor )
        {
            TEIHeaderAuthor otherAuthor = (TEIHeaderAuthor)obj;

            result  =
                result && getName().equals( otherAuthor.getName() );
        }

        return result;
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



