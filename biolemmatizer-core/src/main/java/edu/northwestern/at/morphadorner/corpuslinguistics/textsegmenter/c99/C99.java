package edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter.c99;


// #include "top.lic"

import java.util.Arrays;
import java.util.Vector;

import edu.northwestern.at.utils.CharUtils;
import edu.northwestern.at.utils.math.Convolution;
import edu.northwestern.at.morphadorner.corpuslinguistics.stemmer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.stopwords.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter.struct.*;
import edu.northwestern.at.utils.math.statistics.*;


/** Choi's C99 algorithm for linear text segmentation
 *
 *  @author Freddy Choi
 *  @author Philip R. Burns.  Modified for integration in MorphAdorner.
 *
 *  <p>
 *  Use of this code is free for academic, education, research and
 *  other non-profit making uses only.
 *  </p>
 */

public class C99
{
    /** Text segment region. */

    protected static class Region
    {
        protected int start, end, area = 1;
        double sum = 0;
        protected Region left = null;
        protected Region right = null;

        /** Construct a new region with boundaries start and end.
         *
         *  @param  start   Starting point.
         *  @param  end     Ending point.
         *  @param  m       Similarity matrix.
         */

        public Region( final int start , final int end , final double[][] m )
        {
            this.start  = start;
            this.end    = end;

            area        = ( end - start + 1 ) * ( end - start + 1 );
            sum         = m[ end ][ start ];
        }

        /** Find a boundary that maximizes the inside density.
         *
         *  @param  m   Similarity matrix.
         *  @param  r   region.
         */

        public static void bestBoundary_max( final double[][] m , Region r )
        {
            final int start = r.start;
            final int end   = r.end;

            if ( start < end )
            {
                                //  For each possible boundary,
                                //  compute the density to find the best
                                //  boundary.

                int b           = start;
                double d_max    = 0.0D;
                double d        = 0.0D;

                for (   int i = end , lower = end - start , upper = 1 ;
                        (i--) > start ; lower-- , upper++
                    )
                {
                                //  Compute the inside density.

                    d   =
                        ( m[ i ][ start ] + m[ end ][ i + 1 ] ) /
                        (double)( ( lower * lower ) + ( upper * upper ) );

                                //  Test if it is the maximum density.

                    if ( d > d_max )
                    {
                        d_max   = d;
                        b       = i;
                    }
                }
                                //  Construct result.

                r.left  = new Region( start , b , m );
                r.right = new Region( b + 1 , end , m );
            }
        }
    }

    /** Find density maximizing boundaries for regions in a similarity matrix.
     *
     *  @param  m   Similarity matrix.
     *  @param  n   Number of regions to find.
     *              If n = 1, the algorithm will determine the number of
     *              regions.
     *
     *  @return     Boundaries of regions in selection order.
     */

