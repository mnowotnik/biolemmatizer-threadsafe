package edu.northwestern.at.morphadorner.tools.compareadornedfiles;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;

import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.XStream;

import edu.northwestern.at.morphadorner.tools.*;
import edu.northwestern.at.utils.*;

/** Compare word elements in two adorned files and emit differences.
 *
 *  <p>
 *  Usage:
 *  </p>
 *
 *  <pre>
 *  java edu.northwestern.at.morphadorner.tools.compareadornedfiles.CompareAdornedFiles
 *      oldadorned.xml newadorned.xml diffs.xml
 *  </pre>
 *
 *  <p>
 *  oldadorned.xml  -- "Old" unmodified adorned file.<br />
 *  newadorned.xml  -- "new" modified version of adorned file.<br />
 *  diffs.xml       -- Word differences between two adorned files.
 *  </p>
 */

public class CompareAdornedFiles
{
    /** Create CompareAdornedFiles.
     *
     *  @param  oldAdornedFileName  Old adorned file name.
     *  @param  newAdornedFileName  New adorned file name.
     *  @param  changesFileName     Changes file name.
     *  @param  printStream         Output stream for reporting progress.
     *
     *  @throws Exception   in case of error.
     */

    public CompareAdornedFiles
    (
        String oldAdornedFileName ,
        String newAdornedFileName ,
        String changesFileName ,
        PrintStream printStream
    )
        throws Exception
    {
        long startTime  = System.currentTimeMillis();

                                //  Load words in first (old) adorned file.

        AdornedWordsLoader xmlReader    =
            new AdornedWordsLoader( oldAdornedFileName );

                                //  Load words in second (new) adorned file.

        AdornedWordsLoader xmlReader2   =
            new AdornedWordsLoader( newAdornedFileName );

        long endTime    =
            ( System.currentTimeMillis() - startTime + 999 ) / 1000;

                                //  Extract word ID list from each file.

        List<String> wordIDs    = xmlReader.getAdornedWordIDs();
        List<String> wordIDs2   = xmlReader2.getAdornedWordIDs();

                                //  Report count of words read in each file.

        printStream.println
        (
            "Read " +
            Formatters.formatIntegerWithCommas( wordIDs.size() ) +
            " words from " + oldAdornedFileName + "."
        );

        printStream.println
        (
            "Read " +
            Formatters.formatIntegerWithCommas( wordIDs2.size() ) +
            " words from " + newAdornedFileName + "."
        );

        startTime   = System.currentTimeMillis();

                                //  Create change log to hold list of
                                //  word-level changes made from old to
                                //  new adorned file.

        WordChangeLog changeLog =
            new WordChangeLog
            (
                "Changes from " +
                FileNameUtils.stripPathName( oldAdornedFileName ) + " to " +
                FileNameUtils.stripPathName( newAdornedFileName ) +
                " as determined by CompareAdornedFiles."
            );
                                //  Maintain counts of words added,
                                //  deleted, and modified.

        int wordsModified   = 0;
        int wordsAdded      = 0;
        int wordsDeleted    = 0;

                                //  Start by looking for differences for
                                //  words listed in both adorned files.

        for ( int wordOrd = 0 ; wordOrd < wordIDs.size() ; wordOrd++ )
        {
                                //  Get next word's information.

            String id   = wordIDs.get( wordOrd );

                                //  Get data for this word in old file.

            AdornedWordData w       = xmlReader.getAdornedWordData( id );

                                //  Get data for this word in new file.

            AdornedWordData w2      = xmlReader2.getAdornedWordData( id );

                                //  If the word exists in both files,
                                //  look for differences.

            if ( ( w != null ) && ( w2 != null ) )
            {
                                //  Log word text difference if any.

                boolean wordTextModified    = false;

                String wText    = w.getWordText();
                String wText2   = w2.getWordText();

                if ( !wText.equals( wText2 ) )
                {
                    changeLog.addChange
                    (
                        new WordChange
                        (
                            id ,
                            WordChangeType.modification ,
                            FieldType.text ,
                            null ,
                            wText ,
                            wText2 ,
                            null ,
                            w2.getBlankPrecedes()
                        )
                    );

                    wordTextModified    = true;
                }
                                //  Log attribute value differences.

                boolean attributesModified  =
                    logAttributeDifferences( changeLog , id , w , w2 );

                                //  Increment count of words modified.

                if ( wordTextModified || attributesModified )
                {
                    wordsModified++;
                }
            }
        }
                                //  Look for words which exist in
                                //  old file but not in new files --
                                //  e.g., deleted words.

        Set<String> set1    = SetFactory.createNewSortedSet();
        set1.addAll( wordIDs );

        Set<String> set2    = SetFactory.createNewSortedSet();
        set2.addAll( wordIDs2 );

        Set<String> deletedWords    = SetFactory.createNewSortedSet();
        deletedWords.addAll( set1 );
        deletedWords.removeAll( set2 );

                                //  Look for words which exist in
                                //  new file but not in old file --
                                //  e.g., added words.

        Set<String> addedWords  = SetFactory.createNewSortedSet();
        addedWords.addAll( set2 );
        addedWords.removeAll( set1 );

                                //  Log deleted words.

        for ( String id : deletedWords )
        {
            AdornedWordData w   = xmlReader.getAdornedWordData( id );

            String wText    = ( w == null ) ? "" : w.getWordText();

                                //  Log word deletion.

            changeLog.addChange
            (
                new WordChange
                (
                    id ,
                    WordChangeType.deletion ,
                    FieldType.text ,
                    null ,
                    wText ,
                    null ,
                    w.getSiblingID() ,
                    w.getBlankPrecedes()
                )
            );
                                //  Log attribute value deletions.

            logAttributeDeletions( changeLog , id , w );
        }
                                //  Increment count of deleted words.

        wordsDeleted    += deletedWords.size();

                                //  Log added words.

        for ( String id2 : addedWords )
        {
            AdornedWordData w   = xmlReader2.getAdornedWordData( id2 );

            changeLog.addChange
            (
                new WordChange
                (
                    id2 ,
                    WordChangeType.addition ,
                    FieldType.text ,
                    null ,
                    null ,
                    w.getWordText() ,
                    w.getSiblingID() ,
                    w.getBlankPrecedes()
                )
            );
                                //  Log attribute value additions.

            logAttributeAdditions( changeLog , id2 , w );
        }
                                //  Increment count of added words.

        wordsAdded  += addedWords.size();

                                //  Emit the change log in XML format.

        XStream xstream = new XStream();

        xstream.alias( "change" , WordChange.class );
        xstream.alias( "ChangeLog" , WordChangeLog.class );

        String xmlDiff  = xstream.toXML( changeLog );

                                //  Replace the apostrophe entities.

        xmlDiff = xmlDiff.replaceAll( "&apos;" , "'" );

                                //  Write XML change log to file.

        FileUtils.writeTextFile
        (
            new File( changesFileName ) , false , xmlDiff , "utf-8"
        );
                                //  Report the number of changes.
        endTime =
            ( System.currentTimeMillis() - startTime + 999 ) / 1000;

        printStream.println
        (
            "Found " +
            Formatters.formatIntegerWithCommas
            (
                changeLog.getChanges().size()
            ) +
            " changes in " +
            Formatters.formatLongWithCommas( endTime ) +
            ( ( endTime == 1 ) ? " second." : " seconds." )
        );

        printStream.println
        (
            "   " +
            Formatters.formatIntegerWithCommas
            (
                wordsModified
            ) +
            ( ( wordsModified == 1 ) ? " word " : " words " ) +
            "modified."
        );

        printStream.println
        (
            "   " +
            Formatters.formatIntegerWithCommas
            (
                wordsAdded
            ) +
            ( ( wordsAdded == 1 ) ? " word " : " words " ) +
            "added."
        );

        printStream.println
        (
            "   " +
            Formatters.formatIntegerWithCommas( wordsDeleted ) +
            ( ( wordsDeleted == 1 ) ? " word " : " words " ) +
            "deleted."
        );
    }

