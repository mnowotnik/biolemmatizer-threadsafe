package edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter;

/*  Please see the license information at the end of this file. */

import java.util.List;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.logger.*;

import edu.northwestern.at.morphadorner.corpuslinguistics.stemmer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.stopwords.*;

/** Base class for segmenting a text.
 */

abstract public class AbstractTextSegmenter
    extends IsCloseableObject
    implements TextSegmenter, IsCloseable, UsesLogger
{
    /** Logger used for output. */

    protected Logger logger;

    /** Stop words. */

    protected StopWords stopWords   = new ChoiStopWords();

    /** Stemmer. */

    protected Stemmer stemmer       = new PorterStemmer();

    /** Create a text segmenter.
     */

    public AbstractTextSegmenter()
    {
                                //  Create dummy logger.

        logger  = new DummyLogger();
    }

    /** Get the logger.
     *
     *  @return     The logger.
     */

    public Logger getLogger()
    {
        return logger;
    }

    /** Set the logger.
     *
     *  @param  logger      The logger.
     */

    public void setLogger( Logger logger )
    {
        this.logger = logger;
    }

    /** Get the stop words.
     *
     *  @return     The stop words.
     */

    public StopWords getStopWords()
    {
        return stopWords;
    }

    /** Set stop word set.
     *
     *  @param  stopWords       The stop words.
     */

    public void setStopWords( StopWords stopWords )
    {
        this.stopWords  = stopWords;
    }

    /** Get the stemmer.
     *
     *  @return     The stemmer.
     */

    public Stemmer getStemmer()
    {
        return stemmer;
    }

    /** Set stop word set.
     *
     *  @param  stemmer     The stemmer.
     */

    public void setStemmer( Stemmer stemmer )
    {
        this.stemmer    = stemmer;
    }

    /** Segment text.
     *
     *  @param  sentences   The list of tokenized sentences to segment.
     *
     *  @return             A list of sentence indices which start
     *                      a new text segment.
     */

    abstract public <T> List<Integer> getSegmentPositions
    (
        List<List<T>> sentences
    );
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



