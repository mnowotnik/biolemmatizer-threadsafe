package edu.northwestern.at.morphadorner;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.text.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.megginson.sax.*;

import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.math.*;
import edu.northwestern.at.morphadorner.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.partsofspeech.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.sentencemelder.*;
import edu.northwestern.at.utils.xml.*;

/** XML filter which updates <w> tag IDs and attributes. */

public class IDFixerFilter extends ExtendedXMLFilterImpl
{
    /** Word ID formatters. */

    protected static final NumberFormat ID_FORMATTER =
        NumberFormat.getInstance();

    /** Page number formatter. */

    protected static final NumberFormat PAGE_FORMATTER =
        NumberFormat.getInstance();

    /** Word within page formatter. */

    protected static final NumberFormat WORD_FORMATTER =
        NumberFormat.getInstance();

    static
    {
        PAGE_FORMATTER.setMinimumIntegerDigits( 4 );
        WORD_FORMATTER.setMinimumIntegerDigits( 3 );
        ID_FORMATTER.setMinimumIntegerDigits( 8 );

        PAGE_FORMATTER.setGroupingUsed( false );
        WORD_FORMATTER.setGroupingUsed( false );
        ID_FORMATTER.setGroupingUsed( false );
    }

    /** Word ordinal. */

    protected int wordOrdinal   = 0;

    /** Previous ID value. */

    protected String lastID = "";
    protected String lastIDString   = "";

    /** Current ID value as string. */

    protected String id     = "";

    /** ID attribute name. */

    protected String idAttrName = WordAttributeNames.id;

    /** Base XML file name for generating ID values. */

    protected String baseFileName;

    /** Part of speech tags used in XML file. */

    protected PartOfSpeechTags posTags;

    /** URI for elements without one. */

    protected String elementURI = null;

    /** True to output whitespace elements. */

    protected boolean outputWhitespace  = true;

    /** True to output non-redundant attributes only. */

    protected boolean outputNonredundantAttributesOnly  = false;

    /** True to output non-redundant token attributes only. */

    protected boolean outputNonredundantTokenAttribute  = false;

    /** True to output non-redundant part attributes only. */

    protected boolean outputNonredundantPartAttribute   = false;

    /** True to output non-redundant eos attributes only. */

    protected boolean outputNonredundantEosAttribute    = false;

    /** True to output sentence boundary milestones. */

    protected boolean outputSentenceBoundaryMilestones  = false;

    /** True to use <pc> element to mark end of sentence. */

    protected boolean usePCToMarkEndOfSentence  = false;

    /** True to output page boundary milestones. */

    protected boolean outputPseudoPageBoundaryMilestones    = false;

    /** Page size in number of tokens. */

    protected int pseudoPageSize                = 500;

    /** Current pseudo page count. */

    protected int pseudoPageCount               = 0;

    /** Current pseudo page word count. */

    protected int pseudoPageWordCount           = 0;

    /** True if pseudo page started. */

    protected boolean pseudoPageStarted         = false;

    /** Current number of words emitted. */

    protected int emittedWordCount              = 0;

    /** XML sentence melder. */

    protected XMLSentenceMelder sentenceMelder;

    /** True if we're processing first word in a sentence. */

    protected boolean isFirstWord = false;

    /** Pending word element. */

    protected PendingElement pendingWordElement = null;

    /** Split words map of word ID to # of word parts. */

    protected Map<Integer, Integer> splitWords;

    /** Copy of split words map. */

    protected Map<Integer, Integer> splitWordsCopy;

    /** Foreign language attribute stack. */

    protected QueueStack<String> foreignStack   = new QueueStack<String>();

    /** Jump tag stack.   Saves state across jump tags. */

    protected QueueStack<XMLWriterState> jumpStack  =
        new QueueStack<XMLWriterState>();

    /** Div tag stack. */

    protected QueueStack<String> divStack   = new QueueStack<String>();

    /** Pseudo-page ending div types. */

    protected Set<String> pseudoPageContainerDivTypes   =
        SetFactory.createNewSet();

    /** Sorted sentence and word number information. */

    protected SortedArrayList<SentenceAndWordNumber> sortedWords;

    /** XML writer. */

    protected XMLWriter writer;

    /** Total number of words to emit. */

    protected int totalWordsToEmit  = 0;

    /** Running page number.  Starts at 0. */

    protected int pageNumber    = 0;

    /** Word within page number.  Starts at 1. */

    protected int wordNumberWithinPage  = 0;

    /** "n" from current <pb> element. */

    protected String nFromPB    = "";

    /** "facs" or "ref" from current <pb> element. */

    protected String facsFromPB = "";

    /** "facs" or "ref" from previous <pb> element. */

    protected String prevFacsFromPB = "";

    /** Column (0-based) within current page based upon repeated
     *  facs= value of pb.
     */

    protected int pageColumn    = 0;

    /** ID Spacing. */

    protected int idSpacing = 10;

    /** ID Type. */

    protected MorphAdornerSettings.XMLIDType idType =
        MorphAdornerSettings.XMLIDType.READING_CONTEXT_ORDER;

    /** Emit token label? */

    protected boolean xmlTokenLabelEmit = false;

    /** Token label attribute. */

    protected String xmlTokenLabelAttribute = "n";

    /** Token label spacing. */

    protected int xmlTokenLabelSpacing  = 10;

    /** Token label prepend work name flag. */

    protected boolean xmlTokenLabelPrependWorkName  = false;

    /** Output word ordinal. */

    protected boolean outputWordOrdinal = true;

    /** Map of XML language tags to language name. */

    protected static Map<String, String> languageTags   =
        new TreeMap<String, String>();

    /** Gap count. */

    protected int gapCount  = 0;

    /** MorphAdorner settings. */

    protected MorphAdornerSettings morphAdornerSettings = null;

    /** True if only doing tokenization. */

    protected boolean tokenizingOnly    = false;

    /** "zzzzlj" tag found. */

    protected boolean zzzzljTagSeen     = false;

    /** Create ID filter.
     *
     *  @param  reader              The XML reader to filter.
     *  @param  posTags             The part of speech tags.
     *  @param  outFile             The output file name.
     *  @param  maxID               The maximum integer word ID.
     *  @param  sortedWords         Sentence and word numbers sorted by word ID.
     *  @param  splitWords          Split words.
     *  @param  totalWords          Total words.
     *  @param  totalPageBreaks     Total page breaks.
     *  @param  tokenizingOnly      True if only emitting tokenization.
     */