    /** Log differences in attributes and their values for two adorned words.
     *
     *  @param  changeLog   Change log in which to store changes.
     *  @param  id          The word ID.
     *  @param  w1          First "old" adorned word.
     *  @param  w2          Second "new" adorned word.
     *
     *  @return             true if any attributes added, deleted, or
     *                      modified from w1 to w2.
     */

    protected boolean logAttributeDifferences
    (
        WordChangeLog changeLog ,
        String id ,
        AdornedWordData w1 ,
        AdornedWordData w2
    )
    {
                                //  Assume no attribute changes.

        boolean result  = false;

                                //  Get attribute maps for old and new
                                //  words.

        Map<String, String> w1Map   = w1.getAttributeMap();
        Map<String, String> w2Map   = w2.getAttributeMap();

                                //  Get attribute names for old word.

        Iterator<String> w1AttrsIterator    = w1Map.keySet().iterator();

                                //  Check for changes in these attributes
                                //  in new word.

        while ( w1AttrsIterator.hasNext() )
        {
                                //  Get next attribute name.

            String attrName     = w1AttrsIterator.next();

                                //  Get value for this attribute in old
                                //  word.

            String attrValue1   = w1Map.get( attrName );

                                //  Get value for this attribute in new
                                //  word.

            String attrValue2   = w2Map.get( attrName );

                                //  If the attribute exists in both words,
                                //  look for a value difference.

            if ( ( attrValue1 != null ) && ( attrValue2 != null ) )
            {
                if ( !attrValue1.equals( attrValue2 ) )
                {
                    changeLog.addChange
                    (
                        new WordChange
                        (
                            id ,
                            WordChangeType.modification ,
                            FieldType.attribute ,
                            attrName ,
                            attrValue1 ,
                            attrValue2 ,
                            null ,
                            w2.getBlankPrecedes()
                        )
                    );

                    result  = true;
                }
            }
        }
                                //  Look for attribute which exist in
                                //  old word but not in new word --
                                //  e.g., deleted attributes.

        Set<String> set1    = SetFactory.createNewSortedSet();
        set1.addAll( w1Map.keySet() );

        Set<String> set2    = SetFactory.createNewSortedSet();
        set2.addAll( w2Map.keySet() );

        Set<String> deletedAttributes   = SetFactory.createNewSortedSet();
        deletedAttributes.addAll( set1 );
        deletedAttributes.removeAll( set2 );

                                //  Look for attributes which exist in
                                //  new word but not in old word --
                                //  e.g., added attributes.

        Set<String> addedAttributes = SetFactory.createNewSortedSet();
        addedAttributes.addAll( set2 );
        addedAttributes.removeAll( set1 );

                                //  Log deleted attributes.

        for ( String attrName : deletedAttributes )
        {
            changeLog.addChange
            (
                new WordChange
                (
                    id ,
                    WordChangeType.deletion ,
                    FieldType.attribute ,
                    attrName ,
                    w1Map.get( attrName ),
                    null ,
                    null ,
                    w2.getBlankPrecedes()
                )
            );
        }

        result  = result || ( deletedAttributes.size() > 0 );

                                //  Log added attributes.

        for ( String attrName : addedAttributes )
        {
            changeLog.addChange
            (
                new WordChange
                (
                    id ,
                    WordChangeType.addition ,
                    FieldType.attribute ,
                    attrName ,
                    null ,
                    w2Map.get( attrName ) ,
                    null ,
                    w2.getBlankPrecedes()
                )
            );
        }

        result  = result || ( addedAttributes.size() > 0 );

        return result;
    }

