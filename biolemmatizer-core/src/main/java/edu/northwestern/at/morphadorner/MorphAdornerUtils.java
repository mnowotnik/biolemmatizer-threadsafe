package edu.northwestern.at.morphadorner;

/*  Please see the license information at the end of this file. */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.List;
import java.util.regex.*;

import org.w3c.dom.*;

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

import edu.northwestern.at.morphadorner.xgtagger.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.logger.*;
import edu.northwestern.at.utils.xml.*;

/** Morphological Adorner Utilities.
 *
 *  <p>
 *  Static utility methods used by MorphAdorner.
 *  </p>
 */

public class MorphAdornerUtils
{
    /** Pattern to match _CapCap */

    protected static Pattern underlineCapCapPattern         =
        Pattern.compile(
            "^_([ABCDEFGHIJKLMNOPQRSTUVWXYZ])([ABCDEFGHIJKLMNOPQRSTUVWXYZ])" );

    protected static final Matcher underlineCapCapMatcher   =
        underlineCapCapPattern.matcher( "" );

    /** Runtime system. */

    protected static Runtime runTime    = Runtime.getRuntime();

    /** Count page breaks in a document.
     *
     *  @param  document    The DOM document.
     *
     *  @return             The number of page break <pb> elements.
     */

    public static int countPageBreaks( Document document )
    {
        NodeList pbNodes    = document.getElementsByTagName( "pb" );

        return pbNodes.getLength();
    }

    /** Create spelling mapper.
     *
     *  @param  properties  MorphAdorner properties.
     *
     *  @return             The spelling mapper.
     */

    public static SpellingMapper createSpellingMapper
    (
        UTF8Properties properties
    )
        throws IOException
    {
        return SpellingMapperFactory.newSpellingMapper( properties );
    }

    /** Create proper name standardizer.
     *
     *  @param  wordLexicon         The word lexicon containing names.
     *  @param  adornerSettings     The adorner settings.
     *  @param  adornerLogger       The adorner logger.
     *
     *  @return     The name standardizer.
     */

    public static NameStandardizer createNameStandardizer
    (
        Lexicon wordLexicon ,
        MorphAdornerSettings adornerSettings ,
        MorphAdornerLogger adornerLogger
    )
        throws IOException
    {
        NameStandardizer nameStandardizer   =
            NameStandardizerFactory.newNameStandardizer
            (
                adornerSettings.properties
            );

        if  ( nameStandardizer != null )
        {
                                //  Load names from word lexicon
                                //  into standardizer.

            if ( wordLexicon != null )
            {
                long startTime  = System.currentTimeMillis();

                nameStandardizer.loadNamesFromLexicon( wordLexicon );

                int numberOfNames   =
                    nameStandardizer.getNumberOfNames();

                adornerLogger.println
                (
                    "Loaded_names" ,
                    new Object[]
                    {
                        Formatters.formatIntegerWithCommas
                        (
                            numberOfNames
                        ) ,
                        durationString( adornerSettings , startTime )
                    }
                );
            }
                                //  Set logger into standardizer.

            if ( nameStandardizer instanceof UsesLogger )
            {
                ((UsesLogger)nameStandardizer).setLogger(
                    adornerLogger.getLogger() );
            }
        }

        return nameStandardizer;
    }

    /** Loads the word lexicon.
     *
     *  @param  adornerSettings     The adorner settings.
     *  @param  adornerLogger       The adorner logger.
     *
     *  @return     The word lexicon.
     */

    public static Lexicon loadWordLexicon
    (
        MorphAdornerSettings adornerSettings ,
        MorphAdornerLogger adornerLogger
    )
        throws IOException
    {
                                //  Load word lexicon if given.

        long startTime      = System.currentTimeMillis();

        Lexicon wordLexicon =
            LexiconFactory.newLexicon( adornerSettings.properties );

        if ( adornerSettings.wordLexiconURL != null )
        {
            wordLexicon.loadLexicon(
                adornerSettings.wordLexiconURL , "utf-8" );
        }

        adornerLogger.println
        (
            "Loaded_word_lexicon" ,
            new Object[]
            {
                Formatters.formatIntegerWithCommas
                (
                    wordLexicon.getLexiconSize()
                ) ,
                MorphAdornerUtils.durationString
                (
                    adornerSettings ,
                    startTime
                )
            }
        );
                                //  Set logger into word lexicon.

        ((UsesLogger)wordLexicon).setLogger(
            adornerLogger.getLogger() );

        return wordLexicon;
    }

