package edu.northwestern.at.morphadorner.tools.annolex;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.csv.*;
import edu.northwestern.at.utils.math.*;

/** Reads word correction information file.
 */

public class CorrectedWordsFileReader
{
    /** Number of fields in tabular file.
     */

    public static final int TABFIELDSCOUNT  = 9;

    /** Fields. */

    protected static final int WORKID       = 0;
    protected static final int WORDID       = 1;
    protected static final int SPELL        = 2;
    protected static final int CORSPELL     = 3;
    protected static final int STANSPELL    = 4;
    protected static final int CORLEM       = 5;
    protected static final int CORPOS       = 6;
    protected static final int CHECKBOX     = 7;
    protected static final int UPDATEDID    = 8;

    /** Gap word matcher. */

    protected static Pattern gapWordPattern         =
        Pattern.compile( "(.*)-([0-9.]+)-gap([0-9])+$" );

    protected static final Matcher gapWordMatcher   =
        gapWordPattern.matcher( "" );

    /** Number of lines read. */

    protected int linesRead     = 0;

    /** Tabular file holding corrected words definitions. */

    protected CSVFileReader tabFile = null;

    /** Allowed work IDs. */

    protected Set<String> allowedWorkIDs;

    /** Held corrected word. */

    protected CorrectedWord heldCorrectedWord   = null;

    /** Create tab file reader.
     *
     *  @param  tabInputFileName    Input tab file name.
     *  @param  allowedWorkIDs      Allowed work IDs.
     *
     *  <p>
     *  If allowedWorkIDs is not null, only corrections for words
     *  whose workIDs match an entry in allowedWordIDs will be stored.
     *  </p>
     */

    public CorrectedWordsFileReader
    (
        String tabInputFileName ,
        Set<String> allowedWorkIDs
    )
        throws java.io.IOException
    {
                                //  Create tabbed file reader.
        tabFile =
            new CSVFileReader( tabInputFileName , "utf-8" , '\t' , '\0' );

                                //  Save list of acceptable work IDs.

        this.allowedWorkIDs = allowedWorkIDs;
    }

    /** Read next corrected word.
     *
     *  @return     Corrected word.  Null if no more words to read.
     */

