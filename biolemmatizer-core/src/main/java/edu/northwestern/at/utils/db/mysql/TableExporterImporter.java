package edu.northwestern.at.utils.db.mysql;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.sql.*;
import edu.northwestern.at.utils.StringUtils;

/** MySQL Table Exporter/Importer.
 *
 *  <p>When creating large MySQL tables, it is much more efficient to export
 *  the data to a file, then import the file using the MySQL "load data infile"
 *  command, than it is to directly insert each individual row into the table.
 *  This class facilitates this optimization.
 */

public class TableExporterImporter {

    /** MySQL table name. */

    private String tableName;

    /** MySQL column names, comma-delimited, or null. */

    private String columnNames;

    /** Path to data file. */

    private String path;

    /** PrintWriter for data file. */

    private PrintWriter writer;

    /** True if at beginning of new line. */

    private boolean newLine = true;

    /** Creates a new MySQL table exporter/importer.
     *
     *  @param  tableName       MySQL table name.
     *
     *  @param  columnNames     MySQL column names, comma-delimited, in the
     *                          order in which their values will be printed.
     *                          May be null, in which case the MySQL-defined
     *                          order must be used.
     *
     *  @param  path            Path to data file, or null to use a
     *                          temporary scratch file.
     *
     *  @param  append          True to append to data file, false to
     *                          overwrite any existing data file.
     *
     *  @throws FileNotFoundException
     *  @throws UnsupportedEncodingException
     *  @throws IOException
     */

    public TableExporterImporter (String tableName, String columnNames,
        String path, boolean append)
            throws FileNotFoundException, UnsupportedEncodingException,
                IOException
    {
        this.tableName = tableName;
        this.columnNames = columnNames;
        File file = null;
        if (path == null) {
            file = File.createTempFile("TableExporterImporter", ".txt");
        } else {
            file = new File(path);
        }
        this.path = file.getCanonicalPath();
        FileOutputStream fos = new FileOutputStream(this.path, append);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
        BufferedWriter bw = new BufferedWriter(osw);
        writer = new PrintWriter(bw);
    }

    /** Prints a string field value.
     *
     *  @param  val     Field value.
     */

    public void print (String val) {
        if (!newLine) writer.print('\t');
        if (val == null) {
            writer.print("\\N");
        } else {
            val = val.replaceAll("\\\\", "\\\\\\\\");
            val = val.replaceAll("\n", "\\\\n");
            writer.print(val);
        }
        newLine = false;
    }

    /** Prints a long field value.
     *
     *  @param  val     Field value.
     */

    public void print (long val) {
        print(Long.toString(val));
    }

    /** Prints an integer field value.
     *
     *  @param  val     Field value.
     */

    public void print (int val) {
        print(Integer.toString(val));
    }

    /** Prints a byte field value.
     *
     *  @param  val     Field value.
     */

    public void print (byte val) {
        print(Byte.toString(val));
    }

    /** Prints a null field value.
     */

    public void printNull () {
        if (!newLine) writer.print('\t');
        writer.print("\\N");
        newLine = false;
    }

    /** Prints an Integer field value.
     *
     *  @param  val     Field value.
     */

    public void print (Integer val) {
        if (!newLine) writer.print('\t');
        if (val == null) {
            writer.print("\\N");
        } else {
            writer.print(val.intValue());
        }
        newLine = false;
    }

    /** Prints a Long field value.
     *
     *  @param  val     Field value.
     */

    public void print (Long val) {
        if (!newLine) writer.print('\t');
        if (val == null) {
            writer.print("\\N");
        } else {
            writer.print(val.longValue());
        }
        newLine = false;
    }

    /** Starts a new row.
     */

    public void println () {
        writer.println();
        newLine = true;
    }

    /** Closes the data file.
     */

    public void close () {
        writer.close();
    }

    /** Fix path separators in path.
     *
     *  @param  path    The file path to fix.
     *
     *  @return         The file path with backslashes replaced by
     *                  forward slashes.
     */

    protected String fixPathSeparators( String path )
    {
        return path.replace( '\\' , '/' );
    }

    /** Imports the data into the MySQL table.
     *
     *  <p>The data file is deleted after the data has been imported.
     *
     *  @param  c       JDBC connection to MySQL database.
     *
     *  @return         Number of rows imported.
     *
     *  @throws SQLException
     */

    public int importData (Connection c)
        throws SQLException
    {
        String lineTerm =
            StringUtils.escapeSpecialCharacters(
                System.getProperty("line.separator"));

        Statement s = c.createStatement();
        int ct = 0;
        if (columnNames == null) {
            ct = s.executeUpdate(
                "load data infile '" + fixPathSeparators( path ) +
                "' into table " + tableName +
                " lines terminated by '" + lineTerm + "'" );
        } else {
            ct = s.executeUpdate(
                "load data infile '" + fixPathSeparators( path ) +
                "' into table " + tableName +
                " lines terminated by '" + lineTerm + "'" +
                " (" + columnNames + ")" );
        }
        s.close();
        (new File(path)).delete();
        return ct;
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