    /** Loads the suffix lexicon.
     *
     *  @param  adornerSettings     The adorner settings.
     *  @param  adornerLogger       The adorner logger.
     *
     *  @return             The suffix lexicon.
     */

    public static Lexicon loadSuffixLexicon
    (
        MorphAdornerSettings adornerSettings ,
        MorphAdornerLogger adornerLogger
    )
        throws IOException
    {
                                //  Load suffix lexicon if given.

        long startTime  = System.currentTimeMillis();

        Lexicon suffixLexicon   =
            LexiconFactory.newLexicon( adornerSettings.properties );

        if ( adornerSettings.suffixLexiconURL != null )
        {
            suffixLexicon.loadLexicon(
                adornerSettings.suffixLexiconURL , "utf-8" );
        }

        adornerLogger.println
        (
            "Loaded_suffix_lexicon" ,
            new Object[]
            {
                Formatters.formatIntegerWithCommas
                (
                    suffixLexicon.getLexiconSize()
                ) ,
                MorphAdornerUtils.durationString
                (
                    adornerSettings ,
                    startTime
                )
            }
        );
                                //  Set logger into suffix lexicon.

        ((UsesLogger)suffixLexicon).setLogger(
            adornerLogger.getLogger() );

        return suffixLexicon;
    }

    /** Loads the transition matrix.
     *
     *  @param      tagger              Part of speech tagger.
     *  @param      adornerSettings     The adorner settings.
     *  @param      adornerLogger       The adorner logger.
     *
     *  @return     The transition matrix.
     */

    public static TransitionMatrix loadTransitionMatrix
    (
        PartOfSpeechTagger tagger ,
        MorphAdornerSettings adornerSettings ,
        MorphAdornerLogger adornerLogger
    )
        throws IOException
    {
        TransitionMatrix transitionMatrix   = new TransitionMatrix();

                                //  Load transition matrix if given.

        if  (   ( adornerSettings.transitionMatrixURL != null ) &&
                ( tagger.usesTransitionProbabilities() ) )
        {
            long startTime  = System.currentTimeMillis();

            transitionMatrix.loadTransitionMatrix
            (
                adornerSettings.transitionMatrixURL ,
                "utf-8" ,
                '\t'
            );
                                //  Set transition matrix into
                                //  part of speech tagger.

            tagger.setTransitionMatrix( transitionMatrix );

            adornerLogger.println
            (
                "Loaded_transition_matrix" ,
                new Object[]
                {
                    MorphAdornerUtils.durationString
                    (
                        adornerSettings ,
                        startTime
                    )
                }
            );
                                //  Set logger into transition matrix.

            transitionMatrix.setLogger( adornerLogger.getLogger() );
        }

        return transitionMatrix;
    }

    /** Loads part of speech tagger rules.
     *
     *  @param      tagger              Part of speech tagger.
     *  @param      adornerSettings     The adorner settings.
     *  @param      adornerLogger       The adorner logger.
     */

    public static void loadTaggerRules
    (
        PartOfSpeechTagger tagger ,
        MorphAdornerSettings adornerSettings ,
        MorphAdornerLogger adornerLogger
    )
        throws InvalidRuleException, IOException
    {
        if  (   ( adornerSettings.contextRulesURL != null ) &&
                ( tagger.usesContextRules() ) )
        {
            String[] contextRules   =
                new TextFile
                (
                    adornerSettings.contextRulesURL ,
                    "utf-8"
                ).toArray();

            tagger.setContextRules( contextRules );
        }

        if  (   ( adornerSettings.lexicalRulesURL != null ) &&
                ( tagger.usesLexicalRules() ) )
        {
            String[] lexicalRules   =
                new TextFile
                (
                    adornerSettings.lexicalRulesURL ,
                    "utf-8"
                ).toArray();

            tagger.setLexicalRules( lexicalRules );
        }
    }

    /** Create spelling standardizer.
     *
     *  @param      wordLexicon         The word lexicon.
     *  @param      names               The names list.
     *  @param      adornerSettings     The adorner settings.
     *  @param      adornerLogger       The adorner logger.
     *
     *  @return     The spelling standardizer.
     */

