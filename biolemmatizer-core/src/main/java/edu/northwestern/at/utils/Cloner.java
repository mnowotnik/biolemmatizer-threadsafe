package edu.northwestern.at.utils;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;

/** Cloner -- Clones an object using serialization/deserialization.
 *
 *  <p>
 *  Usage:
 *  </p>

 *  <p>
 *      T clonedObject  = (T)Cloner.deepClone( sourceObject );
 *  </p>
 *
 *  <p>
 *  "sourceObject" must implement serializable or externalizable.
 *  </p>
 */

public class Cloner
{
    /** Deep clones an object using serialization/deserialization.
     *
     *  @param  sourceObject    The object to be cloned.
     *                          Must implement serializable or
     *                          externalizable.
     *
     *  @return                 Clone of the source object.
     *
     *  @throws                 Exception if the clone process fails.
     */

    public static Object deepClone( Object sourceObject )
        throws Exception
    {
        Object result           = null;

        ObjectOutputStream oos  = null;
        ObjectInputStream ois   = null;

        try
        {
            ByteArrayOutputStream bos   = new ByteArrayOutputStream();

            oos = new ObjectOutputStream( bos );

            oos.writeObject( sourceObject );

            oos.flush();

            ByteArrayInputStream bin    =
                new ByteArrayInputStream( bos.toByteArray() );

            ois = new ObjectInputStream( bin );

            result  = ois.readObject();
        }
        catch ( Exception e )
        {
            throw ( e );
        }
        finally
        {
            try
            {
                oos.close();
            }
            catch ( Exception e )
            {
            }

            try
            {
                ois.close();
            }
            catch ( Exception e )
            {
            }
        }

        return result;
    }

    /** Allow overrides but not instantiation.
     */

    protected Cloner()
    {
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



