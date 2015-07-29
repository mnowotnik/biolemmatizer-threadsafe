package edu.northwestern.at.morphadorner.tools.compareadornedfiles;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;

import edu.northwestern.at.utils.ListFactory;

/** Records modifications to a tokenized or adorned TEI XML file.
 *
 *  <p>
 *  The changes are recorded on a token basis in the following format.
 *  </p>
 *
 *  <pre>
 *  &lt;ChangeLog&gt;
 *   &lt;changeTime&gt;The time the change file was created.&lt;/changeTime&gt;
 *   &lt;changeDescription&gt;A description of the changes.&lt;/changeDescription&gt;
 *   &lt;changes&gt;
 *     &lt;change&gt;
 *       &lt;id&gt;xml:id of token to be changed.&lt;/id&gt;
 *       &lt;changeType&gt;addition, modification, or deletion.&lt;/changeType&gt;
 *       &lt;fieldType&gt;Type of field to change: text or attribute.&lt;/fieldType&gt;
 *       &lt;oldValue&gt;Old field value.&lt;/oldValue&gt;
 *       &lt;newValue&gt;New field value.&lt;/newValue&gt;
 *       &lt;siblingID&gt;xml:id of sibling word for a word being added.&lt;/siblingID&gt;
 *       &lt;blankPrecedes&gt;true if blank precedes the token, else false.&lt;/blankPrecedes&gt;
 *       &lt;blankFollows&gt;true if blank follows the token, else false.&lt;/blankFollows&gt;
 *     &lt;/change&gt;
 *        ...
 *        (more &lt;change&gt; entries)
 *        ...
 *   &lt;/changes&gt;
 *  &lt;/ChangeLog&gt;
 *  </pre>
 */

public class WordChangeLog
{
    /** Time/date of change. */

    protected java.util.Date changeTime = new Date();

    /** Description of change. */

    protected String changeDescription  = null;

    /** List of changes. */

    protected List<WordChange> changes  = ListFactory.createNewList();

    /** Create empty change log. */

    public WordChangeLog()
    {
    }

    /** Create change log with a description. */

    public WordChangeLog( String changeDescription )
    {
        this.changeDescription  = changeDescription;
    }

    /** Get time of change.
     *
     *  @return     Time of change.
     */

    public Date getChangeTime()
    {
        return changeTime;
    }

    /** Set time of change.
     *
     *  @param  changeTime  Time of change.
     */

    public void SetChangeTime( Date changeTime )
    {
        this.changeTime = changeTime;
    }

    /** Get description of change.
     *
     *  @return     Description of change.
     */

    public String getChangeDescription()
    {
        return changeDescription;
    }

    /** Set description of change.
     *
     *  @param  changeDescription   Description of change.
     */

    public void SetChangeDescription( String changeDescription )
    {
        this.changeDescription  = changeDescription;
    }

    /** Add entry to change log.
     *
     *  @param  wordChange  The change to add.
     */

    public void addChange( WordChange wordChange )
    {
        changes.add( wordChange );
    }

    /** Get list of changes.
     *
     *  @return     List of changes.
     */

    public List<WordChange> getChanges()
    {
        return changes;
    }

    /** Get number of changes.
     *
     *  @return     The number of changes.
     */

    public int getNumberOfChanges()
    {
        return changes.size();
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



