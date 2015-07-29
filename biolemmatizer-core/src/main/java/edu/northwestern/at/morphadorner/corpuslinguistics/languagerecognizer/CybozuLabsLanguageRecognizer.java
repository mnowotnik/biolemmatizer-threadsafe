package edu.northwestern.at.morphadorner.corpuslinguistics.languagerecognizer;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.net.*;
import java.util.*;

import com.cybozu.labs.langdetect.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.logger.*;

/** Cybozu Labs Language Recognizer.
 */

public class CybozuLabsLanguageRecognizer
    extends AbstractLanguageRecognizer
{
    /** Create a language recognizer with the default language profiles.
     */

    public CybozuLabsLanguageRecognizer()
        throws LangDetectException
    {
//      DetectorFactory.loadDefaultProfiles();
        DetectorFactory.setSeed( 0 );
    }

    /** Create a language recognizer with list of languages to recognize.
     *
     *  @param  languages   List of names of languages to recognize.
     *
     *  <p>
     *  The list of languages references the profile names.
     *  These are usually two or three character ISO 696 language codes,
     *  e.g., "en" for English and "sco" for Scots dialect.
     *  </p>
     */

    public CybozuLabsLanguageRecognizer( List<String> languages )
        throws LangDetectException
    {
        DetectorFactory.loadProfilesByName( languages );
        DetectorFactory.setSeed( 0 );
    }

    /** Returns a scored list of possible languages for a text string.
     *
     *  @param  text    The text for which to determine the language.
     *
     *  @return         Array of ScoredList entries of language names and
     *                  scores sorted in descending order by score.
     *                  Null if language cannot be determined.
     */

     public ScoredString[] recognizeLanguage( String text )
     {
        ScoredString[] result   = null;

        List<ScoredString> resultList   =
            ListFactory.createNewList();

        try
        {
            Detector detector = DetectorFactory.create();

            detector.append( text );

            List<Language> languages    = detector.getProbabilities();

            for ( int i = 0 ; i < languages.size() ; i++ )
            {
                Language language   = languages.get( i );

                resultList.add
                (
                    new ScoredString( language.lang , language.prob )
                );
            }

            result  =
                (ScoredString[])resultList.toArray
                (
                    new ScoredString[ resultList.size() ]
                );
        }
        catch ( Exception e )
        {
//          e.printStackTrace();
            result  = null;
        }

        return result;
     }

    /** Get list of available languages for detection.
     *
     *  @return     String list of available language profile names.
     *              These are the names set when this recognizer
     *              was created.
     *
     *  <p>
     *  The profile names are generally the two or three
     *  character ISO 639 two or three character language
     *  codes.
     *  </p>
     */

    public List<String> getAvailableLanguageProfileNames()
    {
        return DetectorFactory.getLangList();
    }

    /** Get default list of languages for detection.
     *
     *  @return     String list of default language profile names.
     *
     *  <p>
     *  The profile names are generally the two or three
     *  character ISO 639 two or three character language
     *  codes.  The current language recognizer may not use all
     *  of these profiles, or may add extra custom profiles.
     *  </p>
     */

    public static List<String> getDefaultLanguageProfileNames()
    {
//      return DetectorFactory.getDefaultProfileNames();
        return DetectorFactory.getLangList();
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



