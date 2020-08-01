#!/bin/bash

echo "Compiling JSChat..."
javac *.java -d .
jar -cmf manifest.txt jschat.jar com *.class
echo "Cleaning up..."
rm com -rf
rm Launcher*.class
echo "JSChat compiled successfully!"
echo "To launch, execute: java -jar jschat.jar"