    public IDFixerFilter
    (
        XMLReader reader ,
        PartOfSpeechTags posTags ,
        String outFile ,
        int maxID ,
        SortedArrayList<SentenceAndWordNumber> sortedWords ,
        Map<Integer, Integer> splitWords ,
        int totalWords ,
        int totalPageBreaks ,
        MorphAdornerSettings morphAdornerSettings ,
        boolean tokenizingOnly
    )
    {
        super( reader );
                                //  Save MorphAdorner settings.

        this.morphAdornerSettings   = morphAdornerSettings;

                                //  Save ID attribute name.

        this.idAttrName =
            morphAdornerSettings.xgOptions.getIdArgumentName();

                                //  Output non-redundant attributes
                                //  only.

        this.outputNonredundantAttributesOnly   =
            morphAdornerSettings.outputNonredundantAttributesOnly;

                                //  Output non-redundant token attribute
                                //  only.

        this.outputNonredundantTokenAttribute   =
            morphAdornerSettings.outputNonredundantTokenAttribute;

                                //  Output non-redundant part attribute
                                //  only.

        this.outputNonredundantPartAttribute    =
            morphAdornerSettings.outputNonredundantPartAttribute;

                                //  Output non-redundant eos attribute
                                //  only.

        this.outputNonredundantEosAttribute =
            morphAdornerSettings.outputNonredundantEosAttribute;

                                //  Output sentence boundary milestones.

        this.outputSentenceBoundaryMilestones   =
            morphAdornerSettings.outputSentenceBoundaryMilestones;

                                //  Use <pc> element to mark end of
                                //  sentence.

        this.usePCToMarkEndOfSentence   =
            morphAdornerSettings.usePCToMarkEndOfSentence;

                                //  Output word ordinal attribute.

        this.outputWordOrdinal  =
            morphAdornerSettings.outputWordOrdinal;

                                //  Output word token label.

        this.xmlTokenLabelEmit  =
            morphAdornerSettings.xmlTokenLabelEmit;

                                //  Token label attribute.

        this.xmlTokenLabelAttribute =
            morphAdornerSettings.xmlTokenLabelAttribute;

                                //  Token label spacing.

        this.xmlTokenLabelSpacing   =
            morphAdornerSettings.xmlTokenLabelSpacing;

                                //  Token label prepend work name.

        this.xmlTokenLabelPrependWorkName   =
            morphAdornerSettings.xmlTokenLabelPrependWorkName;

                                //  Output pseudo page boundary milestones.

        this.outputPseudoPageBoundaryMilestones =
            morphAdornerSettings.outputPseudoPageBoundaryMilestones;

                                //  Pseudo page length in tokens.

        this.pseudoPageSize     =
            morphAdornerSettings.pseudoPageSize;

                                //  Pseudo page ending div types.

        String[] divTypes   =
            StringUtils.makeTokenArray
            (
                morphAdornerSettings.pseudoPageContainerDivTypes
            );

        for ( int i = 0 ; i < divTypes.length ; i++ )
        {
            this.pseudoPageContainerDivTypes.add( divTypes[ i ].toLowerCase() );
        }
                                //  Save output whitespace option.

        this.outputWhitespace   =
            morphAdornerSettings.outputWhitespaceElements;

                                //  Set sorted words.  Will be set here. */

        this.sortedWords        = sortedWords;

                                //  Set split words.

        setSplitWords( splitWords );

                                //  Save part of speech tags.

        setPosTags( posTags );

                                //  Set word ID format from output
                                //  file name, maximum integer word ID,
                                //  and maximum number of page breaks.

        setIDFormat( outFile , maxID , totalPageBreaks );

                                //  Total words.

        this.totalWordsToEmit   = totalWords;

        elementURI      = null;

                                //  Create list to hold sentence and
                                //  word number information.
        sortedWords         =
            new SortedArrayList<SentenceAndWordNumber>();

                                //  Remember if we're just doing
                                //  tokenization.

        this.tokenizingOnly = tokenizingOnly;
    }

    /** Generate token label based upon current pb element attributes.
     */

    protected String generateTokenLabel()
    {
        String n    = StringUtils.safeString( nFromPB );

        if ( n.length() == 0 )
        {
            n   = "0";
        }

        String facs = StringUtils.safeString( facsFromPB );

        if ( facs.length() > 0 )
        {
            facs    = facs + "-";
        }

        String wName    = "";

        if ( xmlTokenLabelPrependWorkName )
        {
            if ( baseFileName.length() > 0 )
            {
                wName   = baseFileName + "-";
            }
        }

        String sPageColumn  = (char)( (int)'a' + pageColumn ) + "-";

        return
            wName +
            facs +
            sPageColumn +
//          PAGE_FORMATTER.format( pageNumber ) + "-" +
//          n + "-" +
            WORD_FORMATTER.format( wordNumberWithinPage *
                xmlTokenLabelSpacing );
    }

    /** Handle start of an XML element.
     */

