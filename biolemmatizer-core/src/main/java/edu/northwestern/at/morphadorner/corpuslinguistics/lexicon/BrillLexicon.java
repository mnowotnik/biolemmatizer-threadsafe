package edu.northwestern.at.morphadorner.corpuslinguistics.lexicon;

/*  Please see the license information at the end of this file. */

import java.util.*;
import java.io.*;
import java.net.URL;

import edu.northwestern.at.morphadorner.corpuslinguistics.outputter.*;

import edu.northwestern.at.utils.ListFactory;
import edu.northwestern.at.utils.UnicodeReader;

/** Brill format lexicon.
 *
 *  <p>
 *  A Brill lexicon is a {@link java.util.HashMap} that maps from
 *  a lexical entry ({@link java.lang.String}) to possible POS categories
 *  ({@link java.util.List}.
 *  </p>
 *
 *  <p>
 *  A Brill lexicon is a simple utf-8 formatted text file containing
 *  words and their possible part of speech tags.  Each word appears
 *  on a separate line. The first token on each line is the word.
 *  The remaining tokens are the potential parts of speech for the word,
 *  separated by blanks or tab characters.  The most commonly occurring
 *  part of speech should be the first one listed.
 *  </p>
 *
 *  <p>
 *  <code>
 *  word pos1 pos2 pos3 ...
 *  </code>
 *  </p>
 *
 *  <p>
 *  This type of lexicon format was popularized by Eric Brill's
 *  part of speech tagger in the early 1990s.
 *  </p>
 */

public class BrillLexicon extends HashMap<String, List<String>>
{
    /** Create a Brill lexicon.
     *
     *  @param  lexiconURL  URL for the file containing the lexicon.
     *  @param  encoding    Character encoding of lexicon file text.
     */

    public BrillLexicon( URL lexiconURL , String encoding )
        throws IOException
    {
        String line;

        BufferedReader lexiconReader;

        if ( encoding == null )
        {
            lexiconReader =
                new BufferedReader(
                    new UnicodeReader(
                        lexiconURL.openStream() ) );
        }
        else
        {
            lexiconReader   =
                new BufferedReader(
                    new UnicodeReader(
                        lexiconURL.openStream() , encoding ) );
        }

        line    = lexiconReader.readLine();

        String entry    = "";
        List<String> categories;

        while ( line != null )
        {
            line    = line.trim();

            if ( line.length() > 0 )
            {
                StringTokenizer tokens = new StringTokenizer( line );

                entry       = tokens.nextToken();
                categories  = ListFactory.createNewList();

                while ( tokens.hasMoreTokens() )
                {
                    categories.add( tokens.nextToken() );
                }

                put( entry , categories );
            }

            line = lexiconReader.readLine();
        }
    }

    /** Save Brill lexicon to a file.
     *
     *  @param  fileName    File name to which to save the Brill lexicon.
     *  @param  encoding    The file encoding (usually utf-8).
     */

    public void saveToFile( String fileName , String encoding )
        throws IOException
    {
                                //  Get an outputter that writes
                                //  blank separated values.

        AdornedWordOutputter outputter  =
            new PrintStreamAdornedWordOutputter();

        outputter.createOutputFile( fileName , encoding , ' ' );

                                //  Sort the words.

        Set<String> sortedWordSet   = new TreeSet<String>( keySet() );

                                //  Loop over words and write
                                //  parts of speech and lemmata for
                                //  each.

        for ( String word : sortedWordSet )
        {
                                //  Get list of parts of speech and
                                //  lemmata for this word.

            List<String> entry  = get( word );

                                //  Output parts of speech and lemmata.

            outputter.outputWordAndAdornments( entry );
        }
                                //  Close the outputter.
        outputter.close();
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



