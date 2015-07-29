package edu.northwestern.at.utils;

/*  Please see the license information at the end of this file. */

import java.util.*;
import java.io.*;

/** Walk directory tree to create list of files and directories. */

public class DirectoryTreeList
{
    /** Walk directory tree to get sorted of all files and directories.
     *
     *  @param  startingDir Root directory to start walk.
     *
     *  @return Files and directories sorted by name.
     *
     *  @throws FileNotFounfException
     */

    public static List<File> getFileListing
    (
        File startingDir ,
        boolean recurse
    )
        throws FileNotFoundException
    {
        List<File> result   = null;

        if ( startingDir != null )
        {
            result  = getFileListingUnsorted( startingDir , recurse );

            Collections.sort( result );
        }

        return result;
    }

    /** Walk directory tree to get unsorted list of all files and directories.
     *
     *  @param  startingDir Root directory to start walk.
     *
     *  @return Files and directories sorted by name.
     *
     *  @throws FileNotFounfException
     */

    public static List<File> getFileListingUnsorted
    (
        File startingDir ,
        boolean recurse
    )
        throws FileNotFoundException
    {
        List<File> result       = new ArrayList<File>();
        File[] filesAndDirs     = startingDir.listFiles();
        List<File> filesDirs    = Arrays.asList( filesAndDirs );

        for ( File file : filesDirs )
        {
            result.add( file );

            if ( !file.isFile() && recurse )
            {
                List<File> deeperList =
                    getFileListingUnsorted( file , recurse );

                result.addAll( deeperList );
            }
        }

        return result;
    }

    /** Walk directory tree to get sorted list of FileInfo entries.
     */

    public static List<FileInfo> getFiles
    (
        File startingDir ,
        boolean recurse
    )
    {
        List<FileInfo> result   = new ArrayList<FileInfo>();

        try
        {
            List<File> files = getFileListing( startingDir , recurse );

            if ( files != null )
            {
                for ( File file : files )
                {
                    result.add( new FileInfo( file , startingDir ) );
                }
            }
        }
        catch ( Exception e )
        {
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



