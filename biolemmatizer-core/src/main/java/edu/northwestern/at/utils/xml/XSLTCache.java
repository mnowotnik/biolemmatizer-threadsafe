package edu.northwestern.at.utils.xml;

import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import edu.northwestern.at.utils.MapFactory;

/** Caches compiled stylesheets. */

public class XSLTCache
{
    /** Maps XSLT file names to MapEntry instances. */

    protected static Map<String, MapEntry> cache = MapFactory.createNewMap();

    /** Flush all cached stylesheets from memory, emptying the cache. */

    public static synchronized void flushAll()
    {
        cache.clear( );
    }

    /** Flush a specific cached stylesheet from memory.
     *
     *  @param  xsltFileName    File name of XSLT stylesheet to remove.
     */

    public static synchronized void flush( String xsltFileName )
    {
        cache.remove( xsltFileName );
    }

    /** Obtain a new Transformer instance for the specified XSLT file name.
     *
     *  <p>
     *  A new entry will be added to the cache if this is the first request
     *  for the specified file name.
     *  </p>
     *
     *  @param xsltFileName the file name of an XSLT stylesheet.
     *
     *  @return     A transformation context for the given stylesheet.
     */

    public static synchronized Transformer newTransformer
    (
        String xsltFileName
    )
        throws TransformerConfigurationException
    {
        File xsltFile   = new File( xsltFileName );

                                //  When was file was last changed?

        long xslLastModified    = xsltFile.lastModified();

        MapEntry entry          = (MapEntry)cache.get( xsltFileName );

                                //  If file has been changed more recently
                                //  than the cached stylesheet, remove the
                                //  entry reference.
        if ( entry != null )
        {
            if ( xslLastModified > entry.lastModified )
            {
                entry = null;
            }
        }
                                //  Create new entry in the cache if
                                //  needed.
        if ( entry == null )
        {
            Source xslSource    = new StreamSource( xsltFile );

            TransformerFactory transFact    =
                TransformerFactory.newInstance();

            Templates templates = transFact.newTemplates( xslSource );

            entry   = new MapEntry( xslLastModified , templates );

            cache.put( xsltFileName , entry );
        }

        return entry.templates.newTransformer();
    }

    /** Allowed overrides but not instantiation. */

    protected XSLTCache()
    {
    }

    /** Holds a value in the XSLT cache map. */

    static class MapEntry
    {
        /** Time when XSLT file was last changed. */

        long lastModified;

        Templates templates;

        MapEntry( long lastModified , Templates templates )
        {
            this.lastModified   = lastModified;
            this.templates      = templates;
        }
    }
}

