package edu.northwestern.at.morphadorner.tools.annolex;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.text.*;
import java.util.*;

import org.jdom2.*;
import org.jdom2.filter.*;
import org.jdom2.input.*;
import org.jdom2.output.*;

import edu.northwestern.at.morphadorner.tools.*;
import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.xml.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.partsofspeech.*;

/** Merge Annolex corrections into MorphAdorned XML file.
 *
 *  <p>
 *  Usage:
 *  </p>
 *
 *  <p>
 *  MergeAnnolexCorrectionsIntoAdornedXML correctionsdirectory outputdirectory inputfiles
 *  </p>
 *
 *  <ul>
 *  <li>correctionsdirectory -- the input directory with corrections
 *      files in tabular format.</li>
 *  <li>outputdirectory -- the output directory for the corrected
 *      adorned XML files.</li>
 *  <li>inputfiles -- the input adorned XML files to be corrected.</li>
 *  </ul>
 *
 *  <p>
 *  The corrections file is a tab-separated utf-8 file containing
 *  the following columns.
 *  </p>
 *
 *  <ul>
 *  <li>Work ID.</li>
 *  <li>Word ID.</li>
 *  <li>Old spelling.</li>
 *  <li>Corrected spelling.</li>
 *  <li>Standard spelling.</li>
 *  <li>Corrected lemmata.</li>
 *  <li>Corrected parts of speech.</li>
 *  <li>Operation: 1=update, 2=insert, 3=delete, 5=delete nearest gap.</li>
 *  </ul>
 *
 *  <p>
 *  The corrected spelling, lemmata, and parts of speech may
 *  all be empty when the operation is 3 (delete).
 *  </p>
 *
 *  <p>
 *  The value of the "ord" (word ordinal) attribute for each
 *  word is adjusted to account for inserted and deleted words.
 *  The value of the "reg" (standard spelling) and
 *  "tok" attributes (original token) are generated as needed
 *  for updated and inserted words.
 *  </p>
 *
 *  <p>
 *  Whitespace markers "&lt;c&gt; &lt;/c&gt;" are added and deleted as needed
 *  when tokens are added or deleted.  In general, most added punctuation
 *  and symbols do not require added whitespace markers.  When tokens
 *  are deleted, sequences of "&lt;c&gt; &lt;/c&gt;&lt;c&gt; &lt;/c&gt; ..." are compressed
 *  to a single "&lt;c&gt; &lt;/c&gt;" entry.
 *  </p>
 */

public class MergeAnnolexCorrectionsIntoAdornedXML
{
    /** Number of documents to process. */

    protected static int docsToProcess      = 0;

    /** Current document. */

    protected static int currentDocNumber   = 0;

    /** Input directory for adorned files. */

    protected static String inputXMLDirectory;

    /** Input directory for corrections files. */

    protected static String inputCorrectionsDirectory;

    /** Output directory for corrected adorned files. */

    protected static String outputDirectory;

    /** Output file stream. */

    protected static PrintStream outputFileStream;

    /** Wrapper for printStream to allow utf-8 output. */

    protected static PrintStream printStream;

    /** DOM document. */

    protected static Document document  = null;

    /** Map word IDs to word elements in DOM document. */

    protected static Map<String, Element> wordIDsToElements =
        new LinkedHashMap<String, Element>();

    /** Map gap IDs to gap elements in DOM document. */

    protected static Map<String, Element> gapIDsToElements  =
        new LinkedHashMap<String, Element>();

    /** Map word IDs to corrected words. */

    protected static Map<String, CorrectedWord> correctedWordsMap   = null;

    /** # params before input file specs. */

    protected static final int INITPARAMS   = 2;

    /** Pos tags. */

    protected static PartOfSpeechTags posTags   = null;

    /** Holds bad pos tags. */

    protected static Set<String> badPosTags =
        SetFactory.createNewSortedSet();

    /** Holds combined bad pos tags. */

    protected static Set<String> combinedBadPosTags =
        SetFactory.createNewSortedSet();

    /** Holds individual mismatches. */

    protected static Set<String> mismatches =
        SetFactory.createNewSortedSet();

    /** Holds combined mismatches. */

    protected static Set<String> combinedMismatches =
        SetFactory.createNewSortedSet();

    /** Words added, deleted, or modified in current document. */

    protected static int addedWords     = 0;
    protected static int deletedWords   = 0;
    protected static int modifiedWords  = 0;
    protected static int deletedGaps    = 0;

    /** "c" element for cloning. */

    protected static Element clonableCElement   = null;

    /** List of word elements to delete. */

    protected static List<Element> wordElementsToDelete =
        ListFactory.createNewList();

    /** List of gaps to delete. */

    protected static List<Element> gapElementsToDelete  =
        ListFactory.createNewList();

    /** True to produce verbose output. */

    protected static boolean verbose    = true;

    /** True to produce debugging output. */

    protected static boolean debug      = true;

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
            printStream.println( e.getMessage() );
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
                                //  Get input corrections directory.

        inputCorrectionsDirectory   = args[ 0 ];

                                //  Get output directory.

        outputDirectory             = args[ 1 ];

                                //  Get part of speech tags.

        posTags = new DefaultPartOfSpeechTags();

