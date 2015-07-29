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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import edu.northwestern.at.morphadorner.corpuslinguistics.lemmatizer.EnglishLemmatizer;
import edu.northwestern.at.morphadorner.corpuslinguistics.lemmatizer.Lemmatizer;
import edu.northwestern.at.morphadorner.corpuslinguistics.lexicon.DefaultLexicon;
import edu.northwestern.at.morphadorner.corpuslinguistics.lexicon.DefaultWordLexicon;
import edu.northwestern.at.morphadorner.corpuslinguistics.lexicon.Lexicon;
import edu.northwestern.at.morphadorner.corpuslinguistics.lexicon.LexiconEntry;
import edu.northwestern.at.morphadorner.corpuslinguistics.partsofspeech.PartOfSpeechTags;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.PennTreebankTokenizer;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.WordTokenizer;

/**
 * BioLemmatizer: Lemmatize a word in biomedical texts and return its lemma; the part of speech
 * (POS) of the word is optional.
 * 
 * <p>
 * Usage:
 * </p>
 * 
 * <p>
 * <code>
 * 	java -Xmx1G -jar biolemmatizer-core-1.0-jar-with-dependencies.jar [-l] {@literal <input_string>} [POS tag]   or<br>
 *  java -Xmx1G -jar biolemmatizer-core-1.0-jar-with-dependencies.jar [-l] -i {@literal <input_file_name> -o <output_file_name>} or<br>
 *  java -Xmx1G -jar biolemmatizer-core-1.0-jar-with-dependencies.jar [-l] -t<br>
 * 	</code>
 * </p>
 * 
 * <p>
 * Example:
 * </p>
 * 
 * <p>
 * <code>
 * 	java -Xmx1G -jar biolemmatizer-core-1.0-jar-with-dependencies.jar catalyses NNS
 * </code>
 * </p>
 * 
 * <p>
 * Please see the README file for more usage examples
 * </p>
 * 
 * @author Haibin Liu <Haibin.Liu@ucdenver.edu>, William A Baumgartner Jr
 *         <William.Baumgartner@ucdenver.edu> and Karin Verspoor <Karin.Verspoor@ucdenver.edu>
 */

public class BioLemmatizer {
	/** Lemma separator character */
	public static String lemmaSeparator = "||";

	/** BioLemmatizer */
	public Lemmatizer lemmatizer;

	/** Word lexicon for lemma lookup */
	public Lexicon wordLexicon;

	/** NUPOS tags */
	public PartOfSpeechTags partOfSpeechTags;

	/** Extract individual word parts from a contracted word. */
	public WordTokenizer spellingTokenizer;

	/** Hierachical mapping file from PennPOS to NUPOS */
	public Map<String, String[]> mappingPennPOStoNUPOS;

	/** Hierachical mapping file from major class to Penn Treebank POS */
	public Map<String, String[]> mappingMajorClasstoPennPOS;

	/** the Part-Of-Speech mapping file */
	protected static String mappingFileName;

	/** POSEntry object to retrieve POS tag information */
	public POSEntry posEntry;

	/**
	 * Default constructor loads the lexicon from the classpath
	 */
	public BioLemmatizer() {
		this(null);
	}