    public CorrectedWord readNextCorrectedWord()
    {
                                //  Resultant corrected word.

        CorrectedWord correctedWord = null;

                                //  If we have a held corrected word,
                                //  return that now.

        if ( heldCorrectedWord != null )
        {
            correctedWord       = heldCorrectedWord;
            heldCorrectedWord   = null;

            return correctedWord;
        }
                                //  Read next line in tabular file.

        List<String> fields = null;

        if ( tabFile != null )
        {
            try
            {
                fields = tabFile.readFields();
            }
            catch ( Exception e )
            {
            }
        }
                                //  Pick up fields if line read.

        if ( fields != null )
        {
                                //  Increment count of lines read.
            linesRead++;
                                //  Extend short line with
                                //  empty fields.

            if ( fields.size() < TABFIELDSCOUNT )
            {
                for ( int i = fields.size() ; i < TABFIELDSCOUNT ; i++ )
                {
                    fields.add( "" );
                }
            }
                                //  Get values of tabular fields.

            String workID           = fields.get( WORKID ).trim();
            String id               = fields.get( WORDID ).trim();
            String oldSpelling      = fields.get( SPELL ).trim();
            String spelling         = fields.get( CORSPELL ).trim();
            String lemmata          = fields.get( CORLEM ).trim();
            String partsOfSpeech    = fields.get( CORPOS ).trim();
            String correctionType   = fields.get( CHECKBOX ).trim();
            String standardSpelling = fields.get( STANSPELL ).trim();
            String updatedID        = fields.get( UPDATEDID ).trim();

                                //  Create CorrectedWord object to hold
                                //  tabular line values.

            boolean addWord     = true;

            if ( allowedWorkIDs != null )
            {
                addWord = allowedWorkIDs.contains( workID );
            }
                                //  Fix gap marker words.

            if ( addWord && ( !correctionType.equals( "5" ) ) )
            {
                gapWordMatcher.reset( id );

                if ( gapWordMatcher.find() )
                {
                    if ( !partsOfSpeech.equals( "zz" ) )
                    {

                                //  Change ID for pseudo-word for gap
                                //  to real word for insertion.

                        int gapValue    =
                            Integer.parseInt( gapWordMatcher.group( 3 ) );

                        String sIdValue = gapWordMatcher.group( 2 );

                        int iPos    = sIdValue.indexOf( "." );

                        if ( iPos >= 0 )
                        {
                            sIdValue    = sIdValue.substring( 0 , iPos );
                        }

                        long idValue    = Integer.parseInt( sIdValue );

                        String newIdValue   =
                            ( idValue + gapValue + 1 ) + "";

                        updatedID           =
                            gapWordMatcher.group( 1 ) + "-" +
                            StringUtils.dupl
                            (
                                "0" ,
                                sIdValue.length() - newIdValue.length()
                            )
                            + newIdValue;

                        correctionType  = "5";
                    }
                                //  Ignore gap words that are not
                                //  corrected since the original gap
                                //  element remains in the document
                                //  XML.
                    else
                    {
                        addWord = false;
                    }
                }
            }
                                //  Create corrected word from this
                                //  entry.
            if ( addWord )
            {
                correctedWord   =
                    new CorrectedWord
                    (
                        workID ,
                        id ,
                        updatedID ,
                        oldSpelling ,
                        spelling ,
                        standardSpelling ,
                        lemmata ,
                        partsOfSpeech ,
                        correctionType
                    );
            }
        }
        else
        {
                                //  Close tabular file if no more lines
                                //  to read.
            closeFile();
        }
                                //  Return corrected word.
        return correctedWord;
    }

    /** Read specified number of corrected words to map.
     *
     *  @return     Map of word ID to corrected word.
     */

    public Map<String, CorrectedWord> readCorrectedWords( int wordsToRead )
    {
        Map<String, CorrectedWord> correctedWordMap =
            new LinkedHashMap<String, CorrectedWord>();

        for ( int i = 0 ; i < wordsToRead ; i++ )
        {
            CorrectedWord correctedWord = readNextCorrectedWord();

            if ( correctedWord != null )
            {
                correctedWordMap.put
                (
                    correctedWord.getId() ,
                    correctedWord
                );
            }
            else
            {
                if ( tabFile == null )
                {
                    break;
                }
            }
        }

        return correctedWordMap;
    }

    /** Read all corrected words to map.
     *
     *  @return     Map of word ID to corrected word.
     */

    public Map<String, CorrectedWord> readAllCorrectedWords()
    {
        return readCorrectedWords( Integer.MAX_VALUE );
    }

    /** Return list of all corrected words.
     *
     *  @return     List of all corrected words.
     */

    public List<CorrectedWord> getCorrectedWords()
    {
        List<CorrectedWord> result      = ListFactory.createNewList();

        Map<String, CorrectedWord> correctedWordMap =
            readAllCorrectedWords();

        Iterator<String> iterator       =
            correctedWordMap.keySet().iterator();

        while ( iterator.hasNext() )
        {
            String id   = iterator.next();

            result.add( correctedWordMap.get( id ) );
        }

        return result;
    }

    /** Return list of all corrected word IDs.
     *
     *  @return     List of all corrected word IDs (strings).
     */

    public List<String> getCorrectedWordIDs()
    {
        List<String> result = ListFactory.createNewList();

        Map<String, CorrectedWord> correctedWordMap =
            readAllCorrectedWords();

        result.addAll( correctedWordMap.keySet() );

        return result;
    }

    /** Close input file.
     */

    public void closeFile()
    {
        try
        {
            if ( tabFile != null )
            {
                tabFile.close();
                tabFile = null;
            }
        }
        catch ( Exception e )
        {
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



