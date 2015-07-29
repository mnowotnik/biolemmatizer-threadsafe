package edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer;

/*  Please see the license information at the end of this file. */

import java.io.*;

import java.util.*;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.RuleBasedBreakIterator;

import edu.northwestern.at.morphadorner.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.abbreviations.*;
import edu.northwestern.at.utils.*;

/** Word tokenizer which uses ICU library for tokenization. */

public class ICU4JBreakIteratorWordTokenizer
    extends AbstractWordTokenizer
    implements WordTokenizer, CanTokenizeWhitespace, CanSplitAroundPeriods
{
    /** Locale. */

    protected Locale locale = Locale.US;

    /** Store whitespace tokens. */

    protected boolean storeWhitespaceTokens = false;

    /** Merge whitespace tokens. */

    protected boolean mergeWhitespaceTokens = false;

    /** Check for potential splitting of tokens around periods. */

    protected boolean splitAroundPeriods    = true;

    /** The word based break iterator. */

    protected BreakIterator wordIterator = null;

    /** Word break rules template file. */

    protected String wordBreakRulesFileName = "resources/wordbreakrules.txt";

    /** Create a word tokenizer that uses the ICU4J word break iterator.
     */

    public ICU4JBreakIteratorWordTokenizer()
    {
        super();

        createWordIterator();
    }

    /** Create a word tokenizer that uses the ICU4J word break iterator.
     *
     *  @param  locale  Locale to use for tokenization.
     */

    public ICU4JBreakIteratorWordTokenizer( Locale locale )
    {
        super();

        this.locale = locale;

        createWordIterator();
    }

    /** Get store whitespace tokens. */

    public boolean getStoreWhitespaceTokens()
    {
        return storeWhitespaceTokens;
    }

    /** Set store whitespace tokens. */

    public void setStoreWhitespaceTokens( boolean storeWhitespaceTokens )
    {
        this.storeWhitespaceTokens  = storeWhitespaceTokens;
    }

    /** Get merge whitespace tokens. */

    public boolean getMergeWhitespaceTokens()
    {
        return mergeWhitespaceTokens;
    }

    /** Set merge whitespace tokens. */

    public void setMergeWhitespaceTokens( boolean mergeWhitespaceTokens )
    {
        this.mergeWhitespaceTokens  = mergeWhitespaceTokens;
    }

    /** Get splitting around periods. */

    public boolean getSplitAroundPeriods()
    {
        return splitAroundPeriods;
    }

    /** Set splitting around periods. */

    public void setSplitAroundPeriods( boolean splitAroundPeriods )
    {
        this.splitAroundPeriods = splitAroundPeriods;
    }

    /** Create word based break iterator.
     */

    protected void createWordIterator()
    {
                                //  Create noop pretokenizer.

        preTokenizer    = new NoopPreTokenizer();

                                //  Create a word-based break iterator.

        String abbrevsPattern   =
            Abbreviations.createAbbreviationsPattern( null );

        Reader reader   = null;

        try
        {
            reader  =
                new UnicodeReader
                (
                    ICU4JBreakIteratorWordTokenizer.class.getResourceAsStream
                    (
                        wordBreakRulesFileName
                    ),
                    "utf-8"
                );

            String wordBreakRules   = FileUtils.readTextFile( reader );

            reader.close();

            wordBreakRules  =
                StringUtils.replaceAll
                (
                    wordBreakRules ,
                    "%abbreviations%" ,
                    abbrevsPattern
                );

            wordIterator    = new RuleBasedBreakIterator( wordBreakRules );
        }
        catch ( Exception e )
        {
            wordIterator = BreakIterator.getWordInstance( locale );
        }
        finally
        {
            try
            {
                if ( reader != null )
                {
                    reader.close();
                }
            }
            catch ( Exception e2 )
            {
            }
        }
    }

    /** Break text into word tokens.
     *
     *  @param  text            Text to break into word tokens.
     *
     *  @return                 Input text broken into list of tokens.
     */

     public List<String> extractWords( String text )
     {
                                //  Create list to hold extracted tokens.

        List<String> result = ListFactory.createNewList();

                                //  Set the text to tokenize into the
                                //  break iterator.

        String fixedText    = preTokenizer.pretokenize( text );

        wordIterator.setText( fixedText );

                                //  Find the start and end of the
                                //  first token.

        int start   = wordIterator.first();
        int end     = wordIterator.next();

                                //  While there are tokens left to
                                //  extract ...

        while ( end != BreakIterator.DONE )
        {
                                //  Get text for next token.

            String token    = fixedText.substring( start , end );

                                //  Check if token is whitespace.

            if ( Character.isWhitespace( token.charAt( 0 ) ) )
            {
                                //  Check if we're storing whitespace.

                if ( storeWhitespaceTokens )
                {
                                //  Check if we're merging a sequence
                                //  of whitespace tokens into a single
                                //  token.

                    if ( mergeWhitespaceTokens && ( result.size() > 1 ) )
                    {
                                //  Merging whitespace token.  Get
                                //  previous token.

                        String prevToken    =
                            result.get( result.size() - 1 );

                                //  If the previous token is whitespace,
                                //  append the current whitespace token
                                //  and replace the previous token with
                                //  the merged token.

                        if ( Character.isWhitespace( prevToken.charAt( 0 ) ) )
                        {
                            result.set
                            (
                                result.size() - 1 ,
                                prevToken + token
                            );
                        }
                                //  If the previous token was not
                                //  whitespace, store this whitespace
                                //  token.
                        else
                        {
                            addWordToSentence( result , token );
                        }
                    }
                                //  Not merging whitespace tokens.
                                //  Just add this whitespace token
                                //  to the list of tokens.
                    else
                    {
                        addWordToSentence( result , token );
                    }
                }
                                //  Not storing whitespace tokens?
                                //  Ignore this whitespace token.
                else
                {
                }
            }
                                //  Token is not whitespace.
            else
            {
                token   = preprocessToken( token , result );

                                //  If the token is not empty,
                                //  add it to the sentence.

                if ( token.length() > 0 )
                {
                                //  Check if we need to split a token
                                //  containing an internal period.

                    if ( splitAroundPeriods )
                    {
                        String[] tokens = splitToken( token );

                        for ( int k = 0 ; k < tokens.length ; k++ )
                        {
                            if ( tokens[ k ].length() > 0 )
                            {
                                addWordToSentence( result ,  tokens[ k ] );
                            }
                        }
                    }
                                //  Just store token if not checking
                                //  for splits around periods.
                    else
                    {
                        addWordToSentence( result , token );
                    }
                }
            }
                                //  Find start and end of next token
                                //  if any.
            start   = end;
            end     = wordIterator.next();
        }
                                //  Return result.
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