	/**
	 * Constructor to initialize the class fields
	 * 
	 * @param lexiconFile
	 *            a reference to the lexicon file to use. If null, the lexicon that comes with the
	 *            BioLemmatizer distribution is loaded from the classpath
	 */
	public BioLemmatizer(File lexiconFile) {

		// Get the default rule-based lemmatizer.
		try {
			lemmatizer = new MorphAdornerLemmatizer();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// Get default word lexicon.
		try {
			wordLexicon = new BioWordLexicon(lexiconFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// Get the part of speech tags from the word lexicon.
		partOfSpeechTags = wordLexicon.getPartOfSpeechTags();
		// Get spelling tokenizer.
		spellingTokenizer = new PennTreebankTokenizer();
		// Set the lexicon which may provide lemmata.
		lemmatizer.setLexicon(wordLexicon);
		// Set the dictionary for checking lemmata after applying lemmatization
		// rules.
		lemmatizer.setDictionary(setDictionary(wordLexicon));

		// Specify the Part-Of-Speech mapping files
		mappingFileName = "PennPOStoNUPOS.mapping";
		InputStream is = BioLemmatizer.class.getResourceAsStream(mappingFileName);
		try {
			mappingPennPOStoNUPOS = loadPOSMappingFile(is);
		} catch (IOException e) {
			throw new RuntimeException("Unable to load mapping: " + mappingFileName, e);
		}

		mappingFileName = "MajorClasstoPennPOS.mapping";
		is = BioLemmatizer.class.getResourceAsStream(mappingFileName);
		try {
			mappingMajorClasstoPennPOS = loadPOSMappingFile(is);
		} catch (IOException e) {
			throw new RuntimeException("Unable to load mapping: " + mappingFileName, e);
		}

		// Get the POS tagsets
		posEntry = new POSEntry();
	}

	/**
	 * Static method to load a Part-Of-Speech mapping file
	 * 
	 * @param is
	 *            InputStream of the mapping file
	 * @return a Map object that stores the hierachical mapping information in the file
	 * @throws IOException
	 */
	static Map<String, String[]> loadPOSMappingFile(InputStream is) throws IOException {
		Map<String, String[]> mapping = new HashMap<String, String[]>();

		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader input = new BufferedReader(isr);

			String line = null;
			while ((line = input.readLine()) != null) {
				line = line.trim();
				String[] pair = line.split("\t");
				String[] mappingSet = pair[1].split(",");
				mapping.put(pair[0], mappingSet);
				// remove the first empty char with unicode FEFF
				mapping.put(pair[0].replaceAll("^\\uFEFF", ""), mappingSet);
			}

			input.close();
			isr.close();
		} finally {
			is.close();
		}
		return mapping;
	}

	/**
	 * Create a dictionary from a word lexicon for validating lemmata resulted from lemmatization
	 * rules
	 * 
	 * @param wordLexicon
	 *            a word lexicon
	 * @return a set that contains a dictionary generated from the word lexicon
	 */
	private Set<String> setDictionary(Lexicon wordLexicon) {
		Set<String> dictionarySet = new HashSet<String>();

		// generate dictionary from lexicon
		String[] lexiconEntries = wordLexicon.getEntries();
		for (String entry : lexiconEntries) {
			String[] lemmata = wordLexicon.getLemmata(entry);
			for (String lemma : lemmata) {
				dictionarySet.add(lemma.toLowerCase());
			}
		}

		return dictionarySet;
	}

	/**
	 * Retrieve an array of corresponding NUPOS tags of a Penn Treebank POS tag
	 * 
	 * @param partOfSpeech
	 *            a POS tag
	 * @return an array of corresponding NUPOS tags;
	 */
	private String[] getNUPOSTagFromPennPOS(String partOfSpeech) {
		String[] nuPOSTag = mappingPennPOStoNUPOS.get(partOfSpeech.toUpperCase());
		return nuPOSTag != null ? nuPOSTag : new String[] { partOfSpeech };
	}

	/**
	 * Retrieve an array of corresponding Penn Treebank POS tags of a NUPOS tag
	 * 
	 * @param partOfSpeech
	 *            a POS tag
	 * @return an array of corresponding Penn Treebank POS tags;
	 */
	private String[] getPennPOSFromNUPOS(String partOfSpeech) {
		List<String> result = new ArrayList<String>();
		for (String key : mappingPennPOStoNUPOS.keySet()) {
			for (String value : mappingPennPOStoNUPOS.get(key)) {
				if (value.equals(partOfSpeech)) {
					result.add(key);
					break;
				}
			}
		}
		return result.size() != 0 ? result.toArray(new String[result.size()]) : new String[] { partOfSpeech };
	}

	/**
	 * Retrieve sibling Penn Treebank POS tags of a Penn Treebank POS tag from the POS hierarchy
	 * 
	 * @param partOfSpeech
	 *            a Penn Treebank POS tag
	 * @return sibling Penn Treebank POS tags of the Penn Treebank POS tag
	 */
	private String[] getSiblingPennPOSTag(String partOfSpeech) {
		// check if partOfSpeech exists in the hierarchy
		boolean globalFlag = false;
		for (String key : mappingMajorClasstoPennPOS.keySet()) {
			String[] posTag = mappingMajorClasstoPennPOS.get(key);
			for (String pos : posTag) {
				if (pos.equals(partOfSpeech)) {
					globalFlag = true;
					break;
				}
			}
			if (globalFlag)
				break;
		}

		if (globalFlag) {
			String foundKey = "";
			for (String key : mappingMajorClasstoPennPOS.keySet()) {
				String[] posTag = mappingMajorClasstoPennPOS.get(key);
				boolean localFlag = false;
				for (String pos : posTag) {
					if (pos.equals(partOfSpeech)) {
						foundKey = key;
						localFlag = true;
						break;
					}
				}
				if (localFlag)
					break;
			}
			List<String> merge = new ArrayList<String>();
			for (String pos : mappingMajorClasstoPennPOS.get(foundKey)) {
				if (!pos.equals(partOfSpeech))
					merge.add(pos);
			}
			return merge.toArray(new String[merge.size()]);
		} else {
			return new String[] { partOfSpeech };
		}
	}

	/**
	 * Retrieve sibling NUPOS tags of a Penn Treebank POS tag from the POS hierarchy
	 * 
	 * @param partOfSpeech
	 *            a Penn Treebank POS tag
	 * @return sibling NUPOS tags of the Penn Treebank POS tag
	 */
	private String[] getSiblingNUPOSTag(String partOfSpeech) {
		// check if partOfSpeech exists in the hierarchy
		boolean globalFlag = false;
		for (String key : mappingMajorClasstoPennPOS.keySet()) {
			String[] posTag = mappingMajorClasstoPennPOS.get(key);
			for (String pos : posTag) {
				if (pos.equals(partOfSpeech)) {
					globalFlag = true;
					break;
				}
			}
			if (globalFlag)
				break;
		}

		if (globalFlag) {
			String foundKey = "";
			for (String key : mappingMajorClasstoPennPOS.keySet()) {
				String[] posTag = mappingMajorClasstoPennPOS.get(key);
				boolean localFlag = false;
				for (String pos : posTag) {
					if (pos.equals(partOfSpeech)) {
						foundKey = key;
						localFlag = true;
						break;
					}
				}
				if (localFlag)
					break;
			}
			List<String> merge = new ArrayList<String>();
			for (String pos : mappingMajorClasstoPennPOS.get(foundKey)) {
				if (!pos.equals(partOfSpeech)) {
					merge.addAll(Arrays.asList(mappingPennPOStoNUPOS.get(pos.toUpperCase())));
				}
			}

			return merge.toArray(new String[merge.size()]);
		} else {
			return new String[] { partOfSpeech };
		}
	}

	/**
	 * Retrieve lemmas and the corresponding categories of the input string
	 * 
	 * @param spelling
	 *            an input string
	 * @return a Map object that stores lemmas and categories of the string; key: category, value:
	 *         lemma
	 */
	private Map<String, String> getLemmasAndCategories(String spelling) {
		Map<String, String> lemmasAndCategories = new HashMap<String, String>();
		LexiconEntry lexiconEntry = wordLexicon.getLexiconEntry(spelling);
		if (lexiconEntry != null)
			lemmasAndCategories = lexiconEntry.lemmata;
		return lemmasAndCategories;
	}

	/**
	 * Clean up the raw lemma resulted from lemmatization rules
	 * 
	 * @param lemma
	 *            a raw lemma
	 * @return clean lemma
	 */
	private static String cleanUpLemma(String lemma) {
		String newLemma = lemma;
		String lastChar = lemma.substring(lemma.length() - 1);
		if (lastChar.equals("'")) {
			newLemma = lemma.substring(0, lemma.length() - 1);
		}
		return newLemma;
	}

	/**
	 * Convert special unicode characters into modern English spelling
	 * 
	 * @param input
	 *            an input string
	 * @return modern English spelling
	 */
	 static String unicodeHandler(String input) {
		// define the mapping between special unicode characters and modern
		// English spelling
		Map<String, String> specialUnicodeCharToModernEnglishMapping = new HashMap<String, String>();

		specialUnicodeCharToModernEnglishMapping.put("u00E6", "ae");
		specialUnicodeCharToModernEnglishMapping.put("u0153", "oe");
		specialUnicodeCharToModernEnglishMapping.put("u00E4", "a");
		specialUnicodeCharToModernEnglishMapping.put("u00E0", "a");
		specialUnicodeCharToModernEnglishMapping.put("u00E1", "a");
		specialUnicodeCharToModernEnglishMapping.put("u0113", "e");
		specialUnicodeCharToModernEnglishMapping.put("u00E9", "e");
		specialUnicodeCharToModernEnglishMapping.put("u00E8", "e");
		specialUnicodeCharToModernEnglishMapping.put("u00EB", "e");
		specialUnicodeCharToModernEnglishMapping.put("u00EF", "i");
		specialUnicodeCharToModernEnglishMapping.put("u00F1", "n");
		specialUnicodeCharToModernEnglishMapping.put("u014D", "o");
		specialUnicodeCharToModernEnglishMapping.put("u00F6", "o");
		specialUnicodeCharToModernEnglishMapping.put("u00F4", "o");
		specialUnicodeCharToModernEnglishMapping.put("u016B", "u");
		specialUnicodeCharToModernEnglishMapping.put("u00FA", "u");

		String output = input;
		for (String unicode : specialUnicodeCharToModernEnglishMapping.keySet()) {
			String regex = "\\" + unicode;
			output = output.replaceAll(regex, specialUnicodeCharToModernEnglishMapping.get(unicode));
		}

		return output;
	}

	/**
	 * Lemmatize a string with POS tag using Lexicon only
	 * 
	 * @param spelling
	 *            an input string
	 * @param partOfSpeech
	 *            POS tag of the input string
	 * @return a LemmaEntry object containing lemma and POS information
	 */
	public LemmataEntry lemmatizeByLexicon(String spelling, String partOfSpeech) {
		Map<String, String> lemmataAndLemmataTag = new HashMap<String, String>();
		String lemmata = spelling;
		String lemmataTag;
		if (partOfSpeech == null)
			partOfSpeech = "";
		// default POS tag = NONE
		if (partOfSpeech.trim().length() == 0)
			lemmataTag = "NONE";
		else
			lemmataTag = partOfSpeech;

		// check the POS tagset
		String tagSetLabel = posEntry.getTagSetLabel(partOfSpeech);

		String[] nuPOSTag = getNUPOSTagFromPennPOS(partOfSpeech);

		// Different lexicon search methods are tried in order to
		// augument the use of
		// lexicon
		String lemma = "*";
		String category = "*";

		if (tagSetLabel.equals("PennPOS")) {
			// direct PennPOS tag search
			lemma = wordLexicon.getLemma(spelling.toLowerCase(), partOfSpeech);
			if (lemma.equals("*")) {
				lemma = wordLexicon.getLemma(spelling.toUpperCase(), partOfSpeech);
			}
			if (!lemma.equals("*")) {
				lemmata = lemma;
				category = partOfSpeech;
				// System.out.println("found in the Penn direct lexicon: "+lemma);
			}
			// PennPOS tag hierachical search
			if (lemma.equals("*")) {
				String[] hierarachicalPennPOSTag = getSiblingPennPOSTag(partOfSpeech);
				for (String pos : hierarachicalPennPOSTag) {
					lemma = wordLexicon.getLemma(spelling.toLowerCase(), pos);
					if (lemma.equals("*")) {
						lemma = wordLexicon.getLemma(spelling.toUpperCase(), pos);
					}
					if (!lemma.equals("*")) {
						lemmata = lemma;
						category = pos;
						// System.out.println("found in the Penn hierachical lexicon: "+lemma);
						break;
					}
				}
			}
			// Turn PennPOS tag into NUSPOS tag and search
			if (lemma.equals("*")) {
				for (String pos : nuPOSTag) {
					lemma = wordLexicon.getLemma(spelling.toLowerCase(), pos);
					if (lemma.equals("*")) {
						lemma = wordLexicon.getLemma(spelling.toUpperCase(), pos);
					}
					if (!lemma.equals("*")) {
						lemmata = lemma;
						category = pos;
						// System.out.println("found in the converted NU direct lexicon: "+lemma);
						break;
					}
				}
			}
			// NUSPOS tag hierachical search
			if (lemma.equals("*")) {
				String[] hierarachicalNUPOSTag = getSiblingNUPOSTag(partOfSpeech);
				for (String pos : hierarachicalNUPOSTag) {
					lemma = wordLexicon.getLemma(spelling.toLowerCase(), pos);
					if (lemma.equals("*")) {
						lemma = wordLexicon.getLemma(spelling.toUpperCase(), pos);
					}
					if (!lemma.equals("*")) {
						lemmata = lemma;
						category = pos;
						// System.out.println("found in the converted NU hierachical lexicon: "+lemma);
						break;
					}
				}
			}
		} else if (tagSetLabel.equals("NUPOS")) {
			// direct NUPOS tag search
			lemma = wordLexicon.getLemma(spelling.toLowerCase(), partOfSpeech);
			if (lemma.equals("*")) {
				lemma = wordLexicon.getLemma(spelling.toUpperCase(), partOfSpeech);
			}
			if (!lemma.equals("*")) {
				lemmata = lemma;
				category = partOfSpeech;
				// System.out.println("found in the NU direct lexicon: "+lemma);
			}
			// NUPOS tag hierachical search
			if (lemma.equals("*")) {
				String[] hierarachicalNUPOSTag = getSiblingNUPOSTag(getPennPOSFromNUPOS(partOfSpeech)[0]);
				for (String pos : hierarachicalNUPOSTag) {
					lemma = wordLexicon.getLemma(spelling.toLowerCase(), pos);
					if (lemma.equals("*")) {
						lemma = wordLexicon.getLemma(spelling.toUpperCase(), pos);
					}
					if (!lemma.equals("*")) {
						lemmata = lemma;
						category = pos;
						// System.out.println("found in the NU hierachical lexicon: "+lemma);
						break;
					}
				}
			}
		}

		// backup lexicon lookup process: search without POS tags, return all
		// lemmas
		Map<String, String> lemmasAndCategories = new HashMap<String, String>();
		if (tagSetLabel.equals("NONE") || lemma.equals("*")) {
			// if ( tagSetLabel.equals("NONE") ) {
			lemmasAndCategories = getLemmasAndCategories(spelling.toLowerCase());
			if (lemmasAndCategories.isEmpty()) {
				lemmasAndCategories = getLemmasAndCategories(spelling.toUpperCase());
			}
		}

		// found the Lemma
		if (!lemmasAndCategories.isEmpty()) {
			lemmataAndLemmataTag = lemmasAndCategories;
			// System.out.println("found in the lexicon");
		} else if (!lemma.equals("*")) {
			lemmata = lemma;
			lemmataTag = category;
			lemmataAndLemmataTag.put(lemmataTag, lemmata);
		}
		// lexicon has been checked but nothing found, return original input
		else
			lemmataAndLemmataTag.put(lemmataTag, lemmata);

		return new LemmataEntry(lemmataAndLemmataTag, posEntry);
	}

	/**
	 * Lemmatize a string with POS tag using lemmatization rules only
	 * 
	 * @param spelling
	 *            an input string
	 * @param partOfSpeech
	 *            POS tag of the input string
	 * @return a LemmaEntry object containing lemma and POS information
	 */
	public LemmataEntry lemmatizeByRules(String spelling, String partOfSpeech) {
		// option to have a dictionary for rule-based lemmatizer to validate results
		// lemmatizer.setDictionary(new HashSet<String>());

		Map<String, String> lemmataAndLemmataTag = new HashMap<String, String>();
		String lemmata = spelling;
		String lemmataTag;
		// default POS tag = NONE
		if (partOfSpeech == null)
			partOfSpeech = "";
		if (partOfSpeech.trim().length() == 0)
			lemmataTag = "NONE";
		else
			lemmataTag = partOfSpeech;

		String[] nuPOSTag = getNUPOSTagFromPennPOS(partOfSpeech);

		// Use rule-based lemmatizer.

		// Get lemmatization word class for part of speech,
		String lemmaClass = "";
		for (String pos : nuPOSTag) {
			lemmaClass = partOfSpeechTags.getLemmaWordClass(pos);
			if (lemmaClass.length() != 0) {
				break;
			}
		}

		// Do not lemmatize words which should not be lemmatized, ?including
		// proper names?.
		if (lemmatizer.cantLemmatize(spelling) || lemmaClass.equals("none")) {
		} else {
			// Try compound word exceptions list first.
			lemmata = lemmatizer.lemmatize(spelling, "compound");

			// If lemma not found, keep trying.
			if (lemmata.equalsIgnoreCase(spelling)) {
				// Extract individual word parts.
				// May be more than one for a
				// contraction.
				List<String> wordList = spellingTokenizer.extractWords(spelling);

				// If just one word part, get its lemma.
				if (!partOfSpeechTags.isCompoundTag(partOfSpeech) || (wordList.size() == 1)) {
					if (lemmaClass.length() == 0) {
						lemmata = lemmatizer.lemmatize(spelling);
					} else {
						lemmata = lemmatizer.lemmatize(spelling, lemmaClass);
					}
				}
				// More than one word part.
				// Get lemma for each part and
				// concatenate them with the
				// lemma separator to form a
				// compound lemma.
				else {
					lemmata = "";
					String[] posTags = partOfSpeechTags.splitTag(partOfSpeech);

					if (posTags.length == wordList.size()) {
						for (int i = 0; i < wordList.size(); i++) {
							String wordPiece = wordList.get(i);
							if (i > 0) {
								lemmata = lemmata + lemmaSeparator;
							}

							LemmataEntry lemmaPiece = lemmatizeByRules(wordPiece, posTags[i]);

							lemmata = lemmata + lemmaPiece.lemmasToString();
						}
					}
				}
			}
		}

		lemmataAndLemmataTag.put(lemmataTag, lemmata);

		return new LemmataEntry(lemmataAndLemmataTag, posEntry);

	}

	/**
	 * Lemmatize a string with POS tag using both lexicon lookup and lemmatization rules This is the
	 * preferred method as it gives the best lemmatization performance
	 * 
	 * @param spelling
	 *            an input string
	 * @param partOfSpeech
	 *            POS tag of the input string
	 * @return a LemmaEntry object containing lemma and POS information
	 */
	public LemmataEntry lemmatizeByLexiconAndRules(String spelling, String partOfSpeech) {

		Map<String, String> lemmataAndLemmataTag = new HashMap<String, String>();
		String lemmata = spelling;
		String lemmataTag;
		// default POS tag = NONE
		if (partOfSpeech == null)
			partOfSpeech = "";
		if (partOfSpeech.trim().length() == 0)
			lemmataTag = "NONE";
		else
			lemmataTag = partOfSpeech;

		// check the POS tagset
		String tagSetLabel = posEntry.getTagSetLabel(partOfSpeech);

		String[] nuPOSTag = getNUPOSTagFromPennPOS(partOfSpeech);

		// Try lexicon first, different search methods are tried in order to
		// augument the use of
		// lexicon
		String lemma = "*";
		String category = "*";

		if (tagSetLabel.equals("PennPOS")) {
			// direct PennPOS tag search
			lemma = wordLexicon.getLemma(spelling.toLowerCase(), partOfSpeech);
			if (lemma.equals("*")) {
				lemma = wordLexicon.getLemma(spelling.toUpperCase(), partOfSpeech);
			}
			if (!lemma.equals("*")) {
				category = partOfSpeech;
				// System.out.println("found in the Penn direct lexicon: "+lemma);
			}
			// PennPOS tag hierachical search
			if (lemma.equals("*")) {
				String[] hierarachicalPennPOSTag = getSiblingPennPOSTag(partOfSpeech);
				for (String pos : hierarachicalPennPOSTag) {
					lemma = wordLexicon.getLemma(spelling.toLowerCase(), pos);
					if (lemma.equals("*")) {
						lemma = wordLexicon.getLemma(spelling.toUpperCase(), pos);
					}
					if (!lemma.equals("*")) {
						category = pos;
						// System.out.println("found in the Penn hierachical lexicon: "+lemma);
						break;
					}
				}
			}
			// Turn PennPOS tag into NUSPOS tag and search
			if (lemma.equals("*")) {
				for (String pos : nuPOSTag) {
					lemma = wordLexicon.getLemma(spelling.toLowerCase(), pos);
					if (lemma.equals("*")) {
						lemma = wordLexicon.getLemma(spelling.toUpperCase(), pos);
					}
					if (!lemma.equals("*")) {
						category = pos;
						// System.out.println("found in the converted NU direct lexicon: "+lemma);
						break;
					}
				}
			}
			// NUSPOS tag hierachical search
			if (lemma.equals("*")) {
				String[] hierarachicalNUPOSTag = getSiblingNUPOSTag(partOfSpeech);
				for (String pos : hierarachicalNUPOSTag) {
					lemma = wordLexicon.getLemma(spelling.toLowerCase(), pos);
					if (lemma.equals("*")) {
						lemma = wordLexicon.getLemma(spelling.toUpperCase(), pos);
					}
					if (!lemma.equals("*")) {
						category = pos;
						// System.out.println("found in the converted NU hierachical lexicon: "+lemma);
						break;
					}
				}
			}
		} else if (tagSetLabel.equals("NUPOS")) {
			// direct NUPOS tag search
			lemma = wordLexicon.getLemma(spelling.toLowerCase(), partOfSpeech);
			if (lemma.equals("*")) {
				lemma = wordLexicon.getLemma(spelling.toUpperCase(), partOfSpeech);
			}
			if (!lemma.equals("*")) {
				category = partOfSpeech;
				// System.out.println("found in the NU direct lexicon: "+lemma);
			}
			// NUPOS tag hierachical search
			if (lemma.equals("*")) {
				String[] hierarachicalNUPOSTag = getSiblingNUPOSTag(getPennPOSFromNUPOS(partOfSpeech)[0]);
				for (String pos : hierarachicalNUPOSTag) {
					lemma = wordLexicon.getLemma(spelling.toLowerCase(), pos);
					if (lemma.equals("*")) {
						lemma = wordLexicon.getLemma(spelling.toUpperCase(), pos);
					}
					if (!lemma.equals("*")) {
						category = pos;
						// System.out.println("found in the NU hierachical lexicon: "+lemma);
						break;
					}
				}
			}
		}

		// if tagSetLabel is NONE, invoke the backup lexicon lookup process:
		// search without POS tags, return all lemmas
		Map<String, String> lemmasAndCategories = new HashMap<String, String>();
		// if ( tagSetLabel.equals("NONE") || lemma.equals( "*" ) ) {
		if (tagSetLabel.equals("NONE")) {
			lemmasAndCategories = getLemmasAndCategories(spelling.toLowerCase());
			if (lemmasAndCategories.isEmpty()) {
				lemmasAndCategories = getLemmasAndCategories(spelling.toUpperCase());
			}
		}

		// found the Lemma
		if (!lemmasAndCategories.isEmpty()) {
			lemmataAndLemmataTag = lemmasAndCategories;
			// System.out.println("found in the lexicon");
		} else if (!lemma.equals("*")) {
			lemmata = lemma;
			lemmataTag = category;
			lemmataAndLemmataTag.put(lemmataTag, lemmata);
		}
		// for testing purpose to test lexicon only
		// else lemmataAndLemmataTag.put(lemmataTag, lemmata) ;

		// Lemma not found in word lexicon. Use rule-based lemmatizer.
		else {

			// Get lemmatization word class for part of speech,
			String lemmaClass = "";
			for (String pos : nuPOSTag) {
				lemmaClass = partOfSpeechTags.getLemmaWordClass(pos);
				if (lemmaClass.length() != 0) {
					break;
				}
			}

			// Do not lemmatize words which should not be lemmatized, ?including
			// proper names?.
			if (lemmatizer.cantLemmatize(spelling) || lemmaClass.equals("none")) {
			} else {
				// Try compound word exceptions list first.
				lemmata = lemmatizer.lemmatize(spelling, "compound");

				// If lemma not found, keep trying.
				if (lemmata.equalsIgnoreCase(spelling)) {
					// Extract individual word parts.
					// May be more than one for a
					// contraction.
					List<String> wordList = spellingTokenizer.extractWords(spelling);

					// If just one word part, get its lemma.
					if (!partOfSpeechTags.isCompoundTag(partOfSpeech) || (wordList.size() == 1)) {
						if (lemmaClass.length() == 0) {
							lemmata = lemmatizer.lemmatize(spelling);
						} else {
							lemmata = lemmatizer.lemmatize(spelling, lemmaClass);
						}
					}
					// More than one word part.
					// Get lemma for each part and
					// concatenate them with the
					// lemma separator to form a
					// compound lemma.
					else {
						lemmata = "";
						String[] posTags = partOfSpeechTags.splitTag(partOfSpeech);

						if (posTags.length == wordList.size()) {
							for (int i = 0; i < wordList.size(); i++) {
								String wordPiece = wordList.get(i);
								if (i > 0) {
									lemmata = lemmata + lemmaSeparator;
								}

								LemmataEntry lemmaPiece = lemmatizeByLexiconAndRules(wordPiece, posTags[i]);

								lemmata = lemmata + lemmaPiece.lemmasToString();
							}
						}
					}
				}
			}

			lemmataAndLemmataTag.put(lemmataTag, lemmata);
		}

		return new LemmataEntry(lemmataAndLemmataTag, posEntry);
	}

	/**
	 * Input arguments are parsed into a {@link BioLemmatizerCmdOpts} object. Valid input arguments
	 * include:
	 * 
	 * <pre>
	 *  VAL    : Single input to be lemmatized
	 *  VAL    : Part of speech of the single input to be lemmatized
	 *  -f VAL : optional path to a lexicon file. If not set, the default lexicon 
	 *           available on the classpath is used
	 *  -i VAL : the path to the input file
	 *  -l     : if present, only the lemma is returned (part-of-speech information is 
	 *           suppressed)
	 *  -o VAL : the path to the output file
	 *  -t     : if present, the interactive mode is used
	 * </pre>
	 * 
	 * 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		BioLemmatizerCmdOpts options = new BioLemmatizerCmdOpts();
		CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			return;
		}

		File lexiconFile = options.getLexiconFile();
		BioLemmatizer bioLemmatizer = new BioLemmatizer(lexiconFile);
		boolean americanize = options.americanizedLemma();
		boolean outputLemmaOnly = options.outputLemmaOnly();
		boolean useInteractiveMode = options.useInteractiveMode();
		String inputStr = options.getInputStr();
		if (inputStr != null)
			inputStr = inputStr.trim();
		String inputStrPos = options.getInputStrPos();
		File inputFile = options.getInputFile();
		File outputFile = options.getOutputFile();
		System.out.println("=========================================================");
		System.out.println("=========================================================");
		System.out.println("=========================================================");
		System.out.println("Running BioLemmatizer....");
		try {
			if (useInteractiveMode) {
				runInteractiveMode(bioLemmatizer, outputLemmaOnly, americanize);
			} else if (inputStr != null) {
				LemmataEntry lemmata;
				if(americanize) {
					lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(new Americanize().americanize(unicodeHandler(inputStr)), inputStrPos);
				}	
				else
					lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(unicodeHandler(inputStr), inputStrPos);
				if (outputLemmaOnly) {
					System.out.println("The lemma for '" +inputStr+ "' is: " + lemmata.lemmasToString());
				} else {
					System.out.println("The lemma for '" +inputStr+ "' is: " + lemmata);
				}
			} else if (inputFile != null) {
				if (outputFile == null) {
					System.err.println("Output file must be set if the input file parameter is used.");
					parser.printUsage(System.err);
				}
				processInputFile(inputFile, outputFile, bioLemmatizer, outputLemmaOnly, americanize);
			} else {
				System.err.println("Invalid input parameters...");
				parser.printUsage(System.err);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("=========================================================");
		System.out.println("=========================================================");
		System.out.println("=========================================================");
	}

	/**
	 * @param inputFile
	 * @param outputFile
	 * @param bioLemmatizer
	 * @param outputLemmaOnly
	 * @throws IOException
	 */
	private static void processInputFile(File inputFile, File outputFile, BioLemmatizer bioLemmatizer,
			boolean outputLemmaOnly, boolean americanize) throws IOException {
		Americanize convert = null;
		if(americanize) 
			convert = new Americanize();
		BufferedReader input;
		BufferedWriter output;

		try {
			// input = FileReaderUtil.initBufferedReader(inputFile, CharacterEncoding.UTF_8);
			input = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), Charset.forName("UTF-8")
					.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT)));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unable to open the input file: " + inputFile.getAbsolutePath(), e);
		}

		try {
			// output = FileWriterUtil.initBufferedWriter(outputFile, CharacterEncoding.UTF_8,
			// WriteMode.OVERWRITE, FileSuffixEnforcement.OFF);
			output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile, false), Charset
					.forName("UTF-8").newEncoder().onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT)));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unable to open the output file: " + outputFile.getAbsolutePath(), e);
		}

		String line = null;

		while ((line = input.readLine()) != null) {
			if (line.trim().length() == 0) {
				output.write("\n");
				continue;
			}
			line = line.trim();
			String[] pair = line.split("\t");
			String pos;
			if (pair.length == 1) {
				pos = "";
			} else {
				pos = pair[1];
			}
			LemmataEntry lemmata;
			if(americanize)
			    lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(convert.americanize(unicodeHandler(pair[0])), pos);
			else
				lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(unicodeHandler(pair[0]), pos);
			String result;
			if (outputLemmaOnly) {
				result = line + "\t" + lemmata.lemmasToString() + "\n";
			} else {
				result = line + "\t" + lemmata + "\n";
			}
			output.write(result);
		}
		// close input
		input.close();
		// close output
		output.close();
	}

	private static void runInteractiveMode(BioLemmatizer bioLemmatizer, boolean outputLemmaOnly, boolean americanize) throws IOException {
		Americanize convert = null;
		if(americanize) 
			convert = new Americanize();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String input;
		System.out
				.println("Running BioLemmatizer in interactive mode. Please type a word to be lemmatized with an optional part-of-speech, e.g. \"run\" or \"run NN\"");
		while ((input = in.readLine()) != null && input.length() != 0) {
			String[] arguments = input.split("\\s");
			if (arguments.length > 2) {
				System.out.println("Only one word to be lemmatized (with or without POS) is allowed");
				System.exit(0);
			}
			String spelling = arguments[0].trim();
			String partOfSpeech = (arguments.length == 2) ? arguments[1].trim() : null;
			LemmataEntry lemmata; 
			if(americanize) 
			    lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(convert.americanize(unicodeHandler(spelling)), partOfSpeech);
			else
				lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(unicodeHandler(spelling), partOfSpeech);
			if (outputLemmaOnly) {
				System.out.println(lemmata.lemmasToString());
			} else {
				System.out.println(lemmata);
			}
		}
	}
}

