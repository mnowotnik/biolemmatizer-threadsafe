package edu.northwestern.at.utils;

/*  Please see the license information at the end of this file. */

import java.util.*;

/** A hash map which stores its values in compressed form. */

public class CompressedHashMap<K , V>
    implements Map<K, V>
{
    /** The object compressor for the values in the hash map. */

    protected static CompressedSerializer serializer    =
        new CompressedSerializer();

    /** The delagated-to hash map used to store the compressed values. */

    protected HashMap<K, Object> delegateMap;

    /** Create hash map. */

    public CompressedHashMap()
    {
        delegateMap = new HashMap<K,Object>();
    }

    /** Create hash map with specified initial capacity.
     *
     *  @param  initialCapacity The initial capacity.
     */

    public CompressedHashMap( int initialCapacity )
    {
        delegateMap = new HashMap<K, Object>( initialCapacity );
    }

    /** Create hash map with specified initial capacity and load factor.
     *
     *  @param  initialCapacity The initial capacity.
     *  @param  loadFactor      The load factor.
     */

    public CompressedHashMap( int initialCapacity , float loadFactor )
    {
        delegateMap =
            new HashMap<K, Object>( initialCapacity , loadFactor );
    }

    /** Create hash map from another map.
     *
     *  @param  map The other map from which to load entries.
     */

    public CompressedHashMap( Map<? extends K , ? extends V> map )
    {
        delegateMap = new HashMap<K, Object>( map.size() );
        putAll( map );
    }

    @SuppressWarnings("unchecked")
    protected V decompress( Object o )
    {
        V result    = null;

        try
        {
            result  = (V)( serializer.deserializeFromBytes( (byte[])o ) );
        }
        catch ( Exception e )
        {
        }

        return result;
    }

    protected Object compress( Object o )
    {
        Object result   = null;

        try
        {
            result  = serializer.serializeToBytes( o );
        }
        catch ( Exception e )
        {
        }

        return result;
    }

    public void clear()
        throws UnsupportedOperationException
    {
        delegateMap.clear();
    }

    public Object clone()
    {
        return delegateMap.clone();
    }

    public boolean containsKey( Object key )
        throws ClassCastException ,
            NullPointerException
    {
        return delegateMap.containsKey( key );
    }

    public boolean containsValue( Object value )
        throws ClassCastException ,
            NullPointerException
    {
        return delegateMap.containsValue( compress( value ) );
    }

    public Set<Map.Entry<K, V>> entrySet()
    {
        Set<Map.Entry<K,V>> result          =
            new HashSet<Map.Entry<K,V>>();

        Iterator<Map.Entry<K,Object>> iterator  =
            delegateMap.entrySet().iterator();

        while ( iterator.hasNext() )
        {
            Map.Entry<K,Object> entry   = iterator.next();

            Map.Entry<K, V> resultEntry =
                new AbstractMap.SimpleEntry<K, V>
                (
                    entry.getKey() ,
                    decompress( entry.getValue() )
                );

            result.add( resultEntry );
        }

        return result;
    }

    public boolean isEmpty()
    {
        return delegateMap.isEmpty();
    }

    public Set<K> keySet()
    {
        return delegateMap.keySet();
    }

    public int size()
    {
        return delegateMap.size();
    }

    public void putAll( Map< ? extends K , ? extends V > m )
        throws UnsupportedOperationException ,
            ClassCastException ,
            NullPointerException ,
            IllegalArgumentException
    {
        Iterator<? extends K> iterator  = m.keySet().iterator();

        while ( iterator.hasNext() )
        {
            K key   = iterator.next();
            V value = m.get( key );

            put( key , value );
        }
    }

    public Collection<V> values()
    {
        Collection<V> result            = new ArrayList<V>();
        Iterator<Object> iterator       = delegateMap.values().iterator();

        while ( iterator.hasNext() )
        {
            result.add( decompress( iterator.next() ) );
        }

        return result;
    }

    public V remove( Object key )
        throws
            UnsupportedOperationException ,
            ClassCastException ,
            NullPointerException
    {
        Object o    = delegateMap.remove( key );
        V result    = null;

        if ( o != null )
        {
            result  = decompress( o );
        }

        return result;
    }

    public V get( Object key )
        throws ClassCastException ,
            NullPointerException
    {
        Object o    = delegateMap.get( key );
        V result    = null;

        if ( o != null )
        {
            result  = decompress( o );
        }

        return result;
    }

    public V put( K key , V value )
        throws
            UnsupportedOperationException ,
            ClassCastException ,
            NullPointerException ,
            IllegalArgumentException
    {
        V result    = get( key );

        delegateMap.put( key , compress( value ) );

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



