package edu.northwestern.at.morphadorner.corpuslinguistics.postagger.tcpretagger;

/*  Please see the license information at the end of this file. */

import java.util.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.adornedword.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.partsofspeech.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.unigram.*;

/** TCP text retagger.
 *
 *  <p>
 *  This retagger applies a short list of rules to improve tagging
 *  in TCP texts (ECCO, Evans, EEBO).
 *  </p>
 */

public class TCPRetagger
    extends UnigramTagger
    implements PartOfSpeechRetagger
{
    /** Part of speech tags. */

    protected static PartOfSpeechTags posTags;

    /** Create TCP retagger.
     */

    public TCPRetagger()
    {
        super();
    }

    /** Retag a sentence.
     *
     *  @param  sentence    The sentence as an
     *                      {@link edu.northwestern.at.morphadorner.corpuslinguistics.adornedword.AdornedWord} .
     *
     *  @return             The sentence with words retagged.
     */

    @SuppressWarnings("unchecked")
    public<T extends AdornedWord> List<T> retagSentence
    (
        List<T> sentence
    )
    {
                                //  Get noun part of speech tags
                                //  if we don't already have them.

        if ( posTags == null )
        {
            posTags = getLexicon().getPartOfSpeechTags();
        }
                                //  Get proper noun part of speech.

        String properNounTag    = posTags.getSingularProperNounTag();

                                //  Look for "Great Britain" sequences
                                //  and tag both words as proper nouns.
        int i = 1;

        while ( i < sentence.size() )
        {
                                //  Get next word in sentence.

            T adornedWord   = sentence.get( i );

                                //  Is spelling "Britain"?

            if ( adornedWord.getSpelling().equalsIgnoreCase( "britain" ) )
            {
                                //  Was previous word "Great"?

                T prevAdornedWord   = sentence.get( i - 1 );

                if ( prevAdornedWord.getSpelling().equalsIgnoreCase( "great" ) )
                {
                    adornedWord.setPartsOfSpeech( properNounTag );
                    prevAdornedWord.setPartsOfSpeech( properNounTag );
                }
            }

            i++;
        }
                                //  Return updated sentence.
        return sentence;
    }

    /** Can retagger add or delete words in the original sentence?
     *
     *  @return     true if retagger can add or delete words.
     */

    public boolean getCanAddOrDeleteWords()
    {
        return false;
    }

    /** Can retagger add or delete words in the original sentence?
     *
     *  @param  canAddOrDeleteWords     true if retagger can add or
     *                                  delete words.
     *
     *  <p>
     *  Ignored here.
     *  </p>
     */

    public void setCanAddOrDeleteWords( boolean canAddOrDeleteWords )
    {
    }

    /** Return retagger description.
     *
     *  @return     Retagger description.
     */

    public String toString()
    {
        return "TCP retagger";
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



