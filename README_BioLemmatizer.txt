    Colorado Computational Pharmacology, University of Colorado School of Medicine  October 22, 2013



The BioLemmatizer is a lemmatization tool for the morphological analysis of 
biomedical literature. It is tailored to the biological domain through 
integration of several published lexical resources related to molecular 
biology. It focuses on the inflectional morphology of English, including the 
plural form of nouns, the conjugations of verbs, and the comparative and 
superlative form of adjectives and adverbs. The BioLemmatizer retrieves lemmas 
based on the use of a lexicon that covers an exhaustive list of inflected word 
forms and their corresponding lemmas in both general English and the biomedical 
domain, as well as a set of rules that generalize morphological transformations 
to heuristically handle words that are not encountered in the lexicon.

This directory contains the software developed by Haibin Liu 
<Haibin.Liu@ucdenver.edu>, William A Baumgartner Jr <William.Baumgartner@ucdenver.edu> 
and Karin Verspoor <Karin.Verspoor@ucdenver.edu>. The BioLemmatizer is developed 
in Java and is released as open source software to the NLP and text mining 
research communities to be used for research purposes only (see section 8 below 
for copyright information). It can be downloaded via http://biolemmatizer.sourceforge.net. 
If you make any changes, the authors would appreciate it if you can send the details 
of what you have done. A Perl module of the BioLemmatizer Lingua::En::BioLemmatizer 
is developed by Tom Christiansen <tchrist@perl.com> and released on CPAN at 
http://search.cpan.org/perldoc?Lingua::EN::BioLemmatizer

Note: The BioLemmatizer code requires Java version 6 or greater.

1. Files and Folders
---------------------

  README.txt                          this file
  
  biolemmatizer-1.2.tar.gz	      the source code, resources, and license for the BioLemmatizer
  
  biolemmatizer-core-1.2-jar-with-dependencies.jar
                                      Jar file for the biolemmatizer-core module, including all
                                      required dependencies
  
  lexicon.lex.gz                      contains the full lexicon used by the BioLemmatizer
  
  biolemmatizer-eval-datasets.tar.gz  contains all the experimental datasets (CRAFT, OED, LLL), 
                                      and the gold and silver annotations used for testing the
                                      BioLemmatizer (see section 8 for detailed description)                                                                               
     

2. Usage
--------

Set the MAVEN_OPTS environment variable to provide the JVM enough memory to load 
the lexicon file (this command only needs to be executed once):
  export MAVEN_OPTS="-Xmx1G"
  
Lemmatize one single input string:
  mvn -f biolemmatizer-core/pom.xml exec:java -Dexec.mainClass="edu.ucdenver.ccp.nlp.biolemmatizer.BioLemmatizer" -Dexec.args="<input string> [POS tag]"

Lemmatize input strings in a file, output lemmas to a different file:
  mvn -f biolemmatizer-core/pom.xml exec:java -Dexec.mainClass="edu.ucdenver.ccp.nlp.biolemmatizer.BioLemmatizer" -Dexec.args="-i <input file name> -o <output file name>"

Run the BioLemmatizer in interactive mode, i.e. lemmatize input strings from standard input (exit when an empty line is used as input):
  mvn -f biolemmatizer-core/pom.xml exec:java -Dexec.mainClass="edu.ucdenver.ccp.nlp.biolemmatizer.BioLemmatizer" -Dexec.args="-t"

Input parameter descriptions:
  -f VAL  :    optional path to a lexicon file. If not set, the default lexicon 
	           available on the classpath is used
	 
  -l      :    By default, the BioLemmatizer output contains the resulting 
               lemma, the POS tag of the input string and the tagset name of the POS tag. 
               The option -l returns only the lemma and ignores other information.
               
  -a      :    to invoke the Americanization process that normalizes common British English 
               spellings into American English spellings, and retrieves corresponding lemmas.
               This is achieved based on a mapping list and some deterministic rules.
               For instance: the lemma of "haemangioblastoma" will be "hemangioblastoma".        
 
  POS tag :    The POS tag associated with the input string. 
               It is optional and is expected to follow the Penn Treebank tagset. 

  -i VAL  :    to specify the input file name 
  -o VAL  :    to specify the output file name 
  -t      :    to invoke the interactive mode. With this mode, the BioLemmatizer can be easily
               integrated into applications written in other languages, such as Perl. To exit
               the interactive mode enter a blank line.                    

See the following sections for specifications of input and output formats, and examples of usage.



