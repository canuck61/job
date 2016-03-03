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

		try {

			expression = processCommandLine(args);

			if (expression != null) {

				logger.logMessage(Level.INFO, "Input Expression: " + expression);

				expressionStack = buildWorkingStack(expression);

				logger.logMessage(Level.INFO, "Working Stack: " + expressionStack);

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
			}

		} catch (ParseException pex) {
			String message = "An error occured while trying to parse the command line arguments:\n " + args[0];
			writeErrortoConsole(message, null);
			logger.logMessage(Level.ERROR, message, pex);
		} catch (Exception e) {
			String message = "Program exited abnormally";
			writeErrortoConsole(message, e);
			logger.logMessage(Level.ERROR, message, e);
		}

	}

	/**
	 * Process command line arguments
	 * 
	 * @param args
	 *            from main
	 * @return String expression to be evaluated
	 */
	private static String processCommandLine(String[] args) throws ParseException {

		CommandLine cmdLine = null;
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
		cmdLine = parser.parse(options, args);

		// check options passed
		if (cmdLine.hasOption("h")) {
			formatter.printHelp("java -jar calculator.jar " + "[-h] [-info] [-error] [-debug] \"<expression>\"", null,
					options, getFooter());
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
				formatter.printHelp(Calculator.class.getName() + "[-h] [-info] [-error] [-debug] <expression>", null,
						options, getFooter());
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

			logger.logMessage(Level.DEBUG, "Building working stack");

			// put in proper order
			for (int i = exp.length - 1; i >= 0; --i) {

				// force all input characters to lowercase
				element = exp[i].toLowerCase();

				// validate element types
				if (isOperator(element) || isVariable(element) || isInt(element)) {
					workingStack.push(element);
					logger.logMessage(Level.DEBUG,
							"Pushing element on stack: " + element + " (size: " + workingStack.size() + ")");
				} else {
					logger.logMessage(Level.DEBUG, "Nonvalid element type located: " + element);
					System.err.println("Error: Element: " + element + " is not a valid element type.");
					throw new Exception(" Element: " + element + " is not a valid element type.");
				}
			}
		}

		logger.logMessage(Level.DEBUG, "Finished building working stack");

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

		String arg1 = null;
		String arg2 = null;
		Integer intValue1 = null;
		Integer intValue2 = null;
		String element = null;
		Integer intAnswer = null;
		Hashtable<String, String> varValPair = new Hashtable<String, String>();

		while (workingStack.size() > 1) {

			if (workingStack.size() > 0) {
				element = workingStack.pop();
				logger.logMessage(Level.INFO,
						"Popping stack element : " + element + " (remaining size: " + workingStack.size() + ")");
			} else {
				throw new Exception(
						"The stack is empty and the expression has not finished evaluating. Most likely cause is incorrect expression.");
			}

			if (isMathOperator(element)) {

				String operator = element;
				intValue1 = null;
				intValue2 = null;

				if (workingStack.size() > 0) {
					arg1 = workingStack.pop();
					logger.logMessage(Level.INFO,
							"Popping stack element : " + arg1 + " (remaining size: " + workingStack.size() + ")");
				} else {
					throw new Exception(
							"The stack is empty and the expression has not finished evaluating. Most likely cause is incorrect expression.");
				}

				if (isVariable(arg1)) {
					// see if value for variable in hash map
					if (varValPair.containsKey(arg1)) {
						String tempValue = varValPair.get(arg1);
						intValue1 = getInt(tempValue);
						logger.logMessage(Level.INFO, "Getting variable from table: " + arg1 + "=" + intValue1);
					}
				} else if (isInt(arg1)) {
					intValue1 = getInt(arg1);
				} else if (isOperator(arg1)) {
					intValue1 = performOperation(arg1, workingStack, varValPair);
				}

				if (workingStack.size() > 0) {
					arg2 = workingStack.pop();
					logger.logMessage(Level.INFO,
							"Popping stack element : " + arg2 + " (remaining size: " + workingStack.size() + ")");
				} else {
					throw new Exception(
							"The stack is empty and the expression has not finished evaluating. Most likely cause is incorrect expression.");
				}

				if (isVariable(arg2)) {
					if (varValPair.containsKey(arg2)) {
						String tempValue = varValPair.get(arg2);
						intValue2 = getInt(tempValue);
						logger.logMessage(Level.INFO, "Getting variable from table: " + arg2 + "=" + intValue2);
					}
				} else if (isInt(arg2)) {
					intValue2 = getInt(arg2);
				} else if (isOperator(arg2)) {
					intValue2 = performOperation(arg2, workingStack, varValPair);
				}

				// perform operation
				Integer tempResult = eval(operator, intValue1, intValue2);
				workingStack.push(String.valueOf(tempResult.intValue()));
				logger.logMessage(Level.INFO,
						"Pushing element on stack: " + tempResult + " (size: " + workingStack.size() + ")");

			} else if (LET.equalsIgnoreCase(element)) {

				Integer value = null;

				if (workingStack.size() > 0) {
					arg1 = workingStack.pop();
					logger.logMessage(Level.INFO,
							"Popping stack element : " + arg1 + " (remaining size: " + workingStack.size() + ")");
				} else {
					throw new Exception(
							"The stack is empty and the expression has not finished evaluating. Most likely cause is incorrect expression.");
				}

				if (!isVariable(arg1)) {
					throw new Exception(
							"Invalid element sequence, first element after a let must always be a variable. Element located: "
									+ arg1);
				}

				if (workingStack.size() > 0) {
					arg2 = workingStack.pop();
					logger.logMessage(Level.INFO,
							"Popping stack element : " + arg2 + " (remaining size: " + workingStack.size() + ")");
				} else {
					throw new Exception(
							"The stack is empty and the expression has not finished evaluating. Most likely cause is incorrect expression.");
				}

				logger.logMessage(Level.INFO, "Evaluating Expression: " + element + "(" + arg1 + "," + arg2 + ")");

				if (isVariable(arg2)) {
					if (varValPair.containsKey(arg2)) {
						String tempValue = varValPair.get(arg2);
						value = getInt(tempValue);
						logger.logMessage(Level.INFO, "Getting variable from table: " + arg2 + "=" + value);
					}
				} else if (isInt(arg2)) {
					value = getInt(arg2);
				} else if (isOperator(arg2)) {
					value = performOperation(arg2, workingStack, varValPair);
				}

				arg2 = String.valueOf(value);

				// out name/value pair in map
				logger.logMessage(Level.INFO, "Setting variable in table: " + arg1 + "=" + arg2);
				varValPair.put(arg1, arg2);

			} else {
				workingStack.push(element);
				logger.logMessage(Level.INFO,
						"Pushing element on stack: " + element + " (size: " + workingStack.size() + ")");
			}
		}

		if (workingStack.size() == 1) {
			String answer = workingStack.pop();
			logger.logMessage(Level.INFO,
					"Poppping stack element : " + answer + "(remaining size: " + workingStack.size() + ")");
			intAnswer = getInt(answer);
		} else {
			throw new Exception("All items in the stack have been evaluated and no answer has been determined.");
		}

		return intAnswer;

	}

	/**
	 * 
	 * Performs operation with operator
	 * 
	 * @param operator
	 *            operation to be performed
	 * @param workingStack
	 *            elements of the expression in reverse polish notation
	 * @param varValPair
	 *            contains definition assigned variables through a "let"
	 *            statement
	 * @return
	 * @throws Exception
	 */
	private static Integer performOperation(String operator, ArrayDeque<String> workingStack,
			Hashtable<String, String> varValPair) throws Exception {

		String arg1;
		String arg2;
		Integer intValue1 = null;
		Integer intValue2 = null;
		Integer tempResult = null;

		if (isMathOperator(operator)) {

			if (workingStack.size() > 0) {
				arg1 = workingStack.pop();
				logger.logMessage(Level.INFO,
						"Poppping stack element : " + arg1 + "(remaining size: " + workingStack.size() + ")");
			} else {
				throw new Exception(
						"The stack is empty and the expression has not finished evaluating. Most likely cause is incorrect expression.");
			}

			if (workingStack.size() > 0) {
				arg2 = workingStack.pop();
				logger.logMessage(Level.INFO,
						"Poppping stack element : " + arg2 + "(remaining size: " + workingStack.size() + ")");
			} else {
				throw new Exception(
						"The stack is empty and the expression has not finished evaluating. Most likely cause is incorrect expression.");
			}

			logger.logMessage(Level.INFO, "Evaluating Expression: " + operator + "(" + arg1 + "," + arg2 + ")");

			// arg1
			if (isInt(arg1)) {
				intValue1 = getInt(arg1);
			} else {
				// see if value for variable in hash map
				if (varValPair.containsKey(arg1)) {
					String tempValue = varValPair.get(arg1);
					intValue1 = getInt(tempValue);
					logger.logMessage(Level.INFO, "Getting variable from table: " + arg1 + "=" + intValue1);
				}
			}

			// arg2
			if (isInt(arg2)) {
				intValue2 = getInt(arg2);
			} else {
				// see if value for variable in hash map
				if (varValPair.containsKey(arg2)) {
					String tempValue = varValPair.get(arg2);
					intValue2 = getInt(tempValue);
					logger.logMessage(Level.INFO, "Getting variable from table: " + arg2 + "=" + intValue2);
				}
			}

			tempResult = eval(operator, intValue1, intValue2);

		} else if (LET.equalsIgnoreCase(operator)) {

			Integer value = null;

			if (workingStack.size() > 0) {
				arg1 = workingStack.pop();
				logger.logMessage(Level.INFO,
						"Poppping stack element : " + arg1 + "(remaining size: " + workingStack.size() + ")");
			} else {
				throw new Exception(
						"The stack is empty and the expression has not finished evaluating. Most likely cause is incorrect expression.");
			}

			if (!isVariable(arg1)) {

				throw new Exception(
						"Invalid element sequence, first element after a let must always be a variable. Element located: "
								+ arg1);
			}

			if (workingStack.size() > 0) {
				arg2 = workingStack.pop();
				logger.logMessage(Level.INFO,
						"Poppping stack element : " + arg2 + "(remaining size: " + workingStack.size() + ")");
			} else {
				throw new Exception(
						"The stack is empty and the expression has not finished evaluating. Most likely cause is incorrect expression.");
			}

			logger.logMessage(Level.INFO, "Evaluating Expression: " + operator + "(" + arg1 + "," + arg2 + ")");

			if (isVariable(arg2)) {
				if (varValPair.containsKey(arg2)) {
					String tempValue = varValPair.get(arg2);
					value = getInt(tempValue);
					logger.logMessage(Level.INFO, "Getting variable from table: " + arg2 + "=" + value);
				}
			} else if (isInt(arg2)) {
				value = getInt(arg2);
			} else if (isOperator(arg2)) {
				value = performOperation(arg2, workingStack, varValPair);
			}

			arg2 = String.valueOf(value);

			logger.logMessage(Level.INFO, "Setting variable in table: " + arg1 + "=" + arg2);
			varValPair.put(arg1, arg2);

			// pop expression
			if (workingStack.size() > 0) {
				arg1 = workingStack.pop();
				logger.logMessage(Level.INFO,
						"Poppping stack element : " + arg1 + "(remaining size: " + workingStack.size() + ")");
			} else {
				throw new Exception(
						"The stack is empty and the expression has not finished evaluating. Most likely cause is incorrect expression.");
			}

			tempResult = performOperation(arg1, workingStack, varValPair);

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

		if (operator != null && x != null && y != null) {

			if (ADD.equals(operator)) {
				logger.logMessage(Level.INFO, "Performing Operation: " + ADD + "(" + x + "," + y + ")");
				answer = x + y;
			} else if (SUB.equals(operator)) {
				logger.logMessage(Level.INFO, "Performing Operation: " + SUB + "(" + x + "," + y + ")");
				answer = x - y;
			} else if (MULT.equals(operator)) {
				logger.logMessage(Level.INFO, "Performing Operation: " + MULT + "(" + x + "," + y + ")");
				answer = x * y;
			} else if (DIV.equals(operator)) {
				logger.logMessage(Level.INFO, "Performing Operation: " + DIV + "(" + x + "," + y + ")");
				answer = x / y;
			} else {
				throw new Exception("Invalid Operator " + x + " " + operator + " " + y);
			}
		} else {
			
			if (operator == null) {
				throw new Exception("Unable to evaluate expression with arguments: " + x + "," + y + " no operator is present." );				
			} else if (x == null) {
				throw new Exception("Unable to evaluate expression " + operator + " " + y + " missing operand.");					
			} else if (y == null) {
				throw new Exception("Unable to evaluate expression " + operator + " " + x + " missing operand.");					
			}
		}

		return answer;

	}

	private static void writeErrortoConsole(String msg, Exception ex) {

		if (ex == null) {
			System.err.format("%s%n", msg);
		} else {
			System.err.format("%s \nException: %s\n", msg, ex.getMessage());
			ex.printStackTrace(System.out);
		}

	}

	private static String getFooter() {

		StringBuilder headerString = new StringBuilder(1000);

		headerString.append("\nThis program takes in a simple integer expression in the form ");
		headerString.append("specified below and returns the result.");
		headerString.append("\n\nExamples: ");
		headerString.append("\n\nadd(1, 2)");
		headerString.append("\nadd(1, mult(2, 3))");
		headerString.append("\nmult(add(2, 2), div(9, 3))");
		headerString.append("\nlet(a, 5, add(a, a))");
		headerString.append("\nlet(a, 5, let(b, mult(a, 10), add(b, a)))");
		headerString.append("\nlet(a, let(b, 10, add(b, b)), let(b, 20, add(a, b))");
		headerString.append("\n\nAn expression is one of the following:");
		headerString.append("\n• Numbers: integers between Integer.MIN_VALUE and Integer.MAX_VALUE");
		headerString.append("\n• Variables: strings of characters, where each character is one of a-z, A-Z");
		headerString.append(
				"\n• Arithmetic functions: add, sub, mult, div, each taking two arbitrary expressions as arguments.In other words, each argument may be any of the expressions on this list.");
		headerString.append("\n• A “let” operator for assigning values to variables:");
		headerString.append("\n	     let(<variable name>, <value expression>, <expression where variable is used>)");
		headerString.append(
				"\n\nAs with arithmetic functions, the value expression and the expression where the variable is used may be an arbitrary expression from this list.");
		headerString.append(
				"\n\nLog files will be created in the directory the program is invoked in. No log file will be produced unlesss one of the log file options is set.");
		headerString.append("\n\nInvocation examples:");
		headerString.append("\n\njava -jar calculator.jar -h");
		headerString.append("\njava -jar calculator.jar \"add(2,4)\"");
		headerString.append("\njava -jar calculator.jar -info \"let(a, 5, add(a, a))\"");
		return headerString.toString();

	}

}
