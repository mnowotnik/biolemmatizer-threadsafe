package edu.northwestern.at.morphadorner.corpuslinguistics.textsegmenter.struct;

/*  Please see the license information at the end of this file. */

import java.util.*;
import java.io.*;

import edu.northwestern.at.utils.ListFactory;

/** A text collection for linear segmentation.
 *
 *  @author Philip R. Burns.  Modified for integration in MorphAdorner
 *          from a class written by Freddie Choi.
 */

public class RawText
{
    /** The tokens in the text. */

    public List<String> text = ListFactory.createNewList();

    /** The sentence boundaries. */

    public List<Integer> boundaries = ListFactory.createNewList();

    /** Create an empty text collection.
     */

    public RawText()
    {
        super();
    }

    /** Create a text collection from a list of sentences.
     *
     *  @param  sentences   List of list of stringable objects.
     */

    public <T> RawText( List<List<T>> sentences )
    {
        int word    = 0;

        boundaries.add( word );

        for ( int i = 0 ; i < sentences.size() ; i++ )
        {
            List<?> sentence    = sentences.get( i );

            for ( int j = 0 ; j < sentence.size() ; j++ )
            {
                text.add( sentence.get( j ).toString() );
                word++;
            }

            boundaries.add( word );
        }
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