    public void startElement
    (
        String uri ,
        String localName ,
        String qName ,
        Attributes atts
    )
        throws SAXException
    {
                                //  Copy existing attributes for
                                //  this XML element.

        AttributesImpl newAtts  = new AttributesImpl( atts );

                                //  Assume we will pass on this
                                //  element for immediate output.

        boolean outputNow   = true;

                                //  See if we have a path (p=) attribute.

        String p    = atts.getValue( WordAttributeNames.p );

                                //  Get the language attribute and
                                //  push it onto the foreign language
                                //  stack.

        foreignStack.push( getForeignLanguageTag( qName , atts ) );

                                //  Add document name to front of path.

        if ( ( p != null ) && ( p.length() > 0 ) )
        {
            setAttributeValue(
                newAtts, WordAttributeNames.p ,
                "\\" + baseFileName + p );
        }
                                //  Eject "TEIform=" attribute.

        String teiform  = atts.getValue( "TEIform" );

        if ( ( teiform != null ) && ( teiform.length() > 0 ) )
        {
            removeAttribute( newAtts, "TEIform" );
        }
                                //  If we have a page break,
                                //  increment the page count,
                                //  and get values for "n="
                                //  and "facs=" (or "ref=") attributes
                                //  if specified.

        if ( qName.equalsIgnoreCase( "pb" ) )
        {
            pageNumber++;
            wordNumberWithinPage    = 0;

            nFromPB     = atts.getValue( "n" );

            if ( nFromPB == null )
            {
                nFromPB = atts.getValue( "N" );
            }

            facsFromPB  = atts.getValue( "facs" );

            if ( facsFromPB == null )
            {
                facsFromPB  = atts.getValue( "ref" );
            }

            if ( facsFromPB == null )
            {
                facsFromPB  = atts.getValue( "REF" );
            }

            facsFromPB  = StringUtils.safeString( facsFromPB );

                                //  If pb is for same page as last pb,
                                //  increment the column, otherwise
                                //  set the column back to 0.

            if ( facsFromPB.length() > 0 )
            {
                if ( facsFromPB.equals( prevFacsFromPB ) )
                {
                    pageColumn++;
                }
                else
                {
                    pageColumn  = 0;
                }
            }

            prevFacsFromPB  = facsFromPB;
        }
                                //  If we have a word tag element ...

        if  (   qName.equalsIgnoreCase( "w" ) ||
                qName.equalsIgnoreCase( "pc" )
            )
        {
                                //  Reset gap count.

            gapCount    = 0;
                                //  Don't output word element now,
                                //  but wait until we are able to
                                //  pick up its text.

            outputNow   = false;

                                //  Get existing word attribute values.

            id          = atts.getValue( idAttrName );
            String tok  = atts.getValue( WordAttributeNames.tok );
            String spe  = atts.getValue( WordAttributeNames.spe );
            String pos  = atts.getValue( WordAttributeNames.pos );
            String eos  = atts.getValue( WordAttributeNames.eos );
            String lem  = atts.getValue( WordAttributeNames.lem );
            String reg  = atts.getValue( WordAttributeNames.reg );
            String part = atts.getValue( WordAttributeNames.part );
            String rend = atts.getValue( WordAttributeNames.rend );
            String type = atts.getValue( WordAttributeNames.type );
            String unit = atts.getValue( WordAttributeNames.unit );

                                //  Clean tok attribute value of
                                //  any special milestone characters.
            tok         =
                StringUtils.replaceAll
                (
                    tok ,
                    CharUtils.CHAR_FAKE_SOFT_HYPHEN_STRING ,
                    "-"
                );
                                //  Convert nonbreaking blanks to
                                //  regular blanks.
/*
            tok         =
                StringUtils.replaceAll
                (
                    tok ,
                    CharUtils.NONBREAKING_BLANK_STRING ,
                    " "
                );
*/
                                //  Clean tok, spe, and lem of
                                //  sup text marker string.

            if ( tok.indexOf( CharUtils.CHAR_SUP_TEXT_MARKER_STRING ) >= 0 )
            {
                tok         =
                    StringUtils.replaceAll
                    (
                        tok ,
                        CharUtils.CHAR_SUP_TEXT_MARKER_STRING ,
                        ""
                    );

                if ( spe != null )
                {
                    spe         =
                        StringUtils.replaceAll
                        (
                            spe ,
                            CharUtils.CHAR_SUP_TEXT_MARKER_STRING ,
                            ""
                        );
                }

                if ( lem != null )
                {
                    lem         =
                        StringUtils.replaceAll
                        (
                            lem ,
                            CharUtils.CHAR_SUP_TEXT_MARKER_STRING ,
                            ""
                        );
                    }
            }
                                //  Integer version of word ID.

            int thisID  = Integer.parseInt( id );

                                //  Remember if word ID changed from
                                //  previous word.

            boolean idChanged   = !id.equals( lastID );

                                //  If we have a pending word element,
                                //  and the word ID changed,
                                //  emit the pending
                                //  word element now.

            if ( ( pendingWordElement != null ) && idChanged )
            {
                emitWordElement
                (
                    pendingWordElement.getURI() ,
                    pendingWordElement.getLocalName() ,
                    pendingWordElement.getQName() ,
                    pendingWordElement.getAttributes() ,
                    pendingWordElement.getText() ,
                    true ,
                    false
                );
                                //  Clear pending word.

                pendingWordElement  = null;
            }
                                //  Is this a split word?

            if ( splitWords.containsKey( thisID )  )
            {
                                //  Yes.  Emit part="I" for first
                                //  part, part="F" for last part,
                                //  and part="M" for any middle parts.

                int nParts  = splitWordsCopy.get( thisID );

                if ( nParts == splitWords.get( thisID ) )
                {
                    part    = "I";

                                //  Increment word count within page
                                //  only for first word part.

                    wordNumberWithinPage++;
                }
                else if ( nParts <= 1 )
                {
                    part    = "F";
                }
                else
                {
                    part    = "M";
                }

                nParts--;

                splitWordsCopy.put( thisID , nParts );
            }
                                //  Not a split word -- part is "N".
            else
            {
                part    = "N";
                                //  Increment word count within page.

                wordNumberWithinPage++;
            }
                                //  Generate word ID.

            String n    = StringUtils.safeString( nFromPB );

            if ( n.length() == 0 )
            {
                n   = "0";
            }

            String facs = StringUtils.safeString( facsFromPB );

            String idString = baseFileName + "-";

            switch ( idType )
            {
                case READING_CONTEXT_ORDER:
                    idString    +=
                        ID_FORMATTER.format( thisID * idSpacing );
                    break;

                case WORD_WITHIN_PAGE_BLOCK:
                    idString    +=
                        PAGE_FORMATTER.format( pageNumber ) + "-" +
                        WORD_FORMATTER.format( wordNumberWithinPage *
                        idSpacing );
                        break;

                case USE_PAGE_BLOCK_ATTRIBUTES:
                    idString    += generateTokenLabel();
            }
                                //  Split words get a ".partnumber"
                                //  added to the end of the ID.

            if ( !part.equals( "N" ) )
            {
                int partNumber  =
                    splitWords.get( thisID ) -
                    splitWordsCopy.get( thisID );

                idString    = idString + "." + partNumber;
            }
                                //  Set ID attribute value.

            setAttributeValue( newAtts , idAttrName , idString );

                                //  Generate word label.

            if ( xmlTokenLabelEmit )
            {
                setAttributeValue(
                    newAtts, xmlTokenLabelAttribute , generateTokenLabel() );
            }
                                //  Generate word ordinal.

            if ( idChanged ) ++wordOrdinal;

                                //  Generate word ordinal attribute value.

            if ( outputWordOrdinal )
            {
                setAttributeValue(
                    newAtts, WordAttributeNames.ord , wordOrdinal + "" );
            }
                                //  Remember this word's ID.

            lastID          = id;
            lastIDString    = idString;

                                //  If we're in an element with a foreign
                                //  language attribute, and the tag
                                //  isn't for the main language,
                                //  reset the part of speech  to reflect
                                //  this word is foreign.   Also reset the
                                //  lemma to the spelling.

            if  (   !foreignStack.isEmpty() &&
                    ( foreignStack.peek().length() > 0 )
                )
            {
                if (    foreignStack.peek().equals( "fw-en" ) ||
                        posTags.isNumberTag( pos ) ||
                        posTags.isSymbolTag( pos ) ||
                        posTags.isPunctuationTag( pos )
                    )
                {
                }
                else
                {
                    pos = foreignStack.peek();
                    lem = spe;
                }
            }
                                //  If we're in a div with a
                                //  type that has a special part
                                //  of speech tagger attached, reset
                                //  the part of speech to the unknown
                                //  part of speech, and the lemma to
                                //  the spelling.

            if  (   !divStack.isEmpty() &&
                    ( divStack.peek().length() > 0 )
                )
            {
                if (    !divStack.peek().equals( "errata" ) ||
                        posTags.isNumberTag( pos ) ||
                        posTags.isSymbolTag( pos ) ||
                        posTags.isPunctuationTag( pos )
                    )
                {
                }
                else
                {
                    pos = "zz";
                    lem = spe;
                }
            }
                                //  Handle missing attributes.

            if ( spe == null )
            {
                spe = tok;
            }

            if ( pos == null )
            {
                pos = spe;
            }

            if ( lem == null )
            {
                lem = spe;
            }

            if ( eos == null )
            {
                eos = "0";
            }

            if ( reg == null )
            {
                reg = spe;
            }
                                //  Save updated attributes.
/*
            if ( morphAdornerSettings.outputEOSFlag )
            {
                setAttributeValue(
                    newAtts , WordAttributeNames.eos , eos );
            }
            else
            {
                removeAttribute( newAtts , WordAttributeNames.eos );
            }
*/
            setAttributeValue(
                newAtts , WordAttributeNames.eos , eos );

            if ( !tokenizingOnly && morphAdornerSettings.outputLemma )
            {
                setAttributeValue(
                    newAtts , WordAttributeNames.lem , lem );
            }
            else
            {
                removeAttribute( newAtts , WordAttributeNames.lem );
            }

            if ( !tokenizingOnly && morphAdornerSettings.outputPartOfSpeech )
            {
                setAttributeValue(
                    newAtts , WordAttributeNames.pos , pos );
            }
            else
            {
                removeAttribute( newAtts , WordAttributeNames.pos );
            }

            if ( !tokenizingOnly && morphAdornerSettings.outputStandardSpelling )
            {
                setAttributeValue(
                    newAtts , WordAttributeNames.reg , reg );
            }
            else
            {
                removeAttribute( newAtts , WordAttributeNames.reg );
            }

            if ( !tokenizingOnly && morphAdornerSettings.outputSpelling )
            {
                setAttributeValue(
                    newAtts , WordAttributeNames.spe , spe );
            }
            else
            {
                removeAttribute( newAtts , WordAttributeNames.spe );
            }

            if ( !tokenizingOnly && morphAdornerSettings.outputOriginalToken )
            {
                setAttributeValue(
                    newAtts , WordAttributeNames.tok , tok );
            }
            else
            {
                removeAttribute( newAtts , WordAttributeNames.tok );
            }

            setAttributeValue(
                newAtts , WordAttributeNames.part , part );

            if ( rend != null )
            {
                setAttributeValue(
                    newAtts , WordAttributeNames.rend , rend );
            }

            if ( type != null )
            {
                setAttributeValue(
                    newAtts , WordAttributeNames.type , type );
            }

            if ( unit != null )
            {
                setAttributeValue(
                    newAtts , WordAttributeNames.unit , unit );
            }
                                //  Remove redundant attributes
                                //  if requested.

            if ( outputNonredundantAttributesOnly )
            {
                if ( eos.equals( "0" ) )
                {
                    removeAttribute
                    (
                        newAtts ,
                        WordAttributeNames.eos
                    );
                }

                if ( spe.equals( tok ) )
                {
                    removeAttribute
                    (
                        newAtts ,
                        WordAttributeNames.spe
                    );
                }

                if ( lem.equals( spe ) )
                {
                    removeAttribute
                    (
                        newAtts ,
                        WordAttributeNames.lem
                    );
                }

                if ( pos.equals( spe ) )
                {
                    removeAttribute
                    (
                        newAtts ,
                        WordAttributeNames.pos
                    );
                }

                if ( reg.equals( spe ) )
                {
                    removeAttribute
                    (
                        newAtts ,
                        WordAttributeNames.reg
                    );
                }

                if ( part.equals( "N" ) )
                {
                    removeAttribute
                    (
                        newAtts ,
                        WordAttributeNames.part
                    );
                }
            }
                                    //  Output whitespace milestone
                                    //  if needed before this word.
                                    //  For multipart words, we only
                                    //  do this for the first part of
                                    //  the multipart word.
            if ( idChanged )
            {
                if ( outputWhitespace )
                {
                    if ( sentenceMelder.shouldOutputBlank(
                        spe , isFirstWord ) )
                    {
                        sentenceMelder.outputBlank();
                    }

                    sentenceMelder.processWord( spe );
                }

                isFirstWord = eos.equals( "1" );
            }
                                //  Save this word element as
                                //  pending so we can examine its
                                //  text before emitting it.

            pendingWordElement  =
                new PendingElement(
                    uri  , localName , qName , newAtts );
        }
        else
        {
                                //  Emit any pending word element.

            if ( pendingWordElement != null )
            {
                emitWordElement
                (
                    pendingWordElement.getURI() ,
                    pendingWordElement.getLocalName() ,
                    pendingWordElement.getQName() ,
                    pendingWordElement.getAttributes() ,
                    pendingWordElement.getText() ,
                    true ,
                    false
                );
                                //  Clear pending word.

                pendingWordElement  = null;

                gapCount            = 0;
            }
                                //  Add an xml:id to a gap tag
                                //  that doesn't already have one.

            if ( qName.equalsIgnoreCase( "gap" ) )
            {
                String gapID    = atts.getValue( idAttrName );

                                //  Add xml:id for gap based upon
                                //  last word ID.  If no last word ID,
                                //  use formatted zero value.

                if ( ( gapID == null ) || ( gapID.length() == 0 ) )
                {
                    if ( lastIDString.length() == 0 )
                    {
                                //  Generate gap ID.

                        String firstIDString    = baseFileName + "-";

                        switch ( idType )
                        {
                            case READING_CONTEXT_ORDER:
                                firstIDString   +=
                                    ID_FORMATTER.format( 0 );
                            break;

                            case WORD_WITHIN_PAGE_BLOCK:
                                firstIDString   +=
                                    PAGE_FORMATTER.format( pageNumber ) +
                                    "-" +
                                    WORD_FORMATTER.format( 0 );
                            break;

                            case USE_PAGE_BLOCK_ATTRIBUTES:
                                firstIDString   += generateTokenLabel();
                        }

                        gapID   = firstIDString + "-gap" + gapCount;
                    }
                    else
                    {
                        gapID   = lastIDString + "-gap" + gapCount;
                    }

                    gapCount++;

                    setAttributeValue( newAtts , "xml:id" , gapID );

                                //  Add token label to gap if requested.

                    if ( xmlTokenLabelEmit )
                    {
                                //  Increment "word" count within page.
                                //  The gap counts as a word in this
                                //  case.

                        wordNumberWithinPage++;

                                //  Add label to gap.

                        setAttributeValue(
                            newAtts, xmlTokenLabelAttribute ,
                            generateTokenLabel() );
                    }
                }
            }
                                //  Note if we have a div tag.  Save
                                //  the div type if given, otherwise
                                //  save "*div".

            else if ( qName.equalsIgnoreCase( "div" ) )
            {
                String divType  = atts.getValue( "type" );

                if ( divType == null )
                {
                    divType = atts.getValue( "TYPE" );
                }

                if ( ( divType == null ) || ( divType.length() == 0 ) )
                {
                    divType = "*div";
                }

                divStack.push( divType.toLowerCase() );
            }
                                //  If we have a foreign tag element ...

            else if ( qName.equalsIgnoreCase( "foreign" ) )
            {
            }
                                //  Reset the sentence melder at the
                                //  start of a jump or hard tag.

            else if ( !morphAdornerSettings.xgOptions.isSoftTag( qName ) )
            {
                                //  If we have a jump tag, save the state
                                //  of the sentence melder so we can
                                //  restore it when the jump tag ends.

                if ( morphAdornerSettings.xgOptions.isJumpTag( qName ) )
                {
                    jumpStack.push
                    (
                        new XMLWriterState
                        (
                            isFirstWord ,
                            sentenceMelder
                        )
                    );
                }
                                //  Reset the sentence melder.

                sentenceMelder.reset();
                isFirstWord = true;
            }
        }

        if ( elementURI == null )
        {
            elementURI  = uri;

            if ( outputWhitespace )
            {
                sentenceMelder.setURI( elementURI );
            }
        }
                                //  Hold <w> element and its text
                                //  but let others through.
        if ( outputNow )
        {
            if ( !qName.startsWith( "zzzz" ) )
            {
                super.startElement( uri , localName , qName , newAtts );
            }
            else
            {
/*
                zzzzljTagSeen   = qName.equals( "zzzzlj" );

                if ( !zzzzljTagSeen )
                {
                    sentenceMelder.outputBlank();
                }

                zzzzljTagSeen   = false;
*/
                if ( qName.equals( "zzzzbl" ) )
                {
                    sentenceMelder.outputBlank();
                }

                else if ( !qName.equals( "zzzzlj" ) )
                {
                    sentenceMelder.outputBlank();
                }
            }
        }
    }