3. BioLemmatizer Input Specification
-------------------------------------
The BioLemmatizer can be run to lemmatize a single input string or a batch 
of strings submitted in an input file.

Character encoding for all input is assumed to be UTF-8.

(a) Each input token is expected to be of the form <input string> [POS tag]. For examples:

          roles NNS or quantitated VBD

The POS tag associated with the input string is expected to follow the widely 
used Penn Treebank tagset. The POS information is optional. When it is not 
given in the input, the BioLemmatizer returns lemmas for all possible parts 
of speech, in terms of both POS tagsets (NUPOS and Penn Treebank tagsets) 
represented in the lexicon. Our assumption is that without knowing the word 
context, the lemmatizer should return all possible lemmas and allow the user 
or calling application to resolve the ambiguities. 

(b) Each input file is expected to be in the lemmatization format with or without blank lines.

The lemmatization format requires 2 fields:

* FORM: input string 
* POSTAG: POS tag 

Each field is delimited by a tab character ('\t'). Each sentence is delimited 
by a blank line. The POS tag is expected to follow the Penn Treebank tagset. 
Likewise, the POS information is optional. For example:

Bmp7	NN
knockout	NN
mice	NNS
do	VBP
not	RB
show	VB
any	DT
defect	NN
in	IN
limb	NN
polarity	NN
.	.

Bmp2	NN
mutant	NN
embryos	NNS
die	VBP
too	RB
early	RB
to	TO
assess	VB
their	PRP$
limb	NN
phenotypes	NNS
.	.



4. BioLemmatizer Output Specification
--------------------------------------
By default, the BioLemmatizer output consists of the resulting lemma, the POS 
tag of the input string and the tagset name of the POS tag. For example, for 
the input "quantitated VBD", the BioLemmatizer produces "quantitate VBD 
PennPOS". If the POS information is not provided in the input, the 
BioLemmatizer returns lemmas for all possible parts of speech across all POS 
tagsets, separated by a separator "||". For example, for the input 
"diminished", the output is "diminish VBD PennPOS||diminished JJ PennPOS".

BioLemmatizer output is encoded using UTF-8.

The option -l is provided to have the BioLemmatizer return only the lemma in 
the output. With the option -l, the output for the above examples would be 
"quantitate" and "diminish||diminished".

If the input is a file, the resulting lemma is inserted as a new field in the 
output file, delimited by a tab character ('\t'). For example:

Bmp7	NN	Bmp7
knockout	NN	knockout
mice	NNS	mouse
do	VBP	do
not	RB	not
show	VB	show
any	DT	any
defect	NN	defect
in	IN	in
limb	NN	limb
polarity	NN	polarity
.	.	.

Bmp2	NN	Bmp2
mutant	NN	mutant
embryos	NNS	embryo
die	VBP	die
too	RB	too
early	RB	early
to	TO	to
assess	VB	assess
their	PRP$	their
limb	NN	limb
phenotypes	NNS	phenotype
.	.	.



5. Usage Examples (shown using executable jar available in biolemmatizer-core/target/ directory after the project is built)
---------------------------------------------------------------------------------------------------------------------------

(a) java -Xmx1G -jar biolemmatizer-core-1.1-jar-with-dependencies.jar catalyses NNS

    =>   catalysis NNS PennPOS

(b) java -Xmx1G -jar biolemmatizer-core-1.1-jar-with-dependencies.jar -l catalyses NNS 

    =>   catalysis

(c) java -Xmx1G -jar biolemmatizer-core-1.1-jar-with-dependencies.jar running

    =>   run vvg NUPOS||running JJ PennPOS||run j-vvg NUPOS||run n-vvg NUPOS||running NN PennPOS

(d) java -Xmx1G -jar biolemmatizer-core-1.1-jar-with-dependencies.jar -l running

    =>   run||running

(e) java -Xmx1G -jar biolemmatizer-core-1.1-jar-with-dependencies.jar -t
    running
    =>   run vvg NUPOS||running JJ PennPOS||run VBG PennPOS||run j-vvg NUPOS||run n-vvg NUPOS||running NN PennPOS
    catalyses NNS
    =>   catalysis NNS PennPOS

(f) java -Xmx1G -jar biolemmatizer-core-1.1-jar-with-dependencies.jar -l -t
    running
    =>   run||running
    catalyses NNS
    =>   catalysis

(g) java -Xmx1G -jar biolemmatizer-core-1.1-jar-with-dependencies.jar -i inputfile -o outputfile