/** Lemmatizer for English. */
class MorphAdornerLemmatizer extends EnglishLemmatizer {
	/** list of detachment rules. */
	protected static String rulesFileName = "englishrules.txt";

	/**
	 * Create an English lemmatizer.
	 * 
	 * @throws Exception
	 *             because the {@link EnglishLemmatizer} constructor throws Exception
	 * 
	 */
	public MorphAdornerLemmatizer() throws Exception {
		// release the rules of original MorphAdorner Lemmatizer
		rules.clear();
		// load new rules
		try {
			loadRules(BioLemmatizer.class.getResource(rulesFileName), "utf-8");
		} catch (IOException e) {
			throw new RuntimeException("Unable to load English rules file.", e);
		}
		// release the irregularForm file of original MorphAdorner Lemmatizer
		// the irregular English forms are integrated into current Lexicon
		irregularForms.clear();
	}
}

/**
 * BioWordLexicon: Biomedical word Lexicon which extends MorphAdorner's English word lexicon.
 */
class BioWordLexicon extends DefaultLexicon {
	/** Resource path to word lexicon. */
	protected static final String lexiconPath = "lexicon.lex";

	/**
	 * Create an empty lexicon.
	 * 
	 * @throws IOException
	 */
	public BioWordLexicon(File lexiconFile) throws IOException {
		// Create empty lexicon.
		super();
		if (lexiconFile == null) {
			// Load default word lexicon.
			loadLexicon(BioLemmatizer.class.getResource(lexiconPath), "utf-8");
		} else {
			loadLexicon(lexiconFile.toURI().toURL(), "utf-8");
		}
	}
}

