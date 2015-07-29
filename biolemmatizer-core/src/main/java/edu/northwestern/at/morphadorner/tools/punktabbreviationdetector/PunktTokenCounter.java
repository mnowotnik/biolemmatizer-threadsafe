package edu.northwestern.at.morphadorner.tools.punktabbreviationdetector;

/*  Please see the license information at the end of this file. */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.northwestern.at.morphadorner.corpuslinguistics.statistics.BigramLogLikelihood;
import edu.northwestern.at.utils.StringUtils;

/** Punkt token counter.
 *
 */

public class PunktTokenCounter
{
    protected static final int START = 0;
    protected static final int CANDIDATE_1 = 1;
    protected static final int CANDIDATE_2 = 2;

    protected int state;
    protected StringBuilder b;
    protected Map<String, Integer> c;
    protected Set<String> candidates;
    protected int n;

    /** Threshold for considering a token to be an abbreviation. */

    protected double abbreviationThreshold  = 0.3D;

    /** Allow disabling the abbreviation penalty heuristic, which
     *  exponentially disadvantages words that are sometimes found
     *  without a final period.
     */

    protected boolean ignoreAbbreviationPenalty = false;

    /** Create Punkt token counter.
     *
     *  @param  abbreviationThreshhold  Threshold for considering
     *                                  a token to be an abbreviation.
     *                                  0.3D is the usual value.
     *
     *  @param  ignoreAbbreviationPenalty   True to allow disabling
     *                                      the abbreviation penalty
     *                                      heuristic.  Usually false.
     */

    PunktTokenCounter
    (
        double abbreviationThreshhold ,
        boolean ignoreAbbreviationPenalty
    )
    {
        this.abbreviationThreshold      = abbreviationThreshhold;
        this.ignoreAbbreviationPenalty  = ignoreAbbreviationPenalty;

        state       = START;
        b           = new StringBuilder();
        c           = new HashMap<String, Integer>();
        candidates  = new HashSet<String>();
        n           = 0;
    }

    /** Create Punkt token count with default settings.
     */

    PunktTokenCounter()
    {
        this( 0.3D , false );
    }

    protected void count( PunktToken t )
    {
                                //  Ignore null token.
        if ( t == null )
        {
            return;
        }

        switch ( state )
        {
            case START:
                if ( isPeriod( t ) )
                {
                    inc( "." );
                }
                else if
                (   ( t.getTokenType() == PunktTokenType.WORD ) ||
                    ( t.getTokenType() == PunktTokenType.NUMBER )
                )
                {
                    b.append(t.getTokenText());
                    state = CANDIDATE_1;
                }

                break;

            case CANDIDATE_1:
                if ( isPeriod( t ) )
                {
                    b.append( "." );
                    state = CANDIDATE_2;
                }
                else
                {
                    inc( b.toString() );
                    b   = new StringBuilder();
                    state = START;
                }

                break;

            case CANDIDATE_2:
                if ( t.getTokenType() == PunktTokenType.WHITESPACE )
                {
                    inc( b.toString() );
                    inc( "." );

                    b       = new StringBuilder();
                    state   = START;
                }
                else if
                (   ( t.getTokenType() == PunktTokenType.WORD ) ||
                    ( t.getTokenType() == PunktTokenType.NUMBER )
                )
                {
                    b.append( t.getTokenText() );
                    state = CANDIDATE_1;
                }
                else
                {
                    inc( b.toString() );

                    b       = new StringBuilder();
                    state   = START;
                }

                break;
        }
    }

    protected void finish()
    {
        if ( b.length() > 0 )
        {
            String s = b.toString();

            inc( s );

            if ( s.endsWith( "." ) )
            {
                inc( "." );
            }

            b   = new StringBuilder();
        }
    }

    /** Check if token is a period.
     *
     *  @param  token   The token to check.
     *
     *  @return         True if token is a period.
     */

    protected boolean isPeriod( PunktToken token )
    {
        return
            ( token.getTokenType() == PunktTokenType.NONWORD ) &&
            ( token.getTokenText().equals( "." ) );
    }

    protected void inc( String s )
    {
        Integer count = c.get(s);

        if ( count == null )
        {
            c.put( s , 1 );
        }
        else
        {
            c.put( s , count + 1 );
        }

        n++;

        if  (   ( s.length() > 1 ) &&
                s.endsWith( "." ) &&
                !Character.isDigit( s.charAt( 0 ) )
            )
        {
            candidates.add(s);
        }
    }

    /** Get count for a token string.
     *
     *  @param  tokenString     Token string for which to get the count.
     *
     *  @return                 The token count.
     */

    public int getCount( String tokenString )
    {
        Integer count = c.get( tokenString );

        if ( count == null )
        {
            return 0;
        }

        return count.intValue();
    }

    /** Return total number of stored tokens.
     *
     *  @return     Total number of stored tokens.
     */

    public int getN()
    {
        return n;
    }

    /** Return set of abbreviation candidates.
     *
     *  @return     String set of abbreviation candidates.
     */

    public Set<String> getCandidates()
    {
        return candidates;
    }

    /** Get set of detected abbreviations.
     *
     *  @return     String set of detected abbreviations.
     */

    public Set<String> getAbbreviations()
    {
        Set<String> abbreviations = new HashSet<String>();

                                //  Check each token on the list
                                //  of abbreviation candidates.

        for ( String candidate : candidates )
        {
                                //  Add candidate to abbreviations list
                                //  if it passes the tests.

            if ( isAnAbbreviation( candidate ) )
            {
                abbreviations.add( candidate );
            }
        }
                                //  Return set of abbreviations.
        return abbreviations;
    }

    /** Determine if abbreviation candidate is actually an abbreviation.
     *
     *  @param  candidate   Candidate abbreviation token.
     *
     *  @return             true if candidate is an abbreviation, false
     *                      otherwise.
     */

    protected boolean isAnAbbreviation( String candidate )
    {
                                //  Candidate cannot be abbreviation if
                                //  it is less then two characters long
                                //  or it does not end with a period.

        if ( ( candidate.length() < 2 ) || !candidate.endsWith( "." ) )
        {
            return false;
        }
                                //  Remove trailing period from candidate.

        String withoutPeriod =
            candidate.substring( 0 ,  candidate.length() - 1 );

                                //  Get count of candidate with and
                                //  without trailing period.

        int cT          = getCount( candidate ) + getCount( withoutPeriod );

                                //  Get count of candidate with period.

        int cTPeriod    = getCount( candidate );

                                //  Get Dunning's log-likelihood value
                                //  comparing count with period against
                                //  count with and without trailing period.
        double logLambda =
            BigramLogLikelihood.calculateLogLikelihood
            (
                cT ,
                getCount( "." ) ,
                cTPeriod ,
                getN()
            );

        int nPeriods    = StringUtils.countChar( candidate , '.' );
        int fPeriods    = nPeriods + 1;
        int npc         = candidate.length() - nPeriods;

        double fLength  = 1.0D / Math.exp( npc );
        double fPenalty = 1.0D;
//      double fPenalty = getCount( candidate ) / cT;

        if ( !ignoreAbbreviationPenalty )
        {
            fPenalty    = 1.0D / Math.pow( npc , ( cT - cTPeriod ) );
        }
                                //  Calculate abbreviation score.

        double score    = logLambda * fPeriods * fLength * fPenalty;

                                //  If the score exceeds the preset
                                //  threshold, this is an abbreviation.

        return ( score >= abbreviationThreshold );
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