    public static SpellingStandardizer createSpellingStandardizer
    (
        Lexicon wordLexicon ,
        Names names ,
        MorphAdornerSettings adornerSettings ,
        MorphAdornerLogger adornerLogger
    )
        throws IOException
    {
                                //  Create a spelling standardizer.

        SpellingStandardizer spellingStandardizer   =
            SpellingStandardizerFactory.newSpellingStandardizer
            (
                adornerSettings.properties
            );
                                //  If standardizer created successfully ...

        if  ( spellingStandardizer != null )
        {
            long startTime  = System.currentTimeMillis();

                                //  Set word lexicon into standardizer.

            if ( spellingStandardizer instanceof UsesLexicon )
            {
                ((UsesLexicon)spellingStandardizer).setLexicon( wordLexicon );
            }
                                //  Remember number of standard spellings
                                //  for display.

            int numberOfStandardSpellings   = 0;

                                //  Load standard spellings.

            if ( adornerSettings.spellingsURL != null )
            {
                spellingStandardizer.loadStandardSpellings
                (
                    adornerSettings.spellingsURL ,
                    "utf-8"
                );

                numberOfStandardSpellings   =
                    spellingStandardizer.getNumberOfStandardSpellings();

                adornerLogger.println
                (
                    "Loaded_standard_spellings" ,
                    new Object[]
                    {
                        Formatters.formatIntegerWithCommas
                        (
                            numberOfStandardSpellings
                        ) ,
                        durationString
                        (
                            adornerSettings ,
                            startTime
                        )
                    }
                );
            }
                                //  Add name lists to standard spellings.

            spellingStandardizer.addStandardSpellings(
                names.getFirstNames() );

            spellingStandardizer.addStandardSpellings(
                names.getSurnames() );

            spellingStandardizer.addStandardSpellings(
                names.getPlaceNames().keySet() );

                                //  Load alternate spellings.

            if ( adornerSettings.alternateSpellingsURLs != null )
            {
                int altSpellingsCount   = 0;

                for (   int i = 0 ;
                        i < adornerSettings.alternateSpellingsURLs.length ;
                        i++
                    )
                {
                    startTime   = System.currentTimeMillis();

                    spellingStandardizer.loadAlternativeSpellings
                    (
                        adornerSettings.alternateSpellingsURLs[ i ] ,
                        "utf-8" ,
                        "\t"
                    );

                    adornerLogger.println
                    (
                        "Loaded_alternate_spellings" ,
                        new Object[]
                        {
                            Formatters.formatIntegerWithCommas
                            (
                                spellingStandardizer.
                                    getNumberOfAlternateSpellings() -
                                        altSpellingsCount
                            ) ,
                            MorphAdornerUtils.durationString
                            (
                                adornerSettings ,
                                startTime
                            )
                        }
                    );

                    altSpellingsCount   =
                        spellingStandardizer.getNumberOfAlternateSpellings();
                }
            }
                                //  Load alternate spellings by word class.

            if ( adornerSettings.alternateSpellingsByWordClassURLs != null )
            {
                int[] altCountsCum  = new int[]{ 0 , 0 };

                for (   int i = 0 ;
                        i < adornerSettings.alternateSpellingsByWordClassURLs.length ;
                        i++
                    )
                {
                    startTime   = System.currentTimeMillis();

                    spellingStandardizer.loadAlternativeSpellingsByWordClass
                    (
                        adornerSettings.alternateSpellingsByWordClassURLs[ i ] ,
                        "utf-8"
                    );

                    int[] altCounts =
                        spellingStandardizer.getNumberOfAlternateSpellingsByWordClass();

                    adornerLogger.println
                    (
                        "Loaded_alternate_spellings_by_word_class" ,
                        new Object[]
                        {
                            Formatters.formatIntegerWithCommas
                            (
                                altCounts[ 1 ] - altCountsCum[ 1 ]
                            ) ,
                            Formatters.formatIntegerWithCommas
                            (
                                altCounts[ 0 ] - altCountsCum[ 0 ]
                            ) ,
                            MorphAdornerUtils.durationString
                            (
                                adornerSettings ,
                                startTime
                            )
                        }
                    );

                    altCountsCum[ 0 ]   = altCounts[ 0 ];
                    altCountsCum[ 1 ]   = altCounts[ 1 ];
                }
            }
                                //  Set logger into standardizer.

            if ( spellingStandardizer instanceof UsesLogger )
            {
                ((UsesLogger)spellingStandardizer).setLogger(
                    adornerLogger.getLogger() );
            }
        }

        return spellingStandardizer;
    }

