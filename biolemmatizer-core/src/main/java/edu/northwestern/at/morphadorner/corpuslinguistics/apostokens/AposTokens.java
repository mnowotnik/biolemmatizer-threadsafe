package edu.northwestern.at.morphadorner.corpuslinguistics.apostokens;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import edu.northwestern.at.utils.*;

/** Tokens which start or end with an apostrophe.
 */

public class AposTokens
{
                                //  Set of tokens which begin or end
                                //  with an apostrophe.

    protected Set<String> aposTokens    = null;

                                //  Path to apos tokens list resource.

    protected final static String defaultAposTokensFileName =
        "resources/en-apostokens.txt";

    /** Create AposTokens.  Assumes English.
     */

    public AposTokens()
    {
        aposTokens  = loadAposTokensFromResource( "en" );
    }

    /** Create AposTokens for specified ISO language code.
     *
     *  @param  langCode    ISO language code.
     */

    public AposTokens( String langCode )
    {
        aposTokens  = loadAposTokensFromResource( langCode );
    }

    /** Load apostrophe tokens set from resource properties file.
     *
     *  <p>
     *  Each line in the UTF8 apostrophe tokens contains a single
     *  tokens which begins or ends with an apostrophe.
     *
     *  <p>
     *  if there is not a resource file for the given language code,
     *  the aposTokens set will be empty.
     *  </p>
     */

    public static Set<String> loadAposTokensFromResource
    (
        String langCode
    )
    {
                                //  Create properties object to
                                //  hold apostophe tokens.

        Set<String> result  = SetFactory.createNewSet();

                                //  Load apostrophe tokens from
                                //  resource file.

        try
        {
            SetUtils.loadIntoSet
            (
                result ,
                AposTokens.class.getResource
                (
                    "resources/" + langCode + "-apostokens.txt"
                ) ,
                "utf-8"
            );
        }
        catch ( IOException ioe )
        {
//          ioe.printStackTrace();
        }

        return result;
    }

    /** Load aposTokens list from a file.
     *
     *  @param  aposTokensURL       AposTokens URL.
     *
     *  @return                     true if aposTokens loaded OK,
     *                              false if error occurred.
     */

    public boolean loadAposTokens( String aposTokensURL )
    {
        boolean result  = false;

                                //  Create properties object to
                                //  hold aposTokens if not
                                //  already created.

        if ( aposTokens == null )
        {
            aposTokens  = SetFactory.createNewSet();
        }
                                //  Load aposTokens from file.
        try
        {
            aposTokens  = SetUtils.loadSet( aposTokensURL , "utf-8" );

            result  = true;
        }
        catch ( IOException ioe )
        {
//          ioe.printStackTrace();
        }

        return result;
    }

    /** Checks if string is a known token with apostrophe.
     *
     *  @param  str     The string to check.
     *
     *  @return         true if "str" is on the known apostrophe tokens list.
     */

    public boolean isKnownAposToken( String str )
    {
        return aposTokens.contains( str.toLowerCase() );
    }

    /** Get count of known aposrophe Tokens.
     *
     *  @return     Count of known apostrophe tokens.
     */

    public int getAposTokensCount()
    {
        int result  = 0;

        if ( aposTokens != null )
        {
            result  = aposTokens.size();
        }

        return result;
    }

    /** Return current aposTokens.
     *
     *  @return     AposTokens.
     */

    public Set<String> getAposTokens()
    {
        return aposTokens;
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



