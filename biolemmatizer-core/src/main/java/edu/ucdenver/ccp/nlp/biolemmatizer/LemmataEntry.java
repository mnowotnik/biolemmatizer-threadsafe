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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/** LemmataEntry: store POS tags and corresponding lemmas for one lemmata entry */
public class LemmataEntry {
	/**
	 * Map to store lemmata info. A POS tag is the key, and the corresponding lemma is the value.
	 */
	public Map<String, String> lemmasAndCategories;

	/** Lemma separator character */
	public static String lemmaSeparator = "||";

	/**
	 * Provides mappings from POS tags to a corresponding tag set name
	 */
	private final POSEntry posEntry;

	/**
	 * Construtor to initialize the class field
	 * 
	 * @param lemmasAndCategories
	 *            a Map object that stores lemmata info
	 * @param posEntry
	 *            provides mappings from POS tags to a corresponding tag set name
	 * 
	 */
	public LemmataEntry(Map<String, String> lemmasAndCategories, POSEntry posEntry) {
		this.lemmasAndCategories = lemmasAndCategories;
		this.posEntry = posEntry;
	}

	/**
	 * Override toString() method to represent lemma and POS info in a concatenated triplet;
	 * Different lemmas are separated by lemmaSeparator
	 */
	@Override
	public String toString() {
		String lemmas = "*";

		if (!lemmasAndCategories.isEmpty()) {
			lemmas = "";
			String lemma;
			int i = 0;
			for (String key : lemmasAndCategories.keySet()) {
				lemma = lemmasAndCategories.get(key) + " " + key + " " + posEntry.getTagSetLabel(key);

				lemmas += lemma;

				if (i < lemmasAndCategories.keySet().size() - 1) {
					lemmas = lemmas + lemmaSeparator;
				}
				i++;
			}
		}

		return lemmas;
	}

	/**
	 * Represent lemmas of different POS tags; separated by lemmaSeparator
	 * 
	 * @return string representation of lemma info
	 */
	public String lemmasToString() {
		String lemmas = "*";

		if (!lemmasAndCategories.isEmpty()) {
			lemmas = "";
			String lemma;
			int i = 0;

			// remove duplicate lemmas
			Set<String> checkSet = new HashSet<String>();
			for (String key : lemmasAndCategories.keySet()) {
				lemma = lemmasAndCategories.get(key);
				if (!checkSet.contains(lemma))
					checkSet.add(lemma);
			}

			for (String setItem : checkSet) {
				lemmas += setItem;
				if (i < checkSet.size() - 1)
					lemmas = lemmas + lemmaSeparator;
				i++;
			}
		}

		return lemmas;
	}

	/**
	 * @return a {@link Collection} of unique {@link Lemma} objects
	 */
	public Collection<Lemma> getLemmas() {
		Set<Lemma> lemmas = new HashSet<Lemma>();
		for (Entry<String, String> entry : lemmasAndCategories.entrySet()) {
			String posTag = entry.getKey();
			String lemma = entry.getValue();
			String tagSetName = posEntry.getTagSetLabel(posTag);
			lemmas.add(new Lemma(lemma, posTag, tagSetName));
		}
		return lemmas;
	}

	/**
	 * Simple utility class to store a single lemma/pos combination
	 * 
	 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
	 * 
	 */
	public static class Lemma {
		private final String lemma;
		private final String pos;
		private final String tagSetName;

		/**
		 * @param lemma
		 * @param pos
		 * @param tagSetName
		 */
		public Lemma(String lemma, String pos, String tagSetName) {
			super();
			this.lemma = lemma;
			this.pos = pos;
			this.tagSetName = tagSetName;
		}

		/**
		 * @return the lemma
		 */
		public String getLemma() {
			return lemma;
		}

		/**
		 * @return the pos
		 */
		public String getPos() {
			return pos;
		}

		/**
		 * @return the tagSetName
		 */
		public String getTagSetName() {
			return tagSetName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((lemma == null) ? 0 : lemma.hashCode());
			result = prime * result + ((pos == null) ? 0 : pos.hashCode());
			result = prime * result + ((tagSetName == null) ? 0 : tagSetName.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Lemma other = (Lemma) obj;
			if (lemma == null) {
				if (other.lemma != null)
					return false;
			} else if (!lemma.equals(other.lemma))
				return false;
			if (pos == null) {
				if (other.pos != null)
					return false;
			} else if (!pos.equals(other.pos))
				return false;
			if (tagSetName == null) {
				if (other.tagSetName != null)
					return false;
			} else if (!tagSetName.equals(other.tagSetName))
				return false;
			return true;
		}

	}

}
