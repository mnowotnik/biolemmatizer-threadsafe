package edu.northwestern.at.morphadorner.tools.adornedtosimpleteip5;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.text.*;
import java.util.*;

import org.jdom2.*;
import org.jdom2.filter.*;
import org.jdom2.input.*;
import org.jdom2.output.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.xml.*;
import edu.northwestern.at.utils.xml.jdom.*;
import edu.northwestern.at.morphadorner.tools.*;

/** Convert MorphAdorned file to simple TEI P5 format.
 *
 *  <p>
 *  AdornedToSimpleTEIP5 converts a base-level MorphAdorner file to a
 *  more TEI P5-like format.
 *  </p>
 *
 *  <p>
 *  Usage:
 *  </p>
 *
 *  <blockquote>
 *  <p>
 *  <code>
 *  adornedtosimpleteip5 outputdirectory [usereg|usechoice] interpgrp.xml goodfiles.txt badfiles.txt adorned1.xml adorned2.xml ...
 *  </code>
 *  </p>
 *  </blockquote>
 *
 *  <p>
 *  where
 *  </p>
 *
 *  <ul>
 *  <li><strong>outputdirectory</strong> specifies the output directory for the
 *       simplified TEI XML P5 format files.
 *       </li>
 *  <li><strong>usereg</strong> specifies that the standardized spelling
 *      should be emitted as a <em>reg=</em> attribute, while
 *      <strong>usechoice</strong> specifies that the standardized
 *      spelling should be emitted using TEI <em>&lt;choice&gt;</em> structure.
 *       </li>
 *  <li><strong>interpgrp.xml</strong> specifies the file name for a section
 *        of TEI XML which defines an interpGrp element for the part of
 *        speech tags.  This can be an empty file in which case the
 *        interpGrp is not added to each output TEI XML file.
 *        </li>
 *  <li><strong>goodfiles.txt</strong> specifies the name of a file to receive
 *        the names of TEI XML files successfully converted to simple
 *        TEI P5 format.
 *        </li>
 *  <li><strong>badfiles.txt</strong> specifies the name of a file to receive
 *        the names of TEI XML files which could not be successfully
 *        converted to simple TEI P5 format.
 *        </li>
 *  <li><strong>adorned1.xml adorned2.xml ...</strong> specifies the input
 *        MorphAdorned XML files from which to produce TEI XML P5 versions.
 *        </li>
 *  </ul>
 */

public class AdornedToSimpleTEIP5
{
    /** Number of documents to process. */

    protected static int docsToProcess      = 0;

    /** Current document. */

    protected static int currentDocNumber   = 0;

    /** XML text of interGrp section defining part of speech tags. */

    protected static String interpGrpXMLText    = "";

    /** True if interGrp text is not empty. */

    protected static boolean haveInterpGrp  = false;

    /** Force ana=#pos output for part of speech. */

    protected static boolean forceAna   = true;

    /** Use reg= instead of <choice>. */

    protected static boolean useReg = true;

    /** Output directory. */

    protected static String outputDirectory;

    /** Wrapper for printStream to allow utf-8 output. */

    protected static PrintStream printStream;

    /** # params before input file specs. */

    protected static final int INITPARAMS   = 5;

    /** Last word ID processed. */

    protected static String lastID  = "";

    /** Gap count. */

    protected static int gapCount       = 0;

    /** Sentence count. */

    protected static int sentenceCount  = 0;

    /** File name of file to hold names of works which fail conversion. */

    protected static String badWorksFileName    = "";

    /** File names of works containing errors. */

    protected static Set<String> badWorksSet    = null;

    /** File name of file to hold names of works for which conversion
        succeeds.
     */

    protected static String goodWorksFileName   = "";

    /** File names of works converted successfully. */

    protected static Set<String> goodWorksSet   = null;

    /** TEI name space. */

    protected static Namespace teiNamespace =
        Namespace.getNamespace( "http://www.tei-c.org/ns/1.0" );

    /** Main program.
     *
     *  @param  args    Program parameters.
     */

    public static void main( String[] args )
    {
                                //  Initialize.
        try
        {
            if ( !initialize( args ) )
            {
                System.exit( 1 );
            }
                                //  Process all files.

            long startTime      = System.currentTimeMillis();

            int filesProcessed  = processFiles( args );

            long processingTime =
                ( System.currentTimeMillis() - startTime + 999 ) / 1000;

                                //  Terminate.

            terminate( filesProcessed , processingTime );
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
        }
    }