/** POSEntry: store different POS tags and the corresponding tagset label */
class POSEntry {
	public Map<String, String> tagToTagSet;

	/**
	 * Construtor to initialize the class field by loading different POS tagsets
	 */
	public POSEntry() {
		tagToTagSet = new HashMap<String, String>();
		// NUPOS tags
		Lexicon wordLexicon;
		try {
			wordLexicon = new DefaultWordLexicon();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		addNewTagSet(Arrays.asList(wordLexicon.getCategories()), "NUPOS");

		// PennPOS tags
		String mappingFileName = "PennPOStoNUPOS.mapping";
		InputStream is = BioLemmatizer.class.getResourceAsStream(mappingFileName);
		Map<String, String[]> mappingPennPOStoNUPOS;
		try {
			mappingPennPOStoNUPOS = BioLemmatizer.loadPOSMappingFile(is);
		} catch (IOException e) {
			throw new RuntimeException("Error while opening mapping file: " + mappingFileName, e);
		}
		addNewTagSet(mappingPennPOStoNUPOS.keySet(), "PennPOS");
	}

	/**
	 * Add new POS tagset
	 * 
	 * @param tags
	 *            a set of POS tags
	 * @param tagSetLabel
	 *            the corresponding tagset label
	 */
	public void addNewTagSet(Collection<String> tags, String tagSetLabel) {
		for (String tag : tags) {
			tagToTagSet.put(tag, tagSetLabel);
		}
	}

	/**
	 * Retrieve the tagset label of the input POS tag
	 * 
	 * @param category
	 *            an input POS tag
	 * @return the corresponding POS tagset label
	 */
	public String getTagSetLabel(String category) {
		String defaultLabel = "NONE";
		return tagToTagSet.containsKey(category) ? tagToTagSet.get(category) : defaultLabel;
	}
}
