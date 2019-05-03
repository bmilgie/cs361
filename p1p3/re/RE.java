package re;

import java.util.LinkedHashSet;
import java.util.Set;

import fa.nfa.NFA;

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
		NFA term = term();
			
		if(more() && peek() == '|'){
			eat('|');
			NFA regEx = regEx();
			return consume(term, regEx); //choice
		}else{
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
		
		return null;
	}
	
	private NFA concat(NFA reg, NFA reg2) {
		String s2 = reg2.getStartState().getName();
		//Set<State> regFinal = reg.getFinalStates();
		
		
		return null;
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
//star rule
		return null;
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
		String s = "" + stateCounter++;
		nfa.addStartState(s);
		String f = "" + stateCounter++;
		nfa.addFinalState(f);
		
		nfa.addTransition(s, cookie, f);
		
		Set<Character> alphabet = new LinkedHashSet<Character>();
		alphabet.add(cookie);
		//nfa.addAbc(alphabet);
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
	
	private NFA consume(NFA reg, NFA reg2) { 


		return null;
	}

}