    /** Initialize.
     */

    protected static boolean initialize( String[] args )
        throws Exception
    {
                                //  Allow utf-8 output to printStream .
        printStream =
            new PrintStream
            (
                new BufferedOutputStream( System.out ) ,
                true ,
                "utf-8"
            );
                                //  Get the file to check for non-standard
                                //  spellings.

        if ( args.length < ( INITPARAMS + 1  ) )
        {
            System.err.println( "Not enough parameters." );
            return false;
        }
                                //  Get output directory name.

        outputDirectory         = args[ 0 ];

                                //  Get output style for standardized
                                //  spellings.

        String regOutType       = args[ 1 ].toLowerCase();

        if ( regOutType.equals( "usereg" ) )
        {
            useReg  = true;
        }
        else if ( regOutType.equals( "usechoice" ) )
        {
            useReg  = false;
        }
                                //  Load text for <interpGrp> section
                                //  defining part of speech tags.

        interpGrpXMLText    =   "";

        try
        {
            FileUtils.readTextFile( args[ 2 ] , "utf-8" );
        }
        catch ( Exception e )
        {
        }

        interpGrpXMLText    = interpGrpXMLText.trim();

        haveInterpGrp       = ( interpGrpXMLText.length() > 0 );

                                //  File name to hold names of works
                                //  successfully converted.

        goodWorksFileName   = args[ 3 ];

        goodWorksSet        = SetFactory.createNewSortedSet();

                                //  File name to hold names of works
                                //  containing errors in conversion.

        badWorksFileName    = args[ 4 ];

        badWorksSet         = SetFactory.createNewSortedSet();

        return true;
    }

    /** Process one file.
     *
     *  @param  xmlFileName     Adorned XML file name to reformat for Xaira.
     */

