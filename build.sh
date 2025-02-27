# Delete previous build
rm -rf build
# Unpack the Apache library and put it in the right place
cd lib
jar xf commons-text-1.13.0.jar
mv org ..
# Create build directory
cd ..
mkdir build
# Compile the program
javac -d build -Xlint CommandShell.java
cd build
# Pack the program into a jar, including the necessary Apache classes
jar cvfe CommandShell.jar CommandShell *.class\
 ../org/apache/commons/text/similarity/LevenshteinDistance.class\
 ../org/apache/commons/text/similarity/EditDistance.class\
 ../org/apache/commons/text/similarity/SimilarityScore.class\
 ../org/apache/commons/text/similarity/ObjectSimilarityScore.class\
 ../org/apache/commons/text/similarity/SimilarityInput.class\
 ../org/apache/commons/text/similarity/SimilarityCharacterInput.class
cd ..
# Delete intermediate files
rm -rf org lib/org lib/META-INF build/*.class
