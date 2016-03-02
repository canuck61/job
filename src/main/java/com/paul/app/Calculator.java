/**
 * 
 */
package com.paul.app;

import java.util.ArrayDeque;
import java.util.Hashtable;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.paul.utils.log.SimpleLogger;
import com.paul.utils.log.SimpleLogger.Level;

/**
 * @author Paul Canvin
 *
 */
public class Calculator {

	private static final String ADD = "add";
	private static final String SUB = "sub";
	private static final String MULT = "mult";
	private static final String DIV = "div";
	private static final String LET = "let";
	private static final SimpleLogger logger = SimpleLogger.getInstance();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Integer answer = null;
		String expression = null;
		ArrayDeque<String> expressionStack;

		expression = processCommandLine(args);

		if (expression != null) {

			try {
				expressionStack = buildWorkingStack(expression);

				answer = evaluateStack(expressionStack);

				if (answer != null) {
					String message = "Expression Answer: " + answer;
					System.out.println(message);
					logger.logMessage(Level.INFO, message);
				} else {
					String message = "No answer could be evaluated.";
					writeErrortoConsole(message, null);
					logger.logMessage(Level.ERROR, message);
				}

			} catch (Exception e) {
				String message = "Program exited abnormally";
				writeErrortoConsole(message, null);
				logger.logMessage(Level.ERROR, message, e);
			}

		}

	}

	private static String processCommandLine(String[] args) {

		CommandLine cmdLine = null;
		String header = "Do something useful with an input file\n\n";
		String footer = "\nPlease report issues at http://example.com/issues";
		String expression = null;

		HelpFormatter formatter = new HelpFormatter();

		// create Options object
		Options options = new Options();

		Option help = new Option("h", "print this message");
		Option error = new Option("error", "error log level");
		Option info = new Option("info", "info logging level");
		Option debug = new Option("debug", "debug logging level");

		// add t option
		options.addOption(help);
		options.addOption(error);
		options.addOption(info);
		options.addOption(debug);

		CommandLineParser parser = new DefaultParser();
		try {

			cmdLine = parser.parse(options, args);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// check options passed
		if (cmdLine.hasOption("h")) {
			formatter.printHelp(Calculator.class.getName() + "[OPTION] + [EXPRESSION]", header, options, footer, true);
		} else {
			if (cmdLine.hasOption("error")) {
				logger.setLevel(Level.ERROR);
			}
			if (cmdLine.hasOption("debug")) {
				logger.setLevel(Level.DEBUG);
			}
			if (cmdLine.hasOption("info")) {
				logger.setLevel(Level.INFO);
			}

			String[] remainingArguments = cmdLine.getArgs();

			if (remainingArguments.length != 1) {
				formatter.printHelp(Calculator.class.getName() + "[OPTION] + [EXPRESSION]", header, options, footer,
						true);
			} else {
				expression = remainingArguments[0];
			}
		}

		return expression;

	}

	// build a stack to work off of and validate the expression tokens
	/**
	 * Build an expression stack and validates the expression types
	 * 
	 * @param inputExpression
	 * @return ArrayDeque<String> stack of the elements of the expression in
	 *         reverse polish notation
	 * @throws Exception
	 */
	private static ArrayDeque<String> buildWorkingStack(String inputExpression) throws Exception {

		String element = null;
		ArrayDeque<String> workingStack = new ArrayDeque<String>();

		if (inputExpression != null) {

			String testing = inputExpression.replaceAll("[ ]", "");
			testing = testing.replaceAll("[,()]+", " ");
			String exp[] = testing.split(" ");

			// put in proper order
			for (int i = exp.length - 1; i >= 0; --i) {

				// force all input characters to lowercase
				element = exp[i].toLowerCase();

				if (isOperator(element) || isVariable(element) || isInt(element)) {
					workingStack.push(element);
				} else {
					System.err.println("Error: Element: " + element + " is not a valid element type.");
					throw new Exception(" Element: " + element + " is not a valid element type.");
				}
			}
		}

		return workingStack;

	}

	/**
	 * Evaluates the expression
	 * 
	 * @param workingStack
	 *            elements of the expression in reverse polish notation
	 * @return Integer answer of expression or null if expression cannot be
	 *         evaluated
	 * @throws Exception
	 */
	private static Integer evaluateStack(ArrayDeque<String> workingStack) throws Exception {

		String arg1;
		String arg2;
		Integer intValue1;
		Integer intValue2;
		String element;
		Integer intAnswer = null;
		Hashtable<String, String> varValPair = new Hashtable<String, String>();

		while (workingStack.size() > 1) {

			element = workingStack.pop();

			if (isMathOperator(element)) {

				String operator = element;
				intValue1 = null;
				intValue2 = null;

				arg1 = workingStack.pop();

				if (isVariable(arg1)) {
					// see if value for variable in hash map
					if (varValPair.containsKey(arg1)) {
						String tempValue = varValPair.get(arg1);
						intValue1 = getInt(tempValue);
					}
				} else if (isInt(arg1)) {
					intValue1 = getInt(arg1);
				} else if (isOperator(arg1)) {
					intValue1 = performOperation(arg1, workingStack, varValPair);
				}

				arg2 = workingStack.pop();

				if (isVariable(arg2)) {
					if (varValPair.containsKey(arg2)) {
						;
						String tempValue = varValPair.get(arg2);
						intValue2 = getInt(tempValue);
					}

				} else if (isInt(arg2)) {
					intValue2 = getInt(arg2);
				} else if (isOperator(arg2)) {
					intValue2 = performOperation(arg2, workingStack, varValPair);
				}

				if ((intValue1 != null) && (intValue2 != null)) {

					// perform operation
					Integer tempResult = eval(operator, intValue1, intValue2);
					workingStack.push(String.valueOf(tempResult.intValue()));
				}

			} else if (LET.equalsIgnoreCase(element)) {

				Integer value = null;
				arg1 = workingStack.pop();
				arg2 = workingStack.pop();

				if (isVariable(arg2)) {
					if (varValPair.containsKey(arg2)) {
						String tempValue = varValPair.get(arg2);
						arg2 = tempValue;
						value = getInt(arg2);
					}

				} else if (isInt(arg2)) {
					value = getInt(arg2);
				} else if (isOperator(arg2)) {
					value = performOperation(arg2, workingStack, varValPair);
				}

				arg2 = String.valueOf(value);

				// out name/value pair in map
				varValPair.put(arg1, arg2);

			} else {
				workingStack.push(element);
			}
		}

		if (workingStack.size() == 1) {
			String answer = workingStack.pop();
			intAnswer = getInt(answer);
		} else {
			System.out.println("PROBLEM");
		}

		return intAnswer;

	}

	private static Integer performOperation(String operator, ArrayDeque<String> workingStack,
			Hashtable<String, String> varValPair) throws Exception {

		String arg1;
		String arg2;
		Integer intValue1 = null;
		Integer intValue2 = null;
		Integer tempResult = null;

		if (isMathOperator(operator)) {

			arg1 = workingStack.pop();
			arg2 = workingStack.pop();

			// arg1
			if ((intValue1 = getInt(arg1)) == null) {
				// see if value for variable in hash map
				if (varValPair.containsKey(arg1)) {
					String tempValue = varValPair.get(arg1);
					arg1 = tempValue;
					intValue1 = getInt(arg1);
				}
			}

			// arg2
			if ((intValue2 = getInt(arg2)) == null) {
				// see if value for variable in hash map
				if (varValPair.containsKey(arg2)) {
					String tempValue = varValPair.get(arg2);
					arg2 = tempValue;
					intValue2 = getInt(arg2);
				}
			}

			if ((intValue1 != null) && (intValue2 != null)) {
				tempResult = eval(operator, intValue1, intValue2);
			}

		} else if (LET.equalsIgnoreCase(operator)) {

			Integer value = null;
			arg1 = workingStack.pop();
			arg2 = workingStack.pop();

			if (isVariable(arg2)) {
				if (varValPair.containsKey(arg2)) {
					String tempValue = varValPair.get(arg2);
					arg2 = tempValue;
					value = getInt(arg2);
				}

			} else if (isInt(arg2)) {
				value = getInt(arg2);
			} else if (isOperator(arg2)) {
				value = performOperation(arg2, workingStack, varValPair);
			}

			arg2 = String.valueOf(value);

			varValPair.put(arg1, arg2);

			arg1 = workingStack.pop();

			// do i need to check if this is a

			if (isOperator(arg1)) {
				tempResult = performOperation(arg1, workingStack, varValPair);
			}

		}

		return tempResult;

	}

	/**
	 * Checks to if token contains has a value of [ADD, SUB, DIV, MULT, LET]
	 * 
	 * @param token
	 *            expression element
	 * @return true if token is one of [ADD, SUB, DIV, MULT, LET], false
	 *         otherwise
	 */
	static boolean isOperator(String token) {
		return token.equalsIgnoreCase(ADD) || token.equalsIgnoreCase(SUB) || token.equalsIgnoreCase(DIV)
				|| token.equalsIgnoreCase(MULT) || token.equalsIgnoreCase(LET);
	}

	/**
	 * Checks to if token contains has a value of [ADD, SUB, DIV, MULT]
	 * 
	 * @param token
	 *            expression element
	 * @return true if token is one of [ADD, SUB, DIV, MULT], false otherwise
	 */
	static boolean isMathOperator(String token) {
		return token.equalsIgnoreCase(ADD) || token.equalsIgnoreCase(SUB) || token.equalsIgnoreCase(DIV)
				|| token.equalsIgnoreCase(MULT);
	}

	/**
	 * Checks to if token contains has a value of [a-z, A-Z]
	 * 
	 * @param token
	 *            expression element
	 * @return true if token is one of [a-z, A-Z], false otherwise
	 */
	static boolean isVariable(String token) {
		return (token.matches("[a-z,A-Z]"));
	}

	/**
	 * Checks to if token is an Integer
	 * 
	 * @param token
	 *            expression element
	 * @return true if token is an Integer , false otherwise
	 */
	static boolean isInt(String token) {
		boolean isInt = false;

		try {
			Integer.parseInt(token);
			isInt = true;
		} catch (NumberFormatException nex) {
			// ignore
		}

		return isInt;
	}

	/**
	 * Tries to convert the token into an Integer
	 * 
	 * @param token
	 *            expression element
	 * @return Integer value of token or null;
	 */
	static Integer getInt(String token) throws NumberFormatException {
		Integer myInteger = null;

		myInteger = Integer.parseInt(token);

		return myInteger;
	}

	/**
	 * 
	 * @param operator
	 *            one of [ADD, SUB, MULT, DIV]
	 * @param x
	 * @param y
	 * @return Integer answer of (x operator y)
	 * @throws Exception
	 *             if operation cannot be performed
	 */
	private static Integer eval(String operator, Integer x, Integer y) throws Exception {

		Integer answer = null;

		if (operator != null || x != null || y != null) {

			if (ADD.equals(operator)) {
				answer = x + y;
			} else if (SUB.equals(operator)) {
				answer = x - y;
			} else if (MULT.equals(operator)) {
				answer = x * y;
			} else if (DIV.equals(operator)) {
				answer = x / y;
			}
		} else {

			throw new Exception("Unable to evaluate " + x + " " + operator + " " + y);
		}

		return answer;

	}

	private static void writeErrortoConsole(String msg, Exception ex) {

		if (ex == null) {
			System.err.format("%s%n", msg);
		} else {
			System.err.format("Exception: %s", ex);
		}

	}

}
