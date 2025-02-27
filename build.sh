rm -rf build
mkdir build
javac -d build -Xlint -Xdoclint -cp "lib/*:." CommandShell.java
cd build
jar cvfe CommandShell.jar CommandShell *.class ../lib/commons-text-1.13.0.jar
