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
package edu.ucdenver.ccp.nlp.biolemmatizer.uima;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.junit.After;
import org.junit.Before;

import edu.ucdenver.ccp.nlp.biolemmatizer.uima.test.GenericAnnotation;
import edu.ucdenver.ccp.nlp.biolemmatizer.uima.test.Token;

/**
 * This is a base class for the BioLemmatizer_AE test suites. This class contains setup-related
 * methods that are used by the test suite classes.
 * 
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public abstract class BioLemmatizer_AETestBase {

	protected static final Class<Token> TOKEN_CLASS = edu.ucdenver.ccp.nlp.biolemmatizer.uima.test.Token.class;
	protected static final String TOKEN_GET_POS_METHOD_NAME = "getPosTag";
	protected static final Class<LemmaAnnotation> LEMMA_ANNOTATION_CLASS = edu.ucdenver.ccp.nlp.biolemmatizer.uima.LemmaAnnotation.class;

	protected abstract TypeSystemDescription getTypeSystemDescription();

	protected static final String DOCUMENT_TEXT = "The radiolabeled isotope was found in mitochondria.";
	protected static final String TOKEN_TYPE = "token";

	private JCas jcas;

	private TypeSystemDescription tsd;

	@Before
	public void setUp() throws Exception {
		setTsd(getTypeSystemDescription());
		setJCas(JCasFactory.createJCas(getTsd()));
		getJCas().setDocumentText(DOCUMENT_TEXT);
	}

	@After
	public void tearDown() throws Exception {
		if (getJCas() != null) {
			getJCas().release();
		}
	}

	/**
	 * Creates sample tokens over the document text
	 */
	protected void addSampleTokenAnnotations() {
		new Token(getJCas(), 0, 3).addToIndexes(); // The
		new Token(getJCas(), 4, 16).addToIndexes(); // radiolabeled
		new Token(getJCas(), 17, 24).addToIndexes(); // isotope
		new Token(getJCas(), 25, 28).addToIndexes(); // was
		new Token(getJCas(), 29, 34).addToIndexes(); // found
		new Token(getJCas(), 35, 37).addToIndexes(); // in
		new Token(getJCas(), 38, 50).addToIndexes(); // mitochondria
		new Token(getJCas(), 50, 51).addToIndexes(); // .
	}

	/**
	 * Creates sample tokens over the document text and includes part-of-speech information
	 */
	protected void addSampleTokenAnnotationsWithPartsOfSpeech() {
		Token t1 = new Token(getJCas(), 0, 3); // The
		t1.setPosTag("DT");
		t1.addToIndexes();
		Token t2 = new Token(getJCas(), 4, 16); // radiolabeled
		t2.setPosTag("VBZ");
		t2.addToIndexes();
		Token t3 = new Token(getJCas(), 17, 24); // isotope
		t3.setPosTag("NN");
		t3.addToIndexes();
		Token t4 = new Token(getJCas(), 25, 28); // was
		t4.setPosTag("VBZ");
		t4.addToIndexes();
		Token t5 = new Token(getJCas(), 29, 34); // found
		t5.setPosTag("VBZ");
		t5.addToIndexes();
		Token t6 = new Token(getJCas(), 35, 37); // in
		t6.setPosTag("IN");
		t6.addToIndexes();
		Token t7 = new Token(getJCas(), 38, 50); // mitochondria
		t7.setPosTag("NN");
		t7.addToIndexes();
		Token t8 = new Token(getJCas(), 50, 51); // .
		t8.setPosTag(".");
		t8.addToIndexes();
	}

	/**
	 * Creates sample tokens over the document text and includes part-of-speech information
	 */
	protected void addGenericallyTypedTokenAnnotationsWithPartsOfSpeech() {
		GenericAnnotation t1 = new GenericAnnotation(getJCas(), 0, 3); // The
		t1.setAnnotationType(TOKEN_TYPE);
		t1.setFields(UimaUtil.addToStringArray(new StringArray(getJCas(), 0), "POS;DT", getJCas()));
		t1.addToIndexes();
		GenericAnnotation t2 = new GenericAnnotation(getJCas(), 4, 16); // radiolabeled
		t2.setAnnotationType(TOKEN_TYPE);
		t2.setFields(UimaUtil.addToStringArray(new StringArray(getJCas(), 0), "POS;VBZ", getJCas()));
		t2.addToIndexes();
		GenericAnnotation t3 = new GenericAnnotation(getJCas(), 17, 24); // isotope
		t3.setAnnotationType(TOKEN_TYPE);
		t3.setFields(UimaUtil.addToStringArray(new StringArray(getJCas(), 0), "POS;NN", getJCas()));
		t3.addToIndexes();
		GenericAnnotation t4 = new GenericAnnotation(getJCas(), 25, 28); // was
		t4.setAnnotationType(TOKEN_TYPE);
		t4.setFields(UimaUtil.addToStringArray(new StringArray(getJCas(), 0), "POS;VBZ", getJCas()));
		t4.addToIndexes();
		GenericAnnotation t5 = new GenericAnnotation(getJCas(), 29, 34); // found
		t5.setAnnotationType(TOKEN_TYPE);
		t5.setFields(UimaUtil.addToStringArray(new StringArray(getJCas(), 0), "POS;VBZ", getJCas()));
		t5.addToIndexes();
		GenericAnnotation t6 = new GenericAnnotation(getJCas(), 35, 37); // in
		t6.setAnnotationType(TOKEN_TYPE);
		t6.setFields(UimaUtil.addToStringArray(new StringArray(getJCas(), 0), "POS;IN", getJCas()));
		t6.addToIndexes();
		GenericAnnotation t7 = new GenericAnnotation(getJCas(), 38, 50); // mitochondria
		t7.setAnnotationType(TOKEN_TYPE);
		t7.setFields(UimaUtil.addToStringArray(new StringArray(getJCas(), 0), "POS;NN", getJCas()));
		t7.addToIndexes();
		GenericAnnotation t8 = new GenericAnnotation(getJCas(), 50, 51); // .
		t8.setAnnotationType(TOKEN_TYPE);
		t8.setFields(UimaUtil.addToStringArray(new StringArray(getJCas(), 0), "POS;.", getJCas()));
		t8.addToIndexes();
	}

	/**
	 * @return a mapping from the token covered text to the expected lemma (when part-of-speech
	 *         information is used; when it's not used "The" results in multiple possible lemmas)
	 */
	protected Map<String, String> getTokenToLemmaMap() {
		Map<String, String> tokenToLemmaMap = new HashMap<String, String>();
		tokenToLemmaMap.put("The", "the");
		tokenToLemmaMap.put("radiolabeled", "radiolabel");
		tokenToLemmaMap.put("isotope", "isotope");
		tokenToLemmaMap.put("was", "be");
		tokenToLemmaMap.put("found", "find");
		tokenToLemmaMap.put("in", "in");
		tokenToLemmaMap.put("mitochondria", "mitochondrion");
		tokenToLemmaMap.put(".", ".");
		return tokenToLemmaMap;
	}

	/**
	 * Checks that there are 8 {@link LemmaAnnotation} objects in the CASs
	 * 
	 * @param expectedCount
	 */
	protected void assertLemmaAnnotationCount(int expectedCount) {
		int count = 0;
		for (Iterator<LemmaAnnotation> lemmaAnnotIter = JCasUtil.iterator(getJCas(), LEMMA_ANNOTATION_CLASS); lemmaAnnotIter
				.hasNext();) {
			lemmaAnnotIter.next();
			count++;
		}
		assertEquals(expectedCount, count);
	}

	/**
	 * @return the jcas
	 */
	public JCas getJCas() {
		return jcas;
	}

	/**
	 * @param jcas
	 *            the jcas to set
	 */
	private void setJCas(JCas jcas) {
		this.jcas = jcas;
	}

	/**
	 * @return the tsd
	 */
	public TypeSystemDescription getTsd() {
		return tsd;
	}

	/**
	 * @param tsd
	 *            the tsd to set
	 */
	private void setTsd(TypeSystemDescription tsd) {
		this.tsd = tsd;
	}

}
