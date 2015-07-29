package edu.northwestern.at.morphadorner;

/*  Please see the license information at the end of this file. */

import java.io.*;
import java.text.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.megginson.sax.*;

import edu.northwestern.at.morphadorner.MorphAdornerLogger;
import edu.northwestern.at.morphadorner.MorphAdornerSettings;
import edu.northwestern.at.morphadorner.corpuslinguistics.partsofspeech.*;
import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.logger.*;
import edu.northwestern.at.utils.xml.*;

/** Writes an adorned XML file with added ID fields.
 */

public class DefaultMorphAdornerXMLWriter
    implements MorphAdornerXMLWriter
{
    /** Sorted list of word IDs and word and sentence number information. */

    protected SortedArrayList<SentenceAndWordNumber> sortedWords    =
        new SortedArrayList<SentenceAndWordNumber>();

    /** Output XML writer. */

    protected XMLWriter writer;

    /** Create XML writer.
     */

    public DefaultMorphAdornerXMLWriter()
    {
    }

    /** Write XML output.
     *
     *  @param  inFile              The XML input file.
     *  @param  outFile             The XML output file.
     *  @param  maxID               The maximum ID value in the input file.
     *  @param  posTags             The part of speech tags.
     *  @param  splitWords          The map of (word ID, # of word parts)
     *                              for multipart words.
     *  @param  totalWords          Total words.
     *  @param  totalPageBreaks     Total page breaks.
     *  @param  adorner             The adorner.
     *  @param  tokenizingOnly      Only emit tokenization-related attributes.
     *
     *  @throws                     IOException, SAXException
     */

    public void writeXML
    (
        String inFile ,
        String outFile ,
        int maxID ,
        PartOfSpeechTags posTags ,
        Map<Integer, Integer> splitWords ,
        int totalWords ,
        int totalPageBreaks ,
        MorphAdorner adorner ,
        boolean tokenizingOnly
    )
        throws IOException, SAXException
    {
                                //  Get settings.

        MorphAdornerSettings settings   = adorner.morphAdornerSettings;

                                //  Get logger.

        MorphAdornerLogger logger       = adorner.morphAdornerLogger;

                                //  Create XML reader.

        XMLReader reader    = XMLReaderFactory.createXMLReader();

                                //  Add filter for updating word
                                //  tag attribute fields.

        IDFixerFilter idFilter  =
            new IDFixerFilter
            (
                reader ,
                posTags ,
                outFile ,
                maxID ,
                sortedWords ,
                splitWords ,
                totalWords ,
                totalPageBreaks ,
                settings ,
                tokenizingOnly
            );
                                //  If we need to output word numbers,
                                //  sentence numbers, or sentence
                                //  boundary milestones, we write the
                                //  preliminary output to a
                                //  temporary file.  Otherwise we can
                                //  write the final output directly.
        boolean twoStep =
            !tokenizingOnly &&
            (
                settings.outputSentenceBoundaryMilestones ||
                settings.outputSentenceNumber ||
                settings.outputWordNumber
            );
                                //  Create a temporary file to hold
                                //  the preliminary output if we are
                                //  generating output in two steps.

        String tempFileName = "";

        if ( twoStep )
        {
            File tempFile   = File.createTempFile( "mad" , null );

            tempFile.deleteOnExit();

            tempFileName    = tempFile.getAbsolutePath();

            logger.println
            (
                "Using_two_step_output"
            );
        }
                                //  Create XML output writer.

        StringWriter outputStreamWriter = new StringWriter();

                                //  Remove word elements from tags
                                //  which should not have them.

        StripWordElementsFilter stripFilter =
            new StripWordElementsFilter
            (
                idFilter ,
                settings.disallowWordElementsIn
            );

        writer  =
            new IndentingXMLWriter
            (
                stripFilter ,
                outputStreamWriter
            );
                                //  Indent XML by two characters for
                                //  each nested level.

        ((IndentingXMLWriter)writer).setIndentStep( 2 );

                                //  Do not map characters to attributes.

        writer.setOutputCharsAsIs( true );

                                //  Set the output system (DTD) name.
        writer.setDoctype
        (
            settings.xmlDoctypeName ,
            settings.xmlDoctypeSystem
        );
                                //  Set writer into filter.

        idFilter.setWriter( writer );

                                //  Set sentence melder from ID filter
                                //  into strip word elements filter.

        stripFilter.setSentenceMelder( idFilter.getSentenceMelder() );

                                //  Parse the XML file, updating
                                //  the <w> attribute fields, and
                                //  writing the updated XML to a file.

        long startTime  = System.currentTimeMillis();

        writer.parse( inFile );

                                //  Apply final fixup patterns.

        PatternReplacer replacer    =
            new PatternReplacer
            (
                "(\\s+)<unclear>(\\s+)<c> </c>" ,
                "$1<c> </c>$1<unclear>"
            );

        String outputText   =
            replacer.replace( outputStreamWriter.toString() );

        replacer    =
            new PatternReplacer
            (
                "(\\s+)<hi(.*?)>(\\s+)<c> </c>" ,
                "$1<c> </c>$1<hi$2>"
            );

        outputText  = replacer.replace( outputText );

                                //  Write updated XML text to output file.

        String outputFileName   =
            twoStep ? tempFileName : outFile;

        FileUtils.writeTextFile
        (
            new File( outputFileName ) ,
            false ,
            outputText ,
            "utf-8"
        );
                                //  Write updated XML text to file.
        if ( twoStep )
        {
            logger.println
            (
                "First_output_step_completed" ,
                MorphAdornerUtils.durationString
                (
                    adorner.morphAdornerSettings ,
                    startTime
                )
            );
        }

        try
        {
            outputStreamWriter.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
                                //  Add word and sentence numbers,
                                //  and sentence milestones, in a
                                //  second pass.  The temporary
                                //  output file of the first pass
                                //  is processed using word and
                                //  sentence numbers generated here.
        if ( twoStep )
        {
            startTime   = System.currentTimeMillis();

            getWordAndSentenceNumbers( settings );

                                //  Add word and sentence numbers
                                //  and sentence milestones.

            new SentenceNumberAdder(
                tempFileName , outFile , sortedWords , settings );

                                //  Delete the first step temporary
                                //  file.

            FileUtils.deleteFile( tempFileName );

            logger.println
            (
                "Second_output_step_completed" ,
                MorphAdornerUtils.durationString
                (
                    adorner.morphAdornerSettings ,
                    startTime
                )
            );
        }
    }

    /** Get word and sentence numbers.
     */

    protected void getWordAndSentenceNumbers( MorphAdornerSettings settings )
    {
                                //  Initialize sentence and word numbers.

        int sentenceNumber      = 0;
        int wordNumber          = 0;
        int runningWordNumber   = 0;

                                //  Loop over all sorted word IDs.

        for ( int i = 0 ; i < sortedWords.size() ; i++ )
        {
                                //  Get information for this word ID.

            SentenceAndWordNumber swn   = sortedWords.get( i );

                                //  If the current sentence is empty,
                                //  we are starting a new sentence.

            if ( swn.isFirstPart() )
            {
                if ( wordNumber == 0 )
                {
                    sentenceNumber++;
                }
                                //  Increment word number.
                wordNumber++;
                runningWordNumber++;
            }
                                //  Save word and sentence number.

            swn.setSentenceAndWordNumber
            (
                sentenceNumber ,
                settings.outputRunningWordNumbers ?
                    runningWordNumber : wordNumber
            );
                                //  If this word is the last word in the
                                //  sentence, set the word number to zero
                                //  so we will start a new sentence on
                                //  the next word.

            if ( swn.getEOS() )
            {
                wordNumber  = 0;
            }
        }
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



