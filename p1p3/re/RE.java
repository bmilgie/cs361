package re;

import java.util.LinkedHashSet;
import java.util.Set;

import fa.State;
import fa.nfa.NFA;
import fa.nfa.NFAState;

/**
 * Constructs an NFA for a given regular expression
 * @author kellywilmot bridgettemilgie
 *
 */
public class RE implements REInterface {
	String regEx = "";
	int stateCounter = 1;
	
	/**
	 * Constructor
	 * @param regEx String of the regular expression
	 */
	public RE(String regEx) {
		this.regEx = regEx;
	}

	/**
	 * @return The NFA built from the regular expression
	 */
	public NFA getNFA() {
		return regEx();
	}

	/**
	 * Builds an NFA from the regular expression
	 * @return NFA 
	 */
	private NFA regEx() {
		NFA term = term();

		//If the regex requires a union operation
		if (more() && peek() == '|') {

			eat ('|') ;
			NFA regex = regEx() ;
			//now create a union of term and regex NFA
			//1. create a new NFA
			NFA union = new NFA();
			//create a start state for it
			String startName = "q" + stateCounter;
			stateCounter++;
			union.addStartState(startName);

			//add all states from other NFAS
			union.addNFAStates(term.getStates());
			union.addNFAStates(regex.getStates());

			//add the transitions between startName and startStates of other two NFAs
			union.addTransition(startName, 'e', term.getStartState().getName());
			union.addTransition(startName, 'e', regex.getStartState().getName());

			//add the alphabets of the other two NFAs to new NFA
			union.addAbc(term.getABC());
			union.addAbc(regex.getABC());

			return union;

		//If no union is needed, just return the simple NFA
		} else {
			return term;
		}
	}

	/**
	 * Looks at the regex to determine what it needs to build
	 * @return parsed portion of the regex in the form of an NFA
	 */
	private NFA term() {
		NFA factor = new NFA();
		
		while( more() && peek() != ')' && peek() != '|'){
			NFA fact = factor();
			
			//If RE tries to process regex and there no operations to perform, just return simplest NFA
			if(factor.getStates().isEmpty()){
				factor = fact;
			//If there are multiple terms following each other, perform concatenate operation on the NFAs
			}else{
				factor = concat(factor, fact);

			}
		}
		
		return factor;
	}
	
	/**
	 * Concatenates the two NFAs, when two NFAs are in the regex together
	 * @param reg1 - the NFA we are adding reg2 onto
	 * @param reg2 - additional NFA that follows reg1
	 * @return NFA that has been concatenated
	 */
	private NFA concat(NFA reg1, NFA reg2) {
		//Get final states and name of start state in second NFA
		String reg2Start = reg2.getStartState().getName();
		Set<State> reg1Finals = reg1.getFinalStates();

		//Add all states from second NFA to first NFA
		reg1.addNFAStates(reg2.getStates());
		
		//Make sure first NFA's final states are not final
		//But add their transitions to begining of second NFA
		for(State state: reg1Finals) {
			((NFAState)state).setNonFinal();
			reg1.addTransition(state.getName(), 'e', reg2Start);
		}
		
		//Make sure both alphabets are included
		reg1.addAbc(reg2.getABC());
		
		return reg1;
	}

	/**
	 * A factor is a base followed by a possibly empty sequence of '*'.
	 * @return root NFA or root with the star operator if the regex has a '*'
	 */
	private NFA factor() {
		NFA root = root();
		
		//If star op is needed, descend into recursion
		while(more() && peek() == '*'){
			eat('*');
			root = star(root);
		}
		return root;
	}

	/**
	 * Handles the star operator
	 * @param root - NFA we are building upon
	 * @return NFA - the previous NFA has now incorporated the star operator
	 */
	private NFA star(NFA root) {

		NFA retNFA = new NFA();
		
		//Make new start state
		String start = "q" + stateCounter;
		stateCounter++;
		retNFA.addStartState(start);
		
		//Make new final state
		String finState = "q" + stateCounter;
		stateCounter++;
		retNFA.addFinalState(finState);
		
		//Add all states from root to new NFA
		retNFA.addNFAStates(root.getStates());
		
		//Add empty transitions because star allows for 0 occurrences of term/NFA
		retNFA.addTransition(start, 'e', finState);
		retNFA.addTransition(finState, 'e', root.getStartState().getName());
		
		//Tie new start to root NFA
		retNFA.addTransition(start, 'e', root.getStartState().getName());
		
		//Make sure old alphabet is included
		retNFA.addAbc(root.getABC());
		
		//Add empty transitions from old final states to new final state
		for(State state: root.getFinalStates()) {
			retNFA.addTransition(state.getName(), 'e', finState);
			
			//Make sure new final is the only final state in new NFA
			for(State s2: retNFA.getFinalStates()){
				if(s2.getName().equals(state.getName())) {
					((NFAState)s2).setNonFinal();
				}
			}
		}

    	return retNFA;
	}

	/**
	 * Root is a character, an escaped character, or a parenthesized regular expression.
	 * @return an NFA built from the next symbol or within the parenthesis
	 */
	private NFA root() {
		//Check if next symbol requires changing precedent using '()'
		switch (peek()){
		case '(':
			eat('(');
			NFA reg = regEx();
			eat(')');
			return reg;
		default:
			return symbol(next());
		}
	}

	/**
	 * Builds an NFA from the given character
	 * @param c Character to define transition on
	 * @return NFA from given character
	 */
	private NFA symbol(char c) {
		NFA nfa = new NFA();
		
		//Make a new simple NFA with 2 states and a transition on char c
		String s = "q" + stateCounter++;
		nfa.addStartState(s);
		String f = "q" + stateCounter++;
		nfa.addFinalState(f);
		
		nfa.addTransition(s, c, f);
		
		Set<Character> alphabet = new LinkedHashSet<Character>();
		alphabet.add(c);
		nfa.addAbc(alphabet);
		return nfa;
		
	}

	/**
	 * Peeks at the first index of the regex 
	 * @return The next unprocessed character in the regex
	 */
	private char peek(){
		return regEx.charAt(0);
	}
	
	/**
	 * Processes the character and removes from the regex
	 * @param c Character to process
	 */
	private void eat(char c){
		if(peek() == c){
			this.regEx = this.regEx.substring(1);
		}else{
			throw new RuntimeException("Received: " + peek() + "\n" + "Expected: " + c);
		}
	}
	
	/**
	 * Moves the processed character, removing the character from the regex and returning it
	 * @return the character that was processed
	 */
	private char next(){
		char c = peek();
		eat(c);
		return c;
	}
	
	/**
	 * Evaluates if there are more characters to assess in the regex
	 * @return boolean
	 */
	private boolean more(){
		return regEx.length() > 0;
	}
	

}
