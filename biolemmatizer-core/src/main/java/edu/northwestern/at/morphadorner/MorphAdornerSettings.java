package edu.northwestern.at.morphadorner;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.net.*;
import java.util.*;

import edu.northwestern.at.morphadorner.xgtagger.*;
import edu.northwestern.at.utils.*;
import jargs.gnu.CmdLineParser;

/** Global settings for MorphAdorner.
 *
 *  <p>
 *  This class holds the static values of global settings for MorphAdorner.
 *  </p>
 *
 *  <p>
 *  The MorphAdorner resource bundle contains definitions for all the
 *  string constants used in MorphAdorner.  Once the resource strings have
 *  been initialized in the initializeSettings method you can access the
 *  strings by calling getString:
 *  </p>
 *
 *  <p>
 *  <code>
 *  String s    = MorphAdornerSettings.getString( "mystring" , "my string" );
 *  </code>
 *  </p>
 */

public class MorphAdornerSettings implements Serializable
{
    /** XML ID types. */

    public static enum XMLIDType
    {
        READING_CONTEXT_ORDER ,
        WORD_WITHIN_PAGE_BLOCK ,
        USE_PAGE_BLOCK_ATTRIBUTES
    };

    /** The resource strings. */

    protected ResourceBundle resourceBundle = null;

    /** Resource bundle path. */

    protected static String resourceName    =
        "edu.northwestern.at.morphadorner.resources.ma";

    /** The program name. */

    public String programTitle;

    /** The program version. */

    public String programVersion;

    /** The program banner (title and version number) */

    public String programBanner;

    /** Logger. */

    public MorphAdornerLogger morphAdornerLogger    = null;

    /** MorphAdorner configuration properties. */

    public UTF8Properties properties;

    /** XGTagger configuration properties. */

    public XGOptions xgOptions      = new XGOptions();

    /** True if debugging enabled. */

    public boolean debug    = false;

    /** True if MorphAdorner already initialized. */

    protected boolean initialized   = false;

    /** Output directory name. */

    public String outputDirectoryName= "adorned/";

    /** Word lexicon URL. */

    protected String wordLexiconURLString   = null;
    public URL wordLexiconURL               = null;

    /** Suffix lexicon URL. */

    protected String suffixLexiconURLString = null;
    public URL suffixLexiconURL             = null;

    /** Context rules URL. */

    protected String contextRulesURLString  = null;
    public URL contextRulesURL              = null;

    /** Lexical rules URL. */

    protected String lexicalRulesURLString  = null;
    public URL lexicalRulesURL              = null;

    /** Standard spellings URL. */

    protected String spellingsURLString     = null;
    public URL spellingsURL                 = null;

    /** Alternate spellings URLs. */

    protected String[] alternateSpellingsURLStrings = null;
    public URL[] alternateSpellingsURLs             = null;

    /** Alternate spellings by word class URL. */

    protected String[] alternateSpellingsByWordClassURLStrings  = null;
    public URL[] alternateSpellingsByWordClassURLs              = null;

    /** Transition matrix URL. */

    protected String transitionMatrixURLString  = null;
    public URL transitionMatrixURL              = null;

    /** MorphAdorner properties file URL. */

    protected String defaultPropertiesURLString =
        "morphadorner.properties";

    protected String propertiesURLString    = null;
    public URL propertiesURL                = null;

    /** File names to process. */

    public String[] fileNames           = null;

    /** Output sentence number. */

    public boolean outputSentenceNumber = false;

    /** Output sentence number attribute. */

    public String outputSentenceNumberAttribute =
        WordAttributeNames.sn;

    /** Output word ordinal attribute. */

    public String outputWordOrdinalAttribute    =
        WordAttributeNames.ord;

    /** Output word ordinal. */

    public boolean outputWordOrdinal        = false;

    /** Output word number. */

    public boolean outputWordNumber     = false;

    /** Output word number attribute. */

    public String outputWordNumberAttribute =
        WordAttributeNames.wn;

    /** Output running word number. */

    public boolean outputRunningWordNumbers = false;

    /** Output (corrected) spelling. */

    public boolean outputSpelling   = true;

    /** Output spelling attribute. */

    public String outputSpellingAttribute   =
        WordAttributeNames.spe;

    /** Output original token. */

    public boolean outputOriginalToken  = true;

    /** Output original token attribute. */

    public String outputOriginalTokenAttribute  =
        WordAttributeNames.tok;

    /** Output part of speech tag. */

    public boolean outputPartOfSpeech   = true;

    /** Output part of speech tag attribute. */

    public String outputPartOfSpeechAttribute   =
        WordAttributeNames.pos;

    /** Output lemma. */

    public boolean outputLemma  = true;

    /** Output lemma attribute. */

    public String outputLemmaAttribute  =
        WordAttributeNames.lem;

    /** Output standard spelling. */

    public boolean outputStandardSpelling   = true;

    /** Output standard spelling attribute. */

    public String outputStandardSpellingAttribute   =
        WordAttributeNames.reg;

    /** Output KWIC index. */

    public boolean outputKWIC   = false;

    /** Output left KWIC index attribute. */

    public String outputLeftKWICAttribute   =
        WordAttributeNames.kl;

    /** Output right KWIC index attribute. */

    public String outputRightKWICAttribute  =
        WordAttributeNames.kr;

    /** Number of characters in KWIC index entry. */

    public int outputKWICWidth  = 80;

    /** Output end of sentence flag. */

    public boolean outputEOSFlag    = true;

    /** Output end of sentence flag attribute. */

    public String outputEOSFlagAttribute    =
        WordAttributeNames.eos;

    /** XML doctype name for output. */

    public String xmlDoctypeName    = "";

    /** XML doctype system (DTD) for output. */

    public String xmlDoctypeSystem  = "";

    /** Use XGTagger-based XML handler. */

    public boolean useXMLHandler    = false;

    /** Ignore lemma in lexicon when lemmatizing. */

    public boolean ignoreLexiconEntriesForLemmatization = false;

    /** Try standard spellings when guessing parts of speech. */

    public boolean tryStandardSpellings = true;

    /** Use Latin word list. */

    public boolean useLatinWordList = true;

    /** Output whitespace elements. */

    public boolean outputWhitespaceElements = true;

    /** Output only non-redundant word level attributes. */

    public boolean outputNonredundantAttributesOnly = false;

    /** Output only non-redundant token attribute. */

    public boolean outputNonredundantTokenAttribute = false;

    /** Output only non-redundant part attribute. */

    public boolean outputNonredundantPartAttribute  = false;

    /** Output only non-redundant eos attribute. */

    public boolean outputNonredundantEosAttribute   = false;

