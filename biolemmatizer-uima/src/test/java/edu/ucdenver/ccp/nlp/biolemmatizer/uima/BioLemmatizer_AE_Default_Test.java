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

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.junit.Test;

/**
 * This test suite demonstrates how the {@link BioLemmatizer_AE} might integrate into a pipeline
 * that contains an annotation class that represents tokens. If that token annotation class happens
 * to have a String field that stores a part-of-speech tag, the {
 * {@link #testBioLemmatizer_TokenInput_WithPOS_DefaultTypes()} test demonstrates how that field can
 * be accessed by specifying a configuration parameter.
 * 
 * For examples demonstrating interaction with more complex type systems, please see the other test
 * suites that accompany this distribution.
 * 
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class BioLemmatizer_AE_Default_Test extends BioLemmatizer_AETestBase {

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
	 * This test demonstrates how to use the BioLemmatizer giving it the name of a class that
	 * represents tokens as input. In this case, no part-of-speech information is included with the
	 * input. By default, the BioLemmatizer_AE creates one {@link LemmaAnnotation} for each token
	 * processed.
	 * 
	 * @throws ResourceInitializationException
	 * @throws AnalysisEngineProcessException
	 */
	@Test
	public void testBioLemmatizer_TokenInput_NoPOS_DefaultTypes() throws ResourceInitializationException,
			AnalysisEngineProcessException {
		addSampleTokenAnnotations();
		AnalysisEngineDescription aeDesc = BioLemmatizer_AE.createAnalysisEngineDescription(getTsd(), TOKEN_CLASS);
		AnalysisEngine ae = AnalysisEngineFactory.createPrimitive(aeDesc);
		ae.process(getJCas());
		assertLemmaAnnotationCount(8);
	}

	/**
	 * This test demonstrates how to use the BioLemmatizer giving it the name of a class that
	 * represents tokens as input. In this case, the token annotation class stores part-of-speech
	 * information in a field as a simple String, and this field is indicated in the call to the
	 * BioLemmatizer_AE init method using the TOKEN_GET_POS_METHOD_NAME parameter. By default, the
	 * BioLemmatizer_AE creates one {@link LemmaAnnotation} for each token processed.
	 * 
	 * @throws ResourceInitializationException
	 * @throws AnalysisEngineProcessException
	 */
	@Test
	public void testBioLemmatizer_TokenInput_WithPOS_DefaultTypes() throws ResourceInitializationException,
			AnalysisEngineProcessException {
		addSampleTokenAnnotationsWithPartsOfSpeech();
		AnalysisEngineDescription aeDesc = BioLemmatizer_AE.createAnalysisEngineDescription(getTsd(), TOKEN_CLASS,
				TOKEN_GET_POS_METHOD_NAME);
		AnalysisEngine ae = AnalysisEngineFactory.createPrimitive(aeDesc);
		ae.process(getJCas());

		assertLemmaAnnotationCount(8);
	}

}