    /** Handle character data.
     *
     *  @param  ch      Array of characters.
     *  @param  start   The starting position in the array.
     *  @param  length  The number of characters.
     *
     *  @throws org.xml.sax.SAXException If there is an error.
     */

    public void characters( char ch[] , int start , int length )
        throws SAXException
    {
                                //  If there is a pending word element,
                                //  this is text for that word.

        if ( pendingWordElement != null )
        {
            pendingWordElement.appendText( ch , start , length );
        }
                                //  Otherwise just pass on the text
                                //  for immediate output.
        else
        {
            super.characters( ch , start , length );
        }
    }

    /** Emit a word element.
     *
     *  @param  uri             The word element's URI.
     *  @param  localName       The word element's local name.
     *  @param  qName           The word element's qname.
     *  @param  atts            The word element's attributes.
     *  @param  wordText        The word element's text.
     *  @param  allowOutputWhitespace   True to allow outputting
     *                                  whitespace element for word.
     *  @param  forceEOS        True to force end of sentence
     *                          for this word.
     */

    public void emitWordElement
    (
        String uri ,
        String localName ,
        String qName ,
        AttributesImpl atts ,
        String wordText ,
        boolean allowOutputWhitespace ,
        boolean forceEOS
    )
        throws SAXException
    {
                                //  See if we have a path (p=) attribute.

        String p    = atts.getValue( WordAttributeNames.p );

                                //  Get "part=" attribute value.

        String part = atts.getValue( WordAttributeNames.part );

                                //  Is this first or only word part
                                //  for this word?

        boolean isFirstWordPart =
            ( part == null ) || part.equals( "N" ) || part.equals( "I" );

                                //  Is this last or only word part
                                //  for this word?

        boolean isLastWordPart  =
            ( part == null ) || part.equals( "N" ) || part.equals( "F" );

                                //  Create start pseudopage element if
                                //  this is the first word in a
                                //  pseudopage.

        if  (   isLastWordPart &&
                outputPseudoPageBoundaryMilestones &&
                ( pseudoPageWordCount == 0 ) &&
                ( !pseudoPageStarted )
            )
        {
            if ( ( p != null ) && ( p.length() > 0 ) )
            {
                int bsPos   = p.lastIndexOf( "\\" );

                if ( bsPos > 0 )
                {
                    p   = p.substring( 0 , bsPos );
                }

                p   = p + "\\milestone[" + ( pseudoPageCount + 1 ) + "]";
            }

            emitPseudoPageElement
            (
                createPseudoPageElement
                (
                    uri ,
                    false ,
                    true ,
                    p
                )
            );
        }
                                //  Increment count of words in current
                                //  pseudopage.

        pseudoPageWordCount++;
                                //  Assume we don't have a word
                                //  with an eos set to "1" indicating
                                //  and end of sentence boundary.

        boolean emitSentenceBoundary    = false;

                                //  Force end of sentence flag true
                                //  if requested.
        if ( forceEOS )
        {
            setAttributeValue( atts , WordAttributeNames.eos , "1" );
        }
                                //  See if this word is the end if
                                //  the sentence.

        String eos  = atts.getValue( WordAttributeNames.eos );

        emitSentenceBoundary    = ( eos != null ) && eos.equals( "1" );

                                //  Remove eos= attribute if we're not
                                //  emitting it.

        if ( !morphAdornerSettings.outputEOSFlag )
        {
            removeAttribute( atts , WordAttributeNames.eos );
        }
                                //  If the word token is the same as
                                //  the word text, and we are
                                //  outputting abbreviated attributes,
                                //  remove the redundant token
                                //  attribute.

        if  (   outputNonredundantAttributesOnly ||
                outputNonredundantTokenAttribute )
        {
                                //  Get token attribute value.

            String tok  = atts.getValue( WordAttributeNames.tok );

                                //  If word text same as token text,
                                //  remove tok attribute.

            if ( ( tok != null) && tok.equals( wordText ) )
            {
                removeAttribute( atts , WordAttributeNames.tok );
            }
        }
                                //  Remove redundant part= attribute
                                //  if requested.

        if  (   outputNonredundantAttributesOnly ||
                outputNonredundantPartAttribute )
        {
                                //  If part="N", remove part
                                //  attribute.

            if ( ( part != null ) && part.equals( "N" ) )
            {
                removeAttribute( atts , WordAttributeNames.part );
            }
        }
                                //  Remove redundant eos= attribute
                                //  if requested.

        if  (   outputNonredundantAttributesOnly ||
                outputNonredundantEosAttribute )
        {
                                //  If eos="0", remove eos attribute.

            if ( ( eos != null ) && eos.equals( "0" ) )
            {
                removeAttribute( atts , WordAttributeNames.eos );
            }
        }
                                //  If using <pc> element to mark
                                //  end of sentence boundary, and
                                //  this word is the end of a sentence,
                                //  handle this here.

        if ( emitSentenceBoundary && usePCToMarkEndOfSentence )
        {
            removeAttribute( atts , WordAttributeNames.eos );

            if ( qName.equalsIgnoreCase( "pc" ) )
            {
                setAttributeValue( atts , "unit" , "sentence" );
            }
        }
                                //  Remove sentence number and
                                //  word number attributes.  These
                                //  are added back later if needed.

        removeAttribute( atts , WordAttributeNames.sn );
        removeAttribute( atts , WordAttributeNames.wn );

                                //  Output start <w> element.
        super.startElement
        (
            uri ,
            localName ,
            qName ,
            atts
        );
                                //  Output word text.
        wordText    =
            StringUtils.replaceAll
            (
                wordText ,
                CharUtils.CHAR_FAKE_SOFT_HYPHEN_STRING ,
                "-"
            );

        wordText    =
            StringUtils.replaceAll
            (
                wordText ,
                CharUtils.CHAR_SUP_TEXT_MARKER_STRING ,
                ""
            );

        super.characters(
            wordText.toCharArray() , 0 , wordText.length() );

                                //  Output end element.

        super.endElement( uri , localName , qName );

                                //  Save word information for
                                //  generating word and sentence
                                //  numbers later.

        String id   = atts.getValue( idAttrName );
        String ord  = atts.getValue( WordAttributeNames.ord );

        if ( ord == null )
        {
            ord = "0";
        }

        sortedWords.add
        (
            new SentenceAndWordNumber
            (
                id ,
                Integer.parseInt( ord ) ,
                part ,
                emitSentenceBoundary
            )
        );
                                //  Emit empty <pc> element if needed
                                //  to mark end of sentence.

        if ( emitSentenceBoundary && usePCToMarkEndOfSentence )
        {
            if ( !qName.equalsIgnoreCase( "pc" ) )
            {
                                //  Create attributes.

                AttributesImpl pcAtts   = new AttributesImpl();

                setAttributeValue( pcAtts , "unit" , "sentence" );

                                //  Generate "word" ID by adding
                                //  "-eos" to end of previous
                                //  word's ID.

                String pcID = id + "-eos";

                setAttributeValue( pcAtts , "xml:id" , pcID );

                                //  Increment word count within page.

                wordNumberWithinPage++;

                                //  Generate word label if requested.

                if ( xmlTokenLabelEmit )
                {
                    setAttributeValue
                    (
                        pcAtts ,
                        xmlTokenLabelAttribute ,
                        generateTokenLabel()
                    );
                }
                                //  Emit <pc> element.

                super.startElement( uri , "pc" , "pc" , pcAtts );
                super.endElement( uri , "pc" , "pc" );
            }
        }
                                //  Increment count of words emitted.
        emittedWordCount++;
                                //  Output a blank if necessary
                                //  following a word.

        if ( outputWhitespace && allowOutputWhitespace )
        {
            if ( isFirstWord && isLastWordPart )
            {
                sentenceMelder.outputBlank();
            }
        }
                                //  Create end pseudopage element if
                                //  this is the last word in a
                                //  pseudopage.

        if  (   isLastWordPart &&
                outputPseudoPageBoundaryMilestones &&
                (
                    ( pseudoPageWordCount >= pseudoPageSize )
                    ||
                    ( emittedWordCount >= totalWordsToEmit )
                )
            )
        {
            if ( ( p != null ) && ( p.length() > 0 ) )
            {
                int bsPos   = p.lastIndexOf( "\\" );

                if ( bsPos > 0 )
                {
                    p   = p.substring( 0 , bsPos );
                }

                p   = p + "\\milestone[" + ( pseudoPageCount + 1 ) + "]";
            }

            emitPseudoPageElement
            (
                createPseudoPageElement
                (
                    uri ,
                    false ,
                    false ,
                    p
                )
            );
        }
    }