    /** Get duration value for display.
     *
     *  @param  adornerSettings     Adorner settings.
     *  @param  startTime           Start time.
     *
     *  @return                     Duration value for display.
     */

    public static String durationString
    (
        MorphAdornerSettings adornerSettings ,
        long startTime
    )
    {
        StringBuffer result = new StringBuffer();

        long duration           =
            ( System.currentTimeMillis() - startTime + 999 ) / 1000;

        String durationString   =
            Formatters.formatLongWithCommas( duration );

        if ( duration < 1 )
        {
            durationString  = "< 1";
        }

        String secondString = ( duration > 1 ) ? "seconds" : "second";

        result.append( durationString );
        result.append( " " );
        result.append( adornerSettings.getString( secondString ) );
        result.append( "." );

        return result.toString();
    }

    /** Fix empty soft tags.
     *
     *  @param  xgOptions   XML parsing options.
     *  @param  document    The DOM document.
     *
     *  <p>
     *  On exit, the DOM document has empty soft tags
     *  expanded with a single blank as text, except
     *  <gap> tags.  Empty <gap> tags get the special
     *  end of text section character, which prevents
     *  formation of bogus split words over gaps.
     *  </p>
     */

    public static void fixEmptySoftTags
    (
        XGOptions xgOptions ,
        Document document
    )
    {
        List<Node> nodes    = DOMUtils.getDescendants( document );

        for ( int i = 0 ; i < nodes.size() ; i++ )
        {
            Node node   = nodes.get( i );

            String nodeName = node.getNodeName();

            if ( xgOptions.isSoftTag( nodeName ) )
            {
                String text = DOMUtils.getText( node );

                if ( text.length() == 0 )
                {
                    if ( nodeName.equals( "gap" ) )
                    {
                        DOMUtils.setText
                        (   node ,
                            " " +
                            CharUtils.CHAR_END_OF_TEXT_SECTION_STRING +
                            " "
                        );
                    }
                    else if ( nodeName.equals( "pb" ) )
                    {
                    }
                    else
                    {
                        DOMUtils.setText( node , " " );
                    }
                }
            }
        }
    }

    /** Fix spelling.
     *
     *  @param  spelling    Original spelling.
     *
     *  @return             The fixed spelling.
     */

    public static String fixSpelling( String spelling )
    {
                                //  Remove vertical bars.

        String result   = spelling;

        if ( !result.equals( "|" ) )
        {
            result  = StringUtils.replaceAll( result , "|" , "" );
        }
                                //  Remove braces.

        if ( !result.equals( "{" ) )
        {
            result  = StringUtils.replaceAll( result , "{" , "" );
        }

        if ( !result.equals( "}" ) )
        {
            result  = StringUtils.replaceAll( result , "}" , "" );
        }

        if ( !result.equals( "+" ) )
        {
            result  = StringUtils.replaceAll( result , "+" , "" );
        }
                                //  Replace _CapCap at start of word
                                //  by Capcap.

        if ( ( result.length() > 1 ) && ( result.charAt( 0 ) == '_' ) )
        {
            underlineCapCapMatcher.reset( result );

            if ( underlineCapCapMatcher.find() )
            {
                String char1    = result.charAt( 1 ) + "";

                String char2    =
                    Character.toLowerCase( result.charAt( 2 ) ) + "";

                String rest     = "";

                if ( result.length() > 3 )
                {
                    rest    = result.substring( 3 );
                }

                result  = char1 + char2 + rest;
            }
        }

        return result;
    }

    /** Fix sup tags.
     *
     *  @param  document    The DOM document.
     *
     *  <p>
     *  Prepends a special marker character to the start of the text enclosed
     *  in <sup> tags to allow disambiguation of old printer's abbreviations
     *  from other types of abbreviations.  The special marker character is
     *  removed before the adorned XML text is written out.
     *  </p>
     *
     *  <sup>E</sup>        the
     *  y<sup>T</sup>       that
     *  y<sup>c</sup>       the
     *  y<sup>e</sup>       the
     *  y<sup>en</sup>      then
     *  y<sup>ere</sup>     there
     *  y<sup>f</sup>       if
     *  y<sup>i</sup>       thy
     *  y<sup>m</sup>       them
     *  y<sup>n</sup>       than
     *  y<sup>o</sup>       the
     *  y<sup>t</sup>       that
     *  y<sup>u</sup>       thou
     *  y<sup></sup>        that
     *
     *  w<sup>ch</sup>      which
     *  w<sup>t</sup>       with
     */

