package edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter.struct;

/*  Please see the license information in the header below. */

import java.util.*;
import edu.northwestern.at.utils.*;

/** Entropy vector.
 *
 * @author  Freddy Choi
 * @author  Philip R. Burns.  Modified for integration into MorphAdorner.
 *
 *  <p>
 *  Use of this code is free for academic, education, research and
 *  other non-profit making uses only.
 *  </p>
 */

public class EntropyVector
{
    /** Map holding labeled vector values. */

    protected Map<String, TableValue> table =
        MapFactory.createNewMap();

    /** Vector entries. */

    protected static class TableValue
    {
        public double probability;
        public double entropy;
        public double inverse;
    }

    /** Create an entropy vector from a context vector.
     *
     *  @param  v   uk.ac.man.cs.choif.extend.structure.ContextVector
     */

    public EntropyVector( final ContextVector v )
    {
        String[] keys   = v.keys( v );
        int sum         = v.sum( v );

        TableValue kv;

        for ( int i = keys.length ; (i--)  > 0; )
        {
            kv              = new TableValue();

            kv.probability  = ((double)v.freq( keys[ i ] , v ) ) / sum;
            kv.entropy      = -Math.log( kv.probability );
            kv.inverse      = 1.0D / v.freq( keys[ i ] , v );

            table.put( keys[ i ] , kv );
        }
    }

    /** Return entropy for labeled table entry.
     *
     *  @param  s   Entry name.
     *  @param  v   Entropy vector.
     *
     *  @return     entropy from entropy vector for entry "s".
     */

    public static double entropy( final String s , final EntropyVector v )
    {
        double result   = 0.0D;

        TableValue kv   = v.table.get( s );

        if ( kv != null )
        {
            result  = kv.entropy;
        }

        return result;
    }

    /** Return inverse for labeled table entry.
     *
     *  @param  s   Entry name.
     *  @param  v   Entropy vector.
     *
     *  @return     inverse from entropy vector for entry "s".
     */

    public static double inverse( final String s , final EntropyVector v )
    {
        double result   = 0.0D;

        TableValue kv   = v.table.get( s );

        if ( kv != null )
        {
            result  = kv.inverse;
        }

        return result;
    }

    /** Return probability for labeled table entry.
     *
     *  @param  s   Entry name.
     *  @param  v   Entropy vector.
     *
     *  @return     probability from entropy vector for entry "s".
     */

    public static double probability( final String s , final EntropyVector v )
    {
        double result   = 0.0D;

        TableValue kv   = v.table.get( s );

        if ( kv != null )
        {
            result  = kv.probability;
        }

        return result;
    }
}