    /** Handle end of an element.
     *
     *  @param  uri         The XML element's URI.
     *  @param  localName   The XML element's local name.
     *  @param  qName       The XML element's qname.
     */

    public void endElement
    (
        String uri ,
        String localName ,
        String qName
    )
        throws SAXException
    {
                                //  Remember if we pop the div stack.

        boolean removedDiv      = false;
        String removedDivType   = "";

                                //  Pop the foreign language
                                //  attribute stack.

        if ( !foreignStack.isEmpty() )
        {
            foreignStack.pop();
        }
                                //  If this the end of a div tag,
                                //  pop the div stack.

        else if ( qName.equalsIgnoreCase( "div" ) )
        {
            if ( !divStack.isEmpty() )
            {
                removedDivType  = divStack.pop();
                removedDiv      = true;
            }
        }
                                //  Figure out what type of tag this is.

        boolean isJumpTag   =
            morphAdornerSettings.xgOptions.isJumpTag( qName );

        boolean isSoftTag   =
            morphAdornerSettings.xgOptions.isSoftTag( qName );

        boolean isHardTag   = !( isJumpTag || isSoftTag );

        boolean isWordTag   =
            (   qName.equalsIgnoreCase( "w" ) ||
                qName.equalsIgnoreCase( "pc" )
            );
                                    //  Output pending word element
                                    //  and its text if this is not
                                    //  a word element.

        if ( ( pendingWordElement != null ) && !isWordTag )
        {
                                    //  See if we have to force this word
                                    //  to be the end of a sentence.
            boolean forceEOS    =
                ( isHardTag &&
                    morphAdornerSettings.closeSentenceAtEndOfHardTag
                )
                ||
                ( isJumpTag &&
                    morphAdornerSettings.closeSentenceAtEndOfJumpTag
                )
                ||
                (
                    qName.equalsIgnoreCase( "sp" ) ||
                    qName.equalsIgnoreCase( "speaker" )
                );

            emitWordElement
            (
                pendingWordElement.getURI() ,
                pendingWordElement.getLocalName() ,
                pendingWordElement.getQName() ,
                pendingWordElement.getAttributes() ,
                pendingWordElement.getText() ,
                isWordTag || isSoftTag ,
                forceEOS
            );
                                //  Clear pending word.

            pendingWordElement  = null;
        }
                                //  Output end element except for word tag.
                                //  We already output the end element for
                                //  a word tag in emitWordElement.
        if ( !isWordTag )
        {
                                //  If tag to eject, eject it,
                                //  otherwise let it through.

            if ( !qName.startsWith( "zzzz" ) )
            {
                super.endElement( uri , localName , qName );
            }
            else
            {
//              zzzzljTagSeen   = false;
/*
                if ( qName.equals( "zzzzbl" ) )
                {
                    sentenceMelder.outputBlank();
                }
*/
            }
        }
                                //  If we are returning from a jump tag,
                                //  restore the previous sentence melder
                                //  state prior to the jump tag's
                                //  appearance.
        if ( isJumpTag )
        {
            if ( !jumpStack.isEmpty() )
            {
                XMLWriterState state    = jumpStack.pop();

                isFirstWord = state.getIsFirstWord();

                sentenceMelder.setState
                (
                    state.getSentenceMelderState()
                );
            }
        }
                                //  Do nothing for other soft tags.
        else if ( isSoftTag )
        {
        }
                                //  If we have a hard tag, reset sentence
                                //  melder.
        else
        {
            sentenceMelder.reset();
            isFirstWord = true;
        }
                                //  If this is the end of a div,
                                //  and it is a pseudopage ending
                                //  div type, make sure we emit
                                //  the end pseudopage after the
                                //  pending word is emitted by
                                //  setting the count of words in
                                //  the current pseudopage larger
                                //  than the size of a pseudo page.

        String p    = null;

        if  (   outputPseudoPageBoundaryMilestones &&
                removedDiv &&
                pseudoPageContainerDivTypes.contains( removedDivType )
            )
        {
            if ( ( p != null ) && ( p.length() > 0 ) )
            {
                int bsPos   = p.lastIndexOf( "\\" );

                if ( bsPos > 0 )
                {
                    p   = p.substring( 0 , bsPos );
                }

                p   = p + "\\milestone[" + ( pseudoPageCount + 1 ) + "]";
            }

            if ( emittedWordCount < totalWordsToEmit )
            {
                emitPseudoPageElement
                (
                    createPseudoPageElement
                    (
                        uri ,
                        false ,
                        false ,
                        p
                    )
                );

                emitPseudoPageElement
                (
                    createPseudoPageElement
                    (
                        uri ,
                        false ,
                        true ,
                        p
                    )
                );
            }
        }
    }

