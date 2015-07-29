package edu.northwestern.at.morphadorner.corpuslinguistics.postagger;

/*  Please see the license information at the end of this file. */

import java.util.*;

import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.hepple.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.noopretagger.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.propernounretagger.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.simplerulebased.*;

import edu.northwestern.at.utils.*;

/** PartOfSpeechTagger factory.
 */

public class PartOfSpeechRetaggerFactory
{
    /** Map from short to full class names for built-in retaggers. */

    protected static Map<String, String> retaggerClassMap   =
        MapFactory.createNewMap();

    /** Get a part of speech retagger.
     *
     *  @return     The part of speech retagger.
     */

    public static PartOfSpeechRetagger newPartOfSpeechRetagger()
    {
        String className    =
            System.getProperty( "partofspeechretagger.class" );

        if ( className == null )
        {
            className   =
                ClassUtils.packageName
                (
                    PartOfSpeechRetaggerFactory.class.getName()
                ) + "DefaultPartOfSpeechRetagger";
        }

        return newPartOfSpeechRetagger( className );
    }

    /** Get a part of speech retagger.
     *
     *  @param      properties      MorphAdorner properties.
     *
     *  @return     The part of speech retagger.
     */

    public static PartOfSpeechRetagger newPartOfSpeechRetagger
    (
        UTF8Properties properties
    )
    {
        String className    = null;

        if ( properties != null )
        {
            className   =
                properties.getProperty( "partofspeechretagger.class" );
        }

        if ( className == null )
        {
            className   =
                ClassUtils.packageName
                (
                    PartOfSpeechRetaggerFactory.class.getName()
                ) + "DefaultPartOfSpeechRetagger";
        }

        return newPartOfSpeechRetagger( className );
    }

    /** Get a partOfSpeechRetagger of a specified class name.
     *
     *  @param  className   Class name for the partOfSpeechRetagger.
     *
     *  @return             The partOfSpeechRetagger.
     */

    public static PartOfSpeechRetagger newPartOfSpeechRetagger
    (
        String className
    )
    {
        PartOfSpeechRetagger partOfSpeechRetagger   = null;

        try
        {
            partOfSpeechRetagger    =
                (PartOfSpeechRetagger)Class.forName(
                    className ).newInstance();
        }
        catch ( Exception e )
        {
            String fixedClassName   =
                (String)retaggerClassMap.get( className );

            if ( fixedClassName != null )
            {
                try
                {
                    partOfSpeechRetagger    =
                        (PartOfSpeechRetagger)Class.forName(
                            fixedClassName ).newInstance();
                }
                catch ( Exception e2 )
                {
                    System.err.println(
                        "Unable to create part of speech retagger of class " +
                        fixedClassName + ", using default retagger." );

                    partOfSpeechRetagger    =
                        new DefaultPartOfSpeechRetagger();
                }
            }
            else
            {
                System.err.println(
                    "Unable to create part of speech retagger of class " +
                    className + ", using default retagger." );

                partOfSpeechRetagger    = new DefaultPartOfSpeechRetagger();
            }
        }

        return partOfSpeechRetagger;
    }

    /** Create short tagger class name -> full class names.
     */

    static
    {
        String classPrefix  =
            ClassUtils.packageName(
                PartOfSpeechRetaggerFactory.class.getName() );

        retaggerClassMap.put
        (
            "DefaultPartOfSpeechRetagger" ,
            classPrefix + ".DefaultPartOfSpeechRetagger"
        );

        retaggerClassMap.put
        (
            "HeppleTagger" ,
            classPrefix + ".hepple.HeppleTagger"
        );

        retaggerClassMap.put
        (
            "IRetagger" ,
            classPrefix + ".iretagger.IRetagger"
        );

        retaggerClassMap.put
        (
            "NoopRetagger" ,
            classPrefix + ".noopretagger.NoopRetagger"
        );

        retaggerClassMap.put
        (
            "ProperNounRetagger" ,
            classPrefix + ".propernounretagger.ProperNounRetagger"
        );

        retaggerClassMap.put
        (
            "SimpleRuleBasedTagger" ,
            classPrefix + ".simplerulebased.SimpleRuleBasedTagger"
        );

        retaggerClassMap.put
        (
            "TCPRetagger" ,
            classPrefix + ".tcpretagger.TCPRetagger"
        );
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



