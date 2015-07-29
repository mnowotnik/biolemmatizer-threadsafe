package edu.northwestern.at.morphadorner.corpuslinguistics.hyphenator;

/*  Please see the license information at the end of this file. */

import java.io.*;

import edu.northwestern.at.utils.*;
import net.davidashen.text.Hyphenator;

/** BaseHyphenator: Hyphenates words using TeX rule sets.
 */

public class BaseHyphenator
    implements edu.northwestern.at.morphadorner.corpuslinguistics.hyphenator.Hyphenator
{
    /** UK Base hyphenation rules in TeX format. */

    protected static String ukhyphenPath =
        "resources/ukhyphen.tex";

    /** US Base hyphenation rules in TeX format. */

    protected static String ushyphenPath =
        "resources/hyphen.tex";

    /** The internal TeX-based hyphenator. */

    protected net.davidashen.text.Hyphenator texHyphenator;

    /** Create a base hyphenator using UK English rules. */

    public BaseHyphenator()
    {
        try
        {
            texHyphenator   = new net.davidashen.text.Hyphenator();

//          texHyphenator.setErrorHandler( new MyErrorHandler() );

            texHyphenator.loadTable
            (
                BaseHyphenator.class.getResourceAsStream( ukhyphenPath )
            );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /** Load TeX format hyphenation rules from a stream.
     *
     *  @param  hyphenRulesStream   Stream with TeX format hyphenation rules.
     */

    public void loadHyphenationRules( InputStream hyphenRulesStream )
        throws Exception
    {
        texHyphenator.loadTable( hyphenRulesStream );
    }

    /** Load TeX format hyphenation rules from a file.
     *
     *  @param  hyphenRulesFileName File with TeX format hyphenation rules.
     */

    public void loadHyphenationRules( String hyphenRulesFileName )
        throws Exception
    {
        FileInputStream fis = new FileInputStream( hyphenRulesFileName );

        texHyphenator.loadTable( fis );

        fis.close();
    }

    /** Add hyphenation points to a single Base word.
     *
     *  @param  word    The word to hyphenate.
     *
     *  @return         The word with hyphenation points added.
     */

    public String hyphenate( String word )
    {
        return texHyphenator.hyphenate( word );
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



