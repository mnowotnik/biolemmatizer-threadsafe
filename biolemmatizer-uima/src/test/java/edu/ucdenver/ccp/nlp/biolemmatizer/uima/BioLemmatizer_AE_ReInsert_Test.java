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

import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.junit.Test;

import edu.ucdenver.ccp.nlp.biolemmatizer.uima.test.Token;
import edu.ucdenver.ccp.uima.shims.annotation.AnnotationDataExtractor;
import edu.ucdenver.ccp.uima.shims.annotation.Span;
import edu.ucdenver.ccp.uima.shims.annotation.impl.DefaultAnnotationDataExtractor;
import edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.Lemma;
import edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.LemmaDecorator;

/**
 * This test case serves as an example that is slightly different from the default setup. Instead of
 * creating a {@link LemmaAnnotation} class to store the lemmatized output, this example
 * demonstrates how to insert the output of the lemmatizer back into the token annotation that was
 * processed. To do this, we implement a {@link LemmaDecorator} that knows how to add the lemma
 * information to the input token annotation.
 * 
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class BioLemmatizer_AE_ReInsert_Test extends BioLemmatizer_AETestBase {

	/**
	 * The type system used in the tests below consists of three types. An example Token type
	 * available only in the context of these tests, and the PartOfSpeech and LemmaAnnotation types
	 * that come with the BioLemmatizer distribution.
	 */
	@Override
	protected TypeSystemDescription getTypeSystemDescription() {
		return TypeSystemDescriptionFactory.createTypeSystemDescription(
				"edu.ucdenver.ccp.nlp.biolemmatizer.uima.TypeSystem",
				"edu.ucdenver.ccp.nlp.biolemmatizer.uima.TestTypeSystem");
	}

	/**
	 * Input for this test is {@link Token} annotations with parts-of-speech included. Here, we
	 * specify the name of the field storing the part-of-speech information so that it can be used
	 * during the lemmatization procedure. Finally, we have implemented a simple
	 * {@link LemmaDecorator} and it is used to insert the lemma for a given token as a field of
	 * token annotation itself, see {@link ExampleTokenLemmaDecorator}.
	 * 
	 * @throws ResourceInitializationException
	 * @throws AnalysisEngineProcessException
	 */
	@Test
	public void testBioLemmatizer_TokenInput_WithPOS_InsertLemmaIntoToken() throws ResourceInitializationException,
			AnalysisEngineProcessException {
		addSampleTokenAnnotationsWithPartsOfSpeech();
		AnalysisEngineDescription aeDesc = BioLemmatizer_AE.createAnalysisEngineDescription(getTsd(), TOKEN_CLASS,
				TOKEN_GET_POS_METHOD_NAME, DefaultAnnotationDataExtractor.class, ExampleTokenLemmaDecorator.class);
		AnalysisEngine ae = AnalysisEngineFactory.createPrimitive(aeDesc);
		ae.process(getJCas());

		assertLemmaAnnotationCount(0);

		for (Iterator<Token> tokenAnnotIter = JCasUtil.iterator(getJCas(), TOKEN_CLASS); tokenAnnotIter.hasNext();) {
			Token tokenAnnot = tokenAnnotIter.next();
			String coveredText = tokenAnnot.getCoveredText();
			assertTrue(tokenAnnot.getLemmas(0).startsWith(getTokenToLemmaMap().get(coveredText)));
		}
	}

	/**
	 * Example class demonstrating how you might insert the output of the BioLemmatizer into the
	 * annotation that was originally processed.
	 * 
	 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
	 * 
	 */
	public static class ExampleTokenLemmaDecorator implements LemmaDecorator {

		/**
		 * This method not needed for this particular LemmaDecorator implementation
		 */
		@Override
		public Annotation newAnnotation(JCas jcas, String type, Span span) {
			throw new UnsupportedOperationException(
					"This LemmaDecorator places the lemmatized text in a field of a pre-existing "
							+ "token annotation and therefore does not need to create any new annotations.");
		}

		/**
		 * In the case of this LemmaDecorator, we are adding the lemmatized string back to the token
		 * annotation that was originally processed, therefore this method simply returns the input
		 * annotation
		 */
		@Override
		public Annotation getAnnotationToDecorate(Annotation inputAnnotation,
				@SuppressWarnings("unused") AnnotationDataExtractor annotationDataExtractor) {
			return inputAnnotation;
		}

		/**
		 */
		@Override
		public void decorateAnnotation(Annotation annotation, @SuppressWarnings("unused") String attributeType,
				Lemma lemma) {
			insertLemma(annotation, lemma);
		}

		/**
		 * Stores lemma information in the input {@link Annotation}
		 */
		@Override
		public void insertLemma(Annotation annotation, Lemma lemma) {
			checkAnnotationType(annotation);
			Token tokenAnnot = (Token) annotation;
			try {
				addLemma(tokenAnnot, lemma);
			} catch (CASException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Adds a lemma to the input {@link Token} by adding a string to the lemmas StringArray
		 * 
		 * @param tokenAnnot
		 * @param lemma
		 * @throws CASException
		 */
		private static void addLemma(Token tokenAnnot, Lemma lemma) throws CASException {
			JCas jCas = tokenAnnot.getCAS().getJCas();
			StringArray lemmas = tokenAnnot.getLemmas();
			StringArray updatedLemmas = UimaUtil.addToStringArray(lemmas, lemma.serializeToString(), jCas);
			tokenAnnot.setLemmas(updatedLemmas);
		}

		/**
		 * Checks that the input {@link Annotation} is a {@link LemmaAnnotation}
		 * 
		 * @param annotation
		 * @throws IllegalArgumentException
		 *             if the input {@link Annotation} is not a {@link LemmaAnnotation}
		 * 
		 */
		private static void checkAnnotationType(Annotation annotation) {
			if (!(annotation instanceof Token))
				throw new IllegalArgumentException(
						"Expecting Token class. Unable to assign lemma information to annotation of type: "
								+ annotation.getClass().getName());
		}

		@Override
		public List<Lemma> extractAttribute(Annotation annotation, String attributeType) {
			throw new UnsupportedOperationException("This method not used in the context of these unit tests.");
		}

		@Override
		public List<Lemma> extractLemmas(Annotation annotation) {
			throw new UnsupportedOperationException("This method not used in the context of these unit tests.");
		}

	}

}