    /** Create a pseudo page milestone.
     *
     *  @param  uri         Element URI.
     *  @param  forcedEmit  Emit pseudo page milestone even if
     *                      not enough words accumulated, as long as
     *                      at least one word in current block.
     *  @param  start       true if starting milestone, false if ending.
     *  @param  path        Path attribute.  May be null.
     *
     *  @return             The pseudo page element.
     */

    public PendingElement createPseudoPageElement
    (
        String uri ,
        boolean forcedEmit ,
        boolean start ,
        String path
    )
    {
                                //  Increment pseudo page count
                                //  if starting new pseudo page.
        if ( start )
        {
            pseudoPageCount++;
            pseudoPageStarted   = true;
        }
        else
        {
            pseudoPageStarted   = false;
        }
                                //  Clear pseudo page word count.

        pseudoPageWordCount = 0;

                                //  Create attributes holder for
                                //  milestone element.

        AttributesImpl pageAttributes   = new AttributesImpl();

                                //  Create "unit=pseudopage" attribute for
                                //  pseudo page count.
        setAttributeValue
        (
            pageAttributes ,
            "unit" ,
            "pseudopage"
        );
                                //  Create "n=" attribute for
                                //  pseudo page count.
        setAttributeValue
        (
            pageAttributes ,
            "n" ,
            pseudoPageCount + ""
        );
                                //  Create "position=" attribute for
                                //  pseudo page count.
        setAttributeValue
        (
            pageAttributes ,
            "position" ,
            ( start ? "start" : "end" )
        );
                                //  Add path attribute if not null.

        if ( ( path != null ) && ( path.length() > 0 ) )
        {
            setAttributeValue
            (
                pageAttributes ,
                WordAttributeNames.p ,
                path
            );
        }
                                //  Create the pseudo page element.
        return
            new PendingElement
            (
                uri  ,
                "milestone" ,
                "milestone" ,
                pageAttributes
            );
    }

