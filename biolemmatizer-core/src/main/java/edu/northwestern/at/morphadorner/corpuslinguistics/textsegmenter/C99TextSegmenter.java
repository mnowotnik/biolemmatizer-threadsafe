package edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter;

/*  Please see the license information at the end of this file. */

import java.util.List;
import edu.northwestern.at.utils.ListFactory;
import edu.northwestern.at.morphadorner.corpuslinguistics.stemmer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.stopwords.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter.c99.C99;

/** Freddy Choi's C99 linear text segmenter.
 */

public class C99TextSegmenter
    extends AbstractTextSegmenter
    implements TextSegmenter
{
    /** Mask size. */

    protected int maskSize  = 11;

    /** Number of segments wanted. */

    protected int segmentsWanted    = -1;

    /** Create C99 segmenter.
     */

    public C99TextSegmenter()
    {
        super();
    }

    /** Get mask size.
     *
     *  @return     Mask size.
     */

    public int getMaskSize()
    {
        return maskSize;
    }

    /** Set mask size.
     *
     *  @param  maskSize    The mask size.
     */

    public void setMaskSize( int maskSize )
    {
        this.maskSize   = maskSize;
    }

    /** Get number of segments wanted.
     *
     *  @return     Number of segments wanted.
     */

    public int getSegmentsWanted()
    {
        return segmentsWanted;
    }

    /** Set segments wanted.
     *
     *  @param  segmentsWanted  The number of segments wanted.
     */

    public void setSegmentsWanted( int segmentsWanted )
    {
        this.segmentsWanted = segmentsWanted;
    }

    /** Segment text.
     *
     *  @param  sentences   The list of tokenized sentences to segment.
     *
     *  @return             A list of sentence indices which start
     *                      a new text segment.
     */

    public <T> List<Integer> getSegmentPositions( List<List<T>> sentences )
    {
        String[][] docSentences = new String[ sentences.size() ][];

        for ( int i = 0 ; i < sentences.size() ; i++ )
        {
            List<T> sentence    = sentences.get( i );

            docSentences[ i ]   = new String[ sentence.size() ];

            for ( int j = 0 ; j < sentence.size() ; j++ )
            {
                docSentences[ i ][ j ]  = sentence.get( j ).toString();
            }
        }

        String[][][] segments   =
            C99.segment
            (
                docSentences ,
                segmentsWanted ,
                maskSize ,
                stopWords ,
                stemmer
            );

        int segmentCount        = segments.length;
        int sentenceIndex       = 0;
        List<Integer> result    = ListFactory.createNewList();

        for ( int i = 0 ; i < segmentCount ; i++ )
        {
            result.add( sentenceIndex );

            sentenceIndex   += segments[ i ].length;
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