    /** Log attribute additions for an added word.
     *
     *  @param  changeLog   Change log in which to store changes.
     *  @param  id          The word ID.
     *  @param  w           The added adorned word.
     */

    protected void logAttributeAdditions
    (
        WordChangeLog changeLog ,
        String id ,
        AdornedWordData w
    )
    {
                                //  Get attribute map for added word.

        Map<String, String> wMap    = w.getAttributeMap();

                                //  Get attribute names.

        Iterator<String> wAttrsIterator = wMap.keySet().iterator();

                                //  For each attribute name, get its
                                //  value and log the addition.

        while ( wAttrsIterator.hasNext() )
        {
                                //  Get next attribute name.

            String attrName     = wAttrsIterator.next();

                                //  Get value for this attribute.

            String attrValue    = wMap.get( attrName );

                                //  If the attribute value exists,
                                //  log the addition.

            if ( attrValue != null )
            {
                changeLog.addChange
                (
                    new WordChange
                    (
                        id ,
                        WordChangeType.addition ,
                        FieldType.attribute ,
                        attrName ,
                        null ,
                        attrValue ,
                        null ,
                        w.getBlankPrecedes()
                    )
                );
            }
        }
    }

    /** Log attribute deletions for a deleted word.
     *
     *  @param  changeLog   Change log in which to store changes.
     *  @param  id          The word ID.
     *  @param  w           The deleted adorned word.
     */

    protected void logAttributeDeletions
    (
        WordChangeLog changeLog ,
        String id ,
        AdornedWordData w
    )
    {
                                //  Get attribute map for deleted word.

        Map<String, String> wMap    = w.getAttributeMap();

                                //  Get attribute names.

        Iterator<String> wAttrsIterator = wMap.keySet().iterator();

                                //  For each attribute name, get its
                                //  value and log the deletion.

        while ( wAttrsIterator.hasNext() )
        {
                                //  Get next attribute name.

            String attrName     = wAttrsIterator.next();

                                //  Get value for this attribute.

            String attrValue    = wMap.get( attrName );

                                //  If the attribute value exists,
                                //  log the deletion.

            if ( attrValue != null )
            {
                changeLog.addChange
                (
                    new WordChange
                    (
                        id ,
                        WordChangeType.deletion ,
                        FieldType.attribute ,
                        attrName ,
                        attrValue ,
                        null ,
                        null ,
                        w.getBlankPrecedes()
                    )
                );
            }
        }
    }

    /** Main program. */

    public static void main( String[] args )
    {
        try
        {
                                //  Allow utf-8 output standard output.

            PrintStream printStream     =
                new PrintStream
                (
                    new BufferedOutputStream( System.out ) ,
                    true ,
                    "utf-8"
                );
                                //  Make sure we have enough arguments.

            if ( args.length < 3 )
            {
                printStream.println( "Not enough parameters." );
                System.exit( 1 );
            }
                                //  Compare files.

            new CompareAdornedFiles
            (
                args[ 0 ] ,
                args[ 1 ] ,
                args[ 2 ] ,
                printStream
            );
                                //  Close  output stream.
            try
            {
                printStream.close();
            }
            catch ( Exception e )
            {
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
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