    /** Emit a pseudo page milestone.
     *
     *  @param  pseudoPageElement   The pseudo page element to emit.
     */

    public void emitPseudoPageElement( PendingElement pseudoPageElement )
    {
        if ( pseudoPageElement != null )
        {
            try
            {
                super.startElement
                (
                    pseudoPageElement.getURI() ,
                    pseudoPageElement.getLocalName() ,
                    pseudoPageElement.getQName() ,
                    pseudoPageElement.getAttributes()
                );

                super.endElement
                (
                    pseudoPageElement.getURI() ,
                    pseudoPageElement.getLocalName() ,
                    pseudoPageElement.getQName()
                );
            }
            catch ( Exception e )
            {
            }
        }
    }

    /** Set the part of speech tags.
     *
     *  @param  posTags     The part of speech tags.
     */

    public void setPosTags( PartOfSpeechTags posTags )
    {
        this.posTags    = posTags;
    }

    /** Set split words.
     *
     *  @param  splitWords  Map of split words.
     */

    protected void setSplitWords( Map<Integer, Integer> splitWords )
    {
                                //  Save split words map.

        this.splitWords         = splitWords;

                                //  Get a modifyable copy of the
                                //  split words map.

        this.splitWordsCopy     = MapFactory.createNewMap();

        this.splitWordsCopy.putAll( splitWords );
    }

    /** Set word ID format.
     *
     *  @param  outFile         Output file name used to derive word IDs.
     *  @param  maxID           Maximum integer word ID value.
     *  @param  maxPageBreaks   Maximum number of page breaks.
     */

    protected void setIDFormat
    (
        String outFile ,
        int maxID ,
        int maxPageBreaks
    )
    {
                                //  Get base for long word ID values,

                                //  (1) Remove path from file name.

        baseFileName            = FileNameUtils.stripPathName( outFile );

                                //  (2) Remove extension from file name.

        baseFileName            =
            FileNameUtils.changeFileExtension( baseFileName , "" );

                                //  (3) Convert remaining periods to
                                //  underlines.

        baseFileName            =
            StringUtils.replaceAll( baseFileName , "." , "_" );

                                //  Set ID type.

        idType      = morphAdornerSettings.xmlIDType;

                                //  Set ID spacing.

        idSpacing   = morphAdornerSettings.xmlIDSpacing;

                                //  Get number of digits for reading
                                //  context ID.  This is based upon
                                //  the number of digits in the
                                //  largest word ID.

        int numIDDigits     =
            (int)(ArithUtils.log10( (double)( maxID * idSpacing ) ) ) + 1;

        ID_FORMATTER.setMinimumIntegerDigits( numIDDigits );

                                //  Get number of digits for
                                //  page number in word ID.

        int numPageDigits   = 1;

        if ( maxPageBreaks > 0 )
        {
            numPageDigits   =
                (int)(ArithUtils.log10( (double)maxPageBreaks )) + 1;
        }

        PAGE_FORMATTER.setMinimumIntegerDigits( numPageDigits );

                                //  Get number of digits for
                                //  word within page in word ID.

        int numWordDigits   =
            (int)(ArithUtils.log10( (double)( 999 * idSpacing ) ) ) + 1;

        if ( maxPageBreaks <= 0 )
        {
            numWordDigits   = numIDDigits;
        }

        WORD_FORMATTER.setMinimumIntegerDigits( numWordDigits );
    }

