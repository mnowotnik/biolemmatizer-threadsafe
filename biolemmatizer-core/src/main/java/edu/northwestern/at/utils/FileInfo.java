package edu.northwestern.at.utils;

/*  Please see the license information at the end of this file. */

import java.util.*;
import java.io.*;

/** Holds information about a single file. */

public class FileInfo
{
    /** The wrapped file. */

    File file;

    /** The base directory. */

    String baseDirectory;

    /** Create a wrapped file.
     *
     *  @param  file    The file to wrap.
     */

    public FileInfo( File file )
    {
        this.file   = file;
    }

    /** Create a wrapped file.
     *
     *  @param  file            The file to wrap.
     *  @param  baseDirectory   The base directory.
     */

    public FileInfo( File file , File baseDirectory )
    {
        this.file           = file;

        if ( baseDirectory != null )
        {
            try
            {
                this.baseDirectory  = baseDirectory.getCanonicalPath();
            }
            catch ( Exception e )
            {
            }
        }
    }

    /** Get file name.
     *
     *  @return     Short file name without leading path.
     */

    public String getFileName()
    {
        return ( file == null ) ? "" : file.getName();
    }

    /** Get full file name.
     *
     *  @return     Full file name with leading path.
     */

    public String getFullFileName()
    {
        String result   = "";

        if ( file != null )
        {
            try
            {
                result  = file.getCanonicalPath();
            }
            catch ( Exception e )
            {
            }
        }

        return result;
    }

    /** Get full file name relative to base directory.
     *
     *  @return     Full file name with leading path relative to base directory.
     */

    public String getRelativeFileName()
    {
        String result   = "";

        if ( file != null )
        {
            try
            {
                result  = file.getCanonicalPath();
            }
            catch ( Exception e )
            {
            }
        }

        if ( result.length() >= baseDirectory.length() )
        {
            result  = result.substring( baseDirectory.length() );
        }

        return result;
    }

    /** Get file length.
     *
     *  @return     File length.
     */

    public long getLength()
    {
        return ( file == null ) ? 0 : file.length();
    }

    /** Get last modified time.
     *
     *  @return     Last modified time.
     */

    public long getLastModified()
    {
        return ( file == null ) ? 0 : file.lastModified();
    }

    /** Get last modified date/time.
     *
     *  @return     Last modified date/time.
     */

    public Date getLastModifiedDateTime()
    {
        return ( file == null ) ?
            new Date( 0 ) : new Date( file.lastModified() );
    }

    /** True if file is a normal file.
     *
     *  @return     true if file is a normal file.
     */

    public boolean getIsFile()
    {
        return ( file == null ) ? false : file.isFile();
    }

    /** True if file is a directory.
     *
     *  @return     true if file is a directory.
     */

    public boolean getIsDirectory()
    {
        return ( file == null ) ? false : file.isDirectory();
    }

    /** Get parent file.
     *
     *  @return     Parent file.
     */

    public File getParentFile()
    {
        return ( file == null ) ? null : file.getParentFile();
    }

    /** Get parent file path.
     *
     *  @return     Parent file path.
     */

    public String getParentPath()
    {
        String result   = "";

        if ( file != null )
        {
            try
            {
                result  = file.getParentFile().getCanonicalPath();
            }
            catch ( Exception e )
            {
            }
        }

        return result;
    }

    /** Get parent directory path relative to base directory.
     *
     *  @return     Parent directory path relative to base directory.
     */

    public String getRelativeParentPath()
    {
        String result   = "";

        if ( file != null )
        {
            try
            {
                result  = file.getParentFile().getCanonicalPath();
            }
            catch ( Exception e )
            {
            }

            if ( result.length() >= baseDirectory.length() )
            {
                result  = result.substring( baseDirectory.length() );
            }
        }

        return result;
    }

    /** Return full file name as default string value.
     *
     *  @return full file name.
     */

    public String toString()
    {
        return getFullFileName();
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



