package edu.northwestern.at.utils;

/*  Please see the license information at the end of this file. */

import java.util.*;

/** Exception for reporting invalid data input.
 *
 *  <p>
 *  An InvalidDataException can be thrown as an exception in the usual way,
 *  e.g.,
 *  </p>
 *
 *  <p>
 *  <code>
 *  throw new InvalidDataException( "Your imput data is invalid" );
 *  </code>
 *  </p>
 *
 *  <p>
 *  However, an instance of InvalidDataException can also be used to
 *  collect a list of input data errors.  You can throw
 *  the existing InvalidDataException at this point and the detailed
 *  messages will be available to the code which catches the error.
 *  </p>
 *
 *  <p>
 *  Example:
 *  </p>
 *
 *  <pre>
 *  <code>
 *  InvalidDataException ide    = new InvalidDataException();
 *  lineNumber = 0;
 *
 *  ...
 *  // If an input line has a data error, add a descriptive message to ide
 *
 *  ide.addMessage( "Invalid input in line " + lineNumber );
 *  ...
 *
 *  if ( ide.hasMessages() )
 *  {
 *      throw ide;
 *  }
 *  </code>
 *  </pre>
 */

public class InvalidDataException extends Exception
{
    /** List to hold error messages. */

    protected List<String> errorMessages    =
        ListFactory.createNewList();

    /** Create empty data exception. */

    public InvalidDataException()
    {
    }

    /** Create data exception with error description.
     *
     *  @param  description     Description of error.
     */

    public InvalidDataException( String description )
    {
        super( description );
    }

    /** Add error message to list of errors.
     *
     *  @param  errorMessage        The message to add to the list.
     */

    public void addMessage( String errorMessage )
    {
        errorMessages.add( errorMessage );
    }

    /** Get list of error messages.
     *
     *  @return     Error messages as unmodifiable list.
     */

    public List<String> getMessages()
    {
        return Collections.unmodifiableList( errorMessages );
    }

    /** Check there are any error messages.
     *
     *  @return     true if there are any error messages.
     */

    public boolean hasMessages()
    {
        return ( errorMessages.size() > 0 );
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