        return true;
    }

    /** Process corrections for one XML file.
     *
     *  @param  xmlInputFileName    XML input file.
     */

    protected static void processOneFile
    (
        String xmlInputFileName
    )
        throws java.io.IOException
    {
                                //  Get input file name stripped of path.

        String shortInputXmlFileName    =
            FileNameUtils.stripPathName( xmlInputFileName );

                                //  Get work name.
        String workName =
            FileNameUtils.changeFileExtension
            (
                shortInputXmlFileName ,
                ""
            );
                                //  Get output file name.

        String xmlOutputFileName        =
            new File
            (
                outputDirectory ,
                shortInputXmlFileName
            ).getCanonicalPath();

                                //  Get corrections file name.

        String correctionsFileName  =
            new File
            (
                inputCorrectionsDirectory ,
                FileNameUtils.changeFileExtension
                (
                    shortInputXmlFileName ,
                    ".tab"
                )
            ).getCanonicalPath();

                                //  Load and parse XML input document.
        try
        {
            long startTime      = System.currentTimeMillis();

            printStream.println
            (
                "---------- Processing " + shortInputXmlFileName
            );

            Document document   = loadXML( xmlInputFileName );

                                //  Load words in document.

            wordIDsToElements   = extractWords( document );

                                //  Load gaps in document.

            gapIDsToElements    = extractGaps( document );

                                //  Get clonable "c" element.

            clonableCElement    = extractCElement( document );

                                //  Report number of words found in
                                //  input XML file.

            long endTime    =
                ( System.currentTimeMillis() - startTime + 999 ) / 1000;

            printStream.println
            (
                "XML file " + xmlInputFileName + " loaded in " +
                Formatters.formatLongWithCommas
                (
                    endTime
                ) +
                ( ( endTime != 1 ) ? " seconds" : " second" ) +
                " and contains " +
                Formatters.formatIntegerWithCommas
                (
                    wordIDsToElements.size()
                ) +
                ( ( wordIDsToElements.size() != 1 ) ? " words." : " word." )
            );
                                //  Load corrections file.

            CorrectedWordsFileReader correctionsFileReader  =
                loadCorrectionsFile( correctionsFileName );

                                //  Validate corrections.
/*
            startTime   = System.currentTimeMillis();

            int badCorrections  =
                validateCorrections
                (
                    wordIDsToElements
                );

            endTime =
                ( System.currentTimeMillis() - startTime + 999 ) / 1000;

            if ( badCorrections == 0 )
            {
                printStream.println
                (
                    "Corrections checked in " +
                    Formatters.formatLongWithCommas
                    (
                        endTime
                    ) +
                    ( ( endTime != 1 ) ? " seconds" : " second" ) +
                    " and all appear OK."
                );
            }
            else
            {
                printStream.println
                (
                    "Corrections checked in " +
                    Formatters.formatLongWithCommas
                    (
                        endTime
                    ) +
                    ( ( endTime != 1 ) ? " seconds" : " second" ) +
                    " and " +
                    Formatters.formatIntegerWithCommas
                    (
                        badCorrections
                    ) +
                    " appear bad."
                );
            }
*/
                                //  Clear list of word elements to delete.

            wordElementsToDelete.clear();

                                //  Clear list of gap elements to delete.

            gapElementsToDelete.clear();

                                //  Apply corrections.

            applyCorrections( document );

                                //  Get updated word ID to word element
                                //  map.

            wordIDsToElements   = extractWords( document );

                                //  Fix word IDs for split words.

            fixSplitWordIDs( document );

                                //  Compress sequence of multiple "<c>"
                                //  elements to one "<c>" element.

            compressCElements( document );

                                //  Get updated word ID to word element
                                //  map.

            wordIDsToElements   = extractWords( document );

                                //  Fix sequences of adjacent words
                                //  with "eos" set to 1 to have only
                                //  last word in sequence have "eos"
                                //  set to one.

            fixEOSAttributes( wordIDsToElements );

                                //  Update word ordinals.

            updateWordOrdinals( wordIDsToElements );

                                //  Output updated XML.

            startTime   = System.currentTimeMillis();

            new AdornedXMLWriter( document , xmlOutputFileName );

            endTime     =
                ( System.currentTimeMillis() - startTime + 999 ) / 1000;

                                //  Report XML written.

            printStream.println
            (
                "Revised XML written to " + xmlOutputFileName + " in " +
                Formatters.formatLongWithCommas
                (
                    endTime
                ) +
                ( ( endTime != 1 ) ? " seconds." : " second." )
            );
        }
        catch ( Exception e )
        {
            printStream.println( xmlInputFileName + " failed." );
e.printStackTrace();
            printStream.println( "Error: " + e.getMessage() );
        }
    }

    /** Load XML document file.
     *
     *  @param      inputXMLFileName    Input XML file name.
     *
     *  @return                         Parsed XML file as document.
     *
     *  @throws     org.jdom2.JDOMException
     *              java.io.IOException
     */

    protected static Document loadXML( String inputXMLFileName )
        throws org.jdom2.JDOMException,
        java.io.IOException
    {
        return new SAXBuilder().build( inputXMLFileName );
    }

    /** Extract words specified by "w" elements in XML document file.
     *
     *  @param  document    Parsed XML document.
     *
     *  @return             Map from word ID to parsed "w" element
     *                      for each word in XML document.
     */

    protected static Map<String, Element> extractWords( Document document )
    {
                                //  Map with word ID as key and
                                //  parsed "w" element as value.

        Map<String, Element> wordIDsToElements  =
            new LinkedHashMap<String, Element>();

                                //  Filter to extract "w" elements.

        Filter<Element> filter  = Filters.element( "w" );

                                //  Get document root.

        Element root        = document.getRootElement();

                                //  Iterate over all "w" elements
                                //  in document.

        Iterator<Element> iterator  = root.getDescendants( filter );

                                //  Store each "w" element in the
                                //  word ID to "w" element map.

        while ( iterator.hasNext() )
        {
            Element w   = (Element)iterator.next();

            wordIDsToElements.put
            (
                JDOMUtils.getAttributeValue( w , "xml:id" , false ) ,
                w
            );
        }
                                //  Return map from word IDs to "w" elements.

        return wordIDsToElements;
    }

    /** Extract gaps from "gap" elements in XML document file.
     *
     *  @param  document    Parsed XML document.
     *
     *  @return             Map from word ID to parsed "gap" element
     *                      for each gap in XML document.
     */

    protected static Map<String, Element> extractGaps( Document document )
    {
                                //  Map with word ID as key and
                                //  parsed "gap" element as value.

        Map<String, Element> gapIDsToElements   =
            new LinkedHashMap<String, Element>();

                                //  Filter to extract "w" elements.

        Filter<Element> filter  = Filters.element( "gap" );

                                //  Get document root.

        Element root            = document.getRootElement();

                                //  Iterate over all "w" elements
                                //  in document.

        Iterator<Element> iterator  = root.getDescendants( filter );

                                //  Store each "w" element in the
                                //  word ID to "w" element map.

        while ( iterator.hasNext() )
        {
            Element gap = iterator.next();

            gapIDsToElements.put
            (
                JDOMUtils.getAttributeValue( gap , "xml:id" , false ) ,
                gap
            );
        }
                                //  Return map from gap IDs to
                                //  "gap" elements.

        return gapIDsToElements;
    }

    /** Extract a "c" element from XML file to use for cloning copies.
     *
     *  @param  document    Parsed XML document.
     *
     *  @return             A parsed "c" element.
     */

    protected static Element extractCElement( Document document )
    {
        Element result  = null;

                                //  Filter to extract "c" elements.

        Filter<Element> filter  = Filters.element( "c" );

                                //  Get document root.

        Element root            = document.getRootElement();

                                //  Iterate over all "w" elements
                                //  in document.

        Iterator<Element> iterator  = root.getDescendants( filter );

                                //  Find first "c" element which has
                                //  a blank as text.

        while ( iterator.hasNext() )
        {
            Element c   = iterator.next();

            if ( c.getText().equals( " " ) )
            {
                result  = (Element)c.clone();

                break;
            }
        }
                                //  Return copy of the found "c" element.
        return result;
    }

    /** Validate entries in corrections file.
     *
     *  @param  wordIDsToElements       Map of document word IDs to
     *                                  document "w" elements.
     *
     *  @return                         Number of bad corrections.
     */

    protected static int validateCorrections
    (
        Map<String, Element> wordIDsToElements
    )
    {
        int result  = 0;
                                //  See if ID and spelling in XML file
                                //  match up with correction.

        Iterator<String> iterator   = correctedWordsMap.keySet().iterator();

        while ( iterator.hasNext() )
        {
                                //  Get word ID.

            String id           = iterator.next();

                                //  Get word.

            CorrectedWord correctedWord = correctedWordsMap.get( id );

                                //  Get correction type.

            String checkbox     = correctedWord.getCorrectionType();

                                //  See if the word ID for this correction
                                //  appears in the document.

            Element w           = wordIDsToElements.get( id );

                                //  If not, assume we're adding it.
            if ( w == null )
            {
                                //  Adding this word.

                if ( verbose )
                {
                    printStream.println
                    (
                        "Adding new word with id " + id + "   [" +
                        correctedWord.getSpelling() + "]"
                    );
                }
            }
            else if ( checkbox.equals( "2" ) || checkbox.equals( "3" ) )
            {
                if ( verbose )
                {
                    printStream.println
                    (
                        "Deleting word with id " + id  + "   [" +
                        correctedWord.getSpelling() + "]"
                    );
                }
            }
                                //  Word to update.
            else
            {
                String xmlSpelling  =
                    JDOMUtils.getAttributeValue( w , "tok" , false );

                                //  Check that XML spelling
                                //  matches old spelling in correction.

                if ( verbose )
                {
                    if ( !correctedWord.getOldSpelling().equals( xmlSpelling ) )
                    {
                        printStream.println
                        (
                            id + "\tXML has spelling " +
                            xmlSpelling +
                            ", corrections has " +
                            correctedWord.getOldSpelling() +
                            " [" + correctedWord.getSpelling() + "]"
                        );
                    }
                }
                                //  Check for part of speech/lemma
                                //  mismatch in correction.

                String posTag   = correctedWord.getPartsOfSpeech();
                String lemma    = correctedWord.getLemmata().trim();

                int posTagCount = countSeparators( posTag , '|' );
                int lemmaCount  = countSeparators( lemma , '|' );

                if ( posTagCount != lemmaCount )
                {
                    String mismatch =
                        correctedWord.getSpelling() + "\t" +
                        lemma + "\t" + posTag + "\tmismatch";

                    if ( verbose )
                    {
                        printStream.println
                        (
                            correctedWord.getId() + "\t" + mismatch
                        );
                    }

                    mismatches.add
                    (
                        correctedWord.getId() + "\t" + mismatch
                    );

                    combinedMismatches.add
                    (
                        mismatch
                    );
                }
                else if ( lemma.length() == 0 )
                {
                    String mismatch =
                        correctedWord.getSpelling() + "\t" +
                        lemma + "\t" + posTag + "\tmissing lemma";

                    mismatches.add
                    (
                        correctedWord.getId() + "\t" + mismatch
                    );

                    combinedMismatches.add
                    (
                        mismatch
                    );

                    if ( verbose )
                    {
                        printStream.println
                        (
                            correctedWord.getId() + "\t" + mismatch
                        );
                    }
                }

                String[] tags   = posTags.splitTag( posTag );

                for ( int i = 0 ; i < tags.length ; i++ )
                {
                    if ( !posTags.isTag( tags[ i ] ) )
                    {
                        String badPosTag    =
                            correctedWord.getId() + "\t" +
                            correctedWord.getSpelling() + "\t" +
                            lemma + "\t" + posTag +
                            "\tbad part of speech: " + tags[ i ];

                        badPosTags.add( badPosTag );
                        combinedBadPosTags.add( tags[ i ] );

                        if ( verbose )
                        {
                            printStream.println( badPosTag );
                        }
                    }
                }
            }
        }

        return result;
    }

    /** Apply corrections to XML document from corrections file.
     *
     *  @param  document    Document to update.
     */

    protected static void applyCorrections
    (
        Document document
    )
        throws Exception
    {
        long startTime  = System.currentTimeMillis();

        addedWords      = 0;
        deletedWords    = 0;
        modifiedWords   = 0;
        deletedGaps     = 0;
                                //  Process each word in
                                //  corrections file.

        Iterator<String> iterator   =
            correctedWordsMap.keySet().iterator();

        List<String> correctedWordIDs   = ListFactory.createNewList();

        while ( iterator.hasNext() )
        {
            String id   = iterator.next();
            correctedWordIDs.add( id );
        }

        for ( int i = 0 ; i < correctedWordIDs.size() ; i++ )
        {
                                //  Get word ID of word to correct.

            String correctedWordID  = correctedWordIDs.get( i );

                                //  Get correction data for this word in
                                //  tabular file.

            CorrectedWord correctedWord =
                correctedWordsMap.get( correctedWordID );

                                //  Note if we're to delete this word.

            String correctionType   =
                correctedWord.getCorrectionType();

            boolean deleteTheWord   =
                correctionType.equals( "2" ) ||
                correctionType.equals( "3" );

                                //  Note if we're to delete the gap.

            if ( correctionType.equals( "5" ) )
            {
                Element gap =
                    gapIDsToElements.get( correctedWord.getId() );

                gapElementsToDelete.add( gap );

                continue;
            };
                                //  Get all words to update in case
                                //  this word refers to a split word.

            List<String> idsToUpdate    =
                getRelatedWordIDs( correctedWordID );

                                //  Get joined old spelling for
                                //  split words.

            String[] spellingParts      = new String[ idsToUpdate.size() ];
            String oldJoinedSpelling    = "";

            for ( int j = 0 ; j < idsToUpdate.size() ; j++ )
            {
                String idToUpdate   = idsToUpdate.get( j );

                                //  See if word ID in corrections
                                //  exists in XML file.

                if ( wordIDsToElements.containsKey( idToUpdate ) )
                {
                                //  XML word instance to edit/insert/delete.

                    Element wordElement =
                        wordIDsToElements.get( idToUpdate );

                    spellingParts[ j ]  = wordElement.getText();

                    oldJoinedSpelling   += spellingParts[ j ];
                }
            }
                                //  Fix split spellings if the new
                                //  spelling is different from the
                                //  old spelling.

            if ( idsToUpdate.size() > 1 )
            {
                if ( !oldJoinedSpelling.equals( correctedWord.getSpelling() ) )
                {
                    String fixedNewJoined   =
                        StringUtils.stripChars
                        (
                            correctedWord.getSpelling() ,
                            " "
                        );

                    if ( !oldJoinedSpelling.equals( fixedNewJoined ) )
                    {
                        resplit
                        (
                            correctedWord.getId() ,
                            spellingParts ,
                            oldJoinedSpelling ,
                            correctedWord.getSpelling()
                        );
/*
                        printStream.println
                        (
                            "***** after resplit:" +
                            " spelling parts(" + spellingParts.length +
                            ")=" + Arrays.asList( spellingParts )
                        );
*/
                    }
                }
            }
            else
            {
                spellingParts[ 0 ]  = correctedWord.getSpelling();
            }
                                //  Process each related word to update.

            for ( int j = 0 ; j < idsToUpdate.size() ; j++ )
            {
                String idToUpdate   = idsToUpdate.get( j );

                                //  See if word ID in corrections
                                //  exists in XML file.

                if ( wordIDsToElements.containsKey( idToUpdate ) )
                {
                                //  XML word instance to edit/insert/delete.

                    Element wordElement =
                        wordIDsToElements.get( idToUpdate );

                                //  Delete word if requested.

                    if  (   deleteTheWord ||
                            ( spellingParts[ j ].length() == 0 )
                        )
                    {
//                      deleteWord( wordElement , correctedWordIDs , i );
//                      wordIDsToElements   = extractWords( document );

                        wordElementsToDelete.add( wordElement );
                    }
                    else if (   updateWord
                                (
                                    document ,
                                    wordElement ,
                                    correctedWord ,
                                    correctedWordIDs ,
                                    i ,
                                    spellingParts[ j ]
                                )
                            )
                    {
                        modifiedWords++;
                    }
                }
                                //  Word does not exist in XML.
                                //  We are inserting a new word.
                else
                {
                    int added   =
                        insertWord
                        (
                            idToUpdate ,
                            correctedWord ,
                            correctedWordIDs ,
                            i
                        );

                    addedWords  += added;
                }
            }
        }
                                //  Delete words to delete.
        deleteWordElements
        (
            wordElementsToDelete ,
            getSortedWordIDs()
        );
                                //  Replace gaps with words.

        replaceGapElementsWithWords( gapElementsToDelete );

                                //  Delete gaps to delete.
/*
        deleteGapElements( gapElementsToDelete );
*/
        long endTime    =
            ( System.currentTimeMillis() - startTime + 999 ) / 1000;

                                //  Report number of modifications.

        printStream.println
        (
            "Update completed in " +
            Formatters.formatLongWithCommas
            (
                endTime
            ) +
            ( ( endTime != 1 ) ? " seconds." : " second." )
        );

        printStream.println
        (
            "     " +
            Formatters.formatIntegerWithCommas
            (
                addedWords
            ) +
            " words added."
        );

        printStream.println
        (
            "     " +
            Formatters.formatIntegerWithCommas
            (
                deletedWords
            ) +
            " words deleted."
        );

        printStream.println
        (
            "     " +
            Formatters.formatIntegerWithCommas
            (
                modifiedWords
            ) +
            " words modified."
        );

        printStream.println
        (
            "     " +
            Formatters.formatIntegerWithCommas
            (
                deletedGaps
            ) +
            " gaps deleted."
        );

    }

    /** Fix spelling parts of updated split word.
     *
     *
     */

    protected static void resplit
    (
        String id ,
        String[] spellingParts ,
        String oldJoinedSpelling ,
        String updatedSpelling
    )
    {
        if ( updatedSpelling.indexOf( " " ) >= 0 )
        {
/*
            printStream.println
            (
                "***** resplit using embedded blanks rule: id=" + id +
                ": old joined=" + oldJoinedSpelling +
                ", new joined=" + updatedSpelling +
                " spelling parts(" + spellingParts.length + ")=" +
                Arrays.asList( spellingParts )
            );
*/
            String[] tokens = updatedSpelling.split( " " );

            if ( tokens.length > spellingParts.length )
            {
                spellingParts   = new String[ tokens.length ];
            }

            for (   int i = 0 ;
                    i < spellingParts.length ;
                    i++
                )
            {
                if ( i < tokens.length )
                {
                    spellingParts[ i ]  = tokens[ i ];
                }
                else
                {
                    spellingParts[ i ]  = "";
                }
            }
/*
            printStream.println
            (
                "      tokens(" + tokens.length + ")=" +
                Arrays.asList( tokens ) +
                "      spelling parts(" + spellingParts.length + ")=" +
                Arrays.asList( spellingParts )
            );
*/
        }
        else if ( oldJoinedSpelling.length() == updatedSpelling.length() )
        {
            int j   = 0;

            for ( int i = 0 ; i < spellingParts.length ; i++ )
            {
                spellingParts[ i ]  =
                    updatedSpelling.substring
                    (
                        j ,
                        spellingParts[ i ].length() + j
                    );

                j   += spellingParts[ i ].length();
            }
        }
        else if ( oldJoinedSpelling.toLowerCase().startsWith(
                    updatedSpelling.toLowerCase() ) )
        {
            String extendedUpdatedSpelling  =
                updatedSpelling +
                StringUtils.dupl( " " , oldJoinedSpelling.length() );

            int j   = 0;

            for ( int i = 0 ; i < spellingParts.length ; i++ )
            {
                spellingParts[ i ]  =
                    extendedUpdatedSpelling.substring
                    (
                        j ,
                        spellingParts[ i ].length() + j
                    );

                j   += spellingParts[ i ].length();
            }

            for ( int i = 0 ; i < spellingParts.length ; i++ )
            {
                spellingParts[ i ]  = spellingParts[ i ].trim();
            }
        }
        else if (   updatedSpelling.toLowerCase().startsWith(
                    oldJoinedSpelling.toLowerCase() ) )
        {
            String extendedUpdatedSpelling  =
                updatedSpelling +
                StringUtils.dupl( " " , oldJoinedSpelling.length() );

            int j   = 0;

            for ( int i = 0 ; i < spellingParts.length ; i++ )
            {
                spellingParts[ i ]  =
                    extendedUpdatedSpelling.substring
                    (
                        j ,
                        spellingParts[ i ].length() + j
                    );

                j   += spellingParts[ i ].length();
            }

            spellingParts[ spellingParts.length - 1 ]   =
                spellingParts[ spellingParts.length - 1 ] +
                extendedUpdatedSpelling.substring( j );

            for ( int i = 0 ; i < spellingParts.length ; i++ )
            {
                spellingParts[ i ]  = spellingParts[ i ].trim();
            }
/*
            printStream.println
            (
                "***** resplit using matching rule 2: id=" + id +
                ": old joined=" + oldJoinedSpelling +
                ", new joined=" + updatedSpelling +
                ": spelling parts=" + Arrays.asList( spellingParts )
            );
*/
        }
        else if (   ( spellingParts.length == 2 ) &&
                    ( spellingParts[ 1 ].equals( "'s" ) ) &&
                    ( updatedSpelling.endsWith( "'s" ) )
                )
        {
            spellingParts[ 0 ]  =
                updatedSpelling.substring
                (
                    0 ,
                    updatedSpelling.length() - 2
                );
/*
            printStream.println
            (
                "***** resplit using 's rule: id=" + id +
                ": old joined=" + oldJoinedSpelling +
                ", new joined=" + updatedSpelling +
                ": spelling parts=" + Arrays.asList( spellingParts )
            );
*/
        }
        else
        {
/*
            printStream.println
            (
                "***** Old and new spelling lengths differ at id=" + id +
                ": old joined=" + oldJoinedSpelling +
                ", new joined=" + updatedSpelling +
                ": unable to resplit."
            );
*/
            spellingParts[ 0 ]  = updatedSpelling;

            for ( int i = 1 ; i < spellingParts.length ; i++ )
            {
                spellingParts[ i ]  = "";
            }
/*
            printStream.println
            (
                "***** resplit using no-split single word rule: id=" + id +
                ": old joined=" + oldJoinedSpelling +
                ", new joined=" + updatedSpelling +
                ": spelling parts=" + Arrays.asList( spellingParts )
            );
*/
        }
    }

    /** Compress "<c>" elements.
     *
     *  @param  document    Document in which to compress "<c>" elements.
     *
     *  <p>
     *  Deleting words may have left sequences of "<c> </c><c> </c> ..."
     *  elements.  Each such sequence should be compressed to a single
     *  "<c> </c>" element.
     *  </p>
     */

    protected static void compressCElements( Document document )
    {
                                //  Filter to extract all elements.

        Filter<Element> filter      = Filters.element();

                                //  Get document root.

        Element root        = document.getRootElement();

                                //  Previous document element encountered.

        Element previousElement = null;

                                //  Iterator over all elements
                                //  in document.

        Iterator<Element> iterator  = root.getDescendants( filter );

                                //  Holds list of "<c>" elements
                                //  to delete.

        List<Element> cToDelete = ListFactory.createNewList();

                                //  Search for sequences of "<c> </c>"
                                //  and make list of extra ones to be
                                //  deleted.

        while ( iterator.hasNext() )
        {
                                //  Get next element.

            Element e   = iterator.next();

                                //  If this is a "c" element, and
                                //  the last element was a "c" element,
                                //  add this element to list of "c" elements
                                //  to delete.

            if ( previousElement != null )
            {
                if ( e.getName().equals( "c" ) )
                {
                    if ( e.getText().equals( " " ) )
                    {
                        if ( previousElement.getName().equals( "c" ) )
                        {
                            if ( previousElement.getText().equals( " " ) )
                            {
                                cToDelete.add( e );
                            }
                        }
                    }
                }
            }

            previousElement = e;
        }
                                //  Delete extraneous "c" elements.

        for ( int i = 0 ; i < cToDelete.size() ; i++ )
        {
            Element c       = cToDelete.get( i );

            Parent parent   = c.getParent();

            if ( parent != null )
            {
                parent.removeContent( c );
            }
        }
    }

    /** Fix split word IDs.
     *
     *  @param  document    Parsed XML document.
     */

    protected static void fixSplitWordIDs( Document document )
    {
                                //  Look for words with split parts.

        for ( String wordID : wordIDsToElements.keySet() )
        {
                                //  Get next word.

            Element word    = wordIDsToElements.get( wordID );

                                //  Get word ID.
            String id       =
                JDOMUtils.getAttributeValue( word , "xml:id" , false );

                                //  See if it is a split word.

            if ( id.endsWith( ".1" ) )
            {
                                //  Get related word IDs for this word.

                List<String> relatedIDs = getRelatedWordIDs( id );

                Collections.sort( relatedIDs );

                                //  If there is only one word part left,
                                //  remove the ".1" from the word ID
                                //  and set its part attribute to "N".

                if ( relatedIDs.size() == 1 )
                {
                    id  = id.substring( 0 , id.length() - 2 );

                    JDOMUtils.setAttributeValue
                    (
                        word ,
                        "xml:id" ,
                        id
                    );

                    JDOMUtils.setAttributeValue
                    (
                        word ,
                        "part" ,
                        "N"
                    );
                }
                                //  Otherwise, make sure the part flags
                                //  are set correctly.

                else
                {
                    for ( int i = 0 ; i < relatedIDs.size() ; i++ )
                    {
                        Element thisWordElement =
                            wordIDsToElements.get( relatedIDs.get( i ) );

                        String part = "M";

                        if ( i == 0 )
                        {
                            part    = "I";
                        }
                        else if ( i == ( relatedIDs.size() - 1 ) )
                        {
                            part    = "F";
                        }

                        JDOMUtils.setAttributeValue
                        (
                            thisWordElement ,
                            "part" ,
                            part
                        );
                    }
                }
            }
        }
    }

    /** Fix EOS attributes.
     *
     *  @param  wordIDsToElements   Map of word ID to parsed "w" elements.
     *
     *  <p>
     *  Adding or deleting words may have left sequences of "w" elements
     *  which all have the "eos" attribute set to "1".  This method
     *  updates the "w" elements in such as sequence so that only the
     *  last "w" element in the sequence has "eos" set to "1".
     *  </p>
     */

    protected static void fixEOSAttributes
    (
        Map<String, Element> wordIDsToElements
    )
    {
                                //  Holds list of "w" elements sorted
                                //  in ascending order by word ID.
                                //  This puts the words in reading
                                //  order so we can check for
                                //  runs of words with eos="1"
                                //  attribute values.

        List<Element> words = ListFactory.createNewList();

        Set<String> wordIDsSet  = new TreeSet<String>();

        wordIDsSet.addAll( wordIDsToElements.keySet() );

        Iterator<String> iterator   = wordIDsSet.iterator();

        while ( iterator.hasNext() )
        {
            words.add( wordIDsToElements.get( iterator.next() ) );
        }
                                //  Get the first word in reading order
                                //  and its "eos" attribute value.

        Element previousWord    = words.get( 0 );

        String previousEos      =
            JDOMUtils.getAttributeValue( previousWord , "eos" , false );

        if ( previousEos == null )
        {
            previousEos = "0";
        }
                                //  Run over all words in reading order.

        for ( int i = 1 ; i < words.size() ; i++ )
        {
                                //  Get current word.

            Element word    = words.get( i );

                                //  Get current word's "eos" attribute
                                //  value.

            String eos      =
                JDOMUtils.getAttributeValue( word , "eos" , false );

            if ( eos == null )
            {
                eos = "0";
            }
                                //  If the current word's eos value
                                //  is "1", and the previous word's
                                //  eos value was also "1", set the
                                //  previous word's eos value to "0".
                                //  This ensures the last word in a
                                //  sequence of words with eos set to
                                //  "1" will end up the only word in the
                                //  sequence with eos set to "1".

            if ( eos.equals( "1" ) )
            {
                if ( previousEos.equals( "1" ) )
                {
                    JDOMUtils.setAttributeValue
                    (
                        previousWord ,
                        "eos" ,
                        "0"
                    );
                }
            }
                                //  Previous word is now this word.

            previousWord    = word;
            previousEos     = eos;
        }
    }

    /** Update word ordinals.
     *
     *  @param  wordIDsToElements       Map of document word IDs to
     *                                  document "w" elements.
     */

    protected static void updateWordOrdinals
    (
        Map<String, Element> wordIDsToElements
    )
    {
        int wordOrdinal = 0;
                                //  Empty existing word ordinals.

        for ( String wordID : wordIDsToElements.keySet() )
        {
            Element wordElement = wordIDsToElements.get( wordID );

            changeAttribute
            (
                wordElement ,
                "ord" ,
                JDOMUtils.getAttributeValue( wordElement , "ord" , false ) ,
                ""
            );
        }
                                //  Generate new word ordinals.

        for ( String wordID : wordIDsToElements.keySet() )
        {
            Element wordElement = wordIDsToElements.get( wordID );

            String existingOrd  =
                JDOMUtils.getAttributeValue( wordElement , "ord" , false );

            if ( existingOrd.length() == 0 )
            {
                String wordElementID    =
                    JDOMUtils.getAttributeValue
                    (
                        wordElement ,
                        "xml:id" ,
                        false
                    );

                List<String> relatedIDs =
                    getRelatedWordIDs( wordElementID );

                wordOrdinal++;

                for ( int i = 0 ; i < relatedIDs.size() ; i++ )
                {
                    Element thisWordElement =
                        wordIDsToElements.get( relatedIDs.get( i ) );

                    changeAttribute
                    (
                        thisWordElement ,
                        "ord" ,
                        JDOMUtils.getAttributeValue( wordElement , "ord" , false ) ,
                        wordOrdinal + ""
                    );
                }
            }
        }
    }

    /** Count separators in string.
     *
     *  @param  s       The string in which to count separators.
     *  @param  sep     The separator character.
     *
     *  @return         The separator count.
     */

     protected static int countSeparators( String s , char sep )
     {
        int result  = 0;

        if ( ( s.length() == 1 ) && ( s.charAt( 0 ) == sep ) )
        {
        }
        else
        {
            for ( int i = 0 ; i < s.length() ; i++ )
            {
                if ( s.charAt( i ) == sep )
                {
                    result++;
                }
            }
        }

        return result;
     }

    /** Change attribute value.
     *
     *  @param  element     DOM element.
     *  @param  attrName    Attribute name.
     *  @param  oldValue    Old attribute value.
     *  @param  newValue    New attribute value.
     *
     *  @return             true if attribute changed, false otherwise.
     */

    protected static boolean changeAttribute
    (
        Element element ,
        String attrName ,
        String oldValue ,
        String newValue
    )
    {
        boolean result  = false;

        if ( newValue != null )
        {
            if ( ( oldValue == null ) || !newValue.equals( oldValue ) )
            {
                JDOMUtils.setAttributeValue
                (
                    element ,
                    attrName ,
                    newValue
                );

                result  = true;
            }
        }

        return result;
    }

    /** Get related adorned word IDs for a word ID.
     *
     *  @param  wordID  Word ID for which related IDs are wanted.
     *
     *  @return         List of related word IDs.
     *
     *  <p>
     *  Related word IDs are the word IDs for the other parts of
     *  a split word.  The returned list includes the
     *  given wordID.
     *  </p>
     *
     *  <p>
     *  For unsplit words, the single given wordID is returned
     *  in the list.
     *  </p>
     *
     *  <p>
     *  Null is returned when the wordID does not exist.
     *  </p>
     */

    public static List<String> getRelatedWordIDs( String wordID )
    {
        List<String> result = ListFactory.createNewList();

        Element wordElement = wordIDsToElements.get( wordID );

        if ( wordElement == null )
        {
            result.add( wordID );
        }
        else
        {
            String part = wordElement.getAttributeValue( "part" );

            if ( ( part == null ) || part.equals( "N" ) )
            {
                result.add( wordID );
            }
            else
            {
                int lastDotPos  = wordID.lastIndexOf( '.' );

                if ( lastDotPos >= 0 )
                {
                    String rootWordID   = wordID.substring( 0 , lastDotPos );

                    for ( int i = 1 ; i < 20 ; i++ )
                    {
                        String otherWordID  = rootWordID + "." + i;

                        if ( wordIDsToElements.containsKey( otherWordID ) )
                        {
                            result.add( otherWordID );
                        }
                    }
                }
                else
                {
                    result.add( wordID );
                }
            }
        }

        return result;
    }

    /** Process files.
     */

    protected static int processFiles( String[] args )
        throws Exception
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
/*
        printStream.println();
        printStream.println();
        printStream.println( "---------- Summary ----------" );
        printStream.println();

        printMismatches();
*/
                                //  Return # of files processed.

        return fileNames.length;
    }

    /** Print mismatches.
     */

    protected static void printMismatches()
    {

        printStream.println();
        printStream.println();

        printSet( "List of bad part of speech tags." , badPosTags );

        printStream.println();
        printStream.println();

        printSet( "Combined list of bad part of speech tags " ,
            combinedBadPosTags );

        printStream.println();
        printStream.println();

        printSet( "List of individual pos/lemma mismatches" , mismatches );

        printStream.println();
        printStream.println();

        printSet( "Combined list of pos/lemma mismatches " ,
            combinedMismatches );
    }

    /** Print contents of a map (HashMap, TreeMap, etc.).
     *
     *  @param  mapLabel        Label for map.
     *
     *  @param  map             The map to print.
     *
     *  <p>
     *  N.B.  This method assumes both the keys and values have toString() methods.
     *  </p>
     */

    protected static<K,V> void printMap
    (
        String mapLabel ,
        Map<K,V> map
    )
    {
        if ( map == null )
        {
            printStream.println( mapLabel + " is null." );
        }
        else if ( map.size() == 0 )
        {
            printStream.println( mapLabel + " is empty." );
        }
        else
        {
            printStream.println( mapLabel );

            Iterator<K> iterator = map.keySet().iterator();

            int i = 0;

            while ( iterator.hasNext() )
            {
                K key   = iterator.next();
                V value = map.get( key );

                if ( key == null )
                {
                    if ( value == null )
                    {
                        printStream.println( i + ": null=null" );
                    }
                    else
                    {
                        printStream.println(
                            i + ": null=" + value.toString() );
                    }
                }
                else
                {
                    if ( value == null )
                    {
                        printStream.println(
                            i + ": " + key.toString() + "=null" );
                    }
                    else
                    {
                        printStream.println(
                            i + ": " +
                            key.toString() + "=" + value.toString() );
                    }
                }

                i++;
            }
        }
    }

    /** Print contents of a set (HashSet, TreeSet, etc.).
     *
     *  @param  setLabel    Label for set.
     *  @param  set         The set to print.
     *
     *  <p>
     *  N.B.  This method assumes set values have toString() methods.
     *  </p>
     */

    protected static<V> void printSet
    (
        String setLabel ,
        Set<V> set
    )
    {
        if ( set == null )
        {
            printStream.println( setLabel + " is null." );
        }
        else if ( set.size() == 0 )
        {
            printStream.println( setLabel + " is empty." );
        }
        else
        {
            printStream.println( setLabel );

            Iterator<V> iterator = set.iterator();

            while ( iterator.hasNext() )
            {
                V value = iterator.next();

                printStream.println
                (
                    value.toString()
                );
            }
        }
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
        printStream.println();
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

    /** Load word corrections file.
     *
     *  @param  correctionFileName      Name of word correction file.
     */

    protected static CorrectedWordsFileReader loadCorrectionsFile
    (
        String correctionFileName
    )
        throws Exception
    {
                                //  Load corrections file.

        long startTime  = System.currentTimeMillis();

        CorrectedWordsFileReader correctionsFileReader  =
            new CorrectedWordsFileReader( correctionFileName , null );

        long endTime    =
            ( System.currentTimeMillis() - startTime + 999 ) / 1000;

                                //  Get word IDs in tab file.

        correctedWordsMap   = correctionsFileReader.readAllCorrectedWords();

                                //  Report number of words found in
                                //  input tabular file.
        printStream.println
        (
            "Corrections file " + correctionFileName + " loaded in " +
            Formatters.formatLongWithCommas
            (
                endTime
            ) +
            ( ( endTime != 1 ) ? " seconds" : " second" ) +
            " and contains " +
            Formatters.formatIntegerWithCommas
            (
                correctedWordsMap.size()
            ) +
            ( ( correctedWordsMap.size() != 1 ) ? " words." : " word." )
        );

        return correctionsFileReader;
    }

    /** Update word in XML file.
     *
     *  @param  wordElement         Word to update.
     *  @param  correctedWord       Correction for word.
     *  @param  correctedWordIDs    List of corrected word IDs.
     *  @param  i                   Index of word in list of word IDs.
     *  @param  correctedSpelling   Corrected spelling.
     *
     *  @return                     True if word updated.
     */

    protected static boolean updateWord
    (
        Document document ,
        Element wordElement ,
        CorrectedWord correctedWord ,
        List<String> correctedWordIDs ,
        int i ,
        String correctedSpelling
    )
    {
                                //  Current attribute values for word.

        Map<String, String> oldValues   =
            JDOMUtils.getAttributeValues( wordElement );

        String pos      = correctedWord.getPartsOfSpeech();
        String spelling = correctedWord.getSpelling();
        String id       = correctedWord.getId();
/*
        if ( pos.startsWith( "px" ) )
        {
            return
                changeAttribute
                (
                    wordElement ,
                    "reg" ,
                    oldValues.get( "reg" ) ,
                    correctedWord.getLemmata()
                );
        }
*/
                                //  If spelling contains blanks,
                                //  and word ID does not contain
                                //  period, evict the current word
                                //  and insert a split word.

        if ( spelling.indexOf( " " ) >= 0 )
        {
            if ( verbose )
            {
                printStream.println
                (
                    "     Updating [" + spelling +
                    "] at id=" + id + ": contains blanks"
                );
            }

            if ( id.indexOf( "." ) < 0 )
            {
                if ( verbose )
                {
                    printStream.println
                    (
                        "        --- Is not currently a split word, " +
                        "must split it now."
                    );
                }

                insertWord( id , correctedWord , correctedWordIDs , i );

//              wordIDsToElements   = extractWords( document );
//              deleteWord( wordElement , correctedWordIDs , i );
//              wordIDsToElements   = extractWords( document );

                wordElementsToDelete.add( wordElement );

                return true;
            }
        }
                                //  Update word from corrections.

        String correctedJoinedSpelling  =
            StringUtils.stripChars( correctedWord.getSpelling() , " " );

        boolean c1  =
            changeAttribute
            (
                wordElement ,
                "tok" ,
                oldValues.get( "tok" ) ,
//              correctedWord.getSpelling()
                correctedJoinedSpelling
            );

        boolean c2  =
            changeAttribute
            (
                wordElement ,
                "spe" ,
                oldValues.get( "spe" ) ,
//              correctedWord.getSpelling()
                correctedJoinedSpelling
            );

        boolean c3  =
            changeAttribute
            (
                wordElement ,
                "reg" ,
                oldValues.get( "reg" ) ,
                correctedWord.getStandardSpelling()
            );

        boolean c4  =
            changeAttribute
            (
                wordElement ,
                "pos" ,
                oldValues.get( "pos" ) ,
                correctedWord.getPartsOfSpeech()
            );
/*
        changed =
            changed ||
                changeAttribute
                (
                    wordElement ,
                    "eos" ,
                    oldValues.get( "eos" ) ,
                    correctedWord.getEOS() ? "1" : "0"
                );
*/
        boolean c5  =
            changeAttribute
            (
                wordElement ,
                "lem" ,
                oldValues.get( "lem" ) ,
                correctedWord.getLemmata()
            );
/*
        changed =
            changed ||
                changeAttribute
                (
                    wordElement ,
                    "ord" ,
                    oldValues.get( "ord" ) ,
                    correctedWord.getOrd() + ""
                );
*/
//      wordElement.setText( correctedWord.getSpelling() );
        wordElement.setText( correctedSpelling );

        if ( verbose )
        {
            printStream.println
            (
                "     Updating [" + spelling + "] at id=" +
                correctedWord.getId()
            );
        }
                                //  Increment count of modified words
                                //  if any attribute value changed.

        return ( c1 || c2 || c3 || c4 || c5 );
    }

    /** Insert word in XML file.
     *
     *  @param  idToInsert          Word ID to insert.
     *  @param  correctedWord       Data for word to insert.
     *  @param  correctedWordIDs    List of corrected word IDs.
     *  @param  i                   Index of word in list of word IDs.
     *
     *  @return                     Number of new word elements added.
     */

    protected static int insertWord
    (
        String idToInsert ,
        CorrectedWord correctedWord ,
        List<String> correctedWordIDs ,
        int i
    )
    {
        int result          = 0;

        String idSibling    =
            correctedWordIDs.get( Math.max( 0 , i - 1 ) );

        int j   = i - 1;

        while ( ( j >= 0 ) && ( idSibling.indexOf( "-gap" ) >= 0 ) )
        {
            idSibling   =
                correctedWordIDs.get( Math.max( 0 , j ) );

            j--;
        }
/*
        String nextSibling  =
            correctedWordIDs.get
            (
                Math.min
                (
                    correctedWordIDs.size() - 1 ,
                    i + 1
                )
            );
*/
/*
        if ( !wordIDsToElements.containsKey( idSibling ) )
        {
            List<String> wordIDsList    = ListFactory.createNewList();

            wordIDsList.addAll( wordIDsToElements.keySet() );

            Collections.sort( wordIDsList );

            int idIndex =
                Collections.binarySearch( wordIDsList , idToInsert );

            if ( idIndex > 0 )
            {
printStream.println
(
    "     ***** insertWord: binary search failure: id=" + idToInsert +
    " returned index of " + idIndex
);
            }
            else
            {
                idIndex     = -idIndex + 1;
                idSibling   = wordIDsList.get( idIndex );
            }
        }
*/
        if ( wordIDsToElements.containsKey( idSibling ) )
        {
            Element wordElement = wordIDsToElements.get( idSibling );

            Parent parent       = wordElement.getParent();

            if ( parent == null )
            {
                wordElement = wordIDsToElements.get( idToInsert );

                if ( wordElement != null )
                {
                    parent      = wordElement.getParent();
                }
            }

            if ( parent != null )
            {
                Element parentElement   = (Element)parent;

                int index   = parent.indexOf( wordElement );

                String spelling = correctedWord.getSpelling();

                String[] spellParts = spelling.split( " " );

                spelling    =
                    StringUtils.stripChars
                    (
                        spelling ,
                        " "
                    );

                int l   = spellParts.length;

                if ( verbose )
                {
                    printStream.println
                    (
                        "     Adding [" + spelling + "] with " +
                        l + " token parts" + " at id=" + idToInsert
                    );
                }

                for ( int k = 0 ; k < l ; k++ )
                {
                    Element newWordElement  = (Element)wordElement.clone();

                    newWordElement.setText( spellParts[ k ] );

                    int kk  = k + 1;

                    String theIDValue   = idToInsert;

                    if ( l > 1 )
                    {
                        theIDValue  = theIDValue + "." + kk;
                    }

                    JDOMUtils.setAttributeValue
                    (
                        newWordElement ,
                        "xml:id" ,
                        theIDValue
                    );

                    JDOMUtils.setAttributeValue
                    (
                        newWordElement ,
                        "tok" ,
                        spellParts[ k ]
                    );

                    JDOMUtils.setAttributeValue
                    (
                        newWordElement ,
                        "spe" ,
                        spelling
                    );

                    JDOMUtils.setAttributeValue
                    (
                        newWordElement ,
                        "reg" ,
                        correctedWord.getStandardSpelling()
                    );

                    JDOMUtils.setAttributeValue
                    (
                        newWordElement ,
                        "pos" ,
                        correctedWord.getPartsOfSpeech()
                    );

                    String eosValue = "0";

                    if
                    (   spelling.equals( "." ) ||
                        spelling.equals( "?" ) ||
                        spelling.equals( "!" )
                    )
                    {
                        Element nextWordElement =
                            wordIDsToElements.get( idSibling );

                        String nextEOS  =
                            JDOMUtils.getAttributeValue
                            (
                                nextWordElement ,
                                "eos" ,
                                false
                            );

                        if ( nextEOS.equals( "0" ) )
                        {
                            eosValue    = "1";
                        }

                        if ( verbose )
                        {
                            printStream.println
                            (
                                "     Adding [" + spelling +
                                "], nextEOS = [" + nextEOS + "], " +
                                " eosValue=[" + eosValue + "] at id=" +
                                idToInsert
                            );
                        }
                    }

                    JDOMUtils.setAttributeValue
                    (
                        newWordElement ,
                        "eos" ,
                        eosValue
                    );

                    JDOMUtils.setAttributeValue
                    (
                        newWordElement ,
                        "lem" ,
                        correctedWord.getLemmata()
                    );

                    JDOMUtils.setAttributeValue
                    (
                        newWordElement ,
                        "ord" ,
                        "-1"
                    );

                    String partString   = "M";

                    if ( l <= 1 )
                    {
                        partString  = "N";
                    }
                    else if ( kk == 1 )
                    {
                        partString  = "I";
                    }
                    else if ( kk >= spellParts.length )
                    {
                        partString  = "F";
                    }
                    else
                    {
                        partString  = "M";
                    }

                    JDOMUtils.setAttributeValue
                    (
                        newWordElement ,
                        "part" ,
                        partString
                    );
                                //  If new word is not punctuation
                                //  or a symbol, add a blank
                                //  before it.
                                //
                                //  This isn't really good enough.
                                //  Some punctuation should get
                                //  preceding blank.

                    if ( !CharUtils.isPunctuationOrSymbol( spelling ) )
                    {
                        Element cElement    =
                            (Element)clonableCElement.clone();

                        cElement.setText( " " );

                        parentElement.addContent
                        (
                            ++index ,
                            cElement
                        );
                    }
                                //  Add new word.

                    parentElement.addContent
                    (
                        ++index ,
                        newWordElement
                    );

                    wordIDsToElements.put( theIDValue , newWordElement );

                    result++;
                }
            }
            else
            {
                printStream.println
                (
                    "     ***** Adding at id=" + idToInsert +
                    ": sibling id " + idSibling +
                    " parent not found."
                );
            }
        }
        else
        {
            printStream.println
            (
                "     ***** Adding at id=" + idToInsert +
                ": sibling id " + idSibling +
                " not found."
            );
        }

        return result;
    }

    /** Delete words in XML file.
     *
     *  @param  wordElementsToDelete    Word elements to delete.
     *  @param  sortedWordIDs           Sorted word IDs in document.
     */

    protected static void deleteWordElements
    (
        List<Element> wordElementsToDelete ,
        List<String> sortedWordIDs
    )
    {
        for ( int i = 0 ; i < wordElementsToDelete.size() ; i++ )
        {
            deleteWordElement
            (
                wordElementsToDelete.get( i ) ,
                sortedWordIDs
            );
        }
    }

    /** Delete gaps in XML file.
     *
     *  @param  gapElementsToDelete     Gap elements to delete.
     */

    protected static void deleteGapElements
    (
        List<Element> gapElementsToDelete
    )
    {
        for ( int i = 0 ; i < gapElementsToDelete.size() ; i++ )
        {
            deleteGapElement( gapElementsToDelete.get( i ) );
        }
    }

    /** Replace gaps in XML file with real words.
     *
     *  @param  gapElementsToUpdate     Gap elements to update.
     */

    protected static void replaceGapElementsWithWords
    (
        List<Element> gapElementsToUpdate
    )
    {
        for ( int i = 0 ; i < gapElementsToUpdate.size() ; i++ )
        {
            Element gapElement  = gapElementsToUpdate.get( i );

            String id           =
                JDOMUtils.getAttributeValue(
                    gapElement , "xml:id" , false );

            CorrectedWord correctedWord = correctedWordsMap.get( id );

            if ( correctedWord != null )
            {
                gapToWord( gapElement , correctedWord );
            }
            else
            {
                printStream.println
                (
                    "     ***** Replacing gap with id=" + id +
                    ": failed, no matching corrected word found."
                );
            }
        }
    }

    /** Replace a gap element by a word element.
     *
     *  @param  gapElement      Gap element to replace.
     *  @param  correctedWord   Word to replace gap element.
     */

    protected static void gapToWord
    (
        Element gapElement ,
        CorrectedWord correctedWord
    )
    {
        if ( ( gapElement == null ) || ( correctedWord == null ) )
        {
            return;
        }
                                //  Get parent of gap element.

        Element parent  = (Element)gapElement.getParent();

                                //  Remember index of gap element
                                //  in parent.

        int index       = parent.indexOf( gapElement );

                                //  Get next element following
                                //  gap.  Should always exist
                                //  since a gap should never
                                //  be the last element in a
                                //  document.

        int i   = index + 1;
        int n   = parent.getContentSize();

        Content nextChild   = parent.getContent( i );

        while ( ( !(nextChild instanceof Element) ) && ( i < n ) )
        {
            nextChild   = parent.getContent( i++ );
        }

        String nextSpelling = null;

        if ( nextChild instanceof Element )
        {
            Element nextElement = (Element)nextChild;

                                //  If next element is a word,
                                //  get its spelling.

            if ( nextElement.getName().equals( "w" ) )
            {
                nextSpelling    =
                    JDOMUtils.getAttributeValue(
                        nextElement , "spe" , false );
            }
        }
                                //  Get gap element ID.
        String id       =
            JDOMUtils.getAttributeValue( gapElement , "xml:id" , false );

                                //  Holds word attributes.

        List<Attribute> attributes  = new ArrayList<Attribute>();

                                //  Change gap to word.

        gapElement.setName( "w" );

                                //  Add attributes.

        String spelling = correctedWord.getSpelling();

        attributes.add
        (
            new Attribute( "tok" , spelling )
        );

        attributes.add
        (
            new Attribute( "spe" , spelling )
        );

        attributes.add
        (
            new Attribute( "reg" , correctedWord.getStandardSpelling() )
        );

        attributes.add
        (
            new Attribute( "pos" , correctedWord.getPartsOfSpeech() )
        );

        attributes.add
        (
            new Attribute( "eos" , "0" )
        );

        attributes.add
        (
            new Attribute( "lem" , correctedWord.getLemmata() )
        );

        attributes.add
        (
            new Attribute( "ord" , "-1" )
        );

        attributes.add
        (
            new Attribute( "part" , "N" )
        );
                                //  Add word attributes except ID.

        gapElement.setAttributes( attributes );

                                //  Add word ID.

        JDOMUtils.setAttributeValue(
            gapElement , "xml:id" , correctedWord.getUpdatedId() );

                                //  Set word text.

        gapElement.setText( spelling );

                                //  If new word is not punctuation
                                //  or a symbol, add a blank
                                //  before it.
                                //
                                //  This isn't really good enough.
                                //  Some punctuation should get
                                //  preceding blank.
                                //

        Element cElement    = (Element)clonableCElement.clone();

        cElement.setText( " " );

        if ( !CharUtils.isPunctuationOrSymbol( spelling ) )
        {
            parent.addContent( index , cElement );
        }
                                //  Add a blank after the added word
                                //  unless the next element is
                                //  a word containing punctuation.

        if ( nextSpelling != null )
        {
            if ( !CharUtils.isPunctuationOrSymbol( nextSpelling ) )
            {
                cElement    = (Element)clonableCElement.clone();

                cElement.setText( " " );

                parent.addContent( index + 2 , cElement );
            }
        }
                                //  Updated counts.
        deletedGaps++;
        addedWords++;
    }

    /** Delete a gap element.
     *
     *  @param  gapElement  Gap element to delete.
     */

    protected static void deleteGapElement( Element gapElement )
    {
        if ( gapElement == null )
        {
            return;
        }
                                //  Delete gap.

        Parent parent   = gapElement.getParent();

        String id       =
            JDOMUtils.getAttributeValue( gapElement , "xml:id" , false );

        if ( parent != null )
        {
            parent.removeContent( gapElement );

            if ( verbose )
            {
                printStream.println
                (
                    "     Deleting gap at id=" + id
                );
            }

            deletedGaps++;
        }
    }

    /** Delete a word element.
     *
     *  @param  wordElement         Word element to delete.
     */

    protected static void deleteWordElement
    (
        Element wordElement ,
        List<String> sortedWordIDs
    )
    {
        if ( wordElement == null )
        {
            return;
        }

                                //  Get EOS attribute value.
        String eos  =
            JDOMUtils.getAttributeValue( wordElement , "eos" , true );

        String spelling =
            JDOMUtils.getAttributeValue( wordElement , "spe" , true );

        String id       =
            JDOMUtils.getAttributeValue( wordElement , "xml:id" , true );

        if ( eos == null )
        {
            eos = "0";
        }
                                //  Delete word.

        Parent parent   = wordElement.getParent();

        if ( parent != null )
        {
            parent.removeContent( wordElement );

            if ( verbose )
            {
                printStream.println
                (
                    "     Deleting [" + spelling + "] at id=" + id
                );
            }

            deletedWords++;
        }
                                //  If EOS flag was set, we need to
                                //  move the flag to the previous
                                //  word in reading context order.

        if ( eos.equals( "1" ) )
        {
            int index   = Collections.binarySearch( sortedWordIDs , id );

            if ( index > 0 )
            {
                String idSibling    = sortedWordIDs.get( index - 1 );

                if ( wordIDsToElements.containsKey( idSibling ) )
                {
                    Element previousWordElement =
                        wordIDsToElements.get( idSibling );

                    String oldEOS   =
                        JDOMUtils.getAttributeValue
                        (
                            previousWordElement ,
                            "eos" ,
                            true
                        );

                    if
                    (
                        changeAttribute
                        (
                            previousWordElement ,
                            "eos" ,
                            oldEOS ,
                            eos
                        )
                    )
                    {
                        modifiedWords++;

                        if ( verbose )
                        {
                            printStream.println
                            (
                                "        Turned on EOS for word ID " +
                                idSibling
                            );
                        }
                    }
                }
            }
                                //  From deleted word from list of
                                //  sorted word IDs.

            sortedWordIDs.remove( index );
        }
    }

    /** Get sorted word IDs.
     *
     *  @return     List of sorted word IDs.
     */

    protected static List<String> getSortedWordIDs()
    {
        List<String> sortedWordIDs  = ListFactory.createNewList();

        sortedWordIDs.addAll( wordIDsToElements.keySet() );

        Collections.sort( sortedWordIDs );

        return sortedWordIDs;
    }

    /** Delete a word.
     *
     *  @param  wordElement         Word to delete.
     *  @param  correctedWordIDs    List of corrected word IDs.
     *  @param  i                   Index of word in list of word IDs.
     */

    protected static void deleteWordOld
    (
        Element wordElement ,
        List<String> correctedWordIDs ,
        int i
    )
    {
        if ( wordElement != null )
        {
                                //  Get EOS attribute value.
            String eos  =
                JDOMUtils.getAttributeValue( wordElement , "eos" , true );

            String spelling =
                JDOMUtils.getAttributeValue( wordElement , "spe" , true );

            String id       =
                JDOMUtils.getAttributeValue( wordElement , "xml:id" , true );

            if ( eos == null )
            {
                eos = "0";
            }
                                //  Delete word.

            Parent parent   = wordElement.getParent();

            if ( parent != null )
            {
                parent.removeContent( wordElement );

                if ( verbose )
                {
                    printStream.println
                    (
                        "     Deleting [" + spelling + "] at id=" + id
                    );
                }

                deletedWords++;
            }
                                //  If EOS flag was set, we need to
                                //  move the flag to the previous
                                //  word in reading context order.

            if ( eos.equals( "1" ) )
            {
                String idSibling    =
                    correctedWordIDs.get( Math.max( 0 , i - 1 ) );

                if ( wordIDsToElements.containsKey( idSibling ) )
                {
                    Element previousWordElement =
                        wordIDsToElements.get( idSibling );

                    String oldEOS   =
                        JDOMUtils.getAttributeValue
                        (
                            previousWordElement ,
                            "eos" ,
                            true
                        );

                    if
                    (
                        changeAttribute
                        (
                            previousWordElement ,
                            "eos" ,
                            oldEOS ,
                            eos
                        )
                    )
                    {
                        modifiedWords++;

                        if ( verbose )
                        {
                            printStream.println
                            (
                                "        Turned on EOS for word ID " +
                                idSibling
                            );
                        }
                    }
                }
            }
        }
    }
/*
    protected static void deleteNearestGap( String id )
    {
                                //  Get element for this Word ID.

        Element wordElement = wordIDsToElements.get( id );

                                //  Get parent.

        Parent parent   = wordElement.getParent();

        if ( parent != null )
        {
                                //  Find nearest preceding gap.

            int idIndex = parent.indexOf( wordElement );

            boolean gapDeleted  = false;

            for ( int i = idIndex - 1 ; i > 0 ; i-- )
            {
                Content content = parent.getContent( i );

                if ( ((Element)content).getName().equals( "gap" ) )
                {
                    parent.removeContent( content );
                    gapDeleted  = true;
                    break;
                }
            }

            if ( !gapDeleted )
            {
                printStream.println
                (
                    "     ***** Could delete gap for =" + id
                );
            }
        }
        else
        {
            printStream.println
            (
                "     ***** Could not find parent of id=" + id +
                " for deleting related gap."
            );
        }
    }
*/
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



