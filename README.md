# BioLemmatizer v1.2 threadsafe edition

This is a fork of the original v1.2
[BioLemmatizer](http://biolemmatizer.sourceforge.net/). BioLemmatizer
could not be run in multithreaded environment because of underlying
MorphAdorner library that was not threadsafe. Namely, it recycled
[Matcher](http://docs.oracle.com/javase/7/docs/api/java/util/regex/Matcher.html)
with [Matcher#reset](http://docs.oracle.com/javase/7/docs/api/java/util/regex/Matcher.html#reset()).

In this edition, [MorphAdorner](http://morphadorner.northwestern.edu/) has been
upgraded to the newest version, 2.0, and merged with BioLemmatizer.


## Changes to BioLemmatizer

- Updated pom.xml with MorphAdorner's dependencies
- Created gradle build scripts
- Added parts of MorphAdorner to the code base

## Changes to MorphAdorner

- Removed packages that are not crucial to eliminate dependency on ivy packages
- Fixed PatternReplacer.java to eliminate race conditions. The class now creates a Matcher object
for every call instead of recycling it


## Installation

- mvn clean install

or

- gradle build (only biolemmatizer-core)
 
## TODO

- biolemmatizer-uima package cannot be built under gradle. This is because it requires a plugin to build
a UIMA typesystem out of XML descriptors. There is a plugin for this in maven, but I was unable to do force
gradle's [plugin](https://plugins.gradle.org/plugin/com.dictanova.jcasgen) to
work. If you can make this work, please make a pull request.
