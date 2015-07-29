package edu.northwestern.at.morphadorner.tools.annolex;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.Arrays;

import edu.northwestern.at.utils.*;

/** Information about a single word element correction.
 *
 *  <ul>
 *  <li>The work ID.</li>
 *  <li>The permanent word ID.</li>
 *  <li>The updated word ID for gaps.</li>
 *  <li>The old spelling.</li>
 *  <li>The corrected spelling.</li>
 *  <li>The corrected standard spelling.</li>
 *  <li>The corrected lemma.</li>
 *  <li>The corrected part of speech.</li>
 *  <li>The correction type.</li>
 *  </ul>
 */

public class CorrectedWord
    implements Comparable<CorrectedWord>, Serializable
{
    /** Work ID. */

    protected String workId;

    /** Word ID. */

    protected String id;

    /** Updated word ID. */

    protected String updatedId;

    /** Old spelling. */

    protected String oldSpelling;

    /** Corrected spelling. */

    protected String spelling;

    /** Corrected standard spelling. */

    protected String standardSpelling;

    /** Corrected lemmata. */

    protected String lemmata;

    /** Corrected parts of speech. */

    protected String partsOfSpeech;

    /** Correction type. */

    protected String correctionType;

    /** Create empty CorrectedWord object.
     */

    public CorrectedWord()
    {
    }

    /** Create populated CorrectedWord object.
     */

    public CorrectedWord
    (
        String workId ,
        String id ,
        String oldSpelling ,
        String spelling ,
        String standardSpelling ,
        String lemmata ,
        String partsOfSpeech ,
        String correctionType
    )
    {
        this.workId             = workId;
        this.id                 = id;
        this.updatedId          = id;
        this.oldSpelling        = oldSpelling;
        this.spelling           = spelling;
        this.standardSpelling   = standardSpelling;
        this.lemmata            = lemmata;
        this.partsOfSpeech      = partsOfSpeech;
        this.correctionType     = correctionType;
    }

    /** Create populated CorrectedWord object.
     */

    public CorrectedWord
    (
        String workId ,
        String id ,
        String updatedId ,
        String oldSpelling ,
        String spelling ,
        String standardSpelling ,
        String lemmata ,
        String partsOfSpeech ,
        String correctionType
    )
    {
        this.workId             = workId;
        this.id                 = id;
        this.updatedId          = updatedId;
        this.oldSpelling        = oldSpelling;
        this.spelling           = spelling;
        this.standardSpelling   = standardSpelling;
        this.lemmata            = lemmata;
        this.partsOfSpeech      = partsOfSpeech;
        this.correctionType     = correctionType;
    }

    /** Get work ID.
     *
     *  @return     The work ID.
     */

    public String getWorkId()
    {
        return workId;
    }

    /** Set work ID.
     *
     *  @param  workId  The work ID.
     */

    public void setWorkId( String workId )
    {
        this.workId = workId;
    }

    /** Get word ID.
     *
     *  @return     The word ID.
     */

    public String getId()
    {
        return id;
    }

    /** Set word ID.
     *
     *  @param  id  The word ID.
     */

    public void setId( String id )
    {
        this.id = id;
    }

    /** Get updated word ID.
     *
     *  @return     The updated word ID.
     */

    public String getUpdatedId()
    {
        return updatedId;
    }

    /** Set updated word ID.
     *
     *  @param  updatedId   The updated word ID.
     */

    public void setUpdatedId( String updatedId )
    {
        this.updatedId  = updatedId;
    }

    /** Get the spelling.
     *
     *  @return     The spelling.
     */

    public String getSpelling()
    {
        return spelling;
    }

    /** Set the spelling.
     *
     *  @param  spelling    The spelling.
     */

    public void setSpelling( String spelling )
    {
        this.spelling   = spelling;
    }

    /** Get the standard spelling.
     *
     *  @return     The standard spelling.
     */

    public String getStandardSpelling()
    {
        return standardSpelling;
    }

    /** Set the standard spelling.
     *
     *  @param  standardSpelling    The standard spelling.
     */

    public void setStandardSpelling( String standardSpelling )
    {
        this.standardSpelling   = standardSpelling;
    }

    /** Get the old spelling.
     *
     *  @return     The old spelling.
     */

    public String getOldSpelling()
    {
        return oldSpelling;
    }

    /** Set the old spelling.
     *
     *  @param  oldSpelling The old spelling.
     */

    public void setOldSpelling( String oldSpelling )
    {
        this.oldSpelling    = oldSpelling;
    }

    /** Get the lemmata.
     *
     *  @return     The lemmata.
     *
     *  <p>
     *  Compound lemmata are separated by a separator tring.
     *  </p>
     */

    public String getLemmata()
    {
        return lemmata;
    }

    /** Set the lemmata.
     *
     *  @param  lemmata     The lemmata.
     */

    public void setLemmata( String lemmata )
    {
        this.lemmata    = lemmata;
    }

    /** Get the parts of speech.
     *
     *  @return     The parts of speech.
     */

    public String getPartsOfSpeech()
    {
        return partsOfSpeech;
    }

    /** Set the parts of speech.
     *
     *  @param  partsOfSpeech   The parts of speech.
     */

    public void setPartsOfSpeech( String partsOfSpeech )
    {
        this.partsOfSpeech  = partsOfSpeech;
    }

    /** Get correctionType.
     *
     *  @return     The correctionType.
     */

    public String getCorrectionType()
    {
        return correctionType;
    }

    /** Set the correctionType.
     *
     *  @param  correctionType  The correction type.
     */

    public void setCorrectionType( String correctionType )
    {
        this.correctionType = correctionType;
    }

    /** Return correction as a tabbed string.
     *
     *  @return     Tabbed string:
     *              workid<tab>id<tab>old-spelling<tab>spelling<tab>standardspelling<tab>lemma<tab>pos<tab>cortype<tab>correctedId
     */

    public String toString()
    {
        return
            workId + "\t" +
            id + "\t" +
            oldSpelling + "\t" +
            spelling + "\t" +
            standardSpelling + "\t" +
            lemmata + "\t" +
            partsOfSpeech + "\t" +
            correctionType + "\t" +
            updatedId;
    }

    /** Compare this corrected word to another.
     *
     *  @param  otherWord   Other word to compare to this one.
     *
     *  @return         < 0 if the other object is greater than this one,
     *                  = 0 if the two objects are equal,
     *                  > 0 if the other object is less than this one.
     *
     *  <p>
     *  We only compare the word IDs.
     *  </p>
     */

    public int compareTo( CorrectedWord otherWord )
    {
        int result  = Integer.MIN_VALUE;

        if ( otherWord != null )
        {
            result  = Compare.compare( id , otherWord.getId() );
        }

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



