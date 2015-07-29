package edu.northwestern.at.morphadorner.tools.compareadornedfiles;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;

/** Records a token-level change in an adorned file.
 */

public class WordChange
{
    /** ID for word changed.
     *
     *  <p>
     *  The ID is usually provided by the xml:id attribute.
     *  A "*" means the change applies to all words.
     *  </p>
     */

    public String id;

    /** Type of change:  addition, deletion, or modification.
     */

    public WordChangeType changeType;

    /** Field to which change is applied: text or attribute.
     */

    public FieldType fieldType;

    /** Name of field for attribute (empty if word text).
     */

    public String attributeName;

    /** Old field value. */

    public String oldValue;

    /** New field value. */

    public String newValue;

    /** Sibling to determine parent element when adding a word. */

    public String siblingID;

    /** If word is preceded by a blank element. */

    public boolean blankPrecedes;

    /** Create empty word change. */

    public WordChange()
    {
    }

    /** Create fully populated word change.
     *
     *  @param  id              ID (usually xml:id) of word to change.
     *  @param  changeType      Type of change (addition, deletion,
     *                          modification).
     *  @param  fieldType       Type of field to change (text or attribute).
     *  @param  attributeName   Name of attribute (empty if not attribute).
     *  @param  oldValue        Old value of field.
     *  @param  newValue        New value of field.
     *  @param  siblingID       Word ID of sibling when adding new word.
     *  @param  blankPrecedes   Word is preceded by a blank element.
     */

    public WordChange
    (
        String id ,
        WordChangeType changeType ,
        FieldType fieldType ,
        String attributeName ,
        String oldValue ,
        String newValue ,
        String siblingID ,
        boolean blankPrecedes
    )
    {
        this.id             = id;
        this.changeType     = changeType;
        this.fieldType      = fieldType;
        this.attributeName  = attributeName;
        this.oldValue       = oldValue;
        this.newValue       = newValue;
        this.siblingID      = siblingID;
        this.blankPrecedes  = blankPrecedes;
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