    public static void fixSupTags( Document document )
    {
                                //  Extract sup tags.

//      NodeList supNodes   = document.getElementsByTagName( "sup" );
        NodeList supNodes   = document.getElementsByTagName( "hi" );

                                //  Look at each sup tag.

        for ( int i = 0 ; i < supNodes.getLength() ; i++ )
        {
            Element supNode = (Element)supNodes.item( i );

                                //  Skip if not rend="superscript"

            String hiType   = supNode.getAttribute( "rend" );

            if ( ( hiType == null ) || !hiType.equals( "superscript" ) )
            {
                continue;
            }
                                //  Get text of this sup tag.

            String supText  = DOMUtils.getText( supNode );

            if ( supText.startsWith( "^" ) ) continue;

                                //  Get previous sibling of sup tag.

            Node sibling    = supNode.getPreviousSibling();

            if ( sibling != null )
            {
                                //  Get text of sibling.

                String siblingText  = sibling.getTextContent();

                                //  If last characters of sibling text
                                //  plus characters of sup text are a
                                //  printer's abbreviation, add special
                                //  marker character to sup text to
                                //  indicate this.

                if ( siblingText.endsWith( " y" ) )
                {
                    String loSupText    = supText.toLowerCase();

                    if  (   loSupText.equals( "e" ) ||
                            loSupText.equals( "t" ) ||
                            loSupText.equals( "c" ) ||
                            loSupText.equals( "en" ) ||
                            loSupText.equals( "ere" ) ||
                            loSupText.equals( "f" ) ||
                            loSupText.equals( "i" ) ||
                            loSupText.equals( "m" ) ||
                            loSupText.equals( "n" ) ||
                            loSupText.equals( "o" ) ||
                            loSupText.equals( "u" )
                        )
                    {
                        supText =
                            CharUtils.CHAR_SUP_TEXT_MARKER_STRING +
                                supText;

                        DOMUtils.setText( supNode , supText );
                    }
                }
                else if ( siblingText.endsWith( " w" ) )
                {
                    String loSupText    = supText.toLowerCase();

                    if  (   loSupText.equals( "ch" ) ||
                            loSupText.equals( "t" ) ||
                            loSupText.equals( "th" )
                        )
                    {
                        supText =
                            CharUtils.CHAR_SUP_TEXT_MARKER_STRING +
                                supText;

                        DOMUtils.setText( supNode , supText );
                    }
                }
            }
        }
    }

    /** Get lemma (possibly compound) for a spelling.
     *
     *  @param  adorner         The adorner.
     *  @param  spelling        The spelling.
     *  @param  partOfSpeech    The part of speech tag.
     *
     *  @return                 Lemma for spelling.  May contain
     *                          compound spelling in form
     *                          "lemma1:lemma2:...".
     */

