package re;

import java.util.LinkedHashSet;
import java.util.Set;

import fa.State;
import fa.nfa.NFA;
import fa.nfa.NFAState;

/**
 * 
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
	    NFA term = term() ;

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
	    	for(State state: (term.getStates())) {
	    		union.addState(state.getName());
	    	}
	    	
	    	for(State state: regex.getStates()) {
	    		union.addState(state.getName());
	    	}
	    	
	    	//add the transitions between starName and starStates of other two NFAs
	    	union.addTransition(startName, 'e', term.getStartState().getName());
	    	union.addTransition(startName, 'e', regex.getStartState().getName());
	    	union.addAbc(term.getABC());
	    	union.addAbc(regex.getABC());
	    	
	    	for(char c: term.getABC()) {
	    		for(State state: term.getStates())
	    			for(State s2: ((NFAState) state).toStates(c)) {
	    	    		union.addTransition(state.getName(), c, s2.getName());
	    			}
	    	}
	    	
	    	for(char c: regex.getABC()) {
	    		for(State state: regex.getStates())
	    			for(State s2: ((NFAState) state).toStates(c)) {
	    	    		union.addTransition(state.getName(), c, s2.getName());
	    			}
	    	}
	    	
	    	for(State state: term.getFinalStates()) {
	    		for(State s2: union.getStates()) {
	    			if(s2.getName().equals(state.getName())) {
	    				((NFAState) s2).setFinal();
	    			}
	    		}
	    	}
	    	
	    	for(State state: regex.getFinalStates()) {
	    		for(State s2: union.getStates()) {
	    			if(s2.getName().equals(state.getName())) {
	    				((NFAState) s2).setFinal();
	    			}
	    		}
	    	}
	    	
	    	return union;
	    	} else {
		    	return term;
	    	}
	}

	
	/**
	 * 
	 * @return
	 */
	private NFA term() {
		NFA factor = new NFA();
		while( more() && peek() != ')' && peek() != '|'){
			NFA fact = factor();
			if(factor.getStates().isEmpty()){
				factor = fact;
			}else{
				factor = concat(factor, fact);

			}
		}
		
		return factor;
	}
	
	/**
	 * Concatenates the two NFAs
	 * @param reg1
	 * @param reg2
	 * @return NFA 
	 */
	private NFA concat(NFA reg1, NFA reg2) {
		String reg2Start = reg2.getStartState().getName();
		Set<State> reg1Finals = reg1.getFinalStates();

		
		reg1.addNFAStates(reg2.getStates());
		
		for(State state: reg1Finals) {
			((NFAState)state).setNonFinal();
			reg1.addTransition(state.getName(), 'e', reg2Start);
		}
		
		reg1.addAbc(reg2.getABC());
		
		return reg1;
	}

	/**
	 * 
	 * @return
	 */
	private NFA factor() {
		NFA root = root();
		
		while(more() && peek() == '*'){
			eat('*');
			root = star(root);
		}
		return root;
	}

	/**
	 * 
	 * @param root
	 * @return
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
		
		
		//Add all current states to new NFA
		for(State state: root.getStates()) {
			retNFA.addState(state.getName());
		}
		
		//
		for(State state: root.getFinalStates()) {
			NFAState tmp = (NFAState)state;
			tmp.setFinal();
			retNFA.addTransition(tmp.getName(), 'e', finState);
			retNFA.addTransition(tmp.getName(), 'e', root.getStartState().getName());
		}
		
		retNFA.addTransition(start, 'e', root.getStartState().getName());
		retNFA.addTransition(start, 'e', finState);
		retNFA.addTransition(finState, 'e', root.getStartState().getName());
		retNFA.addAbc(root.getABC());
		
    	for(char c: root.getABC()) {
    		for(State state: root.getStates())
    			for(State s2: ((NFAState) state).toStates(c)) {
    	    		retNFA.addTransition(state.getName(), c, s2.getName());
    			}
    	}
    	
    	for(State state: root.eClosure((NFAState) root.getStartState())) {
    		retNFA.addTransition(root.getStartState().getName(), 'e', state.getName());
    	}
    	
    	return retNFA;
	}

	/**
	 * 
	 * @return
	 */
	private NFA root() {
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
	 * 
	 * @param c
	 * @return
	 */
	private NFA symbol(char c) {
		NFA nfa = new NFA();
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
	 * 
	 * @return
	 */
	private char peek(){
		return regEx.charAt(0);
	}
	
	/**
	 * 
	 * @param c
	 */
	private void eat(char c){
		if(peek() == c){
			this.regEx = this.regEx.substring(1);
		}else{
			throw new RuntimeException("Received: " + peek() + "\n" + "Expected: " + c);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private char next(){
		char c = peek();
		eat(c);
		return c;
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean more(){
		return regEx.length() > 0;
	}
	

}