    protected static int[] boundaries( final double[][] m , final int n )
    {
                                //  A list of regions that can be split.
                                //  Initialise it with the entire document
                                //  as the first region.

                                // List of splitable regions

        Vector<Region> R    = new Vector<Region>();

                                //  The entire document as the first region.

        Region r = new Region( 0 , m.length - 1 , m );

                                //  Find the best split point.

        Region.bestBoundary_max( m , r );

                                //  Initialise R with the entire document.
        R.addElement( r );

                                //  Split the document right down to
                                //  elementary units, i.e. sentences.
                                //  The boundary selected at each step of
                                //  this divisive clustering process is
                                //  placed in a list B.
                                //  Each step increases the inside density,
                                //  this increase is placed in a list dD.

                                //  A list of boundary locations.

        int[] B     = new int[ m.length - 1 ];

                                //  Increase in density due to the split.

        double[] dD = new double[ m.length - 1 ];

                                //  -- Temporary program variables. --

                                //  Sum of similarity values for segmentation.

        double sum_region   = r.sum;

                                //  Inside area of segmentation.

        double sum_area     = r.area;

                                //  The current density.

        double D            = sum_region / sum_area;

                                //  Density of test segmentation.
        double tempD;
                                //  Maximum density.
        double maxD;
                                //  Index of region to split.

        int index           = 0;

                                //  Divisive clustering (user or automatic
                                //  termination).

        for (   int i = 0 , ie = ( n != -1 ? n - 1 : m.length - 1 ) ;
                i < ie ;
                i++
            )
        {

                                //  Find the default region to split. It is
                                //  the first splitable region.
            index   = 0;

            do
            {
                r = (Region)R.elementAt( index++ );
            }
            while ( ( index < R.size() ) && ( r.start == r.end ) );

            index--;

                                //  Find the best region to split. It is
                                //  one that maximizes the overall density.

            maxD    = Float.MIN_VALUE;

            for ( int j = R.size() ; j-- > 0; )
            {
                r   = (Region)R.elementAt( j );

                                //  Test if region can be split.

                if ( r.start < r.end )
                {
                                //  Divide the new sum of region by the new
                                //  area.

                    tempD =
                        (   sum_region - r.sum + r.left.sum + r.right.sum) /
                            (double)( sum_area - r.area + r.left.area +
                            r.right.area );

                                //  Maximize.

                    if ( tempD > maxD )
                    {
                        maxD    = tempD;
                        index   = j;
                    }
                }
            }
                                //  Split region at index into two.

            r = (Region)R.elementAt(index);

                                //  Find best split point for left region.

            Region.bestBoundary_max( m , r.left );

                                //  Find best split point for right region.

            Region.bestBoundary_max( m , r.right );

            R.setElementAt( r.right , index );
            R.insertElementAt( r.left , index );

                                //  Store results from a step of divisive
                                //  clustering.

                                //  Boundary location.

            B[ i ]  = r.right.start;

                                //  Compute change in density.

            dD[ i ] = maxD - D;

                                //  Update current density
            D       = maxD;

                                //  Update sums.

            sum_region  = sum_region - r.sum + r.left.sum + r.right.sum;
            sum_area    = sum_area - r.area + r.left.area + r.right.area;
        }

                                //  Now that we have an ordered list of
                                //  boundary locations, the next problem
                                //  is to decide the number of segments to
                                //  have. Granularity is task dependent.
                                //  The following method computes the
                                //  number of segments by tracking the
                                //  change in density caused by each split.

                                //  Output boundaries.
        int[] result;

        if ( n != -1 )
        {
                                //  User specified number of segments.

            result = new int[ n - 1 ];
            System.arraycopy( B , 0 , result , 0 , n - 1 );
        }
        else
        {
                                //  Determine number of segments.

                                //  Smooth gradient.
            dD  =
                Convolution.convolute
                (
                    dD,
                    new double[]{ 1, 2, 4, 8, 4, 2, 1 }
                );
                                //  Capture distribution of gradient.

            Accumulator dist    = new Accumulator();

            dist.addValues( dD );

                                //  Threshold to capture only unusually
                                //  large gradient.

            double threshold    =
                dist.getMean() + 1.2D * dist.getStandardDeviation();

                                //  Determine the number of boundaries.

            int number          = 0;

            for ( ; ( number < dD.length ) && ( dD[ number ] > threshold ) ;
                    number++
                )
            {
                ;
            }
                                //  Generate result.

            result  = new int[ number ];

            System.arraycopy( B , 0 , result , 0 , number );
        }

        return result;
    }

    /** Produce stem frequency tables for a tokenized document.
     *
     *  @param  document    Tokenized document.
     *  @param  stopWords   Stop words.
     *  @param  stemmer     Stemmer.
     *
     *  @return             Context vector of stem frequencies.
     */

    protected static ContextVector[] normalize
    (
        final String[][] document ,
        StopWords stopWords ,
        Stemmer stemmer
    )
    {
        ContextVector[] v   = new ContextVector[ document.length ];

        String token, stem;

        for ( int i = document.length ; (i--) > 0 ; )
        {
            v[ i ]  = new ContextVector();

            for (int j = document[ i ].length ; (j--) > 0 ; )
            {
                token   = document[ i ][ j ].toLowerCase();

                if  (   CharUtils.isAWord( token ) &&
                        !stopWords.isStopWord( token )
                    )
                {
                    stem    = stemmer.stem( token );

                    ContextVector.inc( stem , 1 , v[ i ] );
                }
            }
        }

        return v;
    }

    /** Produce stem frequency tables for a tokenized document.
     *
     *  @param  document    Tokenized document.
     *  @param  tf          Term frequencies in document.
     *  @param  stopWords   Stop words.
     *  @param  stemmer     Stemmer.
     *
     *  @return             Context vector of stem frequencies.
     */

    protected static ContextVector[] normalize
    (
        final String[][] document ,
        ContextVector tf ,
        StopWords stopWords ,
        Stemmer stemmer
    )
    {
        ContextVector[] v   = new ContextVector[ document.length ];

        String token, stem;

        for ( int i = document.length ; (i--) > 0 ; )
        {
            v[ i ]  = new ContextVector();

            for (int j = document[ i ].length ; (j--) > 0 ; )
            {
                token   = document[ i ][ j ].toLowerCase();

                if  (   CharUtils.isAWord( token ) &&
                        !stopWords.isStopWord( token )
                    )
                {
                    stem    = stemmer.stem( token );

                    ContextVector.inc( stem , 1 , v[ i ] );
                    ContextVector.inc( stem , 1 , tf );
                }
            }
        }

        return v;
    }