    public static String getLemma
    (
        MorphAdorner adorner ,
        String spelling ,
        String partOfSpeech
    )
    {
        String result   = spelling;

                                //  Get lemmatization word class
                                //  for part of speech.
        String lemmaClass   =
            adorner.partOfSpeechTags.getLemmaWordClass( partOfSpeech );

                                //  Do not lemmatize words which
                                //  should not be lemmatized,
                                //  including proper names.

        if  (   adorner.lemmatizer.cantLemmatize( spelling ) ||
                lemmaClass.equals( "none" )
            )
        {
            if ( adorner.partOfSpeechTags.isNumberTag( partOfSpeech ) )
            {
                if ( RomanNumeralUtils.isLooseRomanNumeral( result ) )
                {
                    if ( result.charAt( 0 ) == '.' )
                    {
                        result  = result.substring( 1 );
                    }

                    if ( result.charAt( result.length() - 1 ) == '.' )
                    {
                        result  =
                            result.substring( 0 , result.length() - 1 );
                    }
                }
            }
        }
        else
        {
                                //  If compound part of speech tag,
                                //  see if word appears in list of
                                //  known irregular lemmata.

            boolean isCompoundTag   =
                adorner.partOfSpeechTags.isCompoundTag( partOfSpeech );

            if ( isCompoundTag )
            {
                result  = adorner.lemmatizer.lemmatize( spelling , "compound" );

                if ( adorner.lemmatizer.isCompoundLemma( result ) )
                {
                    return result;
                }
            }
                                //  Extract individual word parts.
                                //  May be more than one for a
                                //  contraction.

            List<String> wordList   =
                adorner.spellingTokenizer.extractWords( spelling );

                                //  If just one word part,
                                //  get its lemma.

            if ( !isCompoundTag || ( wordList.size() == 1 ) )
            {
                if ( lemmaClass.length() == 0 )
                {
                    result  =
                        adorner.lemmatizer.lemmatize( spelling , "compound" );

                    if ( result.equals( spelling ) )
                    {
                        result  = adorner.lemmatizer.lemmatize( spelling );
                    }
                }
                else
                {
                    result  =
                        adorner.lemmatizer.lemmatize( spelling , lemmaClass );
                }
            }
                                //  More than one word part.
                                //  Get lemma for each part and
                                //  concatenate them with the
                                //  lemma separator to form a
                                //  compound lemma.
            else
            {
                result              = "";
                String lemmaPiece   = "";
                String[] posTags    =
                    adorner.partOfSpeechTags.splitTag( partOfSpeech );

                if ( posTags.length == wordList.size() )
                {
                    for ( int i = 0 ; i < wordList.size() ; i++ )
                    {
                        String wordPiece    = wordList.get( i );

                        if ( i > 0 )
                        {
                            result  = result + adorner.lemmaSeparator;
                        }

                        lemmaClass  =
                            adorner.partOfSpeechTags.getLemmaWordClass
                            (
                                posTags[ i ]
                            );

                        lemmaPiece  =
                            adorner.lemmatizer.lemmatize
                            (
                                wordPiece ,
                                lemmaClass
                            );

                        result  = result + lemmaPiece;
                    }
                }
            }
        }

        return result;
    }

    /** Get standardized spelling.
     *
     *  @param  adorner                 Adorner.
     *  @param  correctedSpelling       The spelling.
     *  @param  standardizedSpelling    The initial standardized spelling.
     *  @param  partOfSpeech            The part of speech tag.
     *
     *  @return                         Standardized spelling.
     */

    protected static String getStandardizedSpelling
    (
        MorphAdorner adorner ,
        String correctedSpelling ,
        String standardizedSpelling ,
        String partOfSpeech
    )
    {
        String spelling = correctedSpelling;
        String result   = correctedSpelling;

        if ( adorner.partOfSpeechTags.isProperNounTag( partOfSpeech ) )
        {
            result  =
                adorner.nameStandardizer.standardizeProperName( spelling );
        }
        else if (   adorner.partOfSpeechTags.isNounTag( partOfSpeech )  &&
                    CharUtils.hasInternalCaps( spelling ) )
        {
        }
        else if ( adorner.partOfSpeechTags.isForeignWordTag( partOfSpeech ) )
        {
        }
        else if ( adorner.partOfSpeechTags.isNumberTag( partOfSpeech ) )
        {
            if ( RomanNumeralUtils.isLooseRomanNumeral( result ) )
            {
                if ( result.charAt( 0 ) == '.' )
                {
                    result  = result.substring( 1 );
                }

                if ( result.charAt( result.length() - 1 ) == '.' )
                {
                    result  =
                        result.substring( 0 , result.length() - 1 );
                }
            }
        }
        else
        {
            result  =
                adorner.spellingStandardizer.standardizeSpelling
                (
                    spelling ,
                    adorner.partOfSpeechTags.getMajorWordClass
                    (
                        partOfSpeech
                    )
                );

            if ( result.equalsIgnoreCase( spelling ) )
            {
                result  = spelling;
            }
        }

        return result;
    }

    /** Generate a KWIC line for a word in a sentence.
     *
     *  @param  sentence    The sentence as an array list.
     *  @param  wordIndex   The index of the word for which to generate
     *                      a KWIC.
     *  @param  KWICWidth   Maximum width (in characters) of KWIC text.
     *
     *  @return             The KWIC sections as a String array.
     *                      [0] = left KWIC text
     *                      [1] = word
     *                      [2] = right KWIC text
     */

