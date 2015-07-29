package edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter.struct;

/*  Please see the license information in the header below. */

import java.util.*;
import java.io.*;

import edu.northwestern.at.utils.*;

/** Context vector. Maps string to frequency count.
 *
 *  @author Freddy Choi
 *  @author Philip R. Burns.  Modified for integration in MorphAdorner.
 *
 *  <p>
 *  Use of this code is free for academic, education, research and
 *  other non-profit making uses only.
 *  </p>
 */

public class ContextVector implements Externalizable
{
    /** Labeled frquencies. */

    protected Map<String, Frequency> table =
        MapFactory.createNewMap();

    protected int sum   = 0;

    /** Holds an externalizable frequency (count). */

    protected static class Frequency implements Externalizable
    {
        /** The frequency value. */

        public int count    = 0;

        /** Create Frequency object. */

        public Frequency()
        {
        }

        /** Write Frequency to object output. */

        public void writeExternal( ObjectOutput out )
            throws IOException
        {
            out.writeInt( count );
        }

        /** Read Frequency from object input. */

        public void readExternal( ObjectInput in )
            throws IOException
        {
            count   = in.readInt();
        }
    }

    /** Create ContextVector.
     */

    public ContextVector()
    {
    }

    /** Compute the cosine of vector a and b
     *
     *  @param  a   First context vector.
     *  @param  b   Second context vector.
     *
     *  @return     Cosine of angle between vectors a and b.
     */

    public static double cos
    (
        final ContextVector a ,
        final ContextVector b
    )
    {
                                //  Use smaller vector as primary vector.

        final ContextVector va , vb;

        if ( a.table.size() < b.table.size() )
        {
            va = a;
            vb = b;
        }
        else
        {
            va = b;
            vb = a;
        }
                                //  Compute dot product and sum of squared
                                //  frequency.
        Frequency fA , fB;
        String key;
        int dot = 0 , sfA = 0 , sfB = 0;

        for (   Iterator<String> iterator = va.table.keySet().iterator();
                iterator.hasNext();
            )
        {
            key     = iterator.next();

            fA      = va.table.get( key );
            fB      = vb.table.get( key );

            sfA     += ( fA.count * fA.count );

            if ( fB != null )
            {
                dot += ( fA.count * fB.count );
            }
        }

        for (   Iterator<Frequency> iterator = vb.table.values().iterator();
                iterator.hasNext();
            )
        {
            fB      = iterator.next();
            sfB     += ( fB.count * fB.count );
        }
                                //  Compute cosine.

        double magnitude    = (double)Math.sqrt( sfA * sfB );

        double result       = 0.0D;

        if ( magnitude != 0.0D )
        {
            result  = dot / magnitude;
        }

        return result;
    }

    /** Compute the cosine of vector a and b, accounting for term frequency.
     *
     *  @param  a   First vector.
     *  @param  b   Second vector.
     *  @param  tf  Term frequency vector.
     *
     *  @return     cosine value.
     */

    public static double cos
    (
        final ContextVector a ,
        final ContextVector b ,
        final EntropyVector tf
    )
    {
                                //  Use smaller vector as primary vector.

        final ContextVector va , vb;

        if ( a.table.size() < b.table.size() )
        {
            va = a;
            vb = b;
        }
        else
        {
            va = b;
            vb = a;
        }
                                //  Compute dot product and sum of squared
                                //  frequencies.

        Frequency fA , fB;
        String key;
        double dot = 0 , sfA = 0 , sfB = 0 , weight;
        int square;

        for (   Iterator<String> iterator = va.table.keySet().iterator();
                iterator.hasNext();
            )
        {
            key     = iterator.next();
            fA      = va.table.get( key );
            fB      = vb.table.get( key );

            weight  = tf.inverse( key , tf );
            weight  *= weight;

            sfA     += ( fA.count * fA.count * weight );

            if ( fB != null )
            {
                dot += ( fA.count * fB.count * weight );
            }
        }

        for (   Iterator<String> iterator = vb.table.keySet().iterator();
                iterator.hasNext();
            )
        {
            key     = iterator.next();
            fB      = vb.table.get( key );

            weight  = tf.inverse( key , tf );
            weight  *= weight;
            sfB     += ( fB.count * fB.count * weight );
        }
                                //  Compute cosine.

        double magnitude    = (double)Math.sqrt( sfA * sfB );
        double result       = 0.0D;

        if ( magnitude != 0 )
        {
            result  = (double)( dot / magnitude );
        }

        return result;
    }

    /** Compute the dot product of vector a and b.
     *
     *  @param  a   first vector.
     *  @param  b   second vector.
     *
     *  @return     dot product.
     */

    public static int dot( final ContextVector a , final ContextVector b )
    {
                                //  Use smaller vector as primary vector.

        final ContextVector va , vb;

        if ( a.table.size() < b.table.size() )
        {
            va  = a;
            vb  = b;
        }
        else
        {
            va  = b;
            vb  = a;
        }
                                //  Compute dot product.
        Frequency fA , fB;
        String key;
        int dot = 0;

        for (   Iterator<String> iterator = va.table.keySet().iterator();
                iterator.hasNext();
            )
        {
            key = iterator.next();
            fA  = va.table.get( key );
            fB  = vb.table.get( key );

            if ( fB != null )
            {
                dot += ( fA.count * fB.count );
            }
        }

        return dot;
    }

    /** Get the frequency count of key in context vector
     *
     * @param   key     The key.
     * @param   vector  The context vector.
     *
     *  @return         The frequency count.
     */

    public static int freq( final String key , final ContextVector vector )
    {
        int result  = 0;

        Frequency f = vector.table.get( key );

        if ( f != null )
        {
            result  = f.count;
        }

        return result;
    }

    /** Increment frequency count of key.
     *
     *  @param  key         The key.
     *  @param  increment   The increment value.
     *  @param  vector      The context vector.
     *
     *  @return             Updated frequency.
     */

    public static int inc
    (
        final String key ,
        final int increment ,
        ContextVector vector
    )
    {
        Frequency f = vector.table.get( key );

        if ( f == null )
        {
            f       = new Frequency();
            f.count = increment;

            vector.table.put( key , f );
        }
        else
        {
            f.count += increment;
        }

        vector.sum += increment;

        return f.count;
    }

    /** Get the list of context vector keys.
     *
     *  @param  v   The context vector.
     *
     *  @return     Keys as a string array.
     */

    public static String[] keys( final ContextVector v )
    {
        String[] result = new String[ v.table.size() ];

        Iterator<String> iterator = v.table.keySet().iterator();

        for ( int i = 0; i < result.length; i++ )
        {
            result[ i ] = iterator.next();
        }

        return result;
    }

    /** Read ContextVector from object file.
     *
     *  @param  in      The object input file.
     */

    public final void readExternal( java.io.ObjectInput in )
        throws java.io.IOException , ClassNotFoundException
    {
        @SuppressWarnings("unchecked")
        HashMap<String , Frequency> tableObj =
            (java.util.HashMap<String , Frequency>)in.readObject();

        table   = tableObj;
        sum     = in.readInt();
    }

    /** Get the sum of frequencies
     *
     *  @param  vector  The context vector.
     *
     *  @return         Sum of frequencies in vector.
     */

    public static int sum( final ContextVector vector )
    {
        return vector.sum;
    }

    /** Save ContextVector to file as object.
     *
     *  @param  out     The object output file.
     */

    public final void writeExternal( java.io.ObjectOutput out )
        throws java.io.IOException
    {
        out.writeObject( table );
        out.writeInt( sum );
    }
}