(h) java -Xmx1G -jar biolemmatizer-core-1.1-jar-with-dependencies.jar -l -i inputfile -o outputfile


See the above sections "BioLemmatizer Input Specification" and "BioLemmatizer 
Output Specification" for the guideline of the format of input and output files.



6. Lexical data from the BioLexicon
----------------------------------------------------
The BioLemmatizer integrates lexical resources from three sources: MorphAdorner, 
the GENIA tagger and the BioLexicon database. Since the BioLexicon morphological 
data used in the BioLemmatizer is included in the publicly available part of the
data in the BioLexicon (EBI term repository), we are able to redistribute it in 
the public release of the full version of the BioLemmatizer. For the original 
morphological data in the BioLexicon database, please refer to the following 
BioLexicon publication and the download link of the freely available data in the 
BioLexicon.

Thompson P, McNaught J, Montemagni S, Calzolari N, del Gratta R, Lee V, Marchi S, 
Monachini M, Pezik P, Quochi V, Rupp C, Sasaki Y, Venturi G, Rebholz-Schuhmann D, 
Ananiadou S: The BioLexicon: a large-scale terminological resource for biomedical 
text mining. BMC Bioinformatics 2011, 12:397.

Download link of the EBI term repository of the BioLexicon:
http://www.ebi.ac.uk/Rebholz-srv/BioLexicon/biolexicon.html

ELRA link of the full version of the BioLexicon
http://catalog.elra.info/product_info.php?products_id=1113



7. Performance comparison with/without BioLexicon data
-------------------------------------------------------
Please refer to the following publication for more 
detailed performance comparison.

Haibin Liu, Tom Christiansen, William A Baumgartner Jr, and Karin Verspoor
BioLemmatizer: a lemmatization tool for morphological processing of biomedical text
Journal of Biomedical Semantics 2012, 3:3.

After the experiments reported in the publication, we collected all false positive 
lemmas we encountered, and we have fixed nearly all of them, either by adding an 
entry to the BioLemmatizer lexicon or by modifying the rules of detachment, in some 
cases adding the lexicon validation constraint.

Here we provide the lemmatization results on three of our evaluation datasets to 
highlight the performance difference for the BioLemmatizer with and without 
the BioLexicon data, and the tool achieving the second best performance among 
9 lemmatizers we tested. 

Evaluation on silver consensus set of CRAFT
                                  Recall                    Precision                F-score
ExcludeBioLexicon                 99.56% (5836/5862)	    99.56% (5836/5862)	     99.56%
IncludeBioLexicon                 100% (5862/5862)          100% (5862/5862)         100%
Second best (morpha tool)         100% (5862/5862)          100% (5862/5862)         100%

Evaluation on gold difference set of CRAFT
                                  Recall                    Precision                F-score
ExcludeBioLexicon                 94.30% (546/579)	    94.30% (546/579)	     94.30%
IncludeBioLexicon                 99.65% (577/579)          99.65% (577/579)         99.65%
Second best (MorphaAdorner)       81.87% (474/579)	    82.29% (474/576)	     82.08%

Evaluation on gold OED set
                                  Recall                    Precision                F-score
ExcludeBioLexicon                 82.55% (667/808)	    82.55% (667/808)	     82.55%  
IncludeBioLexicon                 84.65% (684/808)          84.65% (684/808)         84.65%
Second best (morpha tool)         75.74% (612/808)          75.74% (612/808)         75.74%

Currently, for the performance on biomedical text (the CRAFT set), the 
overall lemmatization accuracy of the public release of BioLemmatizer is 99.9%
(the full version of BioLemmatizer, including the BioLexicon data). The version 
of the BioLexicon database used in our experiments is: Version of May 22nd, 2009.



8. Description of contents of biolemmatizer-eval-datasets.tar.gz
----------------------------------------------------------------

  CRAFT_development_data              subset of the CRAFT corpus, containing 7 full-text articles 
  CRAFT_consensus_silver              consensus set of CRAFT_development_data (excluding adverbs), 
                                      representing agreement among 6 lemmatizers, to form a 
                                      "silver lemma standard"
  CRAFT_difference_gold               gold lemma annotation of the set of disagreements among 9 lemmatizers                                   
  OED_gold                            gold lemma annotation of the OED (Oxford English Dictionary) set
  LLL_gold                            gold lemma annotation of the LLL05 set, curated with automatically 
                                      generated POS information
  LLL_gold_updated                    LLL_gold with fixed annotation on incorrect or inconsistent 
                                      instances and task-specific normalizations 