    /** Set associated XML writer.
     *
     *  @param  writer  XML writer.
     */

    public void setWriter( XMLWriter writer )
    {
                                //  Save writer.
        this.writer = writer;
                                //  Create XML sentence melder.

        sentenceMelder  = new XMLSentenceMelder( writer );
    }

    /** Set associated sentence melder.
     *
     *  @param  sentenceMelder  Sentence melder.
     */

    public void setSentenceMelder( XMLSentenceMelder sentenceMelder )
    {
                                //  Set XML sentence melder.

        this.sentenceMelder = sentenceMelder;
    }

    /** Get associated sentence melder.
     *
     *  @return     Sentence melder.
     */

    public XMLSentenceMelder getSentenceMelder()
    {
        return sentenceMelder;
    }

    /** Get the foreign language tag for XML element.
     *
     *  @param  qName       XML element name.
     *  @param  atts        XML element attributes.
     *
     *  @return             The language part of speech tag
     *                      for this element.
     */

    public String getForeignLanguageTag
    (
        String qName ,
        Attributes atts
    )
    {
        String languageTag  = "";

                                //  Get language tag from
                                //  "xml:lang" attribute.

        String language     = atts.getValue( "xml:lang" );

                                //  Get the language from the
                                //  "lang" attribute if it is not specified
                                //  by the "xml:lang" attribute.

        if ( language == null )
        {
            language    = atts.getValue( "lang" );
        }

        if ( language == null )
        {
            language    = atts.getValue( "LANG" );
        }
                                //  If no language attribute is given,
                                //  inherit the language tag from the
                                //  parent XML element.  If there is
                                //  no parent element, set the
                                //  language tag to the empty string
                                //  except for a <foreign> tag.

        if ( language == null )
        {
            if ( !foreignStack.isEmpty() )
            {
                languageTag = foreignStack.peek();
            }
            else
            {
                if ( qName.equalsIgnoreCase( "foreign" ) )
                {
                    languageTag =
                        posTags.getForeignWordTag( "unknown" );
                }
                else
                {
                    languageTag = "" ;
                }
            }
        }
                                //  Language was specified in
                                //  attribute.  Get the corresponding
                                //  part of speech tag for this
                                //  language.
        else
        {
                                //  Strip eveything after the first
                                //  hyphen, if any.  We only care
                                //  about the language, not the script
                                //  or other modifiers.

            int iPos    = language.indexOf( "-" );

            if ( iPos >= 0 )
            {
                language    = language.substring( 0 , iPos );
            }
                                //  See if the language tag is one
                                //  we know.  If so, return the
                                //  corresponding language name,
                                //  otherwise return "other".

            if ( languageTags.containsKey( language ) )
            {
                language    = languageTags.get( language );
            }
            else
            {
                language    = "other";
            }
                                //  Add language tag to
                                //  foreign language tag stack.

            languageTag = posTags.getForeignWordTag( language );
        }

        return languageTag;
    }

    /** Get displayable list of element attributes and values.
     *
     *  @param  atts    Attributes.
     *
     *  @return         Displayable string in name=value format.
     */

    protected static String getDisplayableAttributes( Attributes atts )
    {
        StringBuffer sb = new StringBuffer();

        if ( atts != null )
        {
            for ( int i = 0 ; i < atts.getLength() ; i++ )
            {
                sb.append( atts.getLocalName( i ) );
                sb.append( "=" );
                sb.append( atts.getValue( i ) );
                sb.append( " " );
            }
        }

        return sb.toString();
    }
                                //  Initialize language map.
    static
    {
        languageTags.put( "ang" , "old-english" );

        languageTags.put( "ara" , "arabic" );
        languageTags.put( "ar" , "arabic" );

        languageTags.put( "cat" , "catalan" );
        languageTags.put( "ca" , "catalan" );

        languageTags.put( "eng" , "english" );
        languageTags.put( "en" , "english" );

        languageTags.put( "enm" , "middle-english" );

        languageTags.put( "deu" , "german" );
        languageTags.put( "de" , "german" );

        languageTags.put( "dut" , "dutch" );
        languageTags.put( "nld" , "dutch" );
        languageTags.put( "nl" , "german" );

        languageTags.put( "fra" , "french" );
        languageTags.put( "fre" , "french" );
        languageTags.put( "fr" , "french" );

        languageTags.put( "frm" , "middle-french" );

        languageTags.put( "fro" , "old-french" );

        languageTags.put( "grc" , "greek" );
        languageTags.put( "gre" , "greek" );
        languageTags.put( "ell" , "greek" );
        languageTags.put( "el" , "greek" );

        languageTags.put( "heb" , "hebrew" );
        languageTags.put( "he" , "hebrew" );

        languageTags.put( "ita" , "italian" );
        languageTags.put( "ital" , "italian" );
        languageTags.put( "it" , "italian" );

        languageTags.put( "lat" , "latin" );
        languageTags.put( "la" , "latin" );

        languageTags.put( "may" , "malay" );
        languageTags.put( "msa" , "malay" );
        languageTags.put( "ms" , "malay" );

        languageTags.put( "mlg" , "malagasy" );
        languageTags.put( "mg" , "malagasy" );

        languageTags.put( "per" , "persian" );
        languageTags.put( "fas" , "persian" );
        languageTags.put( "fa" , "persian" );

        languageTags.put( "por" , "portuguese" );
        languageTags.put( "pt" , "portuguese" );

        languageTags.put( "sai" , "south-american-indian" );

        languageTags.put( "san" , "sanskrit" );
        languageTags.put( "sa" , "sanskrit" );

        languageTags.put( "sco" , "scots" );

        languageTags.put( "spa" , "spanish" );
        languageTags.put( "es" , "spanish" );

        languageTags.put( "wel" , "welsh" );
        languageTags.put( "cym" , "welsh" );
        languageTags.put( "cy" , "welsh" );
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



