package edu.northwestern.at.utils;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.util.*;
import java.util.zip.*;

/** Zip file utilities.
 */

public class ZipUtils
{
    /** Zip files in a directory tree.
     *
     *  @param  inputDirectory      The directory at which to start zipping.
     *  @param  outputZipFileName   The name of the output zip file.
     *
     *  @throws IOException         When something goes wrong.
     *
     *  <p>
     *  All the files in "inputDirectory" as well as its subdirectories
     *  are compressed and added to the output zip file specified
     *  by "outputZipFileName".  The file names have subdirectory
     *  names prepended which are rooted at the specified input directory.
     *  </p>
     */

    public static void zipDirectoryTree
    (
        String inputDirectory ,
        String outputZipFileName
    )
        throws IOException
    {
                                //  Open zip output stream to
                                //  specified output file.

        ZipOutputStream zipOutputStream =
            new ZipOutputStream
            (
                new FileOutputStream( outputZipFileName )
            );

        final File outputZipFile    = new File( outputZipFileName );

                                //  Zip the specified directory tree.
                                //  We use a filter to ensure the
                                //  zip file we are creating isn't
                                //  added to the output zip file.
        zipDirectoryTree
        (
            inputDirectory ,
            "" ,
            new FilenameFilter()
            {
                public boolean accept( File directory , String name )
                {
                    File testFile   = new File( directory , name );

                    return !testFile.equals( outputZipFile );
                }
            } ,
            zipOutputStream
        );
                                //  Close complete zip file.

        zipOutputStream.close();
    }

    /** Zip files in a directory tree.
     *
     *  @param  inputDirectory      The directory at which to start zipping.
     *  @param  parentPath          The parent path to the input directory.
     *  @param  filenameFilter      FileNameFilter for filtering files
     *                              to zip.
     *  @param  zipOutputStream     ZipOutputStream to which to write
     *                              files in directory tree.
     *                              The stream must already be open
     *                              on entry to this method.
     *
     *  @throws IOException         When something goes wrong.
     *
     *  <p>
     *  All the files in "inputDirectory" as well as its subdirectories
     *  are compressed and added to the ZipOutputStream specified
     *  by "zipOutputStream".
     *  </p>
     */

    public static void zipDirectoryTree
    (
        String inputDirectory ,
        String parentPath ,
        FilenameFilter filenameFilter ,
        ZipOutputStream zipOutputStream
    )
        throws IOException
    {
                                //  Get input directory.

        File inputDir   = new File( inputDirectory );

                                //  Get list of files in input directory.

        File[] files    = inputDir.listFiles();

                                //  Only zip files if we were able to
                                //  get the file list.
        if ( files != null )
        {
                                //  Loop through files and add each
                                //  to the zip output stream.

            byte[] readBuffer   = new byte[ 65536 ];
            int bytesRead       = 0;

            for ( File file : files )
            {
                                //  If file does not pass filename filter,
                                //  skip it.

                if  (   ( filenameFilter == null ) ||
                        ( filenameFilter.accept( inputDir , file.getName() ) ) )
                {
                                //  If entry is a directory,
                                //  recurse to get files in that
                                //  directory.

                    if ( file.isDirectory() )
                    {
                        String directoryPath    = file.getName();

                        if  (   ( parentPath != null ) &&
                                ( parentPath.length() > 0 )
                            )
                        {
                            directoryPath   =
//                              parentPath + File.separator + directoryPath;
                                parentPath + "/" + directoryPath;
                        }

                        zipDirectoryTree
                        (
                            file.getPath() ,
                            directoryPath ,
                            filenameFilter ,
                            zipOutputStream
                        );
                    }
                                //  If entry is a file ...
                    else
                    {
                                //  Open file stream over file.

                        FileInputStream fileInputStream =
                            new FileInputStream( file );

                                //  Create new zip file entry.

                        String entryName    = file.getName();

                        if  (   ( parentPath != null ) &&
                                ( parentPath.length() > 0 )
                            )
                        {
                            entryName   =
//                              parentPath + File.separator + entryName;
                                parentPath + "/" + entryName;
                        }

                        ZipEntry zipEntry   = new ZipEntry( entryName );

                                //  Add zip entry to ZipOutputStream.

                        zipOutputStream.putNextEntry( zipEntry );

                                //  Write file contents to ZipOutputStream.

                        while( ( bytesRead = fileInputStream.read( readBuffer ) ) != -1 )
                        {
                            zipOutputStream.write( readBuffer , 0 , bytesRead );
                        }
                                //  Close input file stream.

                        fileInputStream.close();

                                //  Close zip file entry.

                        zipOutputStream.closeEntry();
                    }
                }
            }
        }
    }

    /** Don't allow instantiation, do allow overrides. */

    protected ZipUtils()
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



