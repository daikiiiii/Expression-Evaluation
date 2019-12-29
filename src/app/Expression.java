package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/
    	
    	expr = expr.trim();
        expr = expr.replaceAll("\t", "");
        expr = expr.replaceAll(" ", "");
        
        
        
        StringTokenizer str = new StringTokenizer(expr, " \t+-*/()]" + "1234567890");
        
        String arrName = "";
        String varName = "";
        
        while (str.hasMoreTokens()) {
        	
        	String token = str.nextToken();
        	
        	if (token.contains("[")) {	
        		for (int i = 0; i < token.length(); i++) {
        			int looper = 0;
        			if (Character.isLetter(token.charAt(i))) {
        				for (int j = i; j < token.length(); j++) {
        					if (token.charAt(j) == '[') {
        						arrName = token.substring(i, j);
        						Array array = new Array(arrName);
        						if (arrays.contains(array)) {
        							break;
        						} else {
        							arrays.add(array);
        							break;
        						}
        					}
        					
        					if (j + 1 == token.length()) {
        						varName = token.substring(i, j + 1);
        						Variable variable = new Variable(varName);
        						if (vars.contains(variable)) {
        							break;
        						} else {
        							vars.add(variable);
        							break;
        						}
        					}	
        					looper ++;
        				}
        			}
        			i += looper;
        		}
        	} else {        		
        		// Goes through the whole expression since there are no ArrayList evaluations to be considered
        		// Only tokens are letters, both capitalized and not
        		Variable variable = new Variable(token);
        		if (vars.contains(variable)) {
        			continue;
        		} else {
        			vars.add(variable);
        		}
        	}
        }
        
        
      System.out.println("ArrayList: " + arrays);
      System.out.println("VariableList: " + vars);
        
       
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	// following line just a placeholder for compilation
    	
    	expr = expr.replaceAll(" ", "");
    	expr = expr.replaceAll("\t", "");
    	
    	boolean isNumerical = false;
    	boolean containsBrackets = false;
    	isNumerical = isNumerical(expr);
    	containsBrackets = containsBrackets(expr);
    	if (isNumerical) {
    		expr = parenthesesEval(expr);
    		expr = rearrangeSigns(expr);
    		float result = arithmeticEval(expr);
    		return result;
    	} else {
    		if (containsBrackets) {
    			expr = evaluateVariables(expr, vars, arrays);
    			expr = evaluateBrackets(expr, vars, arrays);
    			expr = parenthesesEval(expr);
    			float result = arithmeticEval(expr);
    			return result;
    		} else {
    			expr = evaluateVariables(expr, vars, arrays);
    			expr = evaluateBrackets(expr, vars, arrays);
    			System.out.println("After variableEval is called: " + expr);
    			expr = parenthesesEval(expr);
    			System.out.println("After parenthesesEval is called: " + expr);
    			expr = rearrangeSigns(expr);
    			float result = arithmeticEval(expr);
    			return result;
    		}
    	}
    }
    
    /*
    * ARITHMETIC EVAL: HELPER METHOD || evaluates expressions with no parenthesis, variables, or arrays,
    * just numerical values and simple operators
    */
    
    private static float arithmeticEval(String expr) {
	    Stack<Character> operatorStk = new Stack<>();
	   	Stack<Float> operandStk = new Stack<>();
	   	boolean isNegative = false;
	   	boolean factorNegative = false;
	   	expr.trim();
	   	int counter = 0;
	   	for (int i = 0; i < expr.length(); i++) {
	    	float operandNum;
	   		if (expr.charAt(i) == ' ') {
	   			continue;
	   		}
	   		if (expr.charAt(i) >= '0' && expr.charAt(i) <= '9') {
	    		if (!(i + 1 >= expr.length()) && (expr.charAt(i+1) >= '0') && (expr.charAt(i+1) <= '9')) {
	    			int j = i;
	   				while (!(j >= expr.length()) && !(expr.charAt(j) < '0') && !(expr.charAt(j) > '9')) {
	   					counter++;
	   					j++;
	    			}
	    			String digitNumber = expr.substring(i, j);
	    			if (isNegative == true) {	
	    				operandNum = -1 * (Float.parseFloat(digitNumber));
	    				isNegative = false;
	    			} else if (factorNegative == true) {
	    				operandNum = -1 * (Float.parseFloat(digitNumber));
	    				factorNegative = false;
	    			} else {
	    				operandNum = Float.parseFloat(digitNumber);
	    			}
	   				i = j-1;
	   			} else {
	   				if (isNegative == true) {
	   					operandNum = -1 * (expr.charAt(i) - '0');
	    				isNegative = false;
	    			} else if (factorNegative == true) { 
	    				operandNum = -1 * (expr.charAt(i) - '0');
	    				factorNegative = false;
	    			}else {
	    				operandNum = expr.charAt(i) - '0';
	   				}
	    		}
	    		operandStk.push(operandNum);
	   			if ((!operatorStk.isEmpty()) && operatorStk.peek() == '*') {
	  				float secondNum = operandStk.pop();
	   				float firstNum = operandStk.pop();
	   				operatorStk.pop();
	  				operandStk.push(firstNum * secondNum);
	   			}
	 			if ((!operatorStk.isEmpty()) && operatorStk.peek() == '/') {
	   				float secondNum = operandStk.pop();
	    			float firstNum = operandStk.pop();
	   				operatorStk.pop();
	  				operandStk.push(firstNum / secondNum);
	   			}
	   		} else if 
	   		(expr.charAt(i) == '*' || expr.charAt(i) == '/' || expr.charAt(i) == '+' ||  expr.charAt(i) == '-') {
	  			if ((counter == 0 && expr.charAt(i) == '-')) {
	   				isNegative = true;
	   				counter++;
	   				continue;
	   			}
	  			if (factorNegative == true && expr.charAt(i) == '-') {
	  				continue;
	  			}
	  			System.out.println("Where you at?");
	  			if ((expr.charAt(i) == '*' || expr.charAt(i) == '/') && expr.charAt(i+1) == '-') {
	  				factorNegative = true;
	  			}
	   			operatorStk.push(expr.charAt(i));
	   		}
	   		counter ++;
	    }
	    	
	   	// once breaks out of the for-loop, it has checked all numbers and operators, so just add / subtract down
	    	
	   	operatorStk = reverseStack(operatorStk);
	   	operandStk = reverseStack(operandStk);
	    	
	   	while (operatorStk.isEmpty() == false) {
	   		if (operatorStk.peek() == '+') {
	   			operatorStk.pop();
	   			float firstNum = operandStk.pop();
	   			float secondNum = operandStk.pop();
    			operandStk.push(firstNum + secondNum);
	    	} else if (operatorStk.peek() == '-') {
	    		operatorStk.pop();
	   			float firstNum = operandStk.pop();
	   			float secondNum = operandStk.pop();
	   			operandStk.push(firstNum - secondNum);
	   		}
	   	}
	    	
	   	// once breaks out of the while-loop, knows that operatorStk is empty, so just return final value
	    	
	   	float result = operandStk.pop();
	   	System.out.println("arithmetic is: " + result);
	   	return result;
	}
    
    /*
    * PARENTHESES EVAL: HELPER METHOD : Evaluates expressions with parentheses and numerical values, no arrays
    * or no variables. Returns a string that has no parentheses, and will lead to an easy arithmetic eval.
    */
    
    private static String parenthesesEval(String expr) {
		Stack<Character> parenthesisStk = new Stack<Character>();
		String str = "";
		String substring = "";
		String substringToReplace = "";
		int headParenthesis = 0; //first occurrence
		int tailParenthesis = 0; //last occurrence, update until loop breaks
		int counter = 0; //counter used to determine first occurrence of parenthesis
		for (int i = 0; i < expr.length();i++) {
			if (expr.charAt(i) == '(') {
				parenthesisStk.push(expr.charAt(i));
				headParenthesis = i;
			} else if 
			((expr.charAt(i) != '(' && expr.charAt(i) != ')') || (expr.charAt(i) == '[' || expr.charAt(i) == ']') ) {
				System.out.println(expr.charAt(i));
				if (parenthesisStk.isEmpty()) {
					str += expr.charAt(i);
				} else {
					continue;
				}
			} else if (expr.charAt(i) == ')') {
				if (counter == 0) {
					tailParenthesis = i;
					substring = expr.substring(headParenthesis + 1, tailParenthesis);
					substringToReplace = expr.substring(headParenthesis, tailParenthesis + 1);
					counter++;
				} else {
					continue;
				}
			}
		}
		
		if (!parenthesisStk.isEmpty()) {
			parenthesisStk.pop();
			float replacement = arithmeticEval(substring);
			int replacer = (int) replacement;
			String replaceString = Integer.toString(replacer);
			expr = expr.replace(substringToReplace, replaceString);
			return parenthesesEval(expr);
		} 
		
		System.out.println("simplified parentheses: " + expr);
		
		return expr;
		
	}
    
    private static String evaluateBrackets(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	
    	Stack<Character> bracketStk = new Stack<Character>();
    	
    	String newStr = "", bracketEval = "", bracketReplacer = "", valueOfArray = "";
    	String arrayName = "";
    	int counter = 0, amountOfLoops = 0; 	
    	int headBracket = 0, tailBracket = 0;
    	ArrayList<String> listOfNames = new ArrayList<String>();
    	StringTokenizer str = new StringTokenizer(expr, "+-*/()[]");
    	while (str.hasMoreTokens()) {
    		String token = str.nextToken();
    		for (int i = 0; i < arrays.size(); i++) {
    			if (arrays.get(i).name.equals(token)) {
    				listOfNames.add(token);
    			}
    		}
    	}
    	
    	for (int i = 0; i < expr.length(); i++) {
    		if (expr.charAt(i) == '[') {
    			bracketStk.push('[');
    			headBracket = i;
    			amountOfLoops++;
    		} else if (expr.charAt(i) != '[' && expr.charAt(i) != ']') {
    			continue;
    		} else if (expr.charAt(i) == ']') {
    			if (counter == 0) {
    				tailBracket = i;
    				bracketEval = expr.substring(headBracket + 1, tailBracket);
    				bracketReplacer = expr.substring(headBracket, tailBracket + 1);
    				counter++;
    				break;
    			} else {
    				continue;
    			}
    		}
    	}
    	
    	if (!bracketStk.isEmpty()) {
    		bracketStk.pop();
    		bracketEval = evaluateVariables(bracketEval, vars, arrays);
    		bracketEval = parenthesesEval(bracketEval);
    		int index = (int) arithmeticEval(bracketEval);
    		String nameOfArray = listOfNames.get(amountOfLoops - 1);
    		for (int i = 0; i < arrays.size(); i++) {
    			if (arrays.get(i).name.equals(nameOfArray)) {
    				int[] arrayHolder = arrays.get(i).values;
    				valueOfArray = "" + arrayHolder[index];
    			}
    		}
    		String trueReplacer = nameOfArray+bracketReplacer;
    		expr = expr.replace(trueReplacer, valueOfArray);
    		return evaluateBrackets(expr, vars, arrays);
    	}
    	
    	System.out.println("AT THE END OF EVALUATE BRACKTS: " + expr);
    	
    	return expr;
    }
    
    private static String evaluateVariables(String expr, ArrayList<Variable> vars, ArrayList<Array> Arrays) {
    	
    	System.out.println("What is the expression: " + expr);
    	
    	String newStr = "";
    	StringTokenizer str = new StringTokenizer(expr, "+-*/()]",true);
    	while (str.hasMoreTokens()) {
    		String token = str.nextToken();
    		if (token.charAt(0) >= '0' && token.charAt(0) <= '9') {
    			newStr += token;
    		} else if (token.charAt(0) == '+' || token.charAt(0) == '-' || token.charAt(0) == '*' ||
    		    token.charAt(0) == '/') {
    			newStr += token;
            }
    		else if(token.charAt(0) == '(' || token.charAt(0) == ')' || token.charAt(0) == ']') {
    			newStr += token;
    		} else if(token.contains("[")) {
    			newStr += token;
    		} else {
    			for (int i = 0; i < vars.size(); i++) {
    				if (vars.get(i).name.equals(token)) {
    					Integer holder = vars.get(i).value;
    					newStr += holder.toString();
    				}
    			}
    		}
    	}
    	System.out.println("What evaluateVariables ends up as: " + newStr);
    	return newStr;
    }
      
    private static String rearrangeSigns(String expr) {
		String newStr = "";
		for (int i = 0; i < expr.length(); i++) {
			if (Character.isDigit(expr.charAt(i)) == true) {
				newStr += expr.charAt(i);
			} else if (expr.charAt(i) == '+') {
				if (!(i + 1 == expr.length()) && expr.charAt(i+1) == '-') {
					continue;
				} else {
					newStr += expr.charAt(i);
				}
			} else if (expr.charAt(i) == '-') {
				if (!(i+1 == expr.length()) && expr.charAt(i+1) == '-') {
					newStr += '+';
					i++;
				} else if (!(i+1 == expr.length()) && expr.charAt(i+1) == '+') {
					newStr += expr.charAt(i);
					i++;
				} else {
					newStr += expr.charAt(i);
				}
			} else if (expr.charAt(i) == '(' || expr.charAt(i) == ')' || expr.charAt(i) == '[' || 
					   expr.charAt(i) == ']') {
				newStr += expr.charAt(i);
			} else if (expr.charAt(i) == '*' || expr.charAt(i) == '/') {
				newStr += expr.charAt(i);
			}
		}
		
		return newStr;
	}
    
    private static <T> Stack<T> reverseStack(Stack<T> stk) {
		Stack<T> tempStk = new Stack<T>();
		while (!stk.isEmpty()) {
			tempStk.push(stk.pop());
		}
		return tempStk;	 
	}
    
    private static boolean isNumerical(String expr) {
		for (int i = 0; i < expr.length(); i++) {
			if (Character.isLetter(expr.charAt(i))) {
				return false;
			}
		}
		return true;
	}
    
    private static boolean containsBrackets(String expr) {
		for (int i = 0; i < expr.length(); i++) {
			if (expr.charAt(i) == '[' || expr.charAt(i) == ']') {
				return true;
			}
		}
		return false;
	}
}
