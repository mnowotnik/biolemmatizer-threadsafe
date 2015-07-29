package edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter.texttiling;

/*  Please see the license information in the header below. */

import java.io.*;
import java.util.*;

import edu.northwestern.at.utils.ListFactory;
import edu.northwestern.at.utils.MapFactory;
import edu.northwestern.at.utils.SetUtils;
import edu.northwestern.at.morphadorner.corpuslinguistics.stemmer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.stopwords.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter.struct.*;

/** An implementation of Marti Hearst's text tiling algorithm.
 *
 *  @author Freddy Choi
 *  @author Philip R. Burns.  Modified for integration in MorphAdorner.
 *
 *  <p>
 *  Use of this code is free for academic, education, research and
 *  other non-profit making uses only.
 *  </p>
 */

public class TextTiling
{
    /** Wrapper for printStream to allow utf-8 output. */

    protected static PrintStream printStream;

    /** Size of sliding window. */

    protected int w = 100;

    /** Step size. */

    protected int s = 10;

    /** Collection for segmentation. */

    protected RawText C = new RawText();

    /** Stopwords for noise reduction. */

    protected StopWords S = new BaseStopWords();

    /** Token -> stem dictionary */

    protected Map<String , String> stemOf = MapFactory.createNewMap();

    /** Similarity scores and the corresponding locations. */

    protected double[] sim_score    = new double[ 0 ];
    protected int[] site_loc        = new int[ 0 ];

    /** Depth scores. */

    protected double[] depth_score = new double[ 0 ];

    /** Segment boundaries. */

    protected List<Integer> segmentation = ListFactory.createNewList();

    /** Create text tiler.
     */

    public TextTiling()
    {
        super();
    }

    /** Create text tiler from text collection and stop word set.
     *
     * @param   c   Text collection to segment.
     * @param   s   Stop word set.
     */

    public TextTiling( RawText c , StopWords s )
    {
        C   = c;
        S   = s;

        preprocess();
    }

    /** Return segmentation list.
     *
     *  @return segmentation as a list of integer sentence indices.
     */

    public List<Integer> getSegmentation()
    {
        return segmentation;
    }

    /** Return stem dictionary.
     *
     *  @return     stem dictionary.
     */

    public Map<String , String> getStemOf()
    {
        return stemOf;
    }

    /** Return stop words.
     *
     *  @return     Stop words.
     */

    public StopWords getStopWords()
    {
        return S;
    }

    /** Add a term to a block
     * Creation date: (07/12/99 01:41:24)
     * @param term java.lang.String
     * @param B java.util.HashMap
     */

    protected void blockAdd( final String term , Map<String , Integer> B )
    {
        Integer freq = B.get( term );

        if ( freq == null )
        {
            freq = new Integer( 1 );
        }
        else
        {
            freq = new Integer( freq.intValue() + 1 );
        }

        B.put( term , freq );
    }

    public void setWindowSize( int windowSize )
    {
        this.w = windowSize;
    }

    public void setStepSize( int stepSize )
    {
        this.s = stepSize;
    }

    /** Compute the cosine similarity measure for two blocks
     * Creation date: (07/12/99 01:49:16)
     * @return double
     * @param B1 java.util.HashMap
     * @param B2 java.util.HashMap
     */

    protected double blockCosine
    (
        final Map<String , Integer> B1 ,
        final Map<String , Integer> B2
    )
    {
                                //  1. Declare variables

        int W; // Weight of a term (temporary variable)
        int sq_b1 = 0; // Sum of squared weights for B1
        int sq_b2 = 0; // Sum of squared weights for B2
        int sum_b = 0; // Sum of product of weights for common terms in B1 and B2

                                //  2. Compute the squared sum of term
                                //  weights for B1

        for ( Iterator<Integer> e = B1.values().iterator(); e.hasNext(); )
        {
            W = e.next();
            sq_b1 += ( W * W );
        }
                                //  3. Compute the squared sum of term
                                //  weights for B2

        for ( Iterator<Integer> e = B2.values().iterator(); e.hasNext(); )
        {
            W = e.next();
            sq_b2 += ( W * W );
        }
                                //  4. Compute sum of term weights for
                                //  common terms in B1 and B2

                                //  4.1. Union of terms in B1 and B2

        Map<String , Boolean> union =
            MapFactory.createNewMap( B1.size() + B2.size() );

        for ( Iterator<String> e = B1.keySet().iterator(); e.hasNext(); )
        {
            union.put( e.next() , true );
        }

        for ( Iterator<String> e = B2.keySet().iterator(); e.hasNext(); )
        {
            union.put( e.next() , true );
        }

                                //  4.2. Compute sum

        Integer W1; // Weight of a term in B1 (temporary variable)
        Integer W2; // Weight of a term in B2 (temporary variable)
        String term; // A term (temporary variable)

        for (   Iterator<String> e = union.keySet().iterator();
                e.hasNext();
            )
        {
            term    = e.next();

            W1      = B1.get( term );
            W2      = B2.get( term );

            if ( ( W1 != null ) && ( W2 != null ) )
            {
                sum_b   += ( W1.intValue() * W2.intValue() );
            }
        }

                                //  5. Compute similarity

        return  (double)sum_b / (double)Math.sqrt( sq_b1 * sq_b2 );
    }

