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

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.ucdenver.ccp.uima.shims.annotation.AnnotationDataExtractor;
import edu.ucdenver.ccp.uima.shims.annotation.Span;
import edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.Lemma;
import edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.LemmaDecorator;

/**
 * This simple implementation of the {@link LemmaDecorator} interface adds a new
 * {@link LemmaAnnotation} to the CAS for each token annotation processed. 
 * 
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class DefaultLemmaDecorator implements LemmaDecorator {

	/**
	 * @return an initialized {@link LemmaAnnotation}
	 * @see edu.ucdenver.ccp.uima.shims.annotation.AnnotationDecorator#newAnnotation(org.apache.uima.jcas.JCas,
	 *      java.lang.String, edu.ucdenver.ccp.uima.shims.annotation.Span)
	 */
	@Override
	public Annotation newAnnotation(JCas jcas, @SuppressWarnings("unused") String type, Span span) {
		LemmaAnnotation lemmaAnnot = new LemmaAnnotation(jcas, span.getSpanStart(), span.getSpanEnd());
		lemmaAnnot.addToIndexes();
		return lemmaAnnot;
	}

	/**
	 * In the case of the {@link DefaultLemmaDecorator}, the annotation to decorate is a
	 * {@link LemmaAnnotation} with the same span as the token that was used to process the lemma.
	 * This method looks to see if that {@link LemmaAnnotation} exists. If it does it is returned.
	 * If it does not exist, then a new {@link LemmaAnnotation} is created and then returned.
	 * 
	 * @param tokenAnnotation
	 *            in this case, the input annotation represents the token annotation whose covered
	 *            text was lemmatized
	 * 
	 * @see edu.ucdenver.ccp.uima.shims.annotation.AnnotationDecorator#getAnnotationToDecorate(org.apache.uima.jcas.tcas.Annotation,
	 *      edu.ucdenver.ccp.uima.shims.annotation.AnnotationDataExtractor)
	 */
	@Override
	public Annotation getAnnotationToDecorate(Annotation tokenAnnotation,
			@SuppressWarnings("unused") AnnotationDataExtractor annotationDataExtractor) {
		JCas jCas = null;
		try {
			jCas = tokenAnnotation.getCAS().getJCas();
		} catch (CASException e) {
			throw new IllegalStateException(e);
		}
		List<LemmaAnnotation> existingLemmaAnnotations = JCasUtil.selectCovered(jCas, LemmaAnnotation.class,
				tokenAnnotation);
		if (existingLemmaAnnotations.isEmpty()) {
			return newAnnotation(jCas, null, new Span(tokenAnnotation.getBegin(), tokenAnnotation.getEnd()));
		}
		if (existingLemmaAnnotations.size() == 1) {
			return existingLemmaAnnotations.get(0);
		}
		throw new IllegalStateException("Multiple LemmaAnnotations covering: " + tokenAnnotation.toString(0));
	}

	/**
	 * 
	 * @see edu.ucdenver.ccp.uima.shims.annotation.AnnotationDecorator#decorateAnnotation(org.apache
	 *      .uima.jcas.tcas.Annotation, java.lang.String, java.lang.Object)
	 */
	@Override
	public void decorateAnnotation(Annotation annotation, @SuppressWarnings("unused") String attributeType, Lemma lemma) {
		insertLemma(annotation, lemma);
	}

	/**
	 * Inserts information representing the input {@link Lemma} into the input {@link Annotation}
	 * which is assumed to be of type {@link LemmaAnnotation} in this instance.
	 * 
	 * @throws IllegalArgumentException
	 *             if the input {@link Annotation} is not a {@link LemmaAnnotation}
	 * 
	 * @see edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.LemmaDecorator#insertLemma(org.apache.uima.jcas.tcas.Annotation,
	 *      edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.Lemma)
	 */
	@Override
	public void insertLemma(Annotation annotation, Lemma lemma) {
		checkAnnotationType(annotation);
		LemmaAnnotation lemmaAnnot = (LemmaAnnotation) annotation;
		try {
			addLemma(lemmaAnnot, lemma);
		} catch (CASException e) {
			throw new IllegalStateException(e);
		}
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
		if (!(annotation instanceof LemmaAnnotation)) {
			throw new IllegalArgumentException(
					"Expecting LemmaAnnotation class. Unable to assign lemma information to annotation of type: "
							+ annotation.getClass().getName());
		}
	}

	/**
	 * Transfers information from the input {@link Lemma} to the input {@link LemmaAnnotation},
	 * specifically the lemmatized string and its accompanying part-of-speech
	 * 
	 * @param lemmaAnnot
	 * @param lemma
	 * @throws CASException
	 *             if the {@link JCas} is not retrievable from the input {@link LemmaAnnotation}
	 */
	private static void addLemma(LemmaAnnotation lemmaAnnot, Lemma lemma) throws CASException {
		JCas jCas = lemmaAnnot.getCAS().getJCas();
		StringArray lemmas = UimaUtil.addToStringArray(lemmaAnnot.getLemmas(), lemma.getLemma(), jCas);
		PartOfSpeech pos = getPartOfSpeech(lemma, jCas);
		FSArray partsOfSpeech = UimaUtil.addToFSArray(lemmaAnnot.getPartsOfSpeech(), pos, jCas);
		lemmaAnnot.setLemmas(lemmas);
		lemmaAnnot.setPartsOfSpeech(partsOfSpeech);
	}

	/**
	 * @param lemmaAnnot
	 * @return a List of {@link Lemma} objects that were found in the input {@link LemmaAnnotation}
	 */
	private static List<Lemma> extractLemmas(LemmaAnnotation lemmaAnnot) {
		List<Lemma> lemmasToReturn = new ArrayList<Lemma>();
		StringArray lemmas = lemmaAnnot.getLemmas();
		FSArray partsOfSpeech = lemmaAnnot.getPartsOfSpeech();
		for (int i = 0; i < lemmas.size(); i++) {
			String lemmaStr = lemmas.get(i);
			PartOfSpeech pos = (PartOfSpeech) partsOfSpeech.get(i);
			lemmasToReturn.add(new Lemma(lemmaStr, pos.getPosTag(), pos.getTagSetName()));
		}
		return lemmasToReturn;
	}

	/**
	 * @param lemma
	 * @param jCas
	 * @return a {@link PartOfSpeech} object initialized from the input {@link Lemma}
	 */
	private static PartOfSpeech getPartOfSpeech(Lemma lemma, JCas jCas) {
		PartOfSpeech pos = new PartOfSpeech(jCas);
		pos.setPosTag(lemma.getPos().getPosTag());
		pos.setTagSetName(lemma.getPos().getTagSetName());
		return pos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.ucdenver.ccp.uima.shims.annotation.AnnotationDecorator#extractAttribute(org.apache.uima
	 * .jcas.tcas.Annotation, java.lang.String)
	 */
	@Override
	public List<Lemma> extractAttribute(Annotation annotation, String attributeType) {
		return extractLemmas(annotation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.LemmaDecorator#extractLemma(org.apache
	 * .uima.jcas.tcas.Annotation)
	 */
	@Override
	public List<Lemma> extractLemmas(Annotation annotation) {
		checkAnnotationType(annotation);
		LemmaAnnotation lemmaAnnot = (LemmaAnnotation) annotation;
		return extractLemmas(lemmaAnnot);
	}

}