    public static String[] getKWIC
    (
        List<AdornedWord> sentence ,
        int wordIndex ,
        int KWICWidth
    )
    {
        String[] results        = new String[ 3 ];

        StringBuffer KWICBuffer = new StringBuffer();

        AdornedWord KWICWord    = sentence.get( wordIndex );

        int l                   = 0;

        int maxWidth            =
            ( KWICWidth - 4 - KWICWord.getToken().length() ) / 2;

        int i                   = wordIndex - 1;

        while ( ( l < maxWidth ) && ( i >= 0 ) )
        {
            AdornedWord adornedWord = sentence.get( i );

            if ( KWICBuffer.length() > 0 )
            {
                KWICBuffer.insert( 0 , " " );
            }

            KWICBuffer.insert( 0 , adornedWord.getToken() );

            l   += adornedWord.getToken().length() + 1;

            i--;
        }

        results[ 0 ]    = KWICBuffer.toString();
        results[ 1 ]    = KWICWord.getToken();

        KWICBuffer.setLength( 0 );

        i               = wordIndex + 1;
        int nWords      = sentence.size();

        while ( ( KWICBuffer.length() < maxWidth ) && ( i < nWords ) )
        {
            AdornedWord adornedWord = sentence.get( i );

            KWICBuffer.append( adornedWord.getToken() );
            KWICBuffer.append( " " );

            i++;
        }

        results[ 2 ]    = KWICBuffer.toString();

        return results;
    }

    /** Get count of words in a list of sentences.
     *
     *  @param  sentences   List of sentences each containing list of words.
     *
     *  @return             Total number of words in sentences.
     */

    public static int getWordCount( List<List<String>> sentences )
    {
        int result  = 0;

        for ( int i = 0 ; i < sentences.size() ; i++ )
        {
            result  +=  (sentences.get( i )).size();
        }

        return result;
    }

    /** Get actual word and sentence count.
     *
     *  @param  sentences   List of sentences each containing list of words.
     *
     *  @return             Two word int array of sentence and word counts.
     *                          [0] = # of sentences
     *                          [1] = # of words
     *  <p>
     *  Sentences and words containing only the special separator marker
     *  character are not counted.
     *  </p>
     */

    public static int[] getWordAndSentenceCounts
    (
        List<List<String>> sentences
    )
    {
        int result[]    = new int[ 2 ];

        result[ 0 ] = 0;
        result[ 1 ] = 0;

        for ( int i = 0 ; i < sentences.size() ; i++ )
        {
            List<String> sentence   = sentences.get( i );

            int wordCount   = sentence.size();
            boolean done    = false;

            while ( !done )
            {
                String word = sentence.get( wordCount - 1 );

                if (    word.equals(
                    CharUtils. CHAR_END_OF_TEXT_SECTION_STRING ) )
                {
                    wordCount--;
                }
                else
                {
                    break;
                }

                done    = ( wordCount < 1 );
            }

            if ( wordCount > 0 )
            {
                result[ 0 ]++;
            }

            result[ 1 ] +=  wordCount;
        }

        return result;
    }

    /** Get word list.
     *
     *  @param      wordFileName        File name of word list.
     *  @param      posTag              Part of speech tag for each word.
     *  @param      loadedMessage       Message to display when words loaded.
     *  @param      adornerSettings     The adorner settings.
     *  @param      adornerLogger       The adorner logger.
     *
     *  @return     Tagged strings with words.
     */

    public static TaggedStrings getWordList
    (
        String wordFileName ,
        String posTag ,
        String loadedMessage ,
        MorphAdornerSettings adornerSettings ,
        MorphAdornerLogger adornerLogger
    )
    {
        long startTime      = System.currentTimeMillis();

                                //  Load words.
        TextFile wordFile   =
            new TextFile
            (
                MorphAdornerUtils.class.getResourceAsStream
                (
                    wordFileName
                ) ,
                "utf-8"
            );

        SingleTagTaggedStrings words    =
            new SingleTagTaggedStrings( wordFile.toArray() , posTag );

        wordFile    = null;

        adornerLogger.println
        (
            loadedMessage ,
            new Object[]
            {
                Formatters.formatIntegerWithCommas
                (
                    words.getStringCount()
                ) ,
                MorphAdornerUtils.durationString
                (
                    adornerSettings ,
                    startTime
                )
            }
        );

        return words;
    }