    /** Apply hard ranking to matrix using a mask.
     *
     *  @param  f           Matrix to which to apply hard ranking.
     *  @param  maskSize    Mask size.
     *
     *  <p>
     *  Hard ranking replaces a pixel value with the proportion
     *  of neighboring values it exceeds, using a maskSize x maskSize size
     *  mask.
     *  </p>
     */

    protected static double[][] rank
    (
        final double[][] f ,
        final int maskSize
    )
    {
        double[][] m    = new double[ f.length ][ f.length ];

                                //  Compute the offset used for mask.

        final int dS =
            ( ( maskSize % 2 ) == 1 ?
                maskSize / 2 :
                ( maskSize  - 1 ) / 2 );

                                //  Work on m, refers to f.

        int k_is, k_ie, k_js, k_je;
        double v, sum;

        for ( int m_i = m.length; (m_i--) > 0; )
        {
            for ( int m_j = m_i + 1 ; (m_j--) > 0; )
            {
                                 // Grab pixel value.

                v   = f[ m_i ][ m_j ];

                                // Set it to 0.

                m[ m_i ][ m_j ] = 0;

                                //  Compute effective mask range.

                k_is    = m_i - dS;

                if ( k_is < 0 )
                {
                    k_is    = 0;
                }

                k_ie    = m_i + dS + 1;

                if ( k_ie > f.length )
                {
                    k_ie    = f.length;
                }

                k_js    = m_j - dS;

                if ( k_js < 0 )
                {
                    k_js    = 0;
                }

                k_je = m_j + dS + 1;

                if ( k_je > f.length )
                {
                    k_je = f.length;
                }
                                //  Compute active mask region area for
                                //  normalization. Subtract 1 because we
                                //  ignore the middle pixel which will
                                //  always be rank 0.

                sum = ( k_ie - k_is ) * ( k_je - k_js ) - 1;

                                //  Perform ranking.
                if ( sum > 0 )
                {
                    for ( int k_i = k_ie ; (k_i--) > k_is; )
                    {
                        for ( int k_j = k_je ; (k_j--) > k_js; )
                        {
                            if ( v > f[ k_i ][ k_j ] )
                            {
                                m[ m_i ][ m_j ]++;
                            }
                        }
                    }

                    m[ m_i ][ m_j ] /= sum;
                }

                m[ m_j ][ m_i ] = m[ m_i ][ m_j ];
            }
        }

        return m;
    }

    /** Segment document into coherent topic segments.
     *
     *  @param  document    Document text as list of elementary
     *                      text blocks.
     *
     *  @param  n           Number of topic segments desired.
     *                      Set n = -1 to have algorithm select
     *                      number of topic segments by monitoring
     *                      the rate of increase in segment density.
     *
     *  @param  s           Size of ranking mask.
     *                      Must be odd number >= 3.
     *
     *  @param  stopWords   Stop words.
     *
     *  @param  stemmer     Stemmer.
     *
     *  @return             Coherent topic segments.
     */

    public static String[][][] segment
    (
        final String[][] document ,
        final int n ,
        final int s ,
        StopWords stopWords ,
        Stemmer stemmer
    )
    {
        ContextVector[] vectors = normalize( document , stopWords , stemmer );

        double[][] sim          = similarity( vectors );

        vectors = null;

        double[][] rank         = rank( sim , s );

        sim = null;

        double[][] sum          = sum( rank );

        rank    = null;

        int[] bounds            = boundaries( sum , n );

        Arrays.sort( bounds );

        sum     = null;

        return split( document , bounds );
    }

    /** Segment document into coherent topic segments.
     *
     *  @param  document    Document text as list of elementary
     *                      text blocks.
     *
     *  @param  n           Number of topic segments desired.
     *                      Set n = -1 to have algorithm select
     *                      number of topic segments by monitoring
     *                      the rate of increase in segment density.
     *
     *  @param  s           Size of ranking mask.
     *                      Must be odd number >= 3.
     *
     *  @param  stopWords   Stop words.
     *
     *  @param  stemmer     Stemmer.
     *
     *  @return             Coherent topic segments.
     */

