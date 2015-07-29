/*
 Copyright (c) 2012, Regents of the University of Colorado
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this 
    list of conditions and the following disclaimer.
   
 * Redistributions in binary form must reproduce the above copyright notice, 
    this list of conditions and the following disclaimer in the documentation 
    and/or other materials provided with the distribution.
   
 * Neither the name of the University of Colorado nor the names of its 
    contributors may be used to endorse or promote products derived from this 
    software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.ucdenver.ccp.nlp.biolemmatizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Takes a String and returns an Americanized version of it based on
 * a list of British-to-American spellings and some rules.
 * This is deterministic spelling conversion, and so cannot deal with
 * certain cases involving complex ambiguities, but it can do most of the
 * simple case of English to American conversion.
 * <p>
 * <i>The list and rules for handling
 * British-to-American spellings is derived from:</i>
 * <code>http://www.tysto.com/uk-us-spelling-list.html</code>.
 *
 * @author Original author: Christopher Manning; Modified and extended by Haibin Liu
 */
public class Americanize {

    /** No word shorter in length than this is changed by Americanize */
    private static final int MINIMUM_LENGTH_CHANGED = 4;
    /** No word shorter in length than this can match a Pattern */
    private static final int MINIMUM_LENGTH_PATTERN_MATCH = 6;
    /** mapping from British spelling to American spelling */
    private Map<String,String> mappingBritishtoAmerican;
    
    private static final String[] patStrings = { "(ph|an|h|gyn|arch|chim)ae", "haem(at)?o", "aemia$", "([lL])eukaem",
    	"programme(s?)$", "^([a-z]{3,})our(s?)$",

    };

    private static final String[] reps = {
    	"$1e", "hem$1o", "emia", "$1eukem", "program$1", "$1or$2"
    };

    private static final Pattern[] pats = new Pattern[patStrings.length];

    private static final Pattern disjunctivePattern;

    static {
    	StringBuilder foo = new StringBuilder();
    	for (int i = 0, len = pats.length; i < len; i++) {
    		pats[i] = Pattern.compile(patStrings[i]);
    		if (i > 0) {
    			foo.append('|');
    		}
    		foo.append("(?:");
    		// Remove groups from String before appending for speed
    		foo.append(patStrings[i].replaceAll("[()]", ""));
    		foo.append(')');
    	}
    	disjunctivePattern = Pattern.compile(foo.toString());
    }

    private static final String[] OUR_EXCEPTIONS = {
    	"abatjour", "beflour", "bonjour",
    	"calambour", "carrefour", "cornflour", "contour",
    	"de[tv]our", "dortour", "dyvour", "downpour",
    	"giaour", "glamour", "holour", "inpour", "outpour",
    	"pandour", "paramour", "pompadour", "recontour", "repour", "ryeflour",
    	"sompnour",
    	"tambour", "troubadour", "tregetour", "velour"
    };

    private static final Pattern[] excepts = {
    	null, null, null, null, null,
    	Pattern.compile(join(OUR_EXCEPTIONS, "|"))
    };


    /**
     * Constructor to load the British-to-American spelling mapping file
     */
    public Americanize() {
    	//load British to American spelling mapping file
    	String mappingFileName = "BritishToAmerican.mapping";
    	InputStream is = Americanize.class.getResourceAsStream(mappingFileName);
    	try {
    		mappingBritishtoAmerican = loadSpellingMappingFile(is);
    	} catch (IOException e) {
    		throw new RuntimeException("Error while opening mapping file: " + mappingFileName, e);
    	}
    }

    /**
     * Static method to load a British-to-American spelling mapping file
     * 
     * @param is
     *            InputStream of the mapping file
     * @return a Map object that stores the British-to-American spellings
     * @throws IOException
     */
    private static Map<String, String> loadSpellingMappingFile(InputStream is) throws IOException {
    	Map<String, String> mapping = new HashMap<String, String>();
    	try {
    		InputStreamReader isr = new InputStreamReader(is);
    		BufferedReader input = new BufferedReader(isr);

    		String line = null;
    		while ((line = input.readLine()) != null) {
    			line = line.trim();
    			String[] pair = line.split("\t");
    			mapping.put(pair[0], pair[1]);
    		}
    		input.close();
    		isr.close();
    	} finally {
    		is.close();
    	}
    	return mapping;	
    }
  
    /**
     * Convert the spelling of a word from British to American English.
     * This is deterministic spelling conversion, and so cannot deal with
     * certain cases involving complex ambiguities, but it can do most of the
     * simple cases of English to American conversion.
     *
     * @param str The String to be Americanized
     * @return The American spelling of the word.
     */
    public String americanize(String str) {
    	// No ver short words are changed, so short circuit them
    	int length = str.length();
    	if (length < MINIMUM_LENGTH_CHANGED) {
    		return str;
    	}
    	String result;
    	result = mappingBritishtoAmerican.get(str);
    	if (result != null) {
    		return result;
    	}

    	if (length < MINIMUM_LENGTH_PATTERN_MATCH) {
    		return str;
    	}
    	// first do one disjunctive regex and return unless matches. Faster!
    	// (But still allocates matcher each time; avoiding this would make this class not threadsafe....)
    	if ( ! disjunctivePattern.matcher(str).find()) {
    		return str;
    	}
    	for (int i = 0; i < pats.length; i++) {
    		Matcher m = pats[i].matcher(str);
    		if (m.find()) {
    			Pattern ex = excepts[i];
    			if (ex != null) {
    				Matcher me = ex.matcher(str);
    				if (me.find()) {
    					continue;
    				}
    			}
    			// System.err.println("Replacing " + word + " with " +
    			//             pats[i].matcher(word).replaceAll(reps[i]));
    			return m.replaceAll(reps[i]);
    		}
    	}
    	return str;
    }

    /**
     * static method to concatenate String items with a specified delimiter
     * @param s
     * @param delimiter
     * @return concatenated String items with a specified delimiter
     */
    public static final String join(String[] s, String delimiter) {
    	if (s.length == 0) {
    		return "";
    	}
    	Iterator<String> iter = Arrays.asList(s).iterator();
    	StringBuffer buffer = new StringBuffer(iter.next());
    	while (iter.hasNext()) {
    		buffer.append(delimiter).append(iter.next());
    	}
    	return buffer.toString();
    }

    /**
     * Americanize and print the command line arguments.
     * This main method is just for debugging.
     *
     * @param args Command line arguments: a list of words
     */
    public static void main(String[] args) throws IOException {
    	//System.err.println(new Americanize());
    	//System.err.println();
        Americanize convert = new Americanize();
    	if (args.length == 0) { // stdin -> stdout:
    		BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
    		String line;
    		while((line = buf.readLine()) != null) {
    			for(String w : line.split("\\s+")) {
    				System.out.print(convert.americanize(w));
    				System.out.print(' ');
    			}
    			System.out.println();
    		}
    		buf.close();
    	}

    	for (String arg : args) {
    		System.out.print(arg);
    		System.out.print(" --> ");
    		System.out.println(convert.americanize(arg));
    	}
    }
}
