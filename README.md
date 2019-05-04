****************
* Project 1 
* Class cs361
* Date 2/27/19
* Kelly Wilmot && Bridgette Milgie
**************** 

OVERVIEW:

 This program constructs an NFA for a given regular expression

INCLUDED FILES:

Package fa contains:
 * FAInterface.java - interface
 * State.java - interface
 	
 	Package dfa contains:
 	* DFADriver.java - source file
 	* DFAInterface.java - interface
 	* DFAState.java - source file
 	* DFA.java - source file
  
  Package re contains:
  * REDriver.java
  * RE.java
  * REInterface.java

 * README - this file


COMPILING AND RUNNING:

 Compile from the top directory of the files:
 javac -cp ".:./CS361FA.jar" re/REDriver.java
 
 Then to run the fa.dfa.DFADriver: 
 java -cp ".:./CS361FA.jar" re.REDriver ./tests/p3tc1.txt 

PROGRAM DESIGN AND IMPORTANT CONCEPTS:

 This project allowed us to practice implementing interfaces, work with packages
 extending abstract classes and java Collections. 

 The RE implements the REInterface along with all its methods that were inherited.   

TESTING:

 We tested the provided test cases for the project