9. Copyright and License
------------------------------------
The software is released under the New BSD license 
(http://www.opensource.org/licenses/bsd-license.php).

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

Any documentation, advertising materials, publications and other materials 
related to redistribution and use must acknowledge that the software was 
developed by Haibin Liu <Haibin.Liu@ucdenver.edu>, William A Baumgartner Jr 
<William.Baumgartner@ucdenver.edu> and Karin Verspoor <Karin.Verspoor@ucdenver.edu> 
and must refer to the following publication:

Haibin Liu, Tom Christiansen, William A Baumgartner Jr, and Karin Verspoor
BioLemmatizer: a lemmatization tool for morphological processing of biomedical text
Journal of Biomedical Semantics 2012, 3:3.



10. Incorporated software and resources
---------------------------------------
This software incorporates the MorphAdorner software (http://morphadorner.northwestern.edu/), 
lexical resources from the BioLexicon database (http://www.ebi.ac.uk/Rebholz-srv/BioLexicon/biolexicon.html)
and the GENIA Tagger (http://www-tsujii.is.s.u-tokyo.ac.jp/GENIA/tagger/). 
We redistribute these software and resources here.

MorphAdorner license:

The MorphAdorner source code and data files fall under the following NCSA style license. 
Some of the incorporated code and data fall under different licenses as noted in the 
section third-party licenses below.Copyright (c) 2006-2009 by Northwestern University. 
All rights reserved.Developed by:Academic and Research TechnologiesNorthwestern Universityhttp://www.it.northwestern.edu/about/departments/at/

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal with the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
following conditions:
1. Redistributions of source code must retain the above copyright notice, this list of conditions and
the following disclaimers.
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
and the following disclaimers in the documentation and/or other materials provided with the
distribution.
3. Neither the names of Academic and Research Technologies, Northwestern University, nor the
names of its contributors may be used to endorse or promote products derived from this
Software without specific prior written permission.

BioLexicon database citation:

Thompson P, McNaught J, Montemagni S, Calzolari N, del Gratta R, Lee V, Marchi S, 
Monachini M, Pezik P, Quochi V, Rupp C, Sasaki Y, Venturi G, Rebholz-Schuhmann D, 
Ananiadou S: The BioLexicon: a large-scale terminological resource for biomedical 
text mining. BMC Bioinformatics 2011, 12:397.

ELRA link of the full version of the BioLexicon
http://catalog.elra.info/product_info.php?products_id=1113

Download link of the EBI term repository of the BioLexicon:
http://www.ebi.ac.uk/Rebholz-srv/BioLexicon/biolexicon.html

GENIA Tagger License

Copyright (c) 2005, Tsujii Laboratory, The University of Tokyo
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted for non-commercial purposes provided
that the following conditions are met:

- Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.

- Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Since The GENIA Tagger uses a dictionary in WordNet for morphological analysis,
the corresponding WordNet license is also included here. 

WordNet Release 2.1

This software and database is being provided to you, the LICENSEE, by
Princeton University under the following license.  By obtaining, using
and/or copying this software and database, you agree that you have
read, understood, and will comply with these terms and conditions.:

Permission to use, copy, modify and distribute this software and
database and its documentation for any purpose and without fee or
royalty is hereby granted, provided that you agree to comply with
the following copyright notice and statements, including the disclaimer,
and that the same appear on ALL copies of the software, database and
documentation, including modifications that you make for internal
use or for distribution.

WordNet 2.1 Copyright 2005 by Princeton University.  All rights reserved.

THIS SOFTWARE AND DATABASE IS PROVIDED "AS IS" AND PRINCETON
UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR
IMPLIED.  BY WAY OF EXAMPLE, BUT NOT LIMITATION, PRINCETON
UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES OF MERCHANT-
ABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT THE USE
OF THE LICENSED SOFTWARE, DATABASE OR DOCUMENTATION WILL NOT
INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS, TRADEMARKS OR
OTHER RIGHTS.

The name of Princeton University or Princeton may not be used in
advertising or publicity pertaining to distribution of the software
and/or database.  Title to copyright in this software, database and
any associated documentation shall at all times remain with
Princeton University and LICENSEE agrees to preserve same.



11. Acknowledgements
------------------------------------
Many thanks to Professor Lawrence Hunter, Helen Johnson, Kevin B. Cohen, 
and other members of the Colorado Computational Pharmacology group for 
providing valuable effort and suggestions related to this work.