    /** Get extra words list.
     *
     *  @param      wordFileName        File name of word list.
     *  @param      posTag              Part of speech tag for each word.
     *  @param      loadedMessage       Message to display when words loaded.
     *  @param      adornerSettings     The adorner settings.
     *  @param      adornerLogger       The adorner logger.
     *
     *  @return     Tagged strings with words.
     */

    public static TaggedStrings getExtraWordsList
    (
        String wordFileName ,
        String posTag ,
        String loadedMessage ,
        MorphAdornerSettings adornerSettings ,
        MorphAdornerLogger adornerLogger
    )
    {
        long startTime          = System.currentTimeMillis();

        UTF8Properties words    = null;

                                //  Load extra words.
        try
        {
            words   = new UTF8Properties();

            words.load
            (
                MorphAdornerUtils.class.getResourceAsStream
                (
                    wordFileName
                ) ,
                posTag
            );
        }
        catch ( Exception e )
        {
        }

        if ( words.size() > 0 )
        {
            adornerLogger.println
            (
                loadedMessage ,
                new Object[]
                {
                    Formatters.formatIntegerWithCommas
                    (
                        words.getStringCount()
                    ) ,
                    durationString
                    (
                        adornerSettings ,
                        startTime
                    )
                }
            );
        }

        return words;
    }

    /** Check if file is already adorned.
     *
     *  @param  xmlFileName         File to check for being adorned.
     *  @param  maxLinesToCheck     Maximum # of lines to read looking
     *                              for a "<w" element.
     */

    public static boolean isAdorned
    (
        String xmlFileName ,
        int maxLinesToCheck
    )
    {
                                //  Assume file is not adorned.

        boolean result  = false;

                                //  Open file.
        try
        {
            BufferedReader bufferedReader   =
                new BufferedReader
                (
                    new UnicodeReader
                    (
                        new FileInputStream( xmlFileName ) ,
                        "utf-8"
                    )
                );

                                //  Lines read so far.

            int linesRead   = 0;

                                //  Loop until we find a word element
                                //  or we exceed the number of lines
                                //  to check.

            String line = bufferedReader.readLine();

            while
            (
                ( line != null ) &&
                !result &&
                ( linesRead < maxLinesToCheck )
            )
            {
                linesRead++;
                                //  Does input line contain <w> tag?

                int wPos    = line.indexOf( "<w " );

                                //  If line contains <w> tag ...

                if ( wPos >= 0 )
                {
                                //  Split <w> text into attributes
                                //  and word text.

                    String[] groupValues    =
                        WordAttributePatterns.wReplacer.matchGroups( line );

                    try
                    {
                                //  Extract word ID.

                        String[] idValues       =
                            WordAttributePatterns.idReplacer.matchGroups(
                                groupValues[ WordAttributePatterns.ATTRS ] );

                        if ( idValues != null )
                        {
                            String id   =
                                idValues[ WordAttributePatterns.IDVALUE ];

                            if ( ( id != null ) && ( id.length() > 0 ) )
                            {

                                //  Found legitimate word ID.
                                //  File has at least one adorned word,
                                //  so assume it is adorned.

                                result  = true;
                                break;
                            }
                        }
                    }
                    catch ( Exception e )
                    {
                    }
                }
                                //  Read next input line.

                line    = bufferedReader.readLine();
            }
                                //  Close input file.

            bufferedReader.close();
        }
        catch ( Exception e )
        {
        }

        return result;
    }

    /** Log current memory usage.
     *
     *  @param  adornerLogger   The adorner logger.
     *  @param  label           Label for memory usage.
     */

    public static void logMemoryUsage
    (
        MorphAdornerLogger adornerLogger ,
        String label
    )
    {
        long freeMem    = runTime.freeMemory();
        long totalMem   = runTime.totalMemory();

        adornerLogger.println
        (
            "Memory_used" ,
            new Object[]
            {
                label ,
                Formatters.formatLongWithCommas( freeMem ) ,
                Formatters.formatLongWithCommas( totalMem )
            }
        );
    }

    /** Allow overrides but not instantiation.
     */

    protected MorphAdornerUtils()
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