    protected static void processOneFile( String xmlFileName )
    {
        String xmlOutputFileName    = "";

        try
        {
                                //  Strip path from input file name.

            String strippedFileName =
                FileNameUtils.stripPathName( xmlFileName );

            strippedFileName    =
                FileNameUtils.changeFileExtension( strippedFileName , "" );

                                //  Generate output file name.

            xmlOutputFileName   =
                new File
                (
                    outputDirectory ,
                    strippedFileName + ".xml"
                ).getAbsolutePath();

                                //  Make sure output directory exists.

            FileUtils.createPathForFile( xmlOutputFileName );

                                //  Parse document to JDOM tree.

            Document document   = JDOMUtils.parse( xmlFileName );

                                //  Get document root.

            Element root        = document.getRootElement();

                                //  Eject monkHeader section if found.
            root.removeChild
            (
                "monkHeader" ,
                Namespace.getNamespace
                (
                    "http://monk.at.northwestern.edu/ns/1.0"
                )
            );
                                //  Get filter to extract sup elements.
                                //  These must be changed to hi elements.

            Filter<Element> filter  = Filters.element( "sup" );

                                //  Get list of all words and gaps
                                //  in the document.

            Iterator<Element> iterator  = root.getDescendants( filter );
            List<Element> elements      = ListFactory.createNewList();

            while ( iterator.hasNext() )
            {
                Element element = iterator.next();
                elements.add( element );
            }
                                //  Convert sup elements to hi elements.

            for ( int i = 0 ; i < elements.size() ; i++ )
            {
                Element element = elements.get( i );

                replaceSupWithHi( element );
            }
                                //  Get filter to extract words
                                //  and gaps.

            Filter<Content> filter2 = Filters.content();

                                //  Get list of all words and gaps
                                //  in the document.

            Iterator<Content> iterator2 = root.getDescendants( filter2 );
            List<Content> contents      = ListFactory.createNewList();

            String firstWordID          = "";
            String name;

            while ( iterator2.hasNext() )
            {
                Content content = iterator2.next();
                contents.add( content );

                if ( content instanceof Element )
                {
                    Element element = (Element)content;

                    name    = element.getName();

                    if ( ( firstWordID.length() == 0 ) && name.equals( "w" ) )
                    {
                        firstWordID =
                            JDOMUtils.getAttributeValue(
                                element , "xml:id" , true );
                    }
                }
            }

            lastID  =
                strippedFileName + "-" +
                StringUtils.dupl( "0" , firstWordID.length() );

                                //  Now run through element list
                                //  and add xml:id attribute to
                                //  each gap element.

            Set<String> oldWordIDs  = SetFactory.createNewSortedSet();

            int i = 0;

            while( i < contents.size() )
            {
                                //  Get next element.

                Content content = contents.get( i );

                                //  Get element name.
                name    = "";

                if ( content instanceof Element )
                {
                    Element element = (Element)content;

                    name    = element.getName();

                                //  Process <w> .

                    if ( name.equals( "w" ) || name.equals( "pc" ) )
                    {
                        addWordID( element , oldWordIDs );
                    }
                }

                i++;
            }
                                //  Convert MorphAdorner style word
                                //  elements to BNC style.
            i = 0;

            while( i < contents.size() )
            {
                                //  Get next element.

                Content content = contents.get( i );

                                //  Get element name.

                name    = "";

                if ( content instanceof Element )
                {
                    name    = ((Element)content).getName();
                }
                                //  Process <w> and <pc>.

                if ( name.equals( "w" ) || name.equals( "pc" ) )
                {
                    i   = handleW( contents , i );
                }
                                //  Process <gap>.

                else if ( name.equals( "gap" ) )
                {
                    handleGap( content , false , null );
                }

                i++;
            }
                                //  Get list of updated Word IDs.
                                //  Make sure all old Word IDs
                                //  are in the updated file.  If not,
                                //  something bad happened.

            Set<String> newWordIDs  = SetFactory.createNewSortedSet();

            filter  = new ElementsFilter( new String[]{ "w" , "pc" } );

            iterator    = root.getDescendants( filter );

            while ( iterator.hasNext() )
            {
                Content newWord = (Content)iterator.next();
                addWordID( (Element)newWord , newWordIDs );
            }
                                //  Get set of old words that do not
                                //  appear in the new words.  If there
                                //  are any, something went wrong.

            oldWordIDs.removeAll( newWordIDs );

            if ( oldWordIDs.size() > 0 )
            {
                printStream.println( "*** Error *** in " +
                    xmlFileName + ": " +
                    oldWordIDs.size() + " words not properly converted." );

                String[] missingIDs =
                    oldWordIDs.toArray( new String[ oldWordIDs.size() ] );

                for ( int j = 0 ; j < missingIDs.length ; j++ )
                {
                    printStream.println( missingIDs[ j ] );
                }

                badWorksSet.add( xmlFileName );

//              throw new Exception( "Not all words correctly processed." );
            }
            else
            {
                goodWorksSet.add( xmlFileName );
            }
                                //  Add <interpGrp> as last child
                                //  under <text>.

            if ( haveInterpGrp )
            {
                Element text    =
                    root.getChild
                    (
                        "text" ,
                        Namespace.getNamespace
                        (
                            "http://www.tei-c.org/ns/1.0"
                        )
                    );

                if ( text == null )
                {
                    text    = root.getChild( "text" );
                }

                Namespace[] nameSpaceList =
                    new Namespace[]
                    {
                        Namespace.getNamespace
                        (
                            "http://www.tei-c.org/ns/1.0"
                        )
                    };

                JDOMFragmentParser fragmentParser   =
                    new JDOMFragmentParser( nameSpaceList );

                List<Element> elements2 =
                    fragmentParser.parseFragment( interpGrpXMLText );

                Iterator<Element> iterator3;

                for (   iterator3 = elements2.iterator() ;
                        iterator3.hasNext() ;
                    )
                {
                    Element element = iterator3.next();

                    if ( text != null )
                    {
                        text.addContent( element );
                    }
                }
            }
                                //  Prettify fixedup XML file.

            AdornedXMLWriter xmlWriter  =
                new AdornedXMLWriter
                (
                    document ,
                    xmlOutputFileName
                );

            printStream.println
            (
                "Reformatted " + xmlFileName + " to " + xmlOutputFileName
            );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            printStream.println
            (
                "Problem reformatting " + xmlFileName + " to " +
                xmlOutputFileName +
                ": " + e.getMessage()
            );
        }
    }

    /** Save word ID in set.
     *
     *  @param  wordElement     Word element.
     *  @param  wordIDs         Set of word IDs.
     */

    protected static void addWordID
    (
        Element wordElement ,
        Set<String> wordIDs
    )
    {
        String id   =
            JDOMUtils.getAttributeValue( wordElement , "xml:id" , true );

        wordIDs.add( id );
    }