    /** Output sentence boundary milestones. */

    public boolean outputSentenceBoundaryMilestones = false;

    /** Use PC element to mark end of sentence. */

    public boolean usePCToMarkEndOfSentence = false;

    /** Allow proper nouns to be lower case when part of speech tagging. */

    public boolean allowLowerCaseProperNouns = false;

    /** Fix <gap> elements in input text. */

    public boolean fixGapTags   = true;

    /** Fix <orig> elements in input text. */

    public boolean fixOrigTags  = true;

    /** Fix selected split words in input text. */

    public boolean fixSplitWords    = false;

    /** Fix split words pattern replacers. */

    public List<PatternReplacer> fixSplitWordsPatternReplacers  = null;

    /** Pseudo-page size. */

    public int pseudoPageSize   = 300;

    /** Output pseudo-page boundary milestones. */

    public boolean outputPseudoPageBoundaryMilestones   = false;

    /** List of pseudo-page ending div types. */

    public String pseudoPageContainerDivTypes   =
        "volume chapter sermon";

    /** Close sentence at end of hard tag. */

    public boolean closeSentenceAtEndOfHardTag  = false;

    /** Close sentence at end of jump tag. */

    public boolean closeSentenceAtEndOfJumpTag  = true;

    /** XML schema to use when parsing XML input files. */

    public String xmlSchema =
        "http://ariadne.northwestern.edu/monk/dtds/TEIAnalytics.rng";

    /** XML tags from which to remove word tags. */

    public String disallowWordElementsIn    = "figdesc";

    /** XML ID Type. */

    public XMLIDType xmlIDType  =
        XMLIDType.READING_CONTEXT_ORDER;

    /** XML ID spacing. */

    public int xmlIDSpacing = 10;

    /** Emit XML token label? */

    public boolean xmlTokenLabelEmit    = false;

    /** XML token label attribute. */

    public String xmlTokenLabelAttribute    = "n";

    /** XML token label spacing. */

    public int xmlTokenLabelSpacing = 10;

    /** XML token label prepend work name. */

    public boolean xmlTokenLabelPrependWorkName = false;

    /** XML word attributes. */

    public List<String> xmlWordAttributes   = ListFactory.createNewList();

    /** Abbreviations URL. */

    public String abbreviationsURL  = "";

    /** Abbreviations URL for main text. */

    public String abbreviationsMainTextURL  = "";

    /** Abbreviations URL for side text. */

    public String abbreviationsSideTextURL  = "";

    /** Adorn XML files with existing adorned version in output directory. */

    public boolean adornExistingXMLFiles    = true;

    /** Tokenize only:  Override other output than tokenization for XML. */

    public boolean tokenizeOnly = false;

    /** XGTagger configuration item names. */

    protected static final String STR_ID =
        "xml.id.attribute";

    protected static final String STR_ID_TYPE =
        "xml.id.type";

    protected static final String STR_ID_SPACING =
        "xml.id.spacing";

    protected static final String STR_TOKENLABEL_EMIT =
        "xml.tokenlabel.emit";

    protected static final String STR_TOKENLABEL_ATTRIBUTE =
        "xml.tokenlabel.attribute";

    protected static final String STR_TOKENLABEL_SPACING =
        "xml.tokenlabel.spacing";

    protected static final String STR_TOKENLABEL_PREPENDWORKNAME    =
        "xml.tokenlabel.prependworkname";

    protected static final String STR_LOG =
        "xml.log";

    protected static final String STR_WORD_PATH =
        "xml.word_path";

    protected static final String STR_TAGS_PATH =
        "xml.tags_path";

    protected static final String STR_FIELD_DELIMITERS =
        "xml.field_delimiters";

    protected static final String STR_WORD_DELIMITERS =
        "xml.word_delimiters";

    protected static final String STR_SURROUND_MARKER =
        "xml.surround_marker";

    protected static final String STR_WORD_FIELD =
        "xml.word_field";

    protected static final String STR_OUTPUT_FILE =
        "xml.output_file";

    protected static final String STR_ENTITIES_NOT_FILES =
        "xml.entities_not_files";

    protected static final String STR_ENTITIES_TREAT_ALL =
        "xml.entities_treat_all";

    protected static final String STR_ENTITIES_MERGE =
        "xml.entities_merge";

    protected static final String STR_RELATIVE_URI_BASE =
        "xml.relative_uri_base";

    protected static final String STR_REPEAT_ATTRIBUTES =
        "xml.repeat_attributes";

    protected static final String STR_JUMP_TAGS =
        "xml.jump_tags";

    protected static final String STR_SOFT_TAGS =
        "xml.soft_tags";

    protected static final String STR_PUNC_TAG_NAME =
        "xml.punc_tag_name";

    protected static final String STR_WORD_TAG_NAME =
        "xml.word_tag_name";

    protected static final String STR_SPECIAL_SEPARATOR =
        "xml.special_separator";

    protected static final String STR_IGNORE_TAG_CASE   =
        "xml.ignore_tag_case";

    protected static final String STR_DOCTYPE_NAME  =
        "xml.doctype.name";

    protected static final String STR_DOCTYPE_SYSTEM    =
        "xml.doctype.system";

    protected static final String STR_OUTPUT_WHITESPACE_ELEMENTS    =
        "xml.output_whitespace_elements";

    protected static final String STR_OUTPUT_NONREDUNDANT_ATTRIBUTES_ONLY   =
        "xml.output_nonredundant_attributes_only";

    protected static final String STR_OUTPUT_NONREDUNDANT_TOKEN_ATTRIBUTE   =
        "xml.output_nonredundant_token_attribute";

    protected static final String STR_OUTPUT_NONREDUNDANT_PART_ATTRIBUTE    =
        "xml.output_nonredundant_part_attribute";

    protected static final String STR_OUTPUT_NONREDUNDANT_EOS_ATTRIBUTE =
        "xml.output_nonredundant_eos_attribute";

    protected static final String STR_OUTPUT_SENTENCE_BOUNDARY_MILESTONES   =
        "xml.output_sentence_boundary_milestones";

    protected static final String STR_USE_PC_TO_MARK_END_OF_SENTENCE    =
        "xml.use_pc_to_mark_end_of_sentence";

    protected static final String STR_FIX_GAP_TAGS  =
        "xml.fix_gap_tags";

    protected static final String STR_FIX_ORIG_TAGS =
        "xml.fix_orig_tags";

    protected static final String STR_FIX_SPLIT_WORDS   =
        "xml.fix_split_words";

    protected static final String STR_PSEUDO_PAGE_SIZE  =
        "xml.pseudo_page_size";

