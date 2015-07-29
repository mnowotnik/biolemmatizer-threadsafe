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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class BioLemmatizerTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private static BioLemmatizer bioLemmatizer;
	
	private static Americanize convert;

	@BeforeClass
	public static void setUp() {
		bioLemmatizer = new BioLemmatizer();
		convert = new Americanize();
	}

	@Test
	public void testSimpleLemma() {
		String spelling = "radiolabeled";
		String expectedLemma = "radiolabel";
		String partOfSpeech = "VBZ";
		Set<String> expectedLemmas = new HashSet<String>();
		expectedLemmas.add(expectedLemma);

		LemmataEntry lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(spelling, partOfSpeech);
		assertEquals("Lemma not as expected", expectedLemmas, new HashSet<String>(lemmata.lemmasAndCategories.values()));
	}

	@Test
	public void testSimpleLemma2() {
		String spelling = "genesis";
		String expectedLemma = "genesis";
		String partOfSpeech = "NN";
		Set<String> expectedLemmas = new HashSet<String>();
		expectedLemmas.add(expectedLemma);

		LemmataEntry lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(spelling, partOfSpeech);
		assertEquals("Lemma not as expected", expectedLemmas, new HashSet<String>(lemmata.lemmasAndCategories.values()));
	}
	
	@Test
	public void testBritishToAmericanLemma() {
		String spelling = "phaeochromocytomata";
		String expectedLemma = "pheochromocytoma";
		String partOfSpeech = "NNS";
		Set<String> expectedLemmas = new HashSet<String>();
		expectedLemmas.add(expectedLemma);

		LemmataEntry lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(convert.americanize(spelling), partOfSpeech);
		assertEquals("Lemma not as expected", expectedLemmas, new HashSet<String>(lemmata.lemmasAndCategories.values()));
	}
	
	@Test
	public void testInvalidPosInput() {
		String spelling = "radiolabeled";
		String expectedLemma = "radiolabel";
		String partOfSpeech = "BZ";
		Set<String> expectedLemmas = new HashSet<String>();
		expectedLemmas.add(expectedLemma);

		LemmataEntry lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(spelling, partOfSpeech);
		assertEquals("Lemma not as expected", expectedLemmas, new HashSet<String>(lemmata.lemmasAndCategories.values()));
	}

	
	@Test
	public void testInvalidPosInput2() {
		String spelling = "runs";
		String expectedLemma = "run";
		String partOfSpeech = "";
		Set<String> expectedLemmas = new HashSet<String>();
		expectedLemmas.add(expectedLemma);

		LemmataEntry lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(spelling, partOfSpeech);
		assertEquals("Lemma not as expected", expectedLemmas, new HashSet<String>(lemmata.lemmasAndCategories.values()));
	}
	
	@Test
	public void testInPosTag() {
		String spelling = "in";
		String expectedLemma = "in";
		String partOfSpeech = "IN";
		Set<String> expectedLemmas = new HashSet<String>();
		expectedLemmas.add(expectedLemma);

		LemmataEntry lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(spelling, partOfSpeech);
		assertEquals("Lemma not as expected", expectedLemmas, new HashSet<String>(lemmata.lemmasAndCategories.values()));
	}
		
	@Test
	public void testInputFromFileAndOutputToFile() throws IOException {
		File outputFile = folder.newFile("output");
		File inputFile = ClasspathUtil.copyClasspathResourceToDirectory(this.getClass(), "test-input", folder.getRoot());

		String[] args = new String[] {"-l", "-i", inputFile.getAbsolutePath(), "-o" , outputFile.getAbsolutePath()};
		BioLemmatizer.main(args);

		List<String> expectedLines = new ArrayList<String>();
		expectedLines.add("Roles\tNNS\trole");
		expectedLines.add("quantitated\tVBD\tquantitate");
		expectedLines.add("diminished\tJJ\tdiminished");
		expectedLines.add("larger\tJJR\tlarge");
		expectedLines.add("Biosciences\tNNP\tBioscience");
		
		BufferedReader br = new BufferedReader(new FileReader(outputFile));
		for (String expectedLine : expectedLines) {
			String line = br.readLine();
			assertNotNull(line);
			assertEquals(expectedLine, line);
		}
		assertNull(br.readLine());
		br.close();
	}
	
}


