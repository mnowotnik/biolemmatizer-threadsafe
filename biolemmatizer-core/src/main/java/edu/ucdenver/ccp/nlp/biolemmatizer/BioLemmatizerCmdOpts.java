package edu.ucdenver.ccp.nlp.biolemmatizer;

import java.io.File;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * Command-line options for the BioLemmatizer
 * 
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class BioLemmatizerCmdOpts {

	@Option(name = "-f", usage = "optional path to a lexicon file. If not set, the default lexicon available on the classpath is used", required = false)
	private String lexiconFilePath;

	@Option(name = "-i", usage = "the path to the input file", required = false)
	private String inputFilePath;

	@Option(name = "-o", usage = "the path to the output file", required = false)
	private String outputFilePath;
	
	@Option(name = "-a", usage = "if present, normalize common British spellings into American spellings and retrieve lemmas", required = false)
	private boolean americanize = false;

	@Option(name = "-l", usage = "if present, only the lemma is returned (part-of-speech information is suppressed)", required = false)
	private boolean outputLemmaOnly = false;

	@Option(name = "-t", usage = "if present, the interactive mode is used")
	private boolean useInteractiveMode = false;

	@Argument(index = 0, usage = "Single input to be lemmatized", required = false)
	private String inputStr;

	@Argument(index = 1, usage = "Part of speech of the single input to be lemmatized", required = false)
	private String inputStrPos;

	/**
	 * @return the inputFile
	 */
	public File getInputFile() {
		return (inputFilePath != null) ? new File(inputFilePath) : null;
	}

	/**
	 * @return the outputFile
	 */
	public File getOutputFile() {
		return (outputFilePath != null) ? new File(outputFilePath) : null;
	}

	/**
	 * @return the lexiconFile
	 */
	public File getLexiconFile() {
		return (lexiconFilePath != null) ? new File(lexiconFilePath) : null;
	}
	
	/**
	 * @return americanize
	 */
	public boolean americanizedLemma() {
		return americanize;
	}

	/**
	 * @return outputLemmaOnly
	 */
	public boolean outputLemmaOnly() {
		return outputLemmaOnly;
	}

	/**
	 * @return useInteractiveMode
	 */
	public boolean useInteractiveMode() {
		return useInteractiveMode;
	}

	/**
	 * @return the inputStr
	 */
	public String getInputStr() {
		return inputStr;
	}

	/**
	 * @return the inputStrPos
	 */
	public String getInputStrPos() {
		return inputStrPos;
	}

}