    /** Handle w tag.
     *
     *  @param  contents        List of content nodes.
     *  @param  index           Index of "w" node to process.
     *
     *  @return                 Updated node index.
     *                          May change if container "w" node
     *                          added here.
     */

    protected static int handleW( List<Content> contents , int index )
    {
        Element element = (Element)contents.get( index );

        String reg  = element.getAttributeValue( "reg" );

        element     = cleanWElement( element );

        String wordText = element.getText().trim();

        if ( !useReg )
        {
            if ( ( reg != null ) && !reg.equals( wordText ) )
            {
                generateChoice( element , wordText , reg );
            }
        }

        return index;
    }

    /** Clean word element.
     *
     *  @param  element     w element.
     */

    protected static Element cleanWElement( Element element )
    {
        String id       =
            JDOMUtils.getAttributeValue( element , "xml:id" , true );

        lastID          = id;
        gapCount        = 0;
        sentenceCount   = 0;

        boolean eos         =
            ( element.getAttributeValue( "eos" ) != null ) &&
            ( element.getAttributeValue( "eos" ).equals( "1" ) );

        String spe  = element.getAttributeValue( "spe" );

        String part = element.getAttributeValue( "part" );

        if ( part == null )
        {
            part    = "N";
        }

        Attribute lemAttr   = element.getAttribute( "lem" );

        if ( lemAttr != null )
        {
            lemAttr.setName( "lemma" );
        }

        Attribute posAttr   = element.getAttribute( "pos" );
        String pos          = "";

        if ( posAttr == null )
        {
            posAttr = element.getAttribute( "ana" );

            if ( posAttr != null )
            {
                pos     = posAttr.getValue();

                if ( pos.charAt( 0 ) == '#' )
                {
                    pos = pos.substring( 1 );
                }
            }
        }
        else
        {
            pos = posAttr.getValue();
        }

        if ( posAttr != null )
        {
            if ( haveInterpGrp || forceAna )
            {
                posAttr.setName( "ana" );
                posAttr.setValue( "#" + posAttr.getValue() );
            }
            else
            {
                posAttr.setName( "pos" );
                posAttr.setValue( posAttr.getValue() );
            }
        }

        element.removeAttribute( "ord" );

        if ( part.equals( "N" ) )
        {
            element.removeAttribute( "part" );
        }

        element.removeAttribute( "spe" );
        element.removeAttribute( "tok" );

        if ( !useReg )
        {
            element.removeAttribute( "reg" );
        }

        element.removeAttribute( "eos" );
        element.removeAttribute( "ms" );
//      element.removeAttribute( "n" );

        if  (   element.getName().equals( "pc" ) ||
                ( CharUtils.isPunctuation( spe ) )
            )
        {
            element.setName( "pc" );
            element.removeAttribute( "lemma" );
            element.removeAttribute( "ana" );
            element.removeAttribute( "type" );
            element.removeAttribute( "reg" );

            if ( eos )
            {
                element.setAttribute( "unit" , "sentence" );
                eos = false;
            }
        }

        if ( eos )
        {
            Element parent  = element.getParentElement();

            if ( parent != null )
            {
                int wIndex  = parent.indexOf( element );

                Element eosMarker   = createElement( "pc" );

                id  = lastID + "-" + ++sentenceCount;

                JDOMUtils.setAttributeValue( eosMarker , "xml:id" , id );

                eosMarker.setAttribute( "unit" , "sentence" );

                parent.setContent( wIndex + 1 , (Content)eosMarker );
            }
        }

        return element;
    }

    /** Create an element.
     *
     *  @param  name    Element name.
     */

    protected static Element createElement( String name )
    {
        return new Element( name , teiNamespace );
    }

    /** Handle gap tag.
     *
     *  @param  content             Node content.
     *  @param  inSplit             Processing split word.
     *  @param  splitWordElements   Split word elements.
     */

    protected static void handleGap
    (
        Content content ,
        boolean inSplit ,
        List<Element> splitWordElements
    )
    {
        Element element = (Element)content;

                                //  If gap doesn't have an ID,
                                //  add it now.
        String id   =
            JDOMUtils.getAttributeValue( element , "xml:id" , true );

        if ( ( id != null ) && ( id.length() > 0 ) )
        {
        }
        else
        {
                                //  Add xml:id for gap based upon
                                //  last word ID.

            id  = lastID + "-gap" + gapCount;

            gapCount++;

            JDOMUtils.setAttributeValue( element , "xml:id" , id );
        }
    }

