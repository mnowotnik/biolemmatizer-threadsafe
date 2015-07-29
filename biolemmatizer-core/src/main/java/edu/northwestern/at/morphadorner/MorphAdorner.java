package edu.northwestern.at.morphadorner;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.lang.Runtime;
import java.lang.String;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.*;
import java.util.*;

import javax.xml.xpath.*;

import org.w3c.dom.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import edu.northwestern.at.morphadorner.tools.*;
import edu.northwestern.at.morphadorner.xgtagger.*;
import edu.northwestern.at.utils.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.abbreviations.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.adornedword.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.inputter.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.lemmatizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.lexicon.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.namerecognizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.namestandardizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.outputter.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.partsofspeech.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.guesser.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.hepple.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.hepple.rules.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.noopretagger.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.smoothing.contextual.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.smoothing.lexical.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.transitionmatrix.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.sentencemelder.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.sentencesplitter.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.spellingmapper.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.spellingstandardizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.*;
import edu.northwestern.at.utils.html.*;
import edu.northwestern.at.utils.logger.*;
import edu.northwestern.at.utils.xml.*;

/** Morphological Adorner.
 *
 *  <p>
 *  Given an input text, the morphological adorner adorns each word with
 *  morphological information such as part of speech, lemma and
 *  standardized spelling.
 *  </p>
 */

public class MorphAdorner
{
    /** Stores initialized MorphAdorner objects for reuse. */

    protected static Map<String, MorphAdorner> storedAdorners   =
        MapFactory.createNewSynchronizedMap();

    /** Number of chararacters in a KWIC line. */

    public int defaultKWICWidth     = 80;

    /** Latin words file. */

    public String latinWordsFileName    =
        "resources/latinwords.txt";

    /** Extra words file. */

    public String extraWordsFileName    =
        "resources/extrawords.txt";

    /** Extra words. */

    public TaggedStrings extraWords = null;

    /** Spelling tokenizer for lemmatization. */

    public WordTokenizer spellingTokenizer  =
        new PennTreebankTokenizer();

    /** Part of speech tags. */

    public PartOfSpeechTags partOfSpeechTags;

    /** Part of speech tagger. */

    public PartOfSpeechTagger tagger;

    /** Part of speech retagger. */

    public PartOfSpeechRetagger retagger;

    /** Word lexicon. */

    public Lexicon wordLexicon;

    /** Part of speech guesser. */

    public PartOfSpeechGuesser partOfSpeechGuesser;

    /** Suffix lexicon. */

    public Lexicon suffixLexicon;

    /** Transition matrix. */

    public TransitionMatrix transitionMatrix;

    /** Spelling standardizer. */

    public SpellingStandardizer spellingStandardizer;

    /** Spelling mapper. */

    public SpellingMapper spellingMapper;

    /** Proper name standardizer. */

    public NameStandardizer nameStandardizer;

    /** Lemmatizer. */

    public Lemmatizer lemmatizer;

    /** Proper names. */

    public Names names  = new Names();

    /** Abbreviations. */

    public Abbreviations abbreviations  = new Abbreviations();

    /** Main text abbreviations. */

    public Abbreviations mainAbbreviations  = new Abbreviations();

    /** Side text abbreviations. */

    public Abbreviations sideAbbreviations  = new Abbreviations();

    /** Part of speech tag separator. */

    public String tagSeparator  = "|";

    /*  Separates lemmata in compound lemma forms. */

    public String lemmaSeparator    = "|";

    /** MorphAdorner logger. */

    public MorphAdornerLogger morphAdornerLogger    = null;

    /** MorphAdorner settings. */

    public MorphAdornerSettings morphAdornerSettings    = null;

    /** MorphAdorner settings for XML tokenization. */

    public MorphAdornerSettings tokenizationSettings    = null;

    /** Tag classifier. */

    public TEITagClassifier tagClassifier   = new TEITagClassifier();

    /** Create empty MorphAdorner object.
     */

    public MorphAdorner()
    {
    }

    /** Create MorphAdorner object.
     *
     *  @param  args                Command line parameters.
     *  @param  logConfiguration    Log file configuration.
     *  @param  logDirectory        Log file directory.
     */