    protected static final String STR_OUTPUT_PSEUDO_PAGE_BOUNDARY_MILESTONES    =
        "xml.output_pseudo_page_boundary_milestones";

    protected static final String STR_PSEUDO_PAGE_CONTAINER_DIV_TYPES   =
        "xml.pseudo_page_container_div_types";

    protected static final String STR_CLOSE_SENTENCE_AT_END_OF_HARD_TAG =
        "xml.close_sentence_at_end_of_hard_tag";

    protected static final String STR_CLOSE_SENTENCE_AT_END_OF_JUMP_TAG =
        "xml.close_sentence_at_end_of_jump_tag";

    protected static final String STR_XMLSCHEMA =
        "xml.xml_schema";

    protected static final String STR_WORD_LEXICON  =
        "lexicon.word_lexicon";

    protected static final String STR_SUFFIX_LEXICON    =
        "lexicon.suffix_lexicon";

    protected static final String STR_CONTEXT_RULES =
        "partofspeechtagger.context_rules";

    protected static final String STR_LEXICAL_RULES =
        "partofspeechtagger.lexical_rules";

    protected static final String STR_STANDARD_SPELLINGS    =
        "spelling.standard_spellings";

    protected static final String STR_SPELLING_PAIRS    =
        "spelling.spelling_pairs";

    protected static final String STR_SPELLING_PAIRS_BY_WORD_CLASS =
        "spelling.spelling_pairs_by_word_class";

    protected static final String STR_TRANSITION_MATRIX =
        "partofspeechtagger.transition_matrix";

    protected static final String STR_DISALLOW_WORD_ELEMENTS_IN =
        "xml.disallow_word_elements_in";

    protected static final String STR_ADORN_EXISTING_XML_FILES  =
        "xml.adorn_existing_xml_files";

    /** Create MorphAdorner settings.
     */

    public MorphAdornerSettings()
    {
    }

    /** Initialize MorphAdorner settings.
     *
     *  @param  morphAdornerLogger      MorphAdorner logger.
     */

    public void initializeSettings( MorphAdornerLogger morphAdornerLogger )
    {
                                //  Save the logger.

        this.morphAdornerLogger = morphAdornerLogger;

                                //  Set logger into XGOptions.

        this.xgOptions.setLogger( morphAdornerLogger );

                                //  Set MorphAdorner settings into
                                //  XGOptions.

        this.xgOptions.setMorphAdornerSettings( this );

                                //  Get resource strings.
        try
        {
            resourceBundle  = ResourceBundle.getBundle( resourceName );
        }
        catch ( MissingResourceException mre )
        {
            System.err.println( resourceName + ".properties not found" );
            System.exit( 0 );
        }
                                //  Get program title and banner.

        programTitle        = getString( "programTitle" );

        programVersion      = getString( "programVersion" );

        programBanner       = getString( "programBanner" );
    }

    /** Get string from ResourceBundle.  If no string is found, a default
     *  string is used.
     *
     *  @param  resourceName    Name of resource to retrieve.
     *  @param  defaultValue    Default value for resource.
     *
     *  @return                 String value from resource bundle if
     *                          resourceName found there, otherwise
     *                          defaultValue.
     *
     *  <p>
     *  Underline "_" characters are replaced by spaces.
     *  </p>
     */

    public String getString
    (
        String resourceName ,
        String defaultValue
    )
    {
        String result;

        try
        {
            result  = resourceBundle.getString( resourceName );
        }
        catch ( MissingResourceException e )
        {
            result  = defaultValue;
        }

        result  = result.replace( '_' , ' ' );

        return result;
    }

    /** Get string from ResourceBundle.  If no string is found, an empty
     *  string is returned.
     *
     *  @param  resourceName    Name of resource to retrieve.
     *
     *  @return                 String value from resource bundle if
     *                          resourceName found there, otherwise
     *                          empty string.
     *
     *  <p>
     *  Underline "_" characters are replaced by spaces.
     *  </p>
     */

    public String getString( String resourceName )
    {
        String result;

        try
        {
            result  = resourceBundle.getString( resourceName );
        }
        catch ( MissingResourceException e )
        {
            result  = "";
        }

        result  = result.replace( '_' , ' ' );

        return result;
    }

    /** Parse ResourceBundle for a String array.
     *
     *  @param  resourceName    Name of resource.
     *  @param  defaults        Array of default string values.
     *  @return                 Array of strings if resource name found
     *                              in resources, otherwise default values.
     */

    public String[] getStrings
    (
        String resourceName,
        String[] defaults
    )
    {
        String[] result;

        try
        {
            result = splitStrings( resourceBundle.getString( resourceName ) );
        }
        catch ( MissingResourceException e )
        {
            result  = defaults;
        }

        return result;
    }

    /** Split string into a series of substrings on whitespace boundries.
     *
     *  @param  input   Input string.
     *  @return         The array of strings after splitting input.
     *
     *  <p>
     *  This is useful for retrieving an array of strings from the
     *  resource file.  Underline "_" characters are replaced by spaces.
     *  </p>
     */

    public String[] splitStrings( String input )
    {
        Vector<String> v    = new Vector<String>();
        StringTokenizer t   = new StringTokenizer( input );
        String result[];
        String s;

        while ( t.hasMoreTokens() )
        {
            v.addElement( t.nextToken() );
        }

        result  = new String[ v.size() ];

        for ( int i = 0 ; i < result.length ; i++ )
        {
            s           = (String)v.elementAt( i );
            result[ i ] = s.replace( '_' , ' ' );
        }

        return result;
    }

    /** Get command line parameters.
     *
     *  @param  args    Command line parameters.
     */

