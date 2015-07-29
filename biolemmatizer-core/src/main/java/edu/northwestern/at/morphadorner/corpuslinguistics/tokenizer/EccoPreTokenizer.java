package edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer;

/*  Please see the license information at the end of this file. */

import edu.northwestern.at.utils.CharUtils;
import edu.northwestern.at.utils.PatternReplacer;
import edu.northwestern.at.utils.StringUtils;

/** A pretokenizer for ECCO texts.
 */

public class EccoPreTokenizer
    extends AbstractPreTokenizer
    implements PreTokenizer
{
    protected final static String EccoAlwaysSeparators  =
        "(" +
            hyphens + "|" + periods + "|" +
            "[\\(\\)\\[\\]\";:/=\u0060\u00b6<>\u00a1\u00bf\u00ab\u00bb" +
            CharUtils.LDQUOTE +
            CharUtils.RDQUOTE +
//          CharUtils.LONG_DASH +
//          "\\" + CharUtils.VERTICAL_BAR +
            CharUtils.BROKEN_VERTICAL_BAR +
            CharUtils.LIGHT_VERTICAL_BAR +
            "[\\p{InGeneralPunctuation}&&[^" +
            "\\{\\}" +
            "\\" + CharUtils.VERTICAL_BAR +
            CharUtils.SOLIDCIRCLE +
            CharUtils.DEGREES_MARK +
            CharUtils.MINUTES_MARK +
            CharUtils.SECONDS_MARK +
            CharUtils.LSQUOTE +
            CharUtils.RSQUOTE +
            CharUtils.SHORT_DASH +
            CharUtils.NONBREAKING_HYPHEN +
            CharUtils.ELLIPSIS +
            CharUtils.ASTERISM +
            CharUtils.DAGGER +
            CharUtils.DOUBLE_DAGGER +
            CharUtils.SECTION_SIGN +
//          CharUtils.INVISIBLE_SEPARATOR +
            "]]" +
            "\\p{InLetterlikeSymbols}" +
            "\\p{InMathematicalOperators}" +
            "\\p{InMiscellaneousTechnical}" +
            "[\\p{InGeometricShapes}&&[^" +
            CharUtils.BLACKCIRCLE +
            CharUtils.LOZENGE +
            "]]" +
            "\\p{InMiscellaneousSymbols}" +
            "\\p{InDingbats}" +
            "\\p{InAlphabeticPresentationForms}" +
            "]" +
        ")";

    /** Word or span gap. */

    protected final static PatternReplacer wordOrSpanGapReplacer    =
        new PatternReplacer
        (
            "(" +
            CharUtils.LEFT_ANGLE_BRACKET_STRING +
            "[" +
                CharUtils.LOZENGE_STRING +
                "|" +
                CharUtils.ELLIPSIS_STRING +
            "]+" +
            CharUtils.RIGHT_ANGLE_BRACKET_STRING +
            ")" ,
            " \u00241 "
        );

    /** Double back-ticks. */

    protected final static PatternReplacer doubleBackTicksReplacer  =
        new PatternReplacer( "(\u0060\u0060)" , " \u00241 " );

    /** Single back-tick followed by a capital letter. */

    protected final static PatternReplacer singleBackTicksReplacer  =
        new PatternReplacer( "\u0060([A-Z])" , "\u0060 \u00241" );

    /** Create an Ecco pretokenizer.
     */

    public EccoPreTokenizer()
    {
        super();

        alwaysSeparatorsReplacer    =
            new PatternReplacer( EccoAlwaysSeparators , " \u00241 ");
    }

    /** Prepare text for tokenization.
     *
     *  \u0040param line    The text to prepare for tokenization,
     *
     *  \u0040return            The pretokenized text.
     */

    public String pretokenize( String line )
    {
                                //  Do standard pretokenization.

        String result   = super.pretokenize( line );

                                //  Fix word and span gaps.

        result  = wordOrSpanGapReplacer.replace( result );

                                //  Put spaces around long dashes
                                //  at the start or end of a token.
        result  =
            result.replaceAll
            (
                "(\\s|\\.|\\?|!)" + CharUtils.LONG_DASH_STRING ,
                "\u00241 " + CharUtils.LONG_DASH_STRING
            );

        result  =
            result.replaceAll
            (
                "([\\p{L}\\-0-9\\'\u2011\u25cf]{3,})" +
                    CharUtils.LONG_DASH_STRING ,
                "\u00241 " + CharUtils.LONG_DASH_STRING
            );

        result  =
            result.replaceAll
            (
                CharUtils.LONG_DASH_STRING +
                    "([\\p{L}\\-0-9\\'\u2011\u25cf]{3,})" ,
                CharUtils.LONG_DASH_STRING + " \u00241"
            );

        result  =
            result.replaceAll
            (
                "([0-9]+)" + CharUtils.LONG_DASH_STRING ,
                "\u00241 " + CharUtils.LONG_DASH_STRING
            );
                                //  Back-ticks:  treat two in a row
                                //  as a single separable punctuation
                                //  mark.

        result  = doubleBackTicksReplacer.replace( result );

                                //  Treat single back tick followed by
                                //  a capital letter as a separable
                                //  punctuation mark.

        result  = singleBackTicksReplacer.replace( result );

        return result;
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