    /** Handle sup tag.
     *
     *  @param  content     Node content.
     */

    protected static void handleSup( Content content )
    {
        replaceSupWithHi( (Element)content );
    }

    /** Replace sup with hi.
     *
     *  @param  element     sup element to convert and replace with hi.
     */

    protected static void replaceSupWithHi( Element element )
    {
        element.setName( "hi" );

        Attribute typeAttribute = new Attribute( "rend" , "sup" );

        List<Attribute> attributes  = ListFactory.createNewList();

        attributes.add( typeAttribute );

        element.setAttributes( attributes );
    }

    /** Display element.
     *
     *  @param  element     Element to display.
     */

    protected static String displayElement( Element element )
    {
        String id   =
            JDOMUtils.getAttributeValue( element , "xml:id" , true );

        StringBuffer sb = new StringBuffer();

        sb.append( "Name: " + element.getName() );

        if ( ( id != null ) && ( id.length() > 0 ) )
        {
            sb.append( ", id: " + id );
        }

        return sb.toString();
    }

    /** Generate choice element for word structure.
     *
     *  @param  element     Parent element for choice structure.
     *  @param  wordText    Original word text.  May be null.
     *  @param  regText     Standard spelling.
     *
     *  @return             <orig> element within <choice>.
     *
     *  <p>
     *  Emit a choice structure of the form:
     *  </p>
     *
     *  <pre>
     *  &lt;w...&gt;
     *      &lt;choice&gt;
     *          &lt;orig&gt;original spelling&lt;/orig&gt;    or
     *          &lt;orig&gt;
     *              &lt;seg ...&gt;
     *              &lt;seg ...&gt;
     *              ...
     *          &lt;/orig&gt;
     *          &lt;reg&gt;standard spelling&lt;/reg&gt;
     *      &lt;/choice&gt;
     *  &lt;/w&gt;
     *  </pre>
     */

    protected static Element generateChoice
    (
        Element element ,
        String wordText ,
        String regText
    )
    {
                                //  Clear word text.

        element.setText( "" );

                                //  Create <choice> element.

        Element choiceElement   = createElement( "choice" );

                                //  Add <choice> as child of main word
                                //  element.

        element.addContent( choiceElement );

                                //  Create <orig> element.

        Element origElement = createElement( "orig" );

                                //  Text of orig is the original spelling.

        if ( wordText != null )
        {
            origElement.setText( wordText );
        }
                                //  Add <orig> as first child of <choice>.

        choiceElement.addContent( origElement );

                                //  Create <reg> element.

        Element regElement  = createElement( "reg" );

                                //  Set standard spelling as value of
                                //  <reg> element.

        regElement.setText( regText );

                                //  Add <reg> as second child of <choice>.

        choiceElement.addContent( regElement );

        return origElement;
    }

    /** Process files.
     */

    protected static int processFiles( String[] args )
    {
        int result  = 0;
                                //  Get file name/file wildcard specs.

        String[] wildCards  = new String[ args.length - INITPARAMS ];

        for ( int i = INITPARAMS ; i < args.length ; i++ )
        {
            wildCards[ i - INITPARAMS ] = args[ i ];
        }
                                //  Expand wildcards to list of
                                //  file names,

        String[] fileNames  =
            FileNameUtils.expandFileNameWildcards( wildCards );

        docsToProcess       = fileNames.length;

                                //  Process each file.

        for ( int i = 0 ; i < fileNames.length ; i++ )
        {
            processOneFile( fileNames[ i ] );
        }
                                //  Emit list of files which succeeded and
                                //  failed conversion.
        try
        {
            SetUtils.saveSet( goodWorksSet , goodWorksFileName , "utf-8" );
            SetUtils.saveSet( badWorksSet , badWorksFileName , "utf-8" );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
                                //  Return number of files converted.

        return fileNames.length;
    }

    /** Terminate.
     *
     *  @param  filesProcessed  Number of files processed.
     *  @param  processingTime  Processing time in seconds.
     */

    protected static void terminate
    (
        int filesProcessed ,
        long processingTime
    )
    {
        printStream.println
        (
            "Processed " +
            Formatters.formatIntegerWithCommas
            (
                filesProcessed
            ) +
            " files in " +
            Formatters.formatLongWithCommas
            (
                processingTime
            ) +
            " seconds."
        );
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