    /** Remove a term from the block.
     * @param term java.lang.String
     * @param B java.util.HashMap
     */

    protected void blockRemove( final String term , Map<String , Integer> B )
    {
        Integer freq = B.get( term );

        if ( freq != null )
        {
            if ( freq.intValue() == 1 )
            {
                B.remove( term );
            }
            else
            {
                B.put( term , freq.intValue() - 1 );
            }
        }
    }

    /** Identify the boundaries
     */

    public void boundaryIdentification()
    {
        /* Declare variables */

        double mean = 0; // Mean depth score
        double sd = 0; // S.D. of depth score
        double threshold; // Threshold to use for determining boundaries
        int neighbours = 3; // The area to check before assigning boundary

        /* Compute mean and s.d. from depth scores */

        for ( int i = depth_score.length; i-- > 0; )
        {
            mean += depth_score[ i ];
        }

        mean = mean / depth_score.length;

        for ( int i = depth_score.length; i-- > 0; )
        {
            sd += Math.pow( depth_score[ i ] - mean , 2 );
        }

        sd = sd / depth_score.length;

        /* Compute threshold */
        threshold = mean - sd / 2;

        /* Identify segments in pseudo-sentence terms */
        List<Integer> pseudo_boundaries = ListFactory.createNewList();
        boolean largest = true; // Is the potential boundary the largest in the local area?

        for ( int i = depth_score.length; i-- > 0; )
        {
            /* Found a potential boundary */

            if ( depth_score[ i ] >= threshold )
            {
                /* Check if the nearby area has anything better */

                largest = true;

                /* Scan left */

                for ( int j = neighbours; largest && j > 0 && ( i - j ) > 0; j-- )
                {
                    if ( depth_score[ i - j ] > depth_score[ i ] )
                    {
                        largest = false;
                    }
                }

                /* Scan right */

                for ( int j = neighbours; largest && j > 0 &&
                    ( i + j ) < depth_score.length; j-- )
                {
                    if ( depth_score[ i + j ] > depth_score[ i ] )
                    {
                        largest = false;
                    }
                }

                /* Lets make the decision */

                if ( largest )
                {
                    pseudo_boundaries.add( site_loc[ i ] );
                }
            }
        }

        /* Convert pseudo boundaries into real boundaries.
         We use the nearest true boundary. */

        /* Convert real boundaries into array for faster access */

        int[] true_boundaries = new int[ C.boundaries.size() ];

        for ( int i = true_boundaries.length; i-- > 0; )
        {
            true_boundaries[ i ] = C.boundaries.get( i );
        }

        int pseudo_boundary;
        int distance; // Distance between pseudo and true boundary
        int smallest_distance; // Shortest distance
        int closest_boundary; // Nearest real boundary

        for ( int i = pseudo_boundaries.size(); i-- > 0; )
        {
            pseudo_boundary =
                ( (Integer)pseudo_boundaries.get( i ) ).intValue();

            /* This is pretty moronic, but it works. Can definitely be improved */
            smallest_distance =
                Integer.MAX_VALUE;
            closest_boundary = true_boundaries[ 0 ];
            for ( int j = true_boundaries.length; j-- > 0; )
            {
                distance = Math.abs( true_boundaries[ j ] - pseudo_boundary );
                if ( distance <= smallest_distance )
                {
                    smallest_distance = distance;
                    closest_boundary = true_boundaries[ j ];
                }
            }

            segmentation.add( closest_boundary );
        }
    }

    /** Compute depth score after applying similarityDetermination()
     */

