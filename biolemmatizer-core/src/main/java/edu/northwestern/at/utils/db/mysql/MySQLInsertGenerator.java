package edu.northwestern.at.utils.db.mysql;

/*  Please see the license information at the end of this file. */

import java.util.*;
import java.io.*;

import edu.northwestern.at.utils.Env;

/** A MySQL insert statement generator.
 *
 *  <p>
 *  This class provides for constructing a MySQL-specific SQL Insert
 *  statement that takes multiple value lists in one insert.  This kind of
 *  insert typically runs an order of magnitude faster than individual inserts.
 *  </p>
 */

public class MySQLInsertGenerator
{
    /** The name of the database table into which to insert the data rows.
     */

    protected String tableName;

    /** The name of the column fields, in order, for each data row.
     */

    protected String[] fieldNames;

    /** True if a field is a number, false otherwise.  Use to determine
     *  how to format data values in the generated insert statement.
     */

    protected boolean[] isNumeric;

    /** The string buffer in which to build the insert statement.
     */

    protected StringBuffer insertBuffer;

    /** Create a MySQL insert generator.
     *
     *  @param  tableName   The database table name to receive the data.
     *  @param  fieldNames  The name of the column fields, in order, for
     *                      each data row.
     *  @param  isNumeric   True if the associated field is numeric, false
     *                      for a string.
     */

    public MySQLInsertGenerator
    (
        String tableName ,
        String[] fieldNames ,
        boolean[] isNumeric
    )
    {
        this.tableName      = tableName;
        this.fieldNames     = fieldNames;
        this.isNumeric      = isNumeric;

        this.insertBuffer   = new StringBuffer();
    }

    /** Escapes single quotes in a data value.
     *
     *  @param  value   The value to escape.
     *
     *  @return         The value with single quotes escaped using \' .
     */

    protected String escapeSingleQuotes( String value )
    {
//      String result   = value.replaceAll( "\\\\" , "\\\\\\\\" );
        String result   = value;

        if ( result.indexOf( "'" ) >= 0 )
        {
            StringBuffer sb = new StringBuffer();

            for ( int i = 0 ; i < result.length() ; i++ )
            {
                char ch = result.charAt( i );

                if ( ch == '\'' )
                {
                    sb.append( "\\'" );
                }
                else
                {
                    sb.append( ch );
                }
            }

            result  = sb.toString();
        }

        return result;
    }

    /** Add a row of data.
     *
     *  @param  rowData     An Object[] array containing the data values.
     *
     *  <p>
     *  Each row value must have a proper toString() method defined.
     *  The number of row values must match the number of field names
     *  passed in the contructor.  If there are two many values, the
     *  extra values are ignored.  If there are too few values, database
     *  null values are added.
     *  </p>
     */

    public void addRow( Object[] rowData )
    {
                                //  If the buffer is empty,
                                //  generator the initial part of the
                                //  insert statement.

        if ( insertBuffer.length() == 0 )
        {
            insertBuffer.append(  Env.LINE_SEPARATOR );
            insertBuffer.append( "insert into " );
            insertBuffer.append( tableName );
            insertBuffer.append( "(" );

            for ( int i = 0 ; i < fieldNames.length ; i++ )
            {
                if ( i > 0 ) insertBuffer.append( ", " );
                insertBuffer.append( fieldNames[ i ] );
            }

            insertBuffer.append( ") values (" );
        }
        else
        {
            insertBuffer.append( ", (" );
        }
                                //  Append data values for this row
                                //  to the insert statement.

        for (   int i = 0 ;
                i < Math.min( rowData.length , fieldNames.length ) ;
                i++
            )
        {
            if ( i > 0 )
            {
                insertBuffer.append( "," );
            }

            if ( rowData[ i ] == null )
            {
                insertBuffer.append( "NULL" );
            }
            else
            {
                String rowValue = rowData[ i ].toString();

                if ( !isNumeric[ i ] )
                {
                    insertBuffer.append( "'" );
                    rowValue    = escapeSingleQuotes( rowValue );
                }

                insertBuffer.append( rowValue );

                if ( !isNumeric[ i ] )
                {
                    insertBuffer.append( "'" );
                }
            }
        }
                                //  Fill out too-short row data
                                //  with database nulls.  The extra data at
                                //  the end of too-long rows will be ignored.

        for ( int i = rowData.length ; i < fieldNames.length ; i++ )
        {
            if ( i > 0 )
            {
                insertBuffer.append( "," );
            }

            insertBuffer.append( "NULL," );
        }

        insertBuffer.append( ")" );
        insertBuffer.append(  Env.LINE_SEPARATOR );
    }

    /** Get the insert statement.
     *
     *  @return     The completed insert statement.
     *
     *  <p>
     *  The string buffer used to build the insert statement is emptied.
     *  Any subsequent calls to addRow will start a new insert statement
     *  with the same table name and field names are defined in the
     *  constructor call.
     *  </p>
     */

    public String getInsert()
    {
        String result   = insertBuffer.toString();

        insertBuffer    = new StringBuffer();

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


