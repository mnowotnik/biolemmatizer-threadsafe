package edu.northwestern.at.morphadorner.corpuslinguistics.inputter;

/*  Please see the license information at the end of this file. */

import edu.northwestern.at.utils.ClassUtils;
import edu.northwestern.at.utils.UTF8Properties;

/** TextInputter factory.
 */

public class TextInputterFactory
{
    /** Get a TextInputter.
     *
     *  @return     The TextInputter.
     */

    public static TextInputter newTextInputter()
    {
        String className    = System.getProperty( "textinputter.class" );

        if ( className == null )
        {
            className   = "DefaultTextInputter";
        }

        return TextInputterFactory.newTextInputter( className );
    }

    /** Get a TextInputter.
     *
     *  @param      properties      MorphAdorner properties.
     *
     *  @return     The TextInputter.
     */

    public static TextInputter newTextInputter
    (
        UTF8Properties properties
    )
    {
        String className    = null;

        if ( properties != null )
        {
            className   = properties.getProperty( "textinputter.class" );
        }

        if ( className == null )
        {
            className   = "DefaultTextInputter";
        }

        return TextInputterFactory.newTextInputter( className );
    }

    /** Get a TextInputter of a specified class name.
     *
     *  @param  className   Class name for the textInputter.
     *
     *  @return             The textInputter.
     */

    public static TextInputter newTextInputter( String className )
    {
        TextInputter textInputter   = null;

        try
        {
            textInputter    =
                (TextInputter)Class.forName( className ).newInstance();
        }
        catch ( Exception e )
        {
            String fixedClassName   =
                ClassUtils.packageName
                (
                    TextInputterFactory.class.getName()
                ) +
                "." + className;

            try
            {
                textInputter    =
                    (TextInputter)Class.forName(
                        fixedClassName ).newInstance();
            }
            catch ( Exception e2 )
            {
                System.err.println(
                    "Unable to create text inputter of class " +
                    fixedClassName + ", using default." );

                textInputter    = new DefaultTextInputter();
            }
        }

        return textInputter;
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



