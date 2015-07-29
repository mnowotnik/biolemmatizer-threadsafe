package edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter;

/*  Please see the license information at the end of this file. */

import java.util.*;

import edu.northwestern.at.utils.ListFactory;
import edu.northwestern.at.utils.SetFactory;
import edu.northwestern.at.morphadorner.corpuslinguistics.stopwords.*;

import edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter.struct.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter.texttiling.*;

/** Marti Hearst's TextTiling linear text segmenter.
 */

public class TextTilingTextSegmenter
    extends AbstractTextSegmenter
    implements TextSegmenter
{
    /** Sliding window size. */

    protected int slidingWindowSize = 10;

    /** Step size. */

    protected int stepSize          = 100;

    /** Create segmenter.
     */

    public TextTilingTextSegmenter()
    {
        super();
    }

    /** Get sliding window size.
     *
     *  @return     Sliding window size.
     */

    public int getSlidingWindowSize()
    {
        return slidingWindowSize;
    }

    /** Set sliding window size.
     *
     *  @param  slidingWindowSize   The sliding window size.
     */

    public void setSlidingWindowSize( int slidingWindowSize )
    {
        this.slidingWindowSize  = slidingWindowSize;
    }

    /** Get step size.
     *
     *  @return     The step size.
     */

    public int getStepSize()
    {
        return stepSize;
    }

    /** Set step size.
     *
     *  @param  stepSize    The step size.
     */

    public void setStepSize( int stepSize )
    {
        this.stepSize   = stepSize;
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
        RawText c           = new RawText( sentences );

        TextTiling tiler    = new TextTiling( c , stopWords );

        tiler.setWindowSize( slidingWindowSize );
        tiler.setStepSize( stepSize );

        tiler.similarityDetermination();
        tiler.depthScore();
        tiler.boundaryIdentification();

        List<Integer> sentenceBoundaries    = c.boundaries;

        List<Integer> result        = ListFactory.createNewList();
        List<Integer> segmentation  = tiler.getSegmentation();

        result.add( 0 );

        for ( int i = 1 ; i < sentenceBoundaries.size() ; i++ )
        {
                                //  Get sentence boundaries for next segment.

            int start   = sentenceBoundaries.get( i - 1 );
            int end     = sentenceBoundaries.get( i );

                                //  If start is a topic boundary, add its
                                //  index to result list.

            if ( segmentation.contains( start ) )
            {
                result.add( i - 1 );
            }
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



