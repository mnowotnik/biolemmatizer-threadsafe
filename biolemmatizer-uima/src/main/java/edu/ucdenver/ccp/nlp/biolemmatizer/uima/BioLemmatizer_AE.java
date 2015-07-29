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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import edu.ucdenver.ccp.nlp.biolemmatizer.BioLemmatizer;
import edu.ucdenver.ccp.nlp.biolemmatizer.LemmataEntry;
import edu.ucdenver.ccp.uima.shims.annotation.AnnotationDataExtractor;
import edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.LemmaDecorator;
import edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeechDecorator;

/**
 * This annotator processes tokens in the CAS and inserts corresponding lemmas. This annotator is
 * type-system-independent and relies on implementations of TokenAttributeExtractor,
 * TokenAttributeInserter, and AnnotationDataExtractor in order to function as intended.
 * 
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class BioLemmatizer_AE extends JCasAnnotator_ImplBase {

	/**
	 * Parameter name used in the UIMA descriptor file for the token type
	 */
	public static final String PARAM_TOKEN_TYPE_NAME = "tokenTypeName";

	/**
	 * The token type to use. This parameter serves a dual purpose. It can be used to represent the
	 * class name of the token annotation type to retrieve from the CAS, e.g.
	 * org.apache.uima.examples.tokenizer.Token. When this is the case, annotations of this type
	 * will be processed, and lemmas for the text covered by these annotations will be determined.
	 * The second use for this configuration parameter is when it is used in conjunction with an
	 * {@link AnnotationDataExtractor}. The
	 * {@link AnnotationDataExtractor#getAnnotationType(Annotation)} method returns a {@link String}
	 * and when the value of that returned {@link String} equals the value of the tokenTypeName,
	 * then that annotation will be treated as a token and its covered text will be lemmatized.
	 */
	@ConfigurationParameter(mandatory = true, description = "")
	private String tokenTypeName;

	/**
	 * Parameter name used in the UIMA descriptor file for the name of the method that returns a
	 * part-of-speech (as a String) from the token annotation
	 */
	public static final String PARAM_TOKEN_GET_POS_METHOD_NAME = "tokenGetPosMethodName";

	/**
	 * This is an optional parameter. It is used in conjunction with the tokenTypeName when that
	 * parameter represents a class name, i.e. when it represents the name of token annotation
	 * classes in the CAS. The tokenGetPosMethodName should be the name of the method in the
	 * tokenTypeName class that returns the part-of-speech tag. <br>
	 * <br>
	 * If this field is not set, then either the input tokens do not have part-of-speech information
	 * associated with them, or the tokenTypeName configuration parameter is not the name of an
	 * annotation class, but is instead a type as in the second scenario described above.
	 */
	@ConfigurationParameter(mandatory = false, description = "")
	private String tokenGetPosMethodName;

	/**
	 * Parameter name used in the UIMA descriptor file for the {@link PartOfSpeechDecorator}
	 * implementation to use
	 */
	public static final String PARAM_POS_DECORATOR_CLASS = "posDecoratorClassName";

	/**
	 * The name of the {@link PartOfSpeechDecorator} implementation to use
	 */
	@ConfigurationParameter(mandatory = false, description = "name of the PartOfSpeechDecorator implementation to use")
	private String posDecoratorClassName;

	/**
	 * The {@link PartOfSpeechDecorator} that will be initialized to the class specified by the
	 * {@link #posDecoratorClassName} configuration parameter
	 */
	private PartOfSpeechDecorator posDecorator;

	/**
	 * Parameter name used in the UIMA descriptor file for the {@link LemmaDecorator} implementation
	 * to use
	 */
	public static final String PARAM_LEMMA_DECORATOR_CLASS = "lemmaDecoratorClassName";

	/**
	 * The name of the {@link LemmaDecorator} implementation to use
	 */
	@ConfigurationParameter(mandatory = true, description = "name of the LemmaDecorator implementation to use", defaultValue = "edu.ucdenver.ccp.nlp.biolemmatizer.uima.DefaultLemmaDecorator")
	private String lemmaDecoratorClassName;

	/**
	 * The {@link LemmaDecorator} that will be initialized to the class specified by the
	 * {@link #lemmaDecoratorClassName} configuration parameter
	 */
	private LemmaDecorator lemmaDecorator;

	/**
	 * Parameter name used in the UIMA descriptor file for the annotation data extractor
	 * implementation to use
	 */
	public static final String PARAM_ANNOTATION_DATA_EXTRACTOR_CLASS = "annotationDataExtractorClassName";

	/**
	 * The name of the {@link AnnotationDataExtractor} implementation to use
	 */
	@ConfigurationParameter(mandatory = true, description = "name of the AnnotationDataExtractor implementation to use", defaultValue = "edu.ucdenver.ccp.uima.shims.annotation.impl.DefaultAnnotationDataExtractor")
	private String annotationDataExtractorClassName;

	/**
	 * this {@link AnnotationDataExtractor} will be initialized based on the class name specified by
	 * the annotationDataExtractorClassName parameter
	 */
	private AnnotationDataExtractor annotationDataExtractor;

	/**
	 * This {@link BioLemmatizer} will do the bulk of the work in the
	 * {@link BioLemmatizer_AE#process(JCas)} method
	 */
	private BioLemmatizer bioLemmatizer;

	/**
	 * Initializes the {@link BioLemmatizer} that will be used by the
	 * {@link BioLemmatizer_AE#process(JCas)} method
	 * 
	 * @see org.uimafit.component.JCasAnnotator_ImplBase#initialize(org.apache.uima.UimaContext)
	 */
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		bioLemmatizer = new BioLemmatizer();
		lemmaDecorator = (LemmaDecorator) invokeNoArgsConstructor(lemmaDecoratorClassName);
		annotationDataExtractor = (AnnotationDataExtractor) invokeNoArgsConstructor(annotationDataExtractorClassName);
		if (posDecoratorClassName != null) {
			posDecorator = (PartOfSpeechDecorator) invokeNoArgsConstructor(posDecoratorClassName);
		}
	}

	/**
	 * Returns an instantiation of the class specified by the input {@link String}. Assumes default
	 * constructor, i.e. no arguments.
	 * 
	 * @param className
	 * @param arguments
	 * @return
	 */
	public static Object invokeNoArgsConstructor(String className) {
		try {
			Class<?> cls = Class.forName(className);
			Constructor<?> constructor = cls.getConstructor();
			if (!constructor.isAccessible())
				constructor.setAccessible(true);
			return constructor.newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This process(JCas) method cycles through all annotations in the CAS. For those that are
	 * identified as tokens by {@link AnnotationDataExtractor} implementation being used, an attempt
	 * is made to extract part-of-speech information. The covered text for each token is then
	 * lemmatized using the {@link BioLemmatizer}, using the part-of-speech information if it was
	 * available. Results from the {@link BioLemmatizer} are added to the CAS via the specified
	 * {@link LemmaDecorator} implementation.
	 * 
	 * @see org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org.apache.uima.jcas.JCas)
	 */
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Iterator<Annotation> annotIter = jCas.getJFSIndexRepository().getAnnotationIndex().iterator(); annotIter
				.hasNext();) {
			Annotation annotation = annotIter.next();
			String annotationType = annotationDataExtractor.getAnnotationType(annotation);
			if (annotationType != null && annotationType.equals(tokenTypeName)) {
				List<edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeech> posTags = getPartOfSpeechTags(annotation);
				if (posTags == null || posTags.isEmpty()) {
					runBioLemmatizer(annotation, null);
				} else {
					for (edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeech posTag : posTags) {
						runBioLemmatizer(annotation, posTag.getPosTag());
					}
				}
			}
		}
	}

	/**
	 * This method uses the {@link BioLemmatizer} to lemmatize the covered text of the input
	 * {@link Annotation}. The lemma is added to the CAS via the {@link LemmaDecorator}
	 * implementation specified in this AE's configuration.
	 * 
	 * @param annotation
	 * @param posTag
	 */
	private void runBioLemmatizer(Annotation annotation, String posTag) {
		String coveredText = annotationDataExtractor.getCoveredText(annotation);
		LemmataEntry lemmata = bioLemmatizer.lemmatizeByLexiconAndRules(coveredText, posTag);
		Annotation lemmaAnnot = lemmaDecorator.getAnnotationToDecorate(annotation, annotationDataExtractor);
		for (edu.ucdenver.ccp.nlp.biolemmatizer.LemmataEntry.Lemma lemma : lemmata.getLemmas()) {
			lemmaDecorator.insertLemma(lemmaAnnot,
					new edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.Lemma(lemma.getLemma(),
							new edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeech(lemma.getPos(),
									lemma.getTagSetName())));
		}
	}

	/**
	 * This method defaults to using the {@link PartOfSpeechDecorator} instance if there is one
	 * initialized. If not available, it will try to use the getPosMethod specified in the
	 * configuration. If neither are available, it is assumed that there is no input part-of-speech
	 * info and null is returned.
	 * 
	 * @param annotation
	 * 
	 * @return the POS tag as extracted from the input {@link Annotation}
	 */
	private List<edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeech> getPartOfSpeechTags(
			Annotation annotation) {
		if (posDecorator != null) {
			return posDecorator.extractPartsOfSpeech(annotation);
		}
		if (tokenGetPosMethodName != null) {
			List<edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeech> posTagList = new ArrayList<edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeech>();
			posTagList.add(getPosTagUsingSpecifiedMethodName(annotation));
			return posTagList;
		}
		return null;
	}

	/**
	 * If the getPosTag method name is specified (and if no PartOfSpeechDecorator is specified) then
	 * this method is used to call the getPosTag method on the input {@link Annotation}.
	 * 
	 * @param annotation
	 * @return the {@link edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeech}
	 *         extracted from the input {@link Annotation} using the specified
	 *         {@link #tokenGetPosMethodName}.
	 */
	private edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeech getPosTagUsingSpecifiedMethodName(
			Annotation annotation) {
		try {
			Method method = annotation.getClass().getDeclaredMethod(tokenGetPosMethodName);
			String posTag = method.invoke(annotation).toString();
			return new edu.ucdenver.ccp.uima.shims.annotation.syntactic.token.PartOfSpeech(posTag, null);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
					"Error while attempting to retrieve part-of-speech information from class: "
							+ annotation.getClass().getName() + " using method: " + tokenGetPosMethodName + ".", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(
					"Error while attempting to retrieve part-of-speech information from class: "
							+ annotation.getClass().getName() + " using method: " + tokenGetPosMethodName + ".", e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(
					"Error while attempting to retrieve part-of-speech information from class: "
							+ annotation.getClass().getName() + " using method: " + tokenGetPosMethodName + ".", e);
		}
	}

	/**
	 * Initializes an {@link AnalysisEngine} that will determine lemmas for tokens that are present
	 * in the {@link CAS}
	 * 
	 * @param tsd
	 * @param tokenClass
	 * @param tokenGetPosMethodName
	 * @param annotationDataExtractorClass
	 * @param lemmaDecoratorClass
	 * @return
	 * @throws ResourceInitializationException
	 * 
	 */
	public static AnalysisEngineDescription createAnalysisEngineDescription(TypeSystemDescription tsd,
			Class<? extends Annotation> tokenClass, String tokenGetPosMethodName,
			Class<? extends AnnotationDataExtractor> annotationDataExtractorClass,
			Class<? extends LemmaDecorator> lemmaDecoratorClass) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(BioLemmatizer_AE.class, tsd, PARAM_TOKEN_TYPE_NAME,
				tokenClass.getName(), PARAM_TOKEN_GET_POS_METHOD_NAME, tokenGetPosMethodName,
				PARAM_ANNOTATION_DATA_EXTRACTOR_CLASS, annotationDataExtractorClass.getName(),
				PARAM_LEMMA_DECORATOR_CLASS, lemmaDecoratorClass.getName());
	}

	/**
	 * @param tsd
	 * @param tokenType
	 * @param partOfSpeechDecoratorClass
	 * @param annotationDataExtractorClass
	 * @param lemmaDecoratorClass
	 * @return
	 * @throws ResourceInitializationException
	 */
	public static AnalysisEngineDescription createAnalysisEngineDescription(TypeSystemDescription tsd,
			String tokenType, Class<? extends PartOfSpeechDecorator> partOfSpeechDecoratorClass,
			Class<? extends AnnotationDataExtractor> annotationDataExtractorClass,
			Class<? extends LemmaDecorator> lemmaDecoratorClass) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(BioLemmatizer_AE.class, tsd, PARAM_TOKEN_TYPE_NAME,
				tokenType, PARAM_POS_DECORATOR_CLASS, partOfSpeechDecoratorClass.getName(),
				PARAM_ANNOTATION_DATA_EXTRACTOR_CLASS, annotationDataExtractorClass.getName(),
				PARAM_LEMMA_DECORATOR_CLASS, lemmaDecoratorClass.getName());
	}

	/**
	 * @param tsd
	 * @param tokenClass
	 * @return
	 * @throws ResourceInitializationException
	 */
	public static AnalysisEngineDescription createAnalysisEngineDescription(TypeSystemDescription tsd,
			Class<? extends Annotation> tokenClass) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(BioLemmatizer_AE.class, tsd, PARAM_TOKEN_TYPE_NAME,
				tokenClass.getName());
	}

	/**
	 * @param tsd
	 * @param tokenClass
	 * @param tokenGetPosMethodName
	 * @return
	 * @throws ResourceInitializationException
	 */
	public static AnalysisEngineDescription createAnalysisEngineDescription(TypeSystemDescription tsd,
			Class<? extends Annotation> tokenClass, String tokenGetPosMethodName)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(BioLemmatizer_AE.class, tsd, PARAM_TOKEN_TYPE_NAME,
				tokenClass.getName(), PARAM_TOKEN_GET_POS_METHOD_NAME, tokenGetPosMethodName);
	}

}