    protected void getCommandLineParameters( String args[] )
    {
                                //  Display help if no command line
                                //  arguments specified.

        if ( args.length == 0 ) help();

        try
        {

                                //  Create command line arguments parser.

            CmdLineParser parser    = new CmdLineParser();

                                //  Add command line parameter
                                //  definitions.

            CmdLineParser.Option a  =
                parser.addStringOption( 'a' , "alternatespellings" );

            CmdLineParser.Option d  =
                parser.addStringOption( 'd' , "defaultproperties" );

            CmdLineParser.Option h  =
                parser.addBooleanOption( 'h' , "help" );

            CmdLineParser.Option k  =
                parser.addBooleanOption( 'k' , "tokenizeonly" );

            CmdLineParser.Option l  =
                parser.addStringOption( 'l' , "lexicon" );

            CmdLineParser.Option o  =
                parser.addStringOption( 'o' , "outputdirectory" );

            CmdLineParser.Option p  =
                parser.addStringOption( 'p' , "properties" );

            CmdLineParser.Option r  =
                parser.addStringOption( 'r' , "contextrules" );

            CmdLineParser.Option s  =
                parser.addStringOption( 's' , "standardspellings" );

            CmdLineParser.Option t  =
                parser.addStringOption( 't' , "transitionmatrix" );

            CmdLineParser.Option u  =
                parser.addStringOption( 'u' , "suffixlexicon" );

            CmdLineParser.Option w  =
                parser.addStringOption( 'w' , "alternatespellingsbywordclass" );

            CmdLineParser.Option x  =
                parser.addStringOption( 'x' , "lexicalrules" );

                                //  Parse command line arguments.
                                //  Report error if bad parameter(s).
            try
            {
                parser.parse( args );
            }
            catch ( CmdLineParser.OptionException e )
            {
                System.err.println( e.getMessage() );
                help();
                System.exit( 1 );
            }
                                //  If help parameter appears, ignore
                                //  all other parameters and display help.

            if ( (Boolean)parser.getOptionValue( h ) != null )
            {
                help();
                System.exit( 0 );
            }
                                //  Process each command line argument.

                                //  Pick up spelling maps URLs.  There can be
                                //  more than one.

            Vector strings  = parser.getOptionValues( a );

            alternateSpellingsURLStrings    = new String[ strings.size() ];

            for ( int i = 0 ; i < strings.size() ; i++ )
            {
                alternateSpellingsURLStrings[ i ]   =
                    (String)strings.get( i );
            }
                                //  Pick up spelling maps by word class URLs.
                                //  There can be more than one.

            strings =   parser.getOptionValues( w );

            alternateSpellingsByWordClassURLStrings =
                new String[ strings.size() ];

            for ( int i = 0 ; i < strings.size() ; i++ )
            {
                alternateSpellingsByWordClassURLStrings[ i ]    =
                    (String)strings.get( i );
            }
                                //  The remaining parameters take only one value.

            String defPropURLString = (String)parser.getOptionValue( d );

            if ( defPropURLString != null )
            {
                defaultPropertiesURLString  = defPropURLString;
            }

            tokenizeOnly                =
                ( (Boolean)parser.getOptionValue( k ) != null );
            wordLexiconURLString        = (String)parser.getOptionValue( l );
            outputDirectoryName         = (String)parser.getOptionValue( o );
            propertiesURLString         = (String)parser.getOptionValue( p );
            contextRulesURLString       = (String)parser.getOptionValue( r );
            spellingsURLString          = (String)parser.getOptionValue( s );
            transitionMatrixURLString   = (String)parser.getOptionValue( t );
            suffixLexiconURLString      = (String)parser.getOptionValue( u );
            lexicalRulesURLString       = (String)parser.getOptionValue( x );

                                //  Pick up the file names to process.

            fileNames   = parser.getRemainingArgs();

                                //  Expand file name wildcard to
                                //  full file name list.
            fileNames   =
                FileNameUtils.expandFileNameWildcards( fileNames );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /** Get program settings.
     *
     *  @param  args    Command line parameters.
     */

    public void getSettings( String args[] )
        throws Exception
    {
                                //  Get command line parameters.

        getCommandLineParameters( args );

                                //  Load properties.  Some of them
                                //  may be overridden by the values
                                //  provided on the command line.
        loadProperties();
                                //  Get properties from properties file.
        getOptions();
                                //  Rectify settings between properties
                                //  file and command line.
        rectifyOptions();
                                //  Set XGTagger settings.
        setXGOptions();
    }

    /** Get MorphAdorner properties and add them to the System properties.
     */

    public void loadProperties()
    {
        UTF8Properties defaultProperties    = null;

        try
        {
                                //  Get the location of the
                                //  default properties file.

            propertiesURL   =
                URLUtils.getURLFromFileNameOrURL
                (
                    defaultPropertiesURLString
                );
                                //  Load the default properties.

            defaultProperties   =
                UTF8PropertyUtils.loadUTF8Properties( propertiesURL );
        }
        catch ( Exception e )
        {
            try
            {
                propertiesURL   =
                    URLUtils.getURLFromFileNameOrURL
                    (
                        "morphadorner.properties"
                    );
                                //  Load the default properties.

                defaultProperties   =
                    UTF8PropertyUtils.loadUTF8Properties( propertiesURL );

            }
            catch ( Exception e2 )
            {
                System.out.println( "Unable to load default properties from " +
                    defaultPropertiesURLString );
            }
        }
                                //  Get the location of the user-specified
                                //  properties file.
        try
        {
            propertiesURL   =
                URLUtils.getURLFromFileNameOrURL( propertiesURLString );

                                //  Load the properties.
            properties  =
                UTF8PropertyUtils.loadUTF8Properties
                (
                    propertiesURL , defaultProperties
                );
        }
        catch ( Exception e )
        {
//          e.printStackTrace();
            System.out.println( "Unable to load configuration properties " +
                "from " + defaultPropertiesURLString );
        }
                                //  Set properties as system properties.
/*
        for (   Enumeration enumeration = properties.propertyNames() ;
                enumeration.hasMoreElements() ;
            )
        {
            String propertyName = (String)enumeration.nextElement();

            String propertyValue    =
                (String)properties.getProperty( propertyName );

            System.setProperty( propertyName , propertyValue );
        }
*/
    }

    /** Prints the help message.
     */

    protected static void help()
    {
        System.out.println(
            "java edu.northwestern.at.morphadorner.MorphAdorner [parameters ...] filetotag1 filetotag2 filetotag3 ..." );

        System.exit( 1 );
    }

    /** Get options from properties file.  */

    public void getOptions()
        throws Exception
    {
                                //  --- Settings which come from the
                                //      properties file only.

        outputSentenceNumber    =
            getBooleanProperty(
                "adorner.output.sentence_number" ,
                outputSentenceNumber );

        outputSentenceNumberAttribute   =
            getStringProperty(
                "adorner.output.sentence_number_attribute" ,
                outputSentenceNumberAttribute );

        outputWordOrdinalAttribute  =
            getStringProperty(
                "adorner.output.word_ordinal_attribute" ,
                outputWordOrdinalAttribute );

        outputWordOrdinal   =
            getBooleanProperty(
                "adorner.output.word_ordinal" ,
                outputWordOrdinal );

        outputWordNumber    =
            getBooleanProperty(
                "adorner.output.word_number" ,
                outputWordNumber );

        outputWordNumberAttribute   =
            getStringProperty(
                "adorner.output.word_number_attribute" ,
                outputWordNumberAttribute );

        outputRunningWordNumbers    =
            getBooleanProperty(
                "adorner.output.running_word_numbers" ,
                outputRunningWordNumbers );

        outputSpelling  =
            getBooleanProperty(
                "adorner.output.spelling" ,
                outputSpelling );

        outputSpellingAttribute =
            getStringProperty(
                "adorner.output.spelling_attribute" ,
                outputSpellingAttribute );

        outputOriginalToken =
            getBooleanProperty(
                "adorner.output.original_token" ,
                outputOriginalToken );

        outputOriginalTokenAttribute    =
            getStringProperty(
                "adorner.output.original_token_attribute" ,
                outputOriginalTokenAttribute );

        outputPartOfSpeech  =
            getBooleanProperty(
                "adorner.output.part_of_speech" ,
                outputPartOfSpeech );

        outputPartOfSpeechAttribute =
            getStringProperty(
                "adorner.output.part_of_speech_attribute" ,
                outputPartOfSpeechAttribute );

        outputLemma =
            getBooleanProperty(
                "adorner.output.lemma" , outputLemma );

        outputLemmaAttribute    =
            getStringProperty(
                "adorner.output.lemma_attribute" ,
                outputLemmaAttribute );

        outputStandardSpelling  =
            getBooleanProperty(
                "adorner.output.standard_spelling" ,
                outputStandardSpelling );

        outputStandardSpellingAttribute =
            getStringProperty(
                "adorner.output.standard_spelling_attribute" ,
                outputStandardSpellingAttribute );

        outputKWIC  =
            getBooleanProperty(
                "adorner.output.kwic" , outputKWIC );

        outputKWIC  =
            getBooleanProperty(
                "adorner.output.kwic" , outputKWIC );

        outputLeftKWICAttribute =
            getStringProperty(
                "adorner.output.kwic_left_attribute" ,
                outputLeftKWICAttribute );

        outputRightKWICAttribute    =
            getStringProperty(
                "adorner.output.kwic_right_attribute" ,
                outputRightKWICAttribute );

        outputKWICWidth =
            getIntegerProperty(
                "adorner.output.kwic.width" , outputKWICWidth  );

        outputEOSFlag   =
            getBooleanProperty(
                "adorner.output.end_of_sentence_flag" ,
                outputEOSFlag );

        outputEOSFlagAttribute  =
            getStringProperty(
                "adorner.output.end_of_sentence_flag_attribute" ,
                outputEOSFlagAttribute );

        useXMLHandler   =
            getBooleanProperty(
                "adorner.handle_xml" ,
                useXMLHandler );

        ignoreLexiconEntriesForLemmatization    =
            getBooleanProperty(
                "adorner.lemmatization.ignorelexiconentries" ,
                 ignoreLexiconEntriesForLemmatization );

        tryStandardSpellings    =
            getBooleanProperty(
                "partofspeechguesser.try_standard_spellings" ,
                tryStandardSpellings );

        useLatinWordList        =
            getBooleanProperty(
                "wordlists.use_latin_word_list" ,
                useLatinWordList );

        abbreviationsURL    =
            getStringProperty(
                "abbreviations.abbreviations_url" ,
                "" );

        abbreviationsMainTextURL    =
            getStringProperty(
                "abbreviations.main.abbreviations_url" ,
                "" );

        abbreviationsSideTextURL    =
            getStringProperty(
                "abbreviations.side.abbreviations_url" ,
                "" );

        allowLowerCaseProperNouns   =
            getBooleanProperty
            (
                "adorner.postagger.allow_lower_case_proper_nouns" ,
                allowLowerCaseProperNouns
            );

        xmlDoctypeName      =
            getStringProperty( STR_DOCTYPE_NAME , ""  );

        xmlDoctypeSystem        =
            getStringProperty( STR_DOCTYPE_SYSTEM , ""  );

        outputNonredundantAttributesOnly    =
            getBooleanProperty(
                STR_OUTPUT_NONREDUNDANT_ATTRIBUTES_ONLY ,
                outputNonredundantAttributesOnly );

        outputNonredundantTokenAttribute    =
            getBooleanProperty(
                STR_OUTPUT_NONREDUNDANT_TOKEN_ATTRIBUTE ,
                outputNonredundantTokenAttribute );

        outputNonredundantPartAttribute =
            getBooleanProperty(
                STR_OUTPUT_NONREDUNDANT_PART_ATTRIBUTE ,
                outputNonredundantPartAttribute );

        outputNonredundantEosAttribute  =
            getBooleanProperty(
                STR_OUTPUT_NONREDUNDANT_EOS_ATTRIBUTE ,
                outputNonredundantEosAttribute );

        outputSentenceBoundaryMilestones    =
            getBooleanProperty(
                STR_OUTPUT_SENTENCE_BOUNDARY_MILESTONES ,
                outputSentenceBoundaryMilestones  );

        usePCToMarkEndOfSentence    =
            getBooleanProperty(
                STR_USE_PC_TO_MARK_END_OF_SENTENCE ,
                usePCToMarkEndOfSentence );

        adornExistingXMLFiles   =
            getBooleanProperty(
                STR_ADORN_EXISTING_XML_FILES ,
                adornExistingXMLFiles );

        fixGapTags  =
            getBooleanProperty(
                STR_FIX_GAP_TAGS ,
                fixGapTags  );

        fixOrigTags =
            getBooleanProperty(
                STR_FIX_ORIG_TAGS ,
                fixOrigTags  );

        fixSplitWords   =
            getBooleanProperty(
                STR_FIX_SPLIT_WORDS ,
                fixSplitWords  );

        if ( fixSplitWords )
        {
            for ( int i = 1 ; i < 500 ; i++ )
            {
                String matchString  =
                    getStringProperty
                    (
                        STR_FIX_SPLIT_WORDS + ".match" + i ,
                        ""
                    );

                String replaceString    =
                    getStringProperty
                    (
                        STR_FIX_SPLIT_WORDS + ".replace" + i ,
                        ""
                    );

                if ( matchString.length() > 0 )
                {
                    PatternReplacer patternReplacer =
                        new PatternReplacer( matchString , replaceString );

                    if ( fixSplitWordsPatternReplacers == null )
                    {
                        fixSplitWordsPatternReplacers   =
                            ListFactory.createNewList();
                    }

                    fixSplitWordsPatternReplacers.add( patternReplacer );
                }
            }
        }

        pseudoPageSize  =
            getIntegerProperty(
                STR_PSEUDO_PAGE_SIZE ,
                pseudoPageSize );

        outputPseudoPageBoundaryMilestones  =
            getBooleanProperty(
                STR_OUTPUT_PSEUDO_PAGE_BOUNDARY_MILESTONES ,
                outputPseudoPageBoundaryMilestones );

        pseudoPageContainerDivTypes =
            getStringProperty(
                STR_PSEUDO_PAGE_CONTAINER_DIV_TYPES ,
                pseudoPageContainerDivTypes );

        closeSentenceAtEndOfHardTag =
            getBooleanProperty(
                STR_CLOSE_SENTENCE_AT_END_OF_HARD_TAG ,
                closeSentenceAtEndOfHardTag  );

        closeSentenceAtEndOfJumpTag =
            getBooleanProperty(
                STR_CLOSE_SENTENCE_AT_END_OF_JUMP_TAG ,
                closeSentenceAtEndOfJumpTag  );

        xmlSchema       =
            getStringProperty(
                STR_XMLSCHEMA ,
                xmlSchema  );

        disallowWordElementsIn  =
            getStringProperty
            (
                STR_DISALLOW_WORD_ELEMENTS_IN ,
                disallowWordElementsIn
            );
                                //  Word token ID type.
        String idType   =
            getStringProperty( STR_ID_TYPE , "reading_context_order"  );

        if ( idType.equals( "reading_context_order" ) )
        {
            xmlIDType   = XMLIDType.READING_CONTEXT_ORDER;
        }
        else if ( idType.equals( "word_within_page_block" ) )
        {
            xmlIDType   = XMLIDType.WORD_WITHIN_PAGE_BLOCK;
        }
        else if ( idType.equals( "use_page_block_attributes" ) )
        {
            xmlIDType   = XMLIDType.USE_PAGE_BLOCK_ATTRIBUTES;
        }
                                //  Word token ID spacing.
        xmlIDSpacing    =
            getIntegerProperty( STR_ID_SPACING , xmlIDSpacing  );

        xmlIDSpacing    = Math.max( 1 , xmlIDSpacing );

                                //  Word token label emission.

        xmlTokenLabelEmit   =
            getBooleanProperty(
                STR_TOKENLABEL_EMIT ,
                xmlTokenLabelEmit );

                                //  Word token attribute name.

        xmlTokenLabelAttribute  =
            getStringProperty(
                STR_TOKENLABEL_ATTRIBUTE ,
                xmlTokenLabelAttribute );

                                //  Word token label spacing.
        xmlTokenLabelSpacing    =
            getIntegerProperty(
                STR_TOKENLABEL_SPACING ,
                xmlTokenLabelSpacing  );

        xmlTokenLabelSpacing    = Math.max( 1 , xmlTokenLabelSpacing );

                                //  Word token attribute prepend work
                                //  name flag.

        xmlTokenLabelPrependWorkName    =
            getBooleanProperty(
                STR_TOKENLABEL_PREPENDWORKNAME ,
                xmlTokenLabelPrependWorkName );

                                //  Settings which may come from
                                //  properties file but may be
                                //  overridden by command-line
                                //  parameters.

        if ( wordLexiconURLString == null )
        {
            wordLexiconURLString    =
                getStringProperty( STR_WORD_LEXICON , ""  );
        }

        if ( suffixLexiconURLString == null )
        {
            suffixLexiconURLString  =
                getStringProperty( STR_SUFFIX_LEXICON , ""  );
        }

        if ( contextRulesURLString == null )
        {
            contextRulesURLString   =
                getStringProperty( STR_CONTEXT_RULES , ""  );
        }

        if ( lexicalRulesURLString == null )
        {
            lexicalRulesURLString   =
                getStringProperty( STR_LEXICAL_RULES , ""  );
        }

        if ( spellingsURLString == null )
        {
            spellingsURLString      =
                getStringProperty( STR_STANDARD_SPELLINGS , ""  );
        }

        if  (   ( alternateSpellingsURLStrings == null ) ||
                ( alternateSpellingsURLStrings.length == 0 )
            )
        {
            alternateSpellingsURLStrings    =
                new String[]
                {
                    getStringProperty( STR_SPELLING_PAIRS , ""  )
                };
        }

        if  (   ( alternateSpellingsByWordClassURLStrings == null ) ||
                ( alternateSpellingsByWordClassURLStrings.length == 0 )
            )
        {
            alternateSpellingsByWordClassURLStrings =
                new String[]
                {
                    getStringProperty( STR_SPELLING_PAIRS_BY_WORD_CLASS , ""  )
                };
        }

        if ( transitionMatrixURLString == null )
        {
            transitionMatrixURLString   =
                getStringProperty( STR_TRANSITION_MATRIX , ""  );
        }
    }

    /** Rectify options.  */

    public void rectifyOptions()
        throws Exception
    {
                                //  --- Settings which may come from
                                //      properties file but may be
                                //      overridden by command-line
                                //      arguments.
                                //

                                //  Get the location of the
                                //  word lexicon file.

        wordLexiconURL =
            ( wordLexiconURLString == null ) ?
                null :
                new File( wordLexiconURLString ).toURI().toURL();

                                //  Get the location of the
                                //  suffix lexicon file.

        suffixLexiconURL =
            ( suffixLexiconURLString == null ) ?
                null :
                new File( suffixLexiconURLString ).toURI().toURL();

                                //  Get the location of the
                                //  content rules file.

        contextRulesURL =
            ( contextRulesURLString == null ) ?
                null :
                new File( contextRulesURLString ).toURI().toURL();

                                //  Get the location of the
                                //  lexical rules file.

        lexicalRulesURL =
            ( lexicalRulesURLString == null ) ?
                null :
                new File( lexicalRulesURLString ).toURI().toURL();

                                //  Get the location of the
                                //  standard spellings file.

        spellingsURL =
            ( spellingsURLString == null ) ?
                null :
                new File( spellingsURLString ).toURI().toURL();

                                //  Get the location of the primary (first)
                                //  alternate spellings file.

        alternateSpellingsURLs = null;

        if ( alternateSpellingsURLStrings != null )
        {
            alternateSpellingsURLs  =
                new URL[ alternateSpellingsURLStrings.length ];

            for ( int i = 0 ; i < alternateSpellingsURLStrings.length ; i++ )
            {
                alternateSpellingsURLs[ i ] =
                    new File( alternateSpellingsURLStrings[ i ] ).toURI().toURL();
            }
        }
                                //  Get the location of the
                                //  alternate spellings by word class file.

        alternateSpellingsByWordClassURLs = null;

        if ( alternateSpellingsByWordClassURLStrings != null )
        {
            alternateSpellingsByWordClassURLs   =
                new URL[ alternateSpellingsByWordClassURLStrings.length ];

            for ( int i = 0 ; i < alternateSpellingsByWordClassURLStrings.length ; i++ )
            {
                alternateSpellingsByWordClassURLs[ i ]  =
                    new File( alternateSpellingsByWordClassURLStrings[ i ] ).toURI().toURL();
            }
        }
                                //  Get the location of the
                                //  transition matrix file.

        transitionMatrixURL =
            ( transitionMatrixURLString == null ) ?
                null :
                new File( transitionMatrixURLString ).toURI().toURL();
    }

    /** Set XGTagger options. */

    public int setXGOptions()
        throws IOException
    {
                                //  Word tag name.

        xgOptions.setWordTagName
        (
            getStringProperty( STR_WORD_TAG_NAME , "" )
        );

        if ( xgOptions.getWordTagName().length() == 0 )
        {
            morphAdornerLogger.logError
            (
                "You must specify a name for tags that will be added with " +
                STR_WORD_TAG_NAME + " in configuration file."
            );

            return -1;
        }
                                //  Punctuation tag name.

        xgOptions.setPuncTagName
        (
            getStringProperty( STR_PUNC_TAG_NAME , "" )
        );

        if ( xgOptions.getPuncTagName().length() == 0 )
        {
            xgOptions.setPuncTagName
            (
                xgOptions.getWordTagName()
            );
        }
                                //  Check that the property
                                //  STR_WORD_FIELD is an integer.

        int word_field  = getIntegerProperty( STR_WORD_FIELD , 1 );

        if ( word_field > 0 )
        {
            xgOptions.setWordField( word_field );
        }
        else
        {
            morphAdornerLogger.logError
            (
                "Please specify a valid (integer) word field with " +
                "property " + STR_WORD_FIELD +
                " in the configuration file."
            );

            return -1;
        }
                                //  Attributes repetition (default is true).

        xgOptions.repeatAttributes
        (
            getBooleanProperty( STR_REPEAT_ATTRIBUTES , false )
        );
                                //  Base for relative URIs.

        String str  = getStringProperty( STR_RELATIVE_URI_BASE , "" );

        if ( str.length() != 0 )
        {
            File file   = XGMisc.getFile( str );

            if ( !file.isDirectory() )
            {
                morphAdornerLogger.logError
                (
                    "Base for relative URIs " + str +
                    " must be an existing directory."
                );

                return -1;
            }
            else
            {
                xgOptions.setRelativeURIBase( file.getPath() );
            }
        }
                                //  Ignore tag case.

        xgOptions.setIgnoreTagCase
        (
            getBooleanProperty( STR_IGNORE_TAG_CASE , true )
        );
                                //  Jump tags.

        xgOptions.setJumpTags( getStringProperty( STR_JUMP_TAGS , "" ) );

                                //  Soft tags.

        xgOptions.setSoftTags( getStringProperty( STR_SOFT_TAGS , "" ) );

                                //  Output whitespace elements.

        outputWhitespaceElements    =
            getBooleanProperty(
                STR_OUTPUT_WHITESPACE_ELEMENTS , true );

                                //  Delimiters.
        setDelimiters();
                                //  Set IDs.
        setIDs();
                                //  Set word paths.
        setPaths();
                                //  Set log file names.
        setLogFileNames();
                                //  Entity reference handling.

        return entityReferenceHandling();
    }

    /** Get XGTagger entity reference handling. */

    protected int entityReferenceHandling()
    {
                                //  Merge.

        xgOptions.setEntityMerging
        (
            getBooleanProperty( STR_ENTITIES_MERGE , false  )
        );
                                //  Treat all.

        xgOptions.setEntityTreatAll
        (
            getBooleanProperty( STR_ENTITIES_TREAT_ALL , false )
        );
                                //  Not files.

        xgOptions.setEntityIgnoreFiles
        (
            getBooleanProperty( STR_ENTITIES_NOT_FILES , false )
        );

        return 0;
    }

    /** Sets word delimiters.
     *
     *  @return <code>0</code>.
     */

    protected int setDelimiters()
    {
        String s = getStringProperty( STR_FIELD_DELIMITERS , "\t" );

        if ( s.length() > 0 )
        {
            xgOptions.setFieldDelimiters( s );
        }

        s = getStringProperty( STR_WORD_DELIMITERS , "\r\n" );

        if ( s.length() > 0 )
        {
            xgOptions.setWordDelimiters( s );
        }

        s = getStringProperty( STR_SPECIAL_SEPARATOR , "/sep/" );

        if ( s.length() > 0 )
        {
            xgOptions.setSpecialSeparator( s );
        }

        s = getStringProperty( STR_SURROUND_MARKER ,
            " " + CharUtils. CHAR_END_OF_TEXT_SECTION_STRING + " " );

        if ( s.length() > 0 )
        {
            xgOptions.setSurroundMarker( s );
        }

        return 0;
    }

    /** Sets the Path property.
     *
     *  @return <code>0</code> whether the Path property has
     *              been correctly specified,
     *              <code>-1</code> otherwise.
     */

    protected int setPaths()
        throws IOException
    {
                                //  Word path.
        String strProperty  =
            getBooleanStringProperty( STR_WORD_PATH , "false" );

        if ( !strProperty.equals( "false" ) )
        {
            xgOptions.setWritePath( 1 );

            if ( strProperty.equals( "true" ) )
            {
                xgOptions.setWordPathArgumentName( "p" );
            }
            else
            {
                xgOptions.setWordPathArgumentName( strProperty );
            }
        }
                                //  Tag path.
        strProperty =
            getBooleanStringProperty( STR_TAGS_PATH , "false" );

        if ( !strProperty.equals( "false" ) )
        {
            xgOptions.setWritePath( 2 );

            if ( strProperty.equals( "true" ) )
            {
                xgOptions.setTagsPathArgumentName( "p" );
            }
            else
            {
                xgOptions.setTagsPathArgumentName( strProperty );
            }
        }

        return 0;
    }

    /** Sets the Ids property.
     *
     *  @return <code>0</code> whether the Ids property has
     *              been correctly specified, <code>-1</code> otherwise.
     */

    protected int setIDs()
        throws IOException
    {
        String strProperty  = getBooleanStringProperty( STR_ID , "false" );

        if ( strProperty.equals( "false" ) )
        {
            xgOptions.setWriteIds( false );
        }
        else
        {
            xgOptions.setWriteIds( true );

            if ( strProperty.equals( "true" ) )
            {
                strProperty = "id";
            }

            xgOptions.setIdArgumentName( strProperty );
        }

        return 0;
    }

    /** Sets the log file name.
     *
     *  @return <code>0</code>.
     *  <p>
     *  If the log file name has not been specified (but a log asked),
     *  the file name is <code>input_file.log</code>.
     *  </p>
     */

    protected int setLogFileNames()
        throws IOException
    {
        File file;
                                //  Log.

        String strProperty = getBooleanStringProperty( STR_LOG , "" );

        if ( strProperty.equals( "false" ) )
        {
            xgOptions.setWriteLog( false );
        }
        else if ( strProperty.equals( "true" ) )
        {
            xgOptions.setWriteLog( true );
        }

        return 0;
    }

    /** Removes eventual quotation mark around the property.
     *
     *  @param strText the initial text.
     *
     *  @return the same text, without quotation marks.
     */

    public String stripQuotes( String strText )
    {
        String result   = strText;

        if  (   ( strText != null ) &&
                ( strText.length() > 0 ) &&
                ( strText.charAt( 0 ) == '"' ) &&
                ( strText.charAt( strText.length() - 1 ) == '"' )
            )
        {
            result  = strText.substring( 1 , strText.length() - 1 );
        }

        return result;
    }

    /** Get a boolean configuration property.
     *
     *  @param  name            Property name.
     *  @param  defaultValue    Default value.
     *
     *  @return                 Property value or default if not defined.
     */

    protected boolean getBooleanProperty
    (
        String name ,
        boolean defaultValue
    )
    {
        boolean result  = defaultValue;

        String s        =
            stripQuotes( properties.getProperty( name ) );

        if ( s != null )
        {
            result  =
                s.equalsIgnoreCase( "1" ) ||
                s.equalsIgnoreCase( "true" ) ||
                s.equalsIgnoreCase( "yes" ) ||
                s.equalsIgnoreCase( "y" );
        }

        return result;
    }

    /** Get an integer configuration property.
     *
     *  @param  name            Property name.
     *  @param  defaultValue    Default value.
     *
     *  @return                 Property value, or default if not defined.
     */

    protected int getIntegerProperty( String name , int defaultValue )
    {
        int result  = defaultValue;

        String s    =
            stripQuotes( properties.getProperty( name ) );

        if ( s != null )
        {
            try
            {
                result  = Integer.parseInt( s );
            }
            catch ( Exception e )
            {
            }
        }

        return result;
    }

    /** Get a boolean string configuration property.
     *
     *  @param  name            Property name.
     *  @param  defaultValue    Default value.
     *
     *  @return                 Property value, or default if not defined.
     *                              Returns "false" if false, and
     *                              non-empty string if true.
     */

    protected String getBooleanStringProperty
    (
        String name ,
        String defaultValue
    )
    {
        String result   = defaultValue;

        String s        =
            stripQuotes( properties.getProperty( name ) );

        if ( s != null )
        {
            if  (   s.equalsIgnoreCase( "1" ) ||
                    s.equalsIgnoreCase( "true" ) ||
                    s.equalsIgnoreCase( "yes" ) ||
                    s.equalsIgnoreCase( "y" )
                )
            {
                result  = "true";
            }
            else if (   ( s.length() == 0 ) ||
                        s.equalsIgnoreCase( "0" ) ||
                        s.equalsIgnoreCase( "false" ) ||
                        s.equalsIgnoreCase( "no" ) ||
                        s.equalsIgnoreCase( "n" )
                    )
            {
                result  = "false";
            }
            else
            {
                result  = s;
            }
        }
        else
        {
            result  = "false";
        }

        return result;
    }

    /** Get a string configuration property.
     *
     *  @param  name            Property name.
     *  @param  defaultValue    Default value.
     *
     *  @return                 Property value, or default if not defined.
     */

    protected String getStringProperty
    (
        String name ,
        String defaultValue
    )
    {
        String result   = defaultValue;

        String s        = stripQuotes( properties.getProperty( name ) );

        if ( s != null )
        {
            result  = s;
        }

        return result;
    }

    /** Set word attribute names for XML output.
     *
     *  @param  outputOriginalToken     true to output original token.
     *  @param  outputLemma             true to output lemma.
     *  @param  outputStandardSpelling  true to output standard spelling.
     */

    public void setXMLWordAttributes
    (
        boolean outputOriginalToken ,
        boolean outputLemma ,
        boolean outputStandardSpelling
    )
    {
        xmlWordAttributes   = ListFactory.createNewList();

                                //  Sentence number.

        if ( outputSentenceNumber )
        {
            xmlWordAttributes.add( outputSentenceNumberAttribute );
        }
                                //  Word number.

        if ( outputWordNumber )
        {
            xmlWordAttributes.add( outputWordNumberAttribute );
        }
                                //  Original token.
                                //  Note:  MUST output this if
                                //  internal XML handler enabled.

//      if ( outputOriginalToken )
//      {
            xmlWordAttributes.add( outputOriginalTokenAttribute );

            xgOptions.setWordField( xmlWordAttributes.size() );
//      }
                                //  Spelling.

        if ( outputSpelling )
        {
            xmlWordAttributes.add( outputSpellingAttribute );
        }
                                //  Part of speech.

        if ( outputPartOfSpeech)
        {
            xmlWordAttributes.add( outputPartOfSpeechAttribute );
        }
                                //  Standard spelling.

        if ( outputStandardSpelling )
        {
            xmlWordAttributes.add( outputStandardSpellingAttribute );
        }
                                //  Lemma.
        if ( outputLemma )
        {
            xmlWordAttributes.add( outputLemmaAttribute );
        }
                                //  End of sentence flag.
//      if ( outputEOSFlag )
//      {
            xmlWordAttributes.add( outputEOSFlagAttribute );
//      }
                                //  Word ordinal.
/*
                                //  The word ordinal is added on the
                                //  fly by with adorned file XML
                                //  writer.

        if ( outputWordOrdinal )
        {
            xmlWordAttributes.add( outputWordOrdinalAttribute );
        }
*/
                                //  KWIC index.
        if ( outputKWIC )
        {
            xmlWordAttributes.add( outputLeftKWICAttribute );
            xmlWordAttributes.add( outputRightKWICAttribute );
        }
    }

    /** Get XML word attribute.
     *
     *  @param  attrIndex   Attribute index.
     *
     *  @return             Attribute name for specified index,
     *                      or empty string if index is bad.
     */

    public String getXMLWordAttribute( int attrIndex )
    {
        String result   = "";

        if ( ( attrIndex >= 0 ) && ( attrIndex < xmlWordAttributes.size() ) )
        {
            result  = (String)xmlWordAttributes.get( attrIndex );

            if ( result == null )
            {
                result  = "";
            }
        }

        return result;
    }

    /** Get XML word attributes.
     *
     *  @return             XML word attribute names.
     */

    public List<String> getXMLWordAttributes()
    {
        return xmlWordAttributes;
    }

    /** Return settings.
     *
     *  @return     The MorphAdorner settings defined here.
     */

    public MorphAdornerSettings getSettings()
    {
        return this;
    }

    /** Return MorphAdorner program version.
     *
     *  @return     The MorphAdorner program version as a string.
     */

    public String getMorphAdornerVersion()
    {
        return programVersion;
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