    public static String[][][] segmentW
    (
        final String[][] document ,
        final int n ,
        final int s ,
        StopWords stopWords ,
        Stemmer stemmer
    )
    {
        ContextVector tf        = new ContextVector();

        ContextVector[] vectors =
            normalize( document , tf , stopWords , stemmer );

        EntropyVector ev        = new EntropyVector( tf );

        double[][] sim          = similarity( vectors , ev );

        vectors = null;

        double[][] rank         = rank( sim , s );

        sim     = null;

        double[][] sum          = sum( rank );

        rank    = null;

        int[] bounds            = boundaries( sum , n );

        Arrays.sort( bounds );

        sum = null;

        return split( document , bounds );
    }

    /** Given context vectors, compute the similarity matrix.
     *
     *  @param  v       context vectors.
     *
     *  @return         similarity matrix.
     */

    protected static double[][] similarity( final ContextVector[] v )
    {
        double[][] similarity   = new double[ v.length ][ v.length ];

        for ( int i = v.length ; (i--) > 0; )
        {
            for ( int j = i + 1 ; (j--) > 0; )
            {
                similarity[ i ][ j ]    =
                    ContextVector.cos( v[ i ] , v[ j ] );

                similarity[ j ][ i ]    = similarity[ i ][ j ];
            }
        }

        return similarity;
    }

    /** Given context vectors, compute the similarity matrix.
     *
     *  @param  v       context vectors.
     *  @param  entropy entropy vector.
     *
     *  @return         similarity matrix.
     */

    protected static double[][] similarity
    (
        final ContextVector[] v ,
        final EntropyVector entropy
    )
    {
        double[][] similarity   = new double[ v.length ][ v.length ];

        for ( int i = v.length ; (i--) > 0; )
        {
            for ( int j = i + 1; (j--) > 0; )
            {
                similarity[ i ][ j ] =
                    ContextVector.cos( v[ i ] , v[ j ] , entropy );

                similarity[ j ][ i ] = similarity[ i ][ j ];
            }
        }

        return similarity;
    }

    /** Split text into segment blocks given topic boundaries.
     *
     *  @param  text        Source text.
     *  @param  boundaries  Boundaries.
     *
     *  @return             Topic segments.
     */

    protected static String[][][] split
    (
        final String[][] text ,
        final int[] boundaries
    )
    {
                                //  A list of boundaries, includes
                                //  implicit boundaries.

        int[] b = new int[ boundaries.length + 2 ];

                                //  Add the implicit boundaries
                                //  (start and end).
        b[ 0 ]  = 0;
        b[ b.length - 1 ]   = text.length;

        System.arraycopy( boundaries , 0 , b , 1 , boundaries.length );

                                //  Form topic segments.

        String[][][] seg    = new String[ b.length - 1 ][][];

        for ( int i = seg.length ; (i--) > 0; )
        {
            seg[ i ]    = new String[ b[ i + 1 ] - b[ i ] ][];

            System.arraycopy
            (
                text ,
                b[ i ] ,
                seg[ i ] ,
                0 ,
                b[ i + 1 ] - b[ i ]
            );
        }

        return seg;
    }

    /** Compute sum of rank matrix.
     *
     *  @param  rankMatrix  Rank matrix.
     *
     *  @return             Sum of rank matrix.
     */

    protected static double[][] sum( final double[][] rankMatrix )
    {
        double[][] sum  =
            new double[ rankMatrix.length ][ rankMatrix.length ];

                                //  Step 1.

        for ( int i = 0 , ie = rankMatrix.length ; i < ie ; i++ )
        {
            sum[ i ][ i ]   = rankMatrix[ i ][ i ];
        }

                                //  Step 2.

        for ( int i = 0 , ie = rankMatrix.length - 1 , ip ; i < ie ; i++ )
        {
            ip  = i + 1;

            sum[ ip ][ i ]  =
                rankMatrix[ ip ][ i ] * 2.0D + sum[ i ][ i ] +
                sum[ ip ][ ip ];

            sum[ i ] [ip ]  = sum[ ip ][ i ];
        }

                                //  Step 3.

        for ( int j = 2 , ij , ip ; j < rankMatrix.length ; j++ )
        {
            for ( int i = 0 , ie = rankMatrix.length - j ; i < ie ; i++ )
            {
                ij  = i + j;
                ip  = i + 1;

                sum[ ij ][ i ]  =
                    rankMatrix[ ij ][ i ] * 2.0D + sum[ ij - 1 ][ i ] +
                    sum[ ij ][ ip ] - sum[ ij - 1 ][ ip ];

                sum[ i ][ ij ]  = sum[ ij ][ i ];
            }
        }

        return sum;
    }
}

