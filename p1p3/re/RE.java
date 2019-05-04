package re;

import java.util.LinkedHashSet;
import java.util.Set;

import fa.State;
import fa.nfa.NFA;
import fa.nfa.NFAState;

public class RE implements REInterface {
	String regEx = "";
	int stateCounter = 1;
	
	
	public RE(String regEx) {
		this.regEx = regEx;
	}

	public NFA getNFA() {
		return regEx();
	}

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
	    	
	    	return union;
	    	} else {
		    	return term;
	    	}
	}

	

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
	
	private NFA concat(NFA reg1, NFA reg2) {
		String s2 = reg2.getStartState().getName();
		Set<State> reg1Finals = reg1.getFinalStates();
		reg1.addNFAStates(reg2.getStates());
		
		for(State state: reg1Finals) {
			NFAState tmp = (NFAState)state;
			tmp.setNonFinal();
			reg1.addTransition(tmp.getName(), 'e', s2);
		}
		
		reg1.addAbc(reg2.getABC());
		
		return reg1;
	}

	private NFA factor() {
		NFA root = root();
		
		while(more() && peek() == '*'){
			eat('*');
			root = star(root);
		}
		return root;
	}

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
		retNFA.addAbc(root.getABC());
		
		
		return retNFA;
	}

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

	private NFA symbol(char cookie) {
		NFA nfa = new NFA();
		String s = "q" + stateCounter++;
		nfa.addStartState(s);
		String f = "q" + stateCounter++;
		nfa.addFinalState(f);
		
		nfa.addTransition(s, cookie, f);
		
		Set<Character> alphabet = new LinkedHashSet<Character>();
		alphabet.add(cookie);
		nfa.addAbc(alphabet);
		return nfa;
		
	}

	private char peek(){
		return regEx.charAt(0);
	}
	
	private void eat(char cookie){
		if(peek() == cookie){
			this.regEx = this.regEx.substring(1);
		}else{
			throw new RuntimeException("Received: " + peek() + "\n" + "Expected: " + cookie);
		}
	}
	
	private char next(){
		char c = peek();
		eat(c);
		return c;
	}
	
	private boolean more(){
		return regEx.length() > 0;
	}
	

}
