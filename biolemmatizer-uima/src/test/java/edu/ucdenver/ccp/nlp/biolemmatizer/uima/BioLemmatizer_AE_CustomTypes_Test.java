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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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

import edu.ucdenver.ccp.nlp.biolemmatizer.uima.test.GenericAnnotation;
import edu.ucdenver.ccp.nlp.biolemmatizer.uima.test.Token;
import edu.ucdenver.ccp.uima.shims.annotation.AnnotationDataExtractor;
import edu.ucdenver.ccp.uima.shims.annotation.AnnotationTypeExtractor;
import edu.ucdenver.ccp.uima.shims.annotation.Span;
import edu.ucdenver.ccp.uima.shims.annotation.impl.DefaultAnnotationSpanExtractor;
import edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.Lemma;
import edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.LemmaDecorator;
import edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeech;
import edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeechDecorator;

/**
 * This test case serves as a demonstration of the flexibility of the shim architecture when used
 * with the BioLemmatizer. Here we use tokens that are represented by a generic annotation that has
 * a type field where the type field equals "token" for all token annotations. The generated lemmas
 * are added as fields to the generic token annotations. Part-of-speech information is stored in the
 * generic annotation as a field (represented simply as a string in a StringArray for the purposes
 * of this test. In reality the fields can be more complicated). A {@link PartOfSpeechDecorator}
 * implementation is used to extract the POS information.
 * 
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class BioLemmatizer_AE_CustomTypes_Test extends BioLemmatizer_AETestBase {

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
	 * As stated above, this test uses generic annotations with a type="token" to represent token
	 * annotations. Three implementations of three simple interfaces are included in this class, one
	 * for each of {@link PartOfSpeechDecorator}, {@link AnnotationDataExtractor}, and
	 * {@link LemmaDecorator}. These implementations control how the token annotations are
	 * identified, how the part-of-speech information is extracted, and how the lemma information is
	 * placed into the CAS after processing.
	 * 
	 * @throws ResourceInitializationException
	 * @throws AnalysisEngineProcessException
	 */
	@Test
	public void testBioLemmatizer_GenericTokenAnnotationInput_WithPOS_InsertLemmaIntoToken()
			throws ResourceInitializationException, AnalysisEngineProcessException {
		addGenericallyTypedTokenAnnotationsWithPartsOfSpeech();
		AnalysisEngineDescription aeDesc = BioLemmatizer_AE.createAnalysisEngineDescription(getTsd(), TOKEN_TYPE,
				ExampleGenericTokenPartOfSpeechDecorator.class, ExampleGenericAnnotationDataExtractor.class,
				ExampleGenericTokenLemmaDecorator.class);
		AnalysisEngine ae = AnalysisEngineFactory.createPrimitive(aeDesc);
		ae.process(getJCas());

		assertLemmaAnnotationCount(0);

		for (Iterator<GenericAnnotation> annotIter = JCasUtil.iterator(getJCas(), GenericAnnotation.class); annotIter
				.hasNext();) {
			GenericAnnotation annot = annotIter.next();
			if (annot.getAnnotationType().equals(TOKEN_TYPE)) {
				String coveredText = annot.getCoveredText();
				/* first field is the POS, second field should be the lemma */
				assertEquals(2, annot.getFields().size());
				assertTrue(annot.getFields(1).substring("Lemma;".length())
						.startsWith(getTokenToLemmaMap().get(coveredText)));
			}
		}
	}

	/**
	 * Simple implementation of the {@link PartOfSpeechDecorator} interface. In this case POS
	 * information is stored as a "field" which is represented as an entry in a {@link StringArray}.
	 * 
	 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
	 * 
	 */
	public static class ExampleGenericTokenPartOfSpeechDecorator implements PartOfSpeechDecorator {

		@Override
		public Annotation newAnnotation(JCas jcas, String type, Span span) {
			throw new UnsupportedOperationException(
					"This PartOfSpeechDecorator places the pos tag in a field of a pre-existing "
							+ "token annotation and therefore does not need to create any new annotations.");
		}

		@Override
		public Annotation getAnnotationToDecorate(Annotation inputAnnotation,
				AnnotationDataExtractor annotationDataExtractor) {
			return inputAnnotation;
		}

		@Override
		public void insertPartOfSpeech(Annotation annotation,
				edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeech pos) {
			throw new UnsupportedOperationException("This method not needed for usage in this unit test");
		}

		/**
		 * Extracts part-of-speech information from "fields" stored as Strings in a
		 * {@link StringArray}
		 */
		@Override
		public List<PartOfSpeech> extractPartsOfSpeech(Annotation annotation) {
			checkAnnotationType(annotation);
			GenericAnnotation annot = (GenericAnnotation) annotation;
			List<PartOfSpeech> posTags = new ArrayList<PartOfSpeech>();
			for (int i = 0; i < annot.getFields().size(); i++) {
				String field = annot.getFields(i);
				if (field.startsWith("POS;"))
					posTags.add(PartOfSpeech.deserializeFromString(field.substring(4)));
			}
			return posTags;
		}

		/**
		 * Checks that the input {@link Annotation} is a {@link GenericAnnotation}
		 * 
		 * @param annotation
		 * @throws IllegalArgumentException
		 *             if the input {@link Annotation} is not a {@link GenericAnnotation}
		 * 
		 */
		private static void checkAnnotationType(Annotation annotation) {
			if (!(annotation instanceof GenericAnnotation))
				throw new IllegalArgumentException("Expecting GenericAnnotation class. Unable to extract type from: "
						+ annotation.getClass().getName());

		}

		@Override
		public void decorateAnnotation(Annotation annotation, String attributeType, PartOfSpeech attribute) {
			throw new UnsupportedOperationException("This method not used in the context of these unit tests.");

		}

		@Override
		public List<PartOfSpeech> extractAttribute(Annotation annotation, String attributeType) {
			throw new UnsupportedOperationException("This method not used in the context of these unit tests.");
		}
	}

	/**
	 * Simple {@link AnnotationDataExtractor} implementation that uses the
	 * {@link ExampleGenericAnnotationTypeExtractor} below in combination with the
	 * {@link DefaultAnnotationSpanExtractor}
	 * 
	 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
	 * 
	 */
	public static class ExampleGenericAnnotationDataExtractor extends AnnotationDataExtractor {

		/**
		 * @param typeExtractor
		 * @param spanExtractor
		 */
		public ExampleGenericAnnotationDataExtractor() {
			super(new ExampleGenericAnnotationTypeExtractor(), new DefaultAnnotationSpanExtractor());
		}

	}

	/**
	 * Returns the type by accessing the {@link GenericAnnotation#getAnnotationType()} method
	 * 
	 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
	 * 
	 */
	public static class ExampleGenericAnnotationTypeExtractor implements AnnotationTypeExtractor {

		@Override
		public String getAnnotationType(Annotation annotation) {
			if (!(annotation instanceof GenericAnnotation))
				return null;
			GenericAnnotation annot = (GenericAnnotation) annotation;
			return annot.getAnnotationType();
		}
	}

	/**
	 * Example class demonstrating how you might insert the output of the BioLemmatizer into the
	 * annotation that was originally processed.
	 * 
	 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
	 * 
	 */
	public static class ExampleGenericTokenLemmaDecorator implements LemmaDecorator {

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
			GenericAnnotation tokenAnnot = (GenericAnnotation) annotation;
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
		private static void addLemma(GenericAnnotation tokenAnnot, Lemma lemma) throws CASException {
			JCas jCas = tokenAnnot.getCAS().getJCas();
			tokenAnnot.setFields(UimaUtil.addToStringArray(tokenAnnot.getFields(), "Lemma;" + lemma.getLemma(), jCas));
		}

		/**
		 * Checks that the input {@link Annotation} is a {@link Token}
		 * 
		 * @param annotation
		 * @throws IllegalArgumentException
		 *             if the input {@link Annotation} is not a {@link Token}
		 * 
		 */
		private static void checkAnnotationType(Annotation annotation) {
			if (!(annotation instanceof GenericAnnotation))
				throw new IllegalArgumentException(
						"Expecting GenericAnnotation class. Unable to assign lemma information to annotation of type: "
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
