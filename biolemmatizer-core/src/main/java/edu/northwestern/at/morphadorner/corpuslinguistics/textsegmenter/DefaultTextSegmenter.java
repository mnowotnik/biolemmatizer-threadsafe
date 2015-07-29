package edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter;

/*  Please see the license information at the end of this file. */

/** DefaultTextSegmenter: The default MorphAdorner text segmenter.
 *
 *  <p>
 *  Choi's C99 segmenter is the default MorphAdorner text segmenter.
 *  </p>
 */

public class DefaultTextSegmenter
    extends C99TextSegmenter
    implements TextSegmenter
{
    /** Create the default segmenter.
     */

    public DefaultTextSegmenter()
    {
        super();
    }
}