    public void depthScore()
    {
        double maxima = 0; // Local maxima
        double dleft = 0; // Difference for the left side
        double dright = 0; // Difference for the right side

        /* For each position, compute depth score */

        depth_score = new double[ sim_score.length ];

        for ( int i = sim_score.length; i-- > 0; )
        {
            /* Scan left */

            maxima = sim_score[ i ];

            for ( int j = i; j > 0 && sim_score[ j ] >= maxima; j-- )
            {
                maxima = sim_score[ j ];
            }

            dleft = maxima - sim_score[ i ];

            /* Scan right */

            maxima = sim_score[ i ];

            for ( int j = i; j < sim_score.length && sim_score[ j ] >= maxima; j++ )
            {
                maxima = sim_score[ j ];
            }

            dright = maxima - sim_score[ i ];

            /* Declare depth score */

            depth_score[ i ] = dleft + dright;
        }
    }

    /** Decide whether word i is worth using as feature for segmentation.
     * @return boolean
     * @param i int
     */

    protected boolean include( int i )
    {
        /* Noise reduction by filtering out everything but nouns and verbs -
         Best but requires POS tagging
         String pos = (String) C.pos.get(i);
         return (pos.startsWith("N") || pos.startsWith("V")); */

        /* Noise reduction by stopword removal - OK */
        String token = (String)C.text.get( i );

        return !S.isStopWord( token.toLowerCase() );

        /* No noise reduction -- Worst
         return true; */
    }

    /** Perform some preprocessing to save execution time
     */

    protected void preprocess()
    {
        List<String> text = C.text; // Text of the collection
        Stemmer stemmer = new PorterStemmer(); // Stemming algorithm
        String token; // A token

        /* Construct a dictionary of tokens */

        for ( int i = text.size(); i-- > 0; )
        {
            token = text.get( i );
            stemOf.put( token , "" );
        }

        /* Complete mapping token -> stem */

        for ( Iterator<String> e = stemOf.keySet().iterator(); e.hasNext(); )
        {
            token = e.next();
            stemOf.put( token , stemmer.stem( token ) );
        }
    }

    /** Compute the similarity score.
     */

    public void similarityDetermination()
    {
        List<String> text = C.text; // The source text

        Map<String , Integer> left = MapFactory.createNewMap(); // Left sliding window
        Map<String , Integer> right = MapFactory.createNewMap(); // Right sliding window

        List<Float> score = ListFactory.createNewList(); // Scores
        List<Integer> site = ListFactory.createNewList(); // Locations

                                //  Initialise windows.

        for ( int i = w; i-- > 0; )
        {
            blockAdd( stemOf.get( text.get( i ) ) , left );
        }

        for ( int i = w * 2; i-- > w; )
        {
            blockAdd( stemOf.get( text.get( i ) ) , right );
        }
                                //  Slide window and compute score.

        final int end = text.size() - w;
        String token;
        int step = 0;
        int i;

        for ( i = w; i < end; i++ )
        {
                                //  Compute score for a step.
            if ( step == 0 )
            {
                score.add( new Float( blockCosine( left , right ) ) );
                site.add( i );
                step = s;
            }
                                //  Remove word which is at the very left
                                //  of the left window.

            if ( include( i - w ) )
            {
                blockRemove
                (
                    stemOf.get( text.get( i - w ) ) ,
                    left
                );
            }
                                //  Add current word to the left window
                                //  and remove it from the right window.

            if ( include( i ) )
            {
                token   = text.get( i );

                blockAdd( stemOf.get( token ) , left );
                blockRemove( stemOf.get( token ) , right );
            }
                                //  Add the first word after the very right
                                //  of the right window.

            if ( include( i + w ) )
            {
                blockAdd
                (
                    stemOf.get( text.get( i + w ) ) ,
                    right
                );
            }

            step--;
        }
                                //  Compute score for the last step.
        if ( step == 0 )
        {
            score.add( new Float( blockCosine( left , right ) ) );
            site.add( i );
            step = s;
        }
                                //  Smoothing with a window size of 3.

        sim_score   = new double[ score.size() - 2 ];
        site_loc    = new int[ site.size() - 2 ];


        for ( int j = 0; j < sim_score.length; j++ )
        {
            sim_score[ j ] =
                ( ( (Float)score.get( j ) ).doubleValue() +
                    ( (Float)score.get( j + 1 ) ).doubleValue() +
                    ( (Float)score.get( j + 2 ) ).doubleValue() ) / 3;

            site_loc[ j ] = site.get( j + 1 );
        }
    }
}