    public MorphAdorner
    (
        String[] args ,
        String logConfiguration ,
        String logDirectory
    )
    {
                                //  Create settings.

        morphAdornerSettings    = new MorphAdornerSettings();

                                //  Initialize logging.
        try
        {
            morphAdornerLogger  =
                new MorphAdornerLogger
                (
                    logConfiguration ,
                    logDirectory ,
                    morphAdornerSettings
                );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
                                //  Initialize program settings.

        morphAdornerSettings.initializeSettings( morphAdornerLogger );

                                //  Get program settings.
        try
        {
            morphAdornerSettings.getSettings( args );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
                                //  Say adorner is starting.

        morphAdornerLogger.println( "programBanner" );
        morphAdornerLogger.println( "Initializing_please_wait" );

                                //  Initialize adornment classes.

        initializeAdornment();

                                //  Remember initialization complete
                                //  for subsequent entries.

        morphAdornerSettings.initialized    = true;
    }

    /** Create MorphAdorner object.
     *
     *  @param  args    Parameters.
     */

    public MorphAdorner( String[] args )
    {
        this( args , "morphadornerlog.config" , "log" );
    }

    /** Get map of stored adorners.
     *
     *  @return     Map from names to adorner instances.
     */

    public static Map<String, MorphAdorner> getStoredAdorners()
    {
        return storedAdorners;
    }

    /** Set map of stored adorners.
     *
     *  @param  storedAdorners  Map from names to adorner instances.
     */

    public static void setStoredAdorners
    (
        Map<String, MorphAdorner> storedAdorners
    )
    {
        MorphAdorner.storedAdorners = storedAdorners;
    }

    /** Initialize adornment classes.
     */

    protected void initializeAdornment()
    {
        try
        {
                                    //  Create part of speech
                                    //  tags list.
            partOfSpeechTags    =
                PartOfSpeechTagsFactory.newPartOfSpeechTags
                (
                    morphAdornerSettings.properties
                );
                                //  Get part of speech tag separator.

            tagSeparator        = partOfSpeechTags.getTagSeparator();

                                //  Create a posttokenizer for the taggers.

            PostTokenizer postTokenizer =
                PostTokenizerFactory.newPostTokenizer
                (
                    morphAdornerSettings.properties
                );
                                //  Create a part of speech tagger.

            tagger =
                PartOfSpeechTaggerFactory.newPartOfSpeechTagger
                (
                    morphAdornerSettings.properties
                );
                                //  Create a part of speech retagger.

            retagger =
                PartOfSpeechRetaggerFactory.newPartOfSpeechRetagger
                (
                    morphAdornerSettings.properties
                );
                                //  Set post tokenizer into tagger.

            tagger.setPostTokenizer( postTokenizer );

                                //  Set post tokenizer into retagger.

            retagger.setPostTokenizer( postTokenizer );

                                //  Set logger into tagger.

            ((UsesLogger)tagger).setLogger(
                morphAdornerLogger.getLogger() );

                                //  Set logger into retagger.

            ((UsesLogger)retagger).setLogger(
                morphAdornerLogger.getLogger() );

                                //  Get contextual and lexical smoothers
                                //  for tagger.

            ContextualSmoother cSmoother    =
                ContextualSmootherFactory.newContextualSmoother
                (
                    morphAdornerSettings.properties
                );

            cSmoother.setPartOfSpeechTagger( tagger );

            LexicalSmoother lSmoother   =
                LexicalSmootherFactory.newLexicalSmoother
                (
                    morphAdornerSettings.properties
                );

            lSmoother.setPartOfSpeechTagger( tagger );

                                //  Set smoothers into tagger.

            tagger.setContextualSmoother( cSmoother );
            tagger.setLexicalSmoother( lSmoother );

                                //  Get contextual and lexical smoothers
                                //  for retagger.

            ContextualSmoother cSmoother2   =
                ContextualSmootherFactory.newContextualSmoother
                (
                    morphAdornerSettings.properties
                );

            cSmoother2.setPartOfSpeechTagger( retagger );

            LexicalSmoother lSmoother2  =
                LexicalSmootherFactory.newLexicalSmoother
                (
                    morphAdornerSettings.properties
                );

            lSmoother2.setPartOfSpeechTagger( retagger );

                                //  Set smoothers into retagger.

            retagger.setContextualSmoother( cSmoother2 );
            retagger.setLexicalSmoother( lSmoother2 );

                                //  Set retagger into tagger.

            tagger.setRetagger( retagger );

                                //  Display what types of tagger and
                                //  retagger we are using.

            morphAdornerLogger.println
            (
                "Using" , new Object[]{ tagger.toString() }
            );

            morphAdornerLogger.println
            (
                "Using" , new Object[]{ retagger.toString() }
            );
                                //  Load word lexicon.
            wordLexicon =
                MorphAdornerUtils.loadWordLexicon
                (
                    morphAdornerSettings ,
                    morphAdornerLogger
                );
                                //  Set parts of speech into lexicon.

            wordLexicon.setPartOfSpeechTags( partOfSpeechTags );

                                //  Get a part of speech guesser
                                //  for words not in the lexicon.

            partOfSpeechGuesser =
                PartOfSpeechGuesserFactory.newPartOfSpeechGuesser
                (
                    morphAdornerSettings.properties
                );
                                //  Set check possessives flag.

            boolean checkPossessives    =
                morphAdornerSettings.getBooleanProperty(
                    "partofspeechguesser.check_possessives" , false );


            partOfSpeechGuesser.setCheckPossessives( checkPossessives );

                                //  Set guesser into tagger. */

            tagger.setPartOfSpeechGuesser( partOfSpeechGuesser );

                                //  Set guesser into word lexicon. */

            partOfSpeechGuesser.setWordLexicon( wordLexicon );

                                //  Set logger into guesser.

            ((UsesLogger)partOfSpeechGuesser).setLogger(
                morphAdornerLogger.getLogger() );

                                //  Load suffix lexicon if given.
            suffixLexicon   =
                MorphAdornerUtils.loadSuffixLexicon
                (
                    morphAdornerSettings ,
                    morphAdornerLogger
                );
                                //  Set suffix lexicon into guesser.

            partOfSpeechGuesser.setSuffixLexicon( suffixLexicon );

                                //  Add extra words.
            extraWords  =
                MorphAdornerUtils.getExtraWordsList
                (
                    extraWordsFileName ,
                    partOfSpeechTags.getSingularProperNounTag() ,
                    "Loaded_extra_words" ,
                    morphAdornerSettings ,
                    morphAdornerLogger
                );

            partOfSpeechGuesser.addAuxiliaryWordList( extraWords );

                                //  Add name lists.

            partOfSpeechGuesser.addAuxiliaryWordList
            (
                new TaggedStringsSet
                (
                    names.getPlaceNames().keySet() ,
                    partOfSpeechTags.getSingularProperNounTag()
                )
            );

            partOfSpeechGuesser.addAuxiliaryWordList
            (
                new TaggedStringsSet
                (
                    names.getFirstNames() ,
                    partOfSpeechTags.getSingularProperNounTag()
                )
            );

            partOfSpeechGuesser.addAuxiliaryWordList
            (
                new TaggedStringsSet
                (
                    names.getSurnames() ,
                    partOfSpeechTags.getSingularProperNounTag()
                )
            );
                                //  Add latin words.

            if ( morphAdornerSettings.useLatinWordList )
            {
                partOfSpeechGuesser.addAuxiliaryWordList
                (
                    MorphAdornerUtils.getWordList
                    (
                        latinWordsFileName ,
                        partOfSpeechTags.getForeignWordTag( "latin" ) ,
                        "Loaded_latin_words" ,
                        morphAdornerSettings ,
                        morphAdornerLogger
                    )
                );
            }
                                //  Add extra abbreviations.

            if ( morphAdornerSettings.abbreviationsURL.length() > 0 )
            {
                addAbbreviations
                (
                    abbreviations ,
                    URLUtils.getURLFromFileNameOrURL
                    (
                        morphAdornerSettings.abbreviationsURL
                    ).toString() ,
                    "Loaded_abbreviations"
                );
            }

            if ( morphAdornerSettings.abbreviationsMainTextURL.length() > 0 )
            {
                addAbbreviations
                (
                    mainAbbreviations ,
                    URLUtils.getURLFromFileNameOrURL
                    (
                        morphAdornerSettings.abbreviationsMainTextURL
                    ).toString() ,
                    "Loaded_abbreviations"
                );
            }

            if ( morphAdornerSettings.abbreviationsSideTextURL.length() > 0 )
            {
                addAbbreviations
                (
                    sideAbbreviations ,
                    URLUtils.getURLFromFileNameOrURL
                    (
                        morphAdornerSettings.abbreviationsSideTextURL
                    ).toString() ,
                    "Loaded_abbreviations"
                );
            }
                                //  Set tagger to use lexicon.

            tagger.setLexicon( wordLexicon );

                                //  Load tagger rules if given.

            MorphAdornerUtils.loadTaggerRules
            (
                tagger ,
                morphAdornerSettings ,
                morphAdornerLogger
            );
                                //  Load transition matrix if given.

            transitionMatrix        =
                MorphAdornerUtils.loadTransitionMatrix
                (
                    tagger,
                    morphAdornerSettings ,
                    morphAdornerLogger
                );
                                //  Create a spelling standardizer.

            spellingStandardizer    =
                MorphAdornerUtils.createSpellingStandardizer
                (
                    wordLexicon ,
                    names ,
                    morphAdornerSettings ,
                    morphAdornerLogger
                );
                                //  Create a spelling mapper.

            spellingMapper  =
                MorphAdornerUtils.createSpellingMapper
                (
                    morphAdornerSettings.properties
                );
                                //  Create a name standardizer.

            nameStandardizer    =
                MorphAdornerUtils.createNameStandardizer
                (
                    wordLexicon ,
                    morphAdornerSettings ,
                    morphAdornerLogger
                );
                                //  Add spelling standardizer to
                                //  part of speech guesser.

            if ( spellingStandardizer != null )
            {
                partOfSpeechGuesser.setSpellingStandardizer(
                    spellingStandardizer );
            }
                                //  Create a lemmatizer.

            lemmatizer  =
                LemmatizerFactory.newLemmatizer
                (
                    morphAdornerSettings.properties
                );
                                //  Get lemma separator.

            lemmaSeparator  = lemmatizer.getLemmaSeparator();

                                //  Set lexicon for lemmatizer.

            lemmatizer.setLexicon( wordLexicon );

                                //  Set standard word list for lemmatizer.

            lemmatizer.setDictionary
            (
                spellingStandardizer.getStandardSpellings()
            );
                                //  Set logger into lemmatizer.

            ((UsesLogger)lemmatizer).setLogger(
                morphAdornerLogger.getLogger() );

                                //  Add abbreviations to guesser.

            partOfSpeechGuesser.setAbbreviations( abbreviations );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /** Process list of files containing text to adorn.
     *
     *  @param  xmlTokenizeOnly     Only tokenize XML files.
     */

    public void processInputFiles( boolean xmlTokenizeOnly )
    {
        long processStartTime   = System.currentTimeMillis();

                                //  Display # of files to process.

        switch ( morphAdornerSettings.fileNames.length )
        {
            case 0  :   morphAdornerLogger.println
                        (
                            "No_files_to_process"
                        );
                        break;

            case 1  :   morphAdornerLogger.println
                        (
                            "One_file_to_process"
                        );
                        break;

            default :   morphAdornerLogger.println
                        (
                            "Number_of_files_to_process" ,
                            new Object[]
                            {
                                Formatters.formatIntegerWithCommas
                                (
                                    morphAdornerSettings.fileNames.length
                                )
                            }
                        );
                        break;
        }
                                //  Note if we're using our XML handler.

        boolean useXMLHandler   =
            morphAdornerSettings.getBooleanProperty(
                "adorner.handle_xml" , false );

        MorphAdornerUtils.logMemoryUsage
        (
            morphAdornerLogger ,
            "Before processing input texts: "
        );
                                //  Loop over the input file names.

        for ( int i = 0 ; i < morphAdornerSettings.fileNames.length ; i++ )
        {
                                //  Get the next input file name.

            String inputFileName    = morphAdornerSettings.fileNames[ i ];

                                //  Say we're processing it.

            morphAdornerLogger.println
            (
                "Processing_file" ,
                new Object[]{ inputFileName }
            );

            try
            {
                                //  Are we using XGTagger to process
                                //  input XML?

                if ( useXMLHandler )
                {
                                //  See if input file is already adorned
                                //  or at least tokenized.
                                //
                                //  If so, we will (re)adorn it keeping
                                //  the existing word IDs.

                    if ( MorphAdornerUtils.isAdorned( inputFileName , 500 ) )
                    {
                        readorn( inputFileName );
                    }
                    else
                    {
                        adornXML( inputFileName , xmlTokenizeOnly );
                    }
                }
                                //  Not using XML handler -- adorn
                                //  as plain text.
                else
                {
                    adornFile( inputFileName );
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
                                //  Display total processing time.

        if ( morphAdornerSettings.fileNames.length > 0 )
        {
            morphAdornerLogger.println
            (
                "All_files_adorned" ,
                new Object[]
                {
                    MorphAdornerUtils.durationString
                    (
                        morphAdornerSettings ,
                        processStartTime
                    )
                }
            );
        }
    }

    /** Process list of files containing text to adorn.
     */

    public void processInputFiles()
    {
        processInputFiles( false );
    }

    /** Adorn XML file.
     *
     *  @param  inputFileName   File name of XML file to adorn.
     *  @param  tokenizeOnly    Only tokenize.
     *
     *  @throws Exception       For variety of errors.
     */

    public void adornXML( String inputFileName , boolean tokenizeOnly )
        throws Exception
    {
                                //  Skip adornment if output file
                                //  already exists and appropriate
                                //  option is set.

        if  (   !morphAdornerSettings.adornExistingXMLFiles &&
                doesOutputFileNameExist( inputFileName )
            )
        {
            morphAdornerLogger.println
            (
                "Skipping_file_which_is_already_adorned" ,
                new Object[]
                {
                    inputFileName
                }
            );

            return;
        }
                                //  Create a new text inputter.

        TextInputter inputter   =
            TextInputterFactory.newTextInputter
            (
                morphAdornerSettings.properties
            );
                                //  Enable gap fixer.

        inputter.enableGapFixer( morphAdornerSettings.fixGapTags );

                                //  Enable orig fixer.

        inputter.enableOrigFixer( morphAdornerSettings.fixOrigTags );

                                //  Enable split words fixer.

        inputter.enableSplitWordsFixer
        (
            morphAdornerSettings.fixSplitWords ,
            morphAdornerSettings.fixSplitWordsPatternReplacers
        );
                                //  Load input text.  May be
                                //  split into multiple segments.
        URL inputFileURL    =
            URLUtils.getURLFromFileNameOrURL( inputFileName );

        inputter.loadText
        (
            inputFileURL ,
            "utf-8" ,
            morphAdornerSettings.xmlSchema
        );
                                //  Report number of segments.

        int nSegments       = inputter.getSegmentCount();

        String sSegments    =
            Formatters.formatIntegerWithCommas( nSegments );

        morphAdornerLogger.println
        (
            "Input_file_split" ,
            new Object[]{ inputFileName , sSegments }
        );
                                //  Save running word ID from one
                                //  segment to another.

        int runningWordID   = 0;

                                //  Track ID for multipart words.

        Map<Integer, Integer> splitWords    = MapFactory.createNewMap();

                                //  Total number of words adorned.

        int totalWords      = 0;

                                //  Total number of <pb> elements.

        int totalPageBreaks = 0;

                                //  Adorn each segment separately.

        for ( int j = 0 ; j < nSegments ; j++ )
        {
                                //  Get next segment name.

            String segmentName  = inputter.getSegmentName( j );

                                //  Only adorn text segments.

            if ( !segmentName.startsWith( "text" ) ) continue;
            if ( segmentName.equals( "text" ) ) continue;

                                //  Report which segment is being
                                //  adorned.

            morphAdornerLogger.println
            (
                "Processing_segment" ,
                new Object[]
                {
                    segmentName ,
                    Formatters.formatIntegerWithCommas( j + 1 ) ,
                    sSegments
                }
            );
                                //  Get segment text.

            String segmentText  = inputter.getSegmentText( segmentName );

                                //  Convert XML text to DOM document.

            Document document   =
                XGParser.textToDOM
                (
                    morphAdornerSettings.xgOptions ,
                    segmentText
                );
                                //  Fix empty soft tags.

            MorphAdornerUtils.fixEmptySoftTags
            (
                morphAdornerSettings.xgOptions ,
                document
            );
                                //  Fix superscript tags.

            MorphAdornerUtils.fixSupTags( document );

                                //  Add page break count this segment
                                //  to total.

            totalPageBreaks +=
                MorphAdornerUtils.countPageBreaks( document );

                                //  Extract plain text for adornment.
            Object[] o  =
                XGParser.extractText
                (
                    morphAdornerSettings.xgOptions ,
                    document
                );
                                //  Set running word ID.

            XGParser xgParser   = (XGParser)o[ 1 ];

            xgParser.setRunningWordID( runningWordID );

                                //  Create adorned output.

            AdornedWordOutputter outputter  =
                adornText( (String)o[ 0 ] , null );

                                //  Merge adornments with original
                                //  XML text.

            morphAdornerLogger.println
            (
                "Inserting_adornments_into_xml"
            );

            long startTime  = System.currentTimeMillis();

            Map<Integer, Integer> segmentSplitWords =
                XGParser.mergeAdornments
                (
                    morphAdornerSettings.xgOptions ,
                    (XGParser)o[ 1 ] ,
                    document ,
                    segmentName ,
                    outputter ,
                    inputter
                );

//          XGMisc.printNodeToFile( document , "zzbogus" + j + ".xml" );

//          printWords( document );

            fixSideWords( document , sideAbbreviations );

//          printWords( document );

                                //  Output updated DOM tree segment as
                                //  XML text.

            File file   = File.createTempFile( "mad" , null );

            String fileName = file.getAbsolutePath();

            if ( XGMisc.printNodeToFile( document , fileName ) == 1 )
            {
                inputter.setSegmentText( segmentName , file );

                if ( !inputter.usesSegmentFiles() )
                {
                    file.delete();
                }
            }
                                //  Add split words from this segment
                                //  to overall map of split words.

            for ( int wid : segmentSplitWords.keySet() )
            {
                if ( segmentSplitWords.get( wid ) > 1 )
                {
                    splitWords.put
                    (
                        wid ,
                        segmentSplitWords.get( wid )
                    );
                }
            }
                                //  Report adornment merge complete.

            morphAdornerLogger.println
            (
                "Inserted_adornments_into_xml" ,
                new Object[]
                {
                    MorphAdornerUtils.durationString
                    (
                        morphAdornerSettings ,
                        startTime
                    )
                }
            );
                                //  Save running word ID for
                                //  processing next segment.

            runningWordID   = xgParser.getRunningWordID();

                                //  Add count of adorned words to total
                                //  for this document.

            totalWords      += xgParser.getNumberOfAdornedWords();

                                //  Evict the temporary output file.

            if ( !inputter.usesSegmentFiles() )
            {
                FileUtils.deleteFile( outputter.getOutputFileName() );
            }

            xgParser    = null;
            document    = null;
            outputter   = null;
            o[ 0 ]      = null;
            o[ 1 ]      = null;
            o           = null;
        }
                                //  Create name of output file to
                                //  which to write merged adorned XML.

        String outputFileName   = getOutputFileName( inputFileName );

        long startTime  = System.currentTimeMillis();

        morphAdornerLogger.println ( "Merging_adorned" );

                                //  Merged adorned XML segments to
                                //  temporary file.

        File file   = File.createTempFile( "mad" , null );

        String tempFileName = file.getAbsolutePath();

        mergeXML( inputter , tempFileName );

                                //  Read the merged XML file to add
                                //  revised IDs and format the XML nicely.

        morphAdornerLogger.println
        (
            "Writing_merged" ,
            new Object[]{ outputFileName }
        );
                                //  Create XML writer.

        MorphAdornerXMLWriter xmlWriter =
            MorphAdornerXMLWriterFactory.newMorphAdornerXMLWriter
            (
                morphAdornerSettings.properties
            );
                                //  Write XML.
        xmlWriter.writeXML
        (
            tempFileName ,
            outputFileName ,
            runningWordID ,
            partOfSpeechTags ,
            splitWords ,
            totalWords ,
            totalPageBreaks ,
            this ,
            tokenizeOnly
        );
                                //  Delete temporary XML file.
                                //  May not work on some systems,
                                //  but the file will be deleted when
                                //  MorphAdorner exits anyway.

        FileUtils.deleteFile( tempFileName );

                                //  Report the updated XML has been
                                //  written out.

        morphAdornerLogger.println
        (
            "Adorned_XML_written" ,
            new Object[]
            {
                outputFileName ,
                MorphAdornerUtils.durationString
                (
                    morphAdornerSettings ,
                    startTime
                )
            }
        );
                                //  Close inputter.

        ((IsCloseableObject)inputter).close();

        inputter    = null;
        splitWords  = null;
        xmlWriter   = null;

        MorphAdornerUtils.logMemoryUsage
        (
            morphAdornerLogger ,
            "After completing " + inputFileName + ": "
        );
    }

    /** Print words in DOM document.
     *
     *  @param  document    The DOM document containing words to print.
     *
     *  <p>
     *  The text of <w> and <pc> elements is printed.
     *  </p>
     */

    protected void printWords( Document document )
    {
        NodeList nl =
            DOMUtils.getNodesByTagName
            (
                document ,
                new String[]{ "w" , "pc" }
            );

        if ( nl == null )
        {
            System.out.println( "printWords: null node list found" );
            return;
        }

        int numWords    = nl.getLength();

        for ( int i = 0 ; i < numWords ; i++ )
        {
            Node w  = nl.item(i);

            NamedNodeMap nodeMap    = w.getAttributes();

            String id   = "";

            if ( nodeMap != null )
            {
                Node idNode = nodeMap.getNamedItem( "xml:id" );

                if ( idNode != null )
                {
                    id  = idNode.getTextContent();
                }
            }

            System.out.println
            (
                w.getNodeName() + " " +
                id + " " +
                w.getTextContent() + " " +
                inSideText( w )
            );
        }
    }

    /** Fix abbreviations in side text.
     *
     *  @param  document            DOM document containing words to fix.
     *  @param  sideAbbreviations   Abbreviations list for side text.
     */

    protected void fixSideWords
    (
        Document document ,
        Abbreviations sideAbbreviations
    )
    {
        NodeList nl =
            DOMUtils.getNodesByTagName
            (
                document ,
                new String[]{ "w" , "pc" }
            );

        if ( nl == null )
        {
            System.out.println( "fixSideWords: null node list found" );
            return;
        }

        for ( int i = nl.getLength() - 1 ; i >= 0 ; i-- )
        {
            Element w   = (Element)nl.item( i );

            if ( inSideText( w ) )
            {
                String wText    = w.getTextContent();

                if ( wText.equals( "." ) )
                {
                    Element prevW   = (Element)nl.item( i - 1 );

                    String mergedWord   =
                        prevW.getTextContent() + wText;

                    if ( sideAbbreviations.isKnownAbbreviation( mergedWord ) )
                    {
                        String id1  = prevW.getAttribute( "xml:id" );
                        String id2  = w.getAttribute( "xml:id" );
                        String eos  = w.getAttribute( "eos" );

                        if ( eos == null )
                        {
                            eos = "0";
                        }
/*
                        System.out.println
                        (
                            "Merge id " + id1 +
                            " (" + prevW.getTextContent() + ")" +
                            " and id " + id2 +
                            " (" + w.getTextContent() + ")" +
                            " to " +
                            mergedWord + " , eos=" + eos
                        );
*/
                        prevW.setTextContent( mergedWord );

                        if ( eos.equals( "1" ) )
                        {
                            w.setAttribute( "eos" , "1" );
/*
                            System.out.println
                            (
                                "Add -eos to " + id1
                            );

*/                      }

                        w.getParentNode().removeChild( w );
                    }
                }
            }
        }
    }

    /** Is element in side text.
     *
     *  @param  element     Element.
     *
     *  @return             True for side text, false otherwise.
     */

    protected boolean inSideText( Node element )
    {
        boolean result  = false;

        String name     = element.getNodeName();

        if ( tagClassifier.isSideTextTag( name ) )
        {
            result  = true;
        }
        else
        {
            Node parent = element.getParentNode();

            while
            (
                !result && ( parent != null ) &&
                ( parent.getNodeType() != Node.TEXT_NODE )
            )
            {
                result  =
                    result ||
                    tagClassifier.isSideTextTag( parent.getNodeName() );

                if ( !result )
                {
                    parent  = parent.getParentNode();
                }
            }
        }

        return result;
    }

    /** Generate output file name for adorned output.
     *
     *  @param      inputFileName   The input file name.
     *
     *  @return                     The output file name.
     *
     *  @throws     IOException if output directory cannot be created.
     */

    public String getOutputFileName( String inputFileName )
        throws IOException
    {
        String result   =
            FileNameUtils.stripPathName( inputFileName );

        result          =
            new File
            (
                morphAdornerSettings.outputDirectoryName ,
                result
            ).getPath();

        if ( !FileUtils.createPathForFile( result ) )
        {
            throw new IOException
            (
                morphAdornerSettings.getString
                (
                    "Unable_to_create_output_directory"
                )
            );
        };

        result  = FileNameUtils.createVersionedFileName( result );

        return result;
    }

    /** Check if output file name for adorned output already exists.
     *
     *  @param      inputFileName   The input file name.
     *
     *  @return                     True if output file name already exists.
     */

    public boolean doesOutputFileNameExist( String inputFileName )
    {
        String outputFileName   =
            FileNameUtils.stripPathName( inputFileName );

        return
            new File
            (
                morphAdornerSettings.outputDirectoryName ,
                outputFileName
            ).exists();
    }

    /** Perform word adornment processes for a single input file.
     *
     *  @param  fileName    Input file name.
     *
     *  @throws             Exception if an error occurs.
     */

    public AdornedWordOutputter adornFile( String fileName )
        throws IOException
    {
        morphAdornerLogger.println( "Tagging" , new Object[]{ fileName } );

                                //  Get URL for file name.

        URL fileURL = URLUtils.getURLFromFileNameOrURL( fileName );

                                //  Report error if URL bad.
        if ( fileURL == null )
        {
            morphAdornerLogger.println
            (
                "Bad_file_name_or_URL" ,
                new Object[]{ fileName }
            );

            return null;
        }
                                //  Read file text into a string.
                                //  Report error if we cannot.

        String fileText = "";

        long startTime  = System.currentTimeMillis();

        try
        {
                                //  Create text inputter.

            TextInputter inputter   =
                TextInputterFactory.newTextInputter
                (
                    morphAdornerSettings.properties
                );
                                //  Enable gap fixer.

            inputter.enableGapFixer( morphAdornerSettings.fixGapTags );

                                //  Enable orig fixer.

            inputter.enableOrigFixer( morphAdornerSettings.fixOrigTags );

                                //  Load text to adorn.

            inputter.loadText
            (
                fileURL ,
                "utf-8" ,
                morphAdornerSettings.xmlSchema
            );
                                //  Get text.

            fileText    = inputter.getSegmentText( 0 );

            ((IsCloseableObject)inputter).close();
        }
        catch ( Exception e )
        {
            morphAdornerLogger.println
            (
                "Unable_to_read_text" ,
                new Object[]{ fileName }
            );

            return null;
        }

        morphAdornerLogger.println
        (
            "Loaded_text" ,
            new Object[]
            {
                fileName ,
                MorphAdornerUtils.durationString
                (
                    morphAdornerSettings ,
                    startTime
                )
            }
        );

        return adornText( fileText , fileURL );
    }

    /** Perform word adornment processes for a single input file.
     *
     *  @param  textToAdorn     Text to adorn.
     *  @param  outputURL       URL for output.
     *
     *  @throws                 Exception if an error occurs.
     */

    public AdornedWordOutputter adornText
    (
        String textToAdorn ,
        URL outputURL
    )
        throws IOException
    {
        long startTime  = System.currentTimeMillis();

                                //  Get a sentence splitter.

        SentenceSplitter sentenceSplitter   =
            SentenceSplitterFactory.newSentenceSplitter
            (
                morphAdornerSettings.properties
            );
                                //  Set logger into splitter.

        ((UsesLogger)sentenceSplitter).setLogger(
            morphAdornerLogger.getLogger() );

                                //  Set guesser into splitter.

        sentenceSplitter.setPartOfSpeechGuesser( partOfSpeechGuesser );

                                //  Set abbreviations into splitter.

        sentenceSplitter.setAbbreviations( abbreviations );

                                //  Get a word tokenizer.

        WordTokenizer wordTokenizer =
            WordTokenizerFactory.newWordTokenizer
            (
                morphAdornerSettings.properties
            );
                                //  Set a pretokenizer into the word
                                //  tokenizer.

        wordTokenizer.setPreTokenizer
        (
            PreTokenizerFactory.newPreTokenizer
            (
                morphAdornerSettings.properties
            )
        );
                                //  Set abbreviations in word tokenizer.

        wordTokenizer.setAbbreviations( abbreviations );

                                //  Extract the sentences and
                                //  words in the sentences.

        List<List<String>> sentences    =
            sentenceSplitter.extractSentences( textToAdorn , wordTokenizer );

                                //  Get count of sentences and words.

        int[] wordAndSentenceCounts =
            MorphAdornerUtils.getWordAndSentenceCounts( sentences );

        int wordsToTag  = wordAndSentenceCounts[ 1 ];

        morphAdornerLogger.println
        (
            "Extracted_words" ,
            new Object[]
            {
                Formatters.formatIntegerWithCommas( wordsToTag ) ,
                Formatters.formatIntegerWithCommas( wordAndSentenceCounts[ 0 ] ) ,
                MorphAdornerUtils.durationString
                (
                    morphAdornerSettings ,
                    startTime
                )
            }
        );
                                //  See if we should use standard
                                //  spellings to help guess parts of
                                //  speech for unknow  words.

        if ( partOfSpeechGuesser != null )
        {
            partOfSpeechGuesser.setTryStandardSpellings(
                morphAdornerSettings.tryStandardSpellings );
        }
                                //  Can't output lemma without a
                                //  lemmatizer.

        boolean doOutputLemma   =
            morphAdornerSettings.outputLemma &&
            ( lemmatizer != null );

                                //  Can't output standard spelling
                                //  without a standardizer.

        boolean doOutputStandardSpelling    =
            morphAdornerSettings.outputStandardSpelling &&
                ( spellingStandardizer != null );

                                //  Must output original token if
                                //  internal XML handling used.

        boolean doOutputOriginalToken   =
            morphAdornerSettings.outputOriginalToken ||
            morphAdornerSettings.useXMLHandler;

                                //  Set word attribute names.

        morphAdornerSettings.setXMLWordAttributes
        (
            doOutputOriginalToken ,
            doOutputLemma ,
            doOutputStandardSpelling
        );
                                //  Get part of speech tags for
                                //  each word in each sentence.

        startTime       = System.currentTimeMillis();

        List<List<AdornedWord>> result  = tagger.tagSentences( sentences );

        double elapsed  =
            ( System.currentTimeMillis() - startTime );

        int taggingRate = (int)( ( wordsToTag / elapsed ) * 1000.0D );

        morphAdornerLogger.println
        (
            "Tagging_complete" ,
            new Object[]
            {
                MorphAdornerUtils.durationString
                (
                    morphAdornerSettings ,
                    startTime
                ) ,
                Formatters.formatIntegerWithCommas( taggingRate )
            }
        );
                                //  Generate tagged word output.

        morphAdornerLogger.println( "Generating_other_adornments" );

        startTime   = System.currentTimeMillis();

                                //  Create a tagged text output writer.

        AdornedWordOutputter outputter  =
            AdornedWordOutputterFactory.newAdornedWordOutputter
            (
                morphAdornerSettings.properties
            );

        outputter.setWordAttributeNames
        (
            morphAdornerSettings.getXMLWordAttributes()
        );

        if ( outputURL != null )
        {
            outputter.createOutputFile
            (
                getOutputFileName
                (
                    URLUtils.getFileNameFromURL
                    (
                        outputURL ,
                        morphAdornerSettings.outputDirectoryName
                    )
                ) ,
                "utf-8" ,
                '\t'
            );
        }
        else
        {
            File file   = File.createTempFile( "mad" , null );

            String tempFileName = file.getAbsolutePath();

            outputter.createOutputFile
            (
                 tempFileName ,
                "utf-8" ,
                '\t'
            );
        }
                                //  Figure out what we are to output.

        int sentenceNumber      = 0;
        int wordNumber          = 0;

        String lemma                = "";
        String correctedSpelling    = "";
        String standardizedSpelling = "";
        AdornedWord adornedWord;
        String sSentenceNumber      = "";
        String sWordNumber          = "";
        String eosFlag              = "";
        String originalToken        = "";
        String partOfSpeechTag      = "";
        String xmlSurroundMarker    =
            morphAdornerSettings.xgOptions.getSurroundMarker().trim();

                                //  Get undetermined part of speech tag.

        String undeterminedPosTag   = partOfSpeechTags.getUndeterminedTag();

                                //  Holds output for each word.

        List<String> outputAdornments   = ListFactory.createNewList();

                                //  Output the tagged words.

        Iterator<List<AdornedWord>> iterator    = result.iterator();

                                //  Loop over tagged sentences.

        while ( iterator.hasNext() )
        {
                                //  Get next sentence.

            List<AdornedWord> sentenceFromTagger    = iterator.next();

                                //  Get sentence number.

            sentenceNumber++;
            sSentenceNumber = sentenceNumber + "";

            int sentenceSizeM1  = sentenceFromTagger.size() - 1;

                                //  Reset word numbers for each
                                //  sentence if running word numbers
                                //  not requested.

            if ( !morphAdornerSettings.outputRunningWordNumbers )
            {
                wordNumber  = 0;
            }
                                //  Loop over words in the sentence.

            for ( int j = 0 ; j < sentenceFromTagger.size() ; j++ )
            {
                                //  Clear output list for this word.

                outputAdornments.clear();

                                //  Add sentence number to output.

                if ( morphAdornerSettings.outputSentenceNumber )
                {
                    outputAdornments.add( sSentenceNumber );
                }
                                //  Add word number to output.

                wordNumber++;

                if ( morphAdornerSettings.outputWordNumber )
                {
                    sWordNumber = wordNumber + "";
                    outputAdornments.add( sWordNumber );
                }
                                //  Get the word and its part of speech
                                //  tag.

                adornedWord = sentenceFromTagger.get( j );

                                //  Add original spelling to output.

                originalToken   = adornedWord.getToken();

                if ( doOutputOriginalToken )
                {
                    outputAdornments.add( originalToken );
                }
                                //  Add corrected spelling to output.

                correctedSpelling       = adornedWord.getSpelling();

                standardizedSpelling    =
                    adornedWord.getStandardSpelling();

                if ( morphAdornerSettings.outputSpelling )
                {
                    outputAdornments.add( correctedSpelling );
                }
                                //  Get part of speech to output.

                partOfSpeechTag = adornedWord.getPartsOfSpeech();

                                //  Get standardized spelling to output.

                if ( doOutputStandardSpelling )
                {
                    standardizedSpelling    =
                        MorphAdornerUtils.getStandardizedSpelling
                        (
                            this ,
                            correctedSpelling ,
                            standardizedSpelling ,
                            partOfSpeechTag
                        );

                    if ( spellingMapper != null )
                    {
                        standardizedSpelling    =
                            spellingMapper.mapSpelling(
                                standardizedSpelling );
                    }
                }
                                //  Get lemma to output.

                if ( doOutputLemma )
                {
                                //  Try lexicon first unless we're ignoring
                                //  lemma entries in the lexicon.

                    if ( !morphAdornerSettings.ignoreLexiconEntriesForLemmatization )
                    {
                        lemma   =
                            wordLexicon.getLemma
                            (
                                correctedSpelling ,
                                partOfSpeechTag
                            );
                    }
                    else
                    {
                        lemma   = "*";
                    }
                                //  Lemma not found in word lexicon,
                                //  or the number of lemma parts does
                                //  not match the number of parts of speech.
                                //  Use lemmatizer.

                    if ( lemmatizer != null )
                    {
                        if  (   lemma.equals( "*" ) ||
                                ( partOfSpeechTags.countTags( partOfSpeechTag ) !=
                                  lemmatizer.countLemmata( lemma ) )
                            )
                        {
                            if ( standardizedSpelling.length() > 0 )
                            {
                                lemma   =
                                    MorphAdornerUtils.getLemma
                                    (
                                        this ,
                                        standardizedSpelling ,
                                        partOfSpeechTag
                                    );
                            }
                            else
                            {
                                lemma   =
                                    MorphAdornerUtils.getLemma
                                    (
                                        this ,
                                        correctedSpelling ,
                                        partOfSpeechTag
                                    );
                            }
                        }
                    }
                                //  Force lemma to lowercase except
                                //  for proper noun tagged word.

                    if ( lemma.indexOf( lemmaSeparator ) < 0 )
                    {
                        if ( !partOfSpeechTags.isProperNounTag(
                            partOfSpeechTag ) )
                        {
                            lemma   = lemma.toLowerCase();
                        }
                    }
                }
                                //  Rectify # of individual lemmata
                                //  with # of parts of speech for word.
                                //  If they don't match, set the
                                //  parts of speech to undetermined,
                                //  the standard spelling to the
                                //  original spelling,
                                //  and the lemma to the lowercase
                                //  original spelling.

                if ( lemmatizer != null )
                {
                    if  (   partOfSpeechTags.countTags( partOfSpeechTag ) !=
                            lemmatizer.countLemmata( lemma )
                        )
                    {
                        partOfSpeechTag = undeterminedPosTag;
                    }

                    if  (   partOfSpeechTag.equals( undeterminedPosTag ) ||
                            ( lemma.length() == 0 )
                        )
                    {
                        lemma                   =
                            correctedSpelling.toLowerCase();

                        standardizedSpelling    = correctedSpelling;
                        partOfSpeechTag         = undeterminedPosTag;
                    }
                }
                                //  Output rectified part of speech,
                                //  lemma, and standard spelling.

                if ( morphAdornerSettings.outputPartOfSpeech )
                {
                    outputAdornments.add( partOfSpeechTag );
                }

                if ( doOutputStandardSpelling )
                {
                    outputAdornments.add( standardizedSpelling );
                }

                if ( doOutputLemma )
                {
                    outputAdornments.add( lemma );
                }
                                //  Add end of sentence flag.

                if  (   morphAdornerSettings.outputEOSFlag ||
                        morphAdornerSettings.useXMLHandler
                    )
                {
                    if ( morphAdornerSettings.useXMLHandler )
                    {
                        eosFlag = "0";

                        if ( j < sentenceSizeM1 )
                        {
                            AdornedWord nextAdornedWord =
                                sentenceFromTagger.get( j + 1 );

                            if  ( nextAdornedWord.getToken().equals(
                                    xmlSurroundMarker )
                                )
                            {
                                if  (   originalToken.endsWith( "." ) ||
                                        originalToken.endsWith( "!" ) ||
                                        originalToken.endsWith( "?" ) ||
                                        originalToken.endsWith( "'" ) ||
                                        originalToken.endsWith( "\"" ) ||
                                        originalToken.endsWith(
                                            CharUtils.RSQUOTE_STRING ) ||
                                        originalToken.endsWith(
                                            CharUtils.RDQUOTE_STRING  ) ||
                                        originalToken.endsWith( "}" ) ||
                                        originalToken.endsWith( "]" ) ||
                                        originalToken.endsWith( ")" )
                                    )
                                {
                                    eosFlag = "1";
                                }
                            }
                        }
                        else
                        {
                            eosFlag = "1";
                        }
                    }
                    else
                    {
                        eosFlag = ( j >= sentenceSizeM1 ) ? "1" : "0";
                    }

                    outputAdornments.add( eosFlag );
                }
                                //  Add KWIC window to output.

                if ( morphAdornerSettings.outputKWIC )
                {
                    String[] kwics      =
                        MorphAdornerUtils.getKWIC
                        (
                            (List<AdornedWord>)sentenceFromTagger ,
                            j ,
                            morphAdornerSettings.outputKWICWidth
                        );

                    outputAdornments.add( kwics[ 0 ] );
                    outputAdornments.add( kwics[ 2 ] );
                }

                outputter.outputWordAndAdornments( outputAdornments );
            }
        }

        outputter.close();

        if ( outputURL != null )
        {
            morphAdornerLogger.println
            (
                "Adornments_written_to" ,
                new Object[]
                {
                    getOutputFileName
                    (
                        URLUtils.getFileNameFromURL
                        (
                            outputURL ,
                            morphAdornerSettings.outputDirectoryName
                        )
                    ) ,
                    MorphAdornerUtils.durationString
                    (
                        morphAdornerSettings ,
                        startTime
                    )
                }
            );
        }
        else
        {
            morphAdornerLogger.println
            (
                "Adornments_generated" ,
                new Object[]
                {
                    MorphAdornerUtils.durationString
                    (
                        morphAdornerSettings ,
                        startTime
                    )
                }
            );
        }

        sentences.clear();
        result.clear();

        sentences   = null;
        result      = null;

        return outputter;
    }

    /** Readorn adorned XML file.
     *
     *  @param  inputFileName   Input XML file name.
     *
     *  @throws SAXException
     */

    public void readorn( String inputFileName )
        throws SAXException, IOException, FileNotFoundException
    {
        morphAdornerLogger.println( "Loading_previously_adorned" );

        long startTime  = System.currentTimeMillis();

                                //  Create filter to strip
                                //  non-essential word attributes
                                //  from word elements in the adorned
                                //  file.

        StripWordAttributesFilter stripFilter   =
            new StripWordAttributesFilter
            (
                XMLReaderFactory.createXMLReader()
            );

        ExtendedAdornedWordFilter wordInfoFilter    =
            new ExtendedAdornedWordFilter( stripFilter );

        File file   = File.createTempFile( "mad" , null );

        String tempFileName = file.getAbsolutePath();

                                //  Run the attribute-stripping filter
                                //  and gather the stripped words
                                //  into a list of sentences.
        new FilterAdornedFile
        (
            inputFileName ,
            tempFileName ,
            wordInfoFilter
        );
                                //  Get sentences.

        List<List<ExtendedAdornedWord>> sentences   =
            wordInfoFilter.getSentences();

                                //  Report words loaded.

        morphAdornerLogger.println
        (
            "Loaded_existing_words" ,
            new Object[]
            {
                Formatters.formatIntegerWithCommas
                (
                    wordInfoFilter.getNumberOfWords()
                ) ,
                Formatters.formatIntegerWithCommas
                (
                    sentences.size()
                ) ,
                MorphAdornerUtils.durationString
                (
                    morphAdornerSettings ,
                    startTime
                )
            }
        );
                                //  Disable retagger's ability to
                                //  add or delete words.

        boolean savedRetaggerAddOrDelete = retagger.getCanAddOrDeleteWords();

        if ( retagger != null )
        {
            retagger.setCanAddOrDeleteWords( false );
        }
                                //  Retag the sentences.

        startTime       = System.currentTimeMillis();

        tagger.tagAdornedWordSentences
        (
            sentences ,
            stripFilter.getRegIDSet()
        );
                                //  Restore saved retagger add or delete
                                //  words state.

        if ( retagger != null )
        {
            retagger.setCanAddOrDeleteWords( savedRetaggerAddOrDelete );
        }
                                //  Display tagging rate.
        double elapsed  =
            ( System.currentTimeMillis() - startTime );

        int taggingRate =
            (int)( ( wordInfoFilter.getNumberOfWords() / elapsed ) * 1000.0D );

        morphAdornerLogger.println
        (
            "Tagging_complete" ,
            new Object[]
            {
                MorphAdornerUtils.durationString
                (
                    morphAdornerSettings ,
                    startTime
                ) ,
                Formatters.formatIntegerWithCommas( taggingRate )
            }
        );
                                //  Generate other adornments.

        morphAdornerLogger.println( "Generating_other_adornments" );

        startTime   = System.currentTimeMillis();

        updateAdornedSentences( sentences , stripFilter.getRegIDSet() );

                                //  Copy adornments to all parts of
                                //  split words.

        updateSplitWordAdornments( wordInfoFilter );

                                //  Report adornments generated.

        morphAdornerLogger.println
        (
            "Adornments_generated" ,
            new Object[]
            {
                MorphAdornerUtils.durationString
                (
                    morphAdornerSettings ,
                    startTime
                )
            }
        );
                                //  Create filter to add updated
                                //  word attributes.

        AddWordAttributesFilter addFilter   =
            new AddWordAttributesFilter
            (
                XMLReaderFactory.createXMLReader() ,
                wordInfoFilter ,
                morphAdornerSettings
            );

        XMLFilter filter    = addFilter;

                                //  If we're adding pseudopages,
                                //  create a pseudopage adder filter.

        if ( morphAdornerSettings.outputPseudoPageBoundaryMilestones )
        {
            PseudoPageAdderFilter pseudoPageFilter  =
                new PseudoPageAdderFilter
                (
                    addFilter ,
                    morphAdornerSettings.pseudoPageSize ,
                    morphAdornerSettings.pseudoPageContainerDivTypes
                );

            filter  = pseudoPageFilter;
        }
                                //  Create name of output file to
                                //  which to write merged adorned XML.

        String outputFileName   = getOutputFileName( inputFileName );

        morphAdornerLogger.println
        (
            "Writing_merged" ,
            new Object[]{ outputFileName }
        );

        startTime   = System.currentTimeMillis();

        new FilterAdornedFile( tempFileName , outputFileName , filter );

        morphAdornerLogger.println
        (
            "Adorned_XML_written" ,
            new Object[]
            {
                outputFileName ,
                MorphAdornerUtils.durationString
                (
                    morphAdornerSettings ,
                    startTime
                )
            }
        );
                                //  Delete temporary XML file.
                                //  May not work on some systems,
                                //  but the file will be deleted when
                                //  MorphAdorner exits anyway.

        FileUtils.deleteFile( tempFileName );
    }

    /** Adorn a list of sentences containing adorned words.
     *
     *  @param  sentences   Previously adorned sentences to readorn.
     *  @param  regIDSet    Word IDs of words with preset standard spellings.
     */

    public void updateAdornedSentences
    (
        List<List<ExtendedAdornedWord>> sentences ,
        Set<String> regIDSet
    )
    {
                                //  Loop over sentences.

        for ( int i = 0 ; i < sentences.size() ; i++ )
        {
                                //  Update adornments in next sentence.

            updateAdornedSentence( sentences.get( i ) , regIDSet );
        }
    }

    /** Update adornments for split words.
     *
     *  @param  wordFilter  ExtendedAdornedWordFilter with words to update.
     */

    protected void updateSplitWordAdornments
    (
        ExtendedAdornedWordFilter wordFilter
    )
    {
                                //  Get word list of all words.

        List<String> wordIDs    = wordFilter.getAdornedWordIDs();

                                //  Loop over all words looking for
                                //  split words.

        for ( int i = 0 ; i < wordIDs.size() ; i++ )
        {
                                //  Get next adorned word.

            ExtendedAdornedWord adornedWord =
                wordFilter.getExtendedAdornedWord( wordIDs.get( i ) );

                                //  Check if this is the first part of
                                //  a split word.

            if ( adornedWord.isSplitWord() && adornedWord.isFirstPart() )
            {
                                //  Get adorned word's ID.

                String id   = adornedWord.getID();

                                //  Get the related word IDs.

                List<String> relatedIDs =
                    wordFilter.getRelatedSplitWordIDs( id );

                                //  Set adornments in related words
                                //  to the values in the first word part.

                for ( int j = 0 ; j < relatedIDs.size() ; j++ )
                {
                    ExtendedAdornedWord relatedWord =
                        wordFilter.getExtendedAdornedWord
                        (
                            relatedIDs.get( j )
                        );

                    relatedWord.setPartsOfSpeech
                    (
                        adornedWord.getPartsOfSpeech()
                    );

                    relatedWord.setLemmata( adornedWord.getLemmata() );

                    relatedWord.setSpelling
                    (
                        adornedWord.getSpelling()
                    );

                    relatedWord.setStandardSpelling
                    (
                        adornedWord.getStandardSpelling()
                    );
                }
            }
        }
    }

    /** Adorn a list of sentences containing adorned words.
     *
     *  @param  sentence    Previously adorned sentence to update.
     *  @param  regIDSet    Word IDs of words with preset standard spellings.
     */

    public void updateAdornedSentence
    (
        List<ExtendedAdornedWord> sentence ,
        Set<String> regIDSet
    )
    {
        String lemma                = "";
        String correctedSpelling    = "";
        String standardizedSpelling = "";
        ExtendedAdornedWord adornedWord;
        String originalToken        = "";
        String partOfSpeechTag      = "";
        String id                   = "";

                                //  Update adornments for each word
                                //  in sentence.

        for ( int j = 0 ; j < sentence.size() ; j++ )
        {
                                //  Get next word in sentence.

            adornedWord             = sentence.get( j );

                                //  Update standard spelling.

            id                      = adornedWord.getID();
            originalToken           = adornedWord.getToken();
            correctedSpelling       = adornedWord.getSpelling();
            standardizedSpelling    = adornedWord.getStandardSpelling();
            partOfSpeechTag         = adornedWord.getPartsOfSpeech();

            standardizedSpelling    =
                MorphAdornerUtils.getStandardizedSpelling
                (
                    this ,
                    ( regIDSet.contains( id ) ?
                        standardizedSpelling : correctedSpelling ) ,
                    standardizedSpelling ,
                    partOfSpeechTag
                );

            if ( spellingMapper != null )
            {
                standardizedSpelling    =
                    spellingMapper.mapSpelling
                    (
                        standardizedSpelling
                    );
            }

            adornedWord.setStandardSpelling( standardizedSpelling );

                                //  Update lemma.

            if ( !morphAdornerSettings.ignoreLexiconEntriesForLemmatization )
            {
                lemma   =
                    wordLexicon.getLemma
                    (
                        correctedSpelling ,
                        partOfSpeechTag
                    );
            }
            else
            {
                lemma   = "*";
            }
                                //  Lemma not found in word lexicon.
                                //  Use lemmatizer.

            if  (   lemma.equals( "*" ) && ( lemmatizer != null ) )
            {
                if ( standardizedSpelling.length() > 0 )
                {
                    lemma   =
                        MorphAdornerUtils.getLemma
                        (
                            this ,
                            standardizedSpelling ,
                            partOfSpeechTag
                        );
                }
                else
                {
                    lemma   =
                        MorphAdornerUtils.getLemma
                        (
                            this ,
                            correctedSpelling ,
                            partOfSpeechTag
                        );
                }
            }
                                //  Force lemma to lowercase except
                                //  for proper noun tagged word.

            if ( lemma.indexOf( lemmaSeparator ) < 0 )
            {
                if ( !partOfSpeechTags.isProperNounTag(
                    partOfSpeechTag ) )
                {
                    lemma   = lemma.toLowerCase();
                }
            }

            adornedWord.setLemmata( lemma );
        }
    }

    /** Add abbreviations.
     *
     *  @param  abbreviationsURL    Abbreviations URL.
     *  @param  loadedMessage       Message to display when words loaded.
     */

    public void addAbbreviations
    (
        Abbreviations abbreviations ,
        String abbreviationsURL ,
        String loadedMessage
    )
    {
        long startTime      = System.currentTimeMillis();

        int currentCount    = abbreviations.getAbbreviationsCount();

                                //  Add abbreviations.

        abbreviations.loadAbbreviations( abbreviationsURL );

                                //  Report number added.

        int added   = abbreviations.getAbbreviationsCount() - currentCount;

        morphAdornerLogger.println
        (
            loadedMessage ,
            new Object[]
            {
                Formatters.formatIntegerWithCommas( added ) ,
                MorphAdornerUtils.durationString
                (
                    morphAdornerSettings ,
                    startTime
                )
            }
        );
    }

    /** Merge xml fragments into one xml file.
     */

    protected static void mergeXML
    (
        TextInputter inputter ,
        String xmlFileName
    )
    {
        try
        {
                                //  Open output file.

            FileOutputStream outputStream       =
                new FileOutputStream( new File( xmlFileName ) , false );

            BufferedOutputStream bufferedStream =
                new BufferedOutputStream( outputStream );

            OutputStreamWriter writer           =
                new OutputStreamWriter( bufferedStream , "utf-8" );

                                //  Get list of text entries sorted
                                //  by entry name,

            SortedArrayList<String> entryNames  =
                new SortedArrayList<String>();

            int nEntries    = inputter.getSegmentCount();

            for ( int i = 0 ; i < nEntries ; i++ )
            {
                entryNames.add( inputter.getSegmentName( i ) );
            }
                                //  Write out entries in order.

            String endText  = "";
            String entryName;
            String entryText;

            for ( int i = 0 ; i < entryNames.size() ; i++ )
            {
                entryName   = entryNames.get( i ).toString();
                entryText   = inputter.getSegmentText( entryName );

                                //  Split "head.xml" as needed.

                if ( entryName.equals( "head" ) )
                {
                                //  Look for "</eebo" or "</TEI"
                                //  and split off
                                //  ending string at this point.
                                //  Add it to the end of the output
                                //  later.

                    int iPos    =
                        StringUtils.indexOfIgnoreCase(
                            entryText , "</eebo" );

                    if ( iPos < 0 )
                    {
                        iPos    = entryText.indexOf( "</TEI" );
                    }

                    if ( iPos < 0 )
                    {
                        iPos    = entryText.indexOf( "</tei." );
                    }

                    if ( iPos >= 0 )
                    {
                        endText     = entryText.substring( iPos );
                        entryText   = entryText.substring( 0 , iPos );
                    }
                }
                                //  Split "text.xml" as needed.
                                //  First part output now, rest
                                //  prepended to ending string and
                                //  added to the end of the output later.

                else if ( entryName.equals( "text" ) )
                {
                    entryText   = entryText.trim();

                    entryText   =
                        StringUtils.replaceAll( entryText , "/>" , ">" );

                    if ( entryText.startsWith( "<group" ) )
                    {
                        endText = "</group>" + endText;
                    }
                    else if ( entryText.startsWith( "<GROUP" ) )
                    {
                        endText = "</GROUP>" + endText;
                    }
                    else if ( entryText.startsWith( "<text" ) )
                    {
                        endText = "</text>" + endText;
                    }
                    else
                    {
                        endText = "</TEXT>" + endText;
                    }

                    if  (   entryText.endsWith( "</text>" ) ||
                            entryText.endsWith( "</TEXT>" )
                        )
                    {
                        entryText   =
                            entryText.substring
                            (
                                0 ,
                                entryText.length() - 7
                            );
                    }
                }
                                //  Change trailing " >" to ">".

                while ( entryText.endsWith( " >" ) )
                {
                    entryText   =
                        entryText.substring(
                            0 , entryText.length() - 2 ) + ">";
                }
                                //  Write out XML portion.
                writer.write
                (
                    entryText ,
                    0 ,
                    entryText.length()
                );
            }
                                //  Output accumulated end text.

            endText = StringUtils.replaceAll( endText , " >" , ">" );

            writer.write( endText , 0 , endText.length() );

                                //  Close XML output file.
            writer.close();
            bufferedStream.close();
            outputStream.close();

            writer          = null;
            bufferedStream  = null;
            outputStream    = null;
            entryNames      = null;

            endText         = null;
            entryName       = null;
            entryText       = null;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        System.gc();
    }

    /** Create and run a Morphological Adorner.
     *
     *  @param  args    Program arguments.
     */

    public static void main( String[] args )
    {
                                //  Create the adorner.

        MorphAdorner adorner    = new MorphAdorner( args );

                                //  Run adornment processes on specified
                                //  input files.

        if ( adorner.morphAdornerSettings.fileNames.length > 0 )
        {
            adorner.processInputFiles
            (
                adorner.morphAdornerSettings.tokenizeOnly
            );
        }
        else
        {
            adorner.morphAdornerLogger.println(
                "No_files_found_to_process" );
        }
                                //  Close down logging.

        adorner.morphAdornerLogger.terminate();
    }

    /** Create a Morphological Adorner.
     *
     *  @param  adornerName             Name for this adorner.
     *  @param  replaceAdorner          Replace existing adorner.
     *  @param  adornerArgs             Adorner arguments.
     *  @param  adornerLogConfig        Adorner log file configuration.
     *  @param  adornerLogDirectory     Adorner log directory.
     *
     *  @return     The adorner.
     */

    public static MorphAdorner createAdorner
    (
        String adornerName ,
        boolean replaceAdorner ,
        String[] adornerArgs ,
        String adornerLogConfig ,
        String adornerLogDirectory
    )
    {
                                //  Use existing adorner of the given
                                //  name if found.

        MorphAdorner adorner    = storedAdorners.get( adornerName );

                                //  No existing adorner of the given
                                //  name.  Create a new one.

        if ( replaceAdorner || ( adorner == null ) )
        {
            adorner =
                new MorphAdorner
                (
                    adornerArgs ,
                    adornerLogConfig ,
                    adornerLogDirectory
                );

            storedAdorners.put( adornerName , adorner );
        }
        else
        {
                                //  Recreate the wrapper logger for
                                //  use by MorphAdorner in case it's
                                //  closed.

            if ( !adorner.morphAdornerLogger.getLogger().isLoggerEnabled() )
            {
                adorner.morphAdornerLogger.setLogger
                (
                    adorner.morphAdornerLogger.createWrappedLogger
                    (
                        adornerLogConfig ,
                        adornerLogDirectory
                    )
                );
            }
        }
                                //  Return the adorner.
        return adorner;
    }

    /** Run a Morphological Adorner.
     *
     *  @param  adorner             The adorner to run.
     *  @param  outputDirectory     Adorned files output directory.
     *  @param  filesToAdorn        File names to adorn.
     *  @param  tokenizeOnly        Only tokenize XML files.
     *
     *  @return                     The adorner used.
     *
     *  <p>
     *  If the adorner specified is null, no processing is performed,
     *  and null is returned as the adorned used.
     *  </p>
     */

    public static MorphAdorner runAdorner
    (
        MorphAdorner adorner ,
        String outputDirectory ,
        String[] filesToAdorn ,
        boolean tokenizeOnly
    )
    {
                                //  Do nothing if the adorner is null.

        if ( adorner == null )
        {
            return null;
        }
                                //  Set output directory.

        adorner.morphAdornerSettings.outputDirectoryName    =
            outputDirectory;
                                //  Set input file names.

        adorner.morphAdornerSettings.fileNames  =
            FileNameUtils.expandFileNameWildcards( filesToAdorn );

                                //  Run adornment processes on specified
                                //  input files.

        if ( adorner.morphAdornerSettings.fileNames.length > 0 )
        {
            adorner.processInputFiles( tokenizeOnly );
        }
        else
        {
            adorner.morphAdornerLogger.println(
                "No_files_found_to_process" );
        }
                                //  Return the adorner that was used.
        return adorner;
    }

    /** Run a Morphological Adorner.
     *
     *  @param  adornerName         Name for this adorner.
     *  @param  outputDirectory     Adorned files output directory.
     *  @param  filesToAdorn        File names to adorn.
     *  @param  tokenizeOnly        Only tokenize XML files.
     *
     *  @return                     The adorner used.  Null if not found.
     *
     *  <p>
     *  If the requested adorner was not found, no processing is performed,
     *  and null is return as the adorner used.
     *  </p>
     */

    public static MorphAdorner runAdorner
    (
        String adornerName ,
        String outputDirectory ,
        String[] filesToAdorn ,
        boolean tokenizeOnly
    )
    {
        return
            runAdorner
            (
                storedAdorners.get( adornerName ) ,
                outputDirectory ,
                filesToAdorn ,
                tokenizeOnly
            );
    }

    /** Create and run a Morphological Adorner.
     *
     *  @param  adornerName             Name for this adorner.
     *  @param  replaceAdorner          Replace existing adorner.
     *  @param  adornerArgs             Adorner arguments.
     *  @param  adornerLogConfig        Adorner log file configuration.
     *  @param  adornerLogDirectory     Adorner log directory.
     *  @param  outputDirectory         Adorned files output directory.
     *  @param  filesToAdorn            File names to adorn.
     *  @param  tokenizeOnly            Only tokenize XML files.
     *
     *  @return     The adorner.
     */

    public static MorphAdorner createAndRunAdorner
    (
        String adornerName ,
        boolean replaceAdorner ,
        String[] adornerArgs ,
        String adornerLogConfig ,
        String adornerLogDirectory ,
        String outputDirectory ,
        String[] filesToAdorn ,
        boolean tokenizeOnly
    )
    {
                                //  Create the adorner.

        MorphAdorner adorner    =
            createAdorner
            (
                adornerName ,
                replaceAdorner ,
                adornerArgs ,
                adornerLogConfig ,
                adornerLogDirectory
            );
                                //  Run the adorned on the specified
                                //  files.
        return
            runAdorner
            (
                storedAdorners.get( adornerName ) ,
                outputDirectory ,
                filesToAdorn ,
                tokenizeOnly
            );
    }

    /** Finalize. */

    public void finalize()
        throws Throwable
    {
                                //  Close down logging.
        try
        {
            morphAdornerLogger.terminate();
        }
        catch ( Exception e )
        {
        }

        super.finalize();
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



