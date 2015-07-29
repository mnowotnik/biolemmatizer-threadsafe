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

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP;

/**
 * This class contains a few simple utility methods related to UIMA
 * 
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class UimaUtil {

	/**
	 * Adds a single feature structure to the input {@link FSArray}. IF the input {@link FSArray} is
	 * null, a new one is initialized
	 * 
	 * @param fsArray
	 * @param featureStructureToAdd
	 * @param jcas
	 * @return a copy of the input {@link FSArray} with the input featureStructureToAdd added to it
	 */
	public static FSArray addToFSArray(FSArray fsArray, TOP featureStructureToAdd, JCas jcas) {
		FSArray fsArrayToReturn;
		if (fsArray == null) {
			fsArrayToReturn = new FSArray(jcas, 1);
		} else {
			fsArrayToReturn = new FSArray(jcas, fsArray.size() + 1);
			for (int i = 0; i < fsArray.size(); i++) {
				fsArrayToReturn.set(i, fsArray.get(i));
			}
		}

		fsArrayToReturn.set(fsArrayToReturn.size() - 1, featureStructureToAdd);
		return fsArrayToReturn;
	}

	/**
	 * Adds a single String to the input {@link StringArray}. If the input {@link StringArray} is
	 * null, a new one is initialized
	 * 
	 * @param stringArray
	 * @param stringToAdd
	 * @param jcas
	 * @return a copy of the input {@link StringArray} with the input stringToAdd added to it
	 */
	public static StringArray addToStringArray(StringArray stringArray, String stringToAdd, JCas jcas) {
		StringArray stringArrayToReturn;
		if (stringArray == null) {
			stringArrayToReturn = new StringArray(jcas, 1);
		} else {
			stringArrayToReturn = new StringArray(jcas, stringArray.size() + 1);
			for (int i = 0; i < stringArray.size(); i++) {
				stringArrayToReturn.set(i, stringArray.get(i));
			}
		}

		stringArrayToReturn.set(stringArrayToReturn.size() - 1, stringToAdd);
		return stringArrayToReturn;
	}

}
