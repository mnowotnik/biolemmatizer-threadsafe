package edu.northwestern.at.morphadorner.tools.punktabbreviationdetector;

/*  Please see the license information at the end of this file. */

/** A token for use by the Punkt abbreviation detection algorithm.
 */

public class PunktToken
{
    /** The text of the token. */

    protected String tokenText;

    /** The starting position of the token. */

    protected int startPosition;

    /** The Punkt token type. */

    protected PunktTokenType tokenType;

    /** Create a Punkt token.
     *
     *  @param  tokenText   The text of the token.
     *  @param  start       Starting position of the token.
     *  @param  tokenType   The type of the token.
     */

    public PunktToken( String tokenText , int start , PunktTokenType tokenType )
    {
        this.tokenText      = tokenText;
        this.startPosition  = start;
        this.tokenType      = tokenType;
    }

    /** Create a Punkt token.
     *
     *  @param  tokenText   The text of the token.
     *  @param  tokenType   The type of the token.
     */

    public PunktToken( String tokenText , PunktTokenType tokenType )
    {
        this.tokenText      = tokenText;
        this.startPosition  = 0;
        this.tokenType      = tokenType;
    }

    /** Get the token text.
     *
     *  @return     The token text.
     */

    public String getTokenText()
    {
        return tokenText;
    }

    /** Get the token type.
     *
     *  @return     The token type.
     */

    public PunktTokenType getTokenType()
    {
        return tokenType;
    }

    /** Get the starting position of the token.
     *
     *  @return     The starting position of the token.
     */

    public int getStartPosition()
    {
        return startPosition;
    }

    /** Get the ending position of the token.
     *
     *  @return     The ending position of the token.
     */

    public int getEndPosition()
    {
        return startPosition + tokenText.length();
    }

    /** Get the character code for a single character token.
     *
     *  @return     Unicode string for character.
     *              Null if token is not a single character.
     */

    public String getSingleCharCode()
    {
        String result   = null;

        if ( tokenText.length() == 1 )
        {
            result  =
                "u" + Integer.toHexString( (int)tokenText.charAt( 0 ) );
        }

        return result;
    }

    /** Get the length of the token.
     *
     *  @return     The length of the token.
     */

    public int getLength()
    {
        return tokenText.length();
    }

    /** Return token as a string.
     *
     *  @return     The token string.
     */

    public String toString()
    {
        return tokenText;
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




