package edu.northwestern.at.utils;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;

/** Directory utilities.
 *
 *  <p>
 *  This static class provides various utility methods for manipulating
 *  directories.
 *  </p>
 */

public class DirUtils
{
    /** Maximum number of tries to create a temporary directory. */

    protected static int MAX_DIR_CREATE_TRIES   = 10000;

    /** Get temporary files directory.
     *
     *  @return     Temporary files directory.
     */

    public static String getTemporaryFilesDirectory()
    {
        return System.getProperty( "java.io.tmpdir" );
    }

    /** Create a temporary directory.
     *
     *  @param  baseName    Base name for generating temporory name.
     *
     *  @return             Temporary directory.
     *
     *  @throws             IllegalStateException
     *                          If temporary directory cannot be created.
     */

    public static File createTemporaryDirectory( String baseName )
        throws IllegalStateException
    {
                                //  Get system temporary files directory.

        File baseDir    =
            new File( System.getProperty( "java.io.tmpdir" ) );

                                //  Add current time to given base name.

        if ( baseName == null )
        {
            baseName    = "";
        }

        baseName    = baseName + System.currentTimeMillis() + "-";

                                //  Try to create directory.
                                //  Add increment to end of file name
                                //  and try again if the directory
                                //  already exists.

        for ( int i = 0 ; i < MAX_DIR_CREATE_TRIES ; i++ )
        {
            File tempDir    = new File( baseDir , baseName + i );

            if ( tempDir.mkdir() )
            {
                return tempDir;
            }
        }
                                //  Throw exception if directory
                                //  could not be created.

        throw new IllegalStateException
        (
            "Could not create temporary directory."
        );
    }

    /** Delete a directory.
     *
     *  @param  directory   Directory to delete
     *
     *  @return     true if directory deleted, false otherwise.
     */

    public static boolean deleteDirectory( File directory )
    {
                                //  Assume deletion works.

        boolean result  = true;

                                //  If directory exists, try to delete
                                //  its contents.

        if  (   ( directory != null ) &&
                directory.exists() &&
                directory.isDirectory()
            )
        {
            emptyOutDirectory( directory );

                                //  Remove top level directory.

            result  = directory.delete();
        }

        return result;
    }

    /** Delete a directory.
     *
     *  @param  directory   Directory to delete
     *
     *  @return     true if directory deleted, false otherwise.
     */

    public static boolean deleteDirectory( String directory )
    {
        return deleteDirectory( new File( directory ) );
    }

    /** Empty out a directory.
     *
     *  @param  directory   Directory to empty.
     */

    protected static void emptyOutDirectory( File directory )
    {
        if ( ( directory != null ) &&
            directory.exists() &&
            directory.isDirectory()
        )
        {
            File[] files    = directory.listFiles();

            for ( int i = 0 ; i < files.length ; i++ )
            {
                File file   = files[ i ];

                if ( file.isFile() )
                {
                    file.delete();
                }
                else
                {
                    emptyOutDirectory( file );
                }
            }
        }
    }

    /** Don't allow instantiation, do allow overrides. */

    protected DirUtils()
    {
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



