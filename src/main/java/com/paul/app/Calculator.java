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

	public static final String ADD = "add";
	public static final String SUB = "sub";
	public static final String MULT = "mult";
	public static final String DIV = "div";
	public static final String LET = "let";

	private static final SimpleLogger logger = SimpleLogger.getInstance();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Integer answer = null;
		ArrayDeque<String> workingStack;
		String expression = null;

		expression = processCommandLine(args);
		
		if (expression != null) {

			workingStack = buildWorkingStack(expression);

			answer = evaluateStack(workingStack);

			if (answer != null) {
				System.out.println("My Answer: " + answer);
				logger.logMessage(Level.INFO, "My Answer: " + answer);
			} else {
				System.err.println("Something Wrong");
				logger.logMessage(Level.ERROR, "Something Wrong");
			}
		}
		

	}

	static boolean isOperator(String token) {
		return token.equalsIgnoreCase(ADD) || token.equalsIgnoreCase(SUB) || token.equalsIgnoreCase(DIV)
				|| token.equalsIgnoreCase(MULT) || token.equalsIgnoreCase(LET);
	}

	static boolean isMathOperator(String token) {
		return token.equalsIgnoreCase(ADD) || token.equalsIgnoreCase(SUB) || token.equalsIgnoreCase(DIV)
				|| token.equalsIgnoreCase(MULT);
	}

	static boolean isVariable(String token) {
		return (token.matches("[a-z,A-Z]"));
	}

	// int true or false
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

	// returns Integer or null
	static Integer getInt(String token) {
		Integer myInteger = null;

		try {
			myInteger = Integer.parseInt(token);
		} catch (NumberFormatException nex) {
			// ignore
		}

		return myInteger;
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
		Option version = new Option("v", "print the version information and exit");
		Option error = new Option("error", "error log level");
		Option info = new Option("info", "info logging level");
		Option debug = new Option("debug", "debug logging level");

		// add t option
		options.addOption(help);
		options.addOption(version);
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

	private static Integer eval(String operator, Integer x, Integer y) throws AssertionError {

		if (ADD.equalsIgnoreCase(operator)) {
			return x + y;
		} else if (SUB.equalsIgnoreCase(operator)) {
			return x - y;
		} else if (MULT.equalsIgnoreCase(operator)) {
			return x * y;
		} else if (DIV.equalsIgnoreCase(operator)) {
			return x / y;
		}
		throw new AssertionError("Unknown operator: " + operator);
	}

	// build a stack to work off of and validate the expression is correct
	private static ArrayDeque<String> buildWorkingStack(String inputExpression) {

		String element = null;
		ArrayDeque<String> workingStack = new ArrayDeque<String>();

		if (inputExpression != null) {

			String testing = inputExpression.replaceAll("[ ]", "");
			testing = testing.replaceAll("[,()]+", " ");
			String exp[] = testing.split(" ");
		
            // put in proper order
			for (int i = exp.length - 1; i >= 0; --i) {

				element = exp[i];
				
				if (isOperator(element) || isVariable(element) || isInt(element)) {
					workingStack.push(element);
				} else {
					System.err.println("Error: Element: " + element + " is not a valid element type.");
					System.err.println("Valid types are: ");
				}
			}
		}

		return workingStack;

	}

	private static Integer evaluateStack(ArrayDeque<String> workingStack) {

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
						arg1 = tempValue;
						intValue1 = getInt(arg1);
					}
				} else if (isInt(arg1)) {
					intValue1 = getInt(arg1);
				} else if (isOperator(arg1)) {
					intValue1 = performOperation2(arg1, workingStack, varValPair);
				}

				arg2 = workingStack.pop();

				if (isVariable(arg2)) {
					if (varValPair.containsKey(arg2)) {
						;
						String tempValue = varValPair.get(arg2);
						arg2 = tempValue;
						intValue2 = getInt(arg2);
					}

				} else if (isInt(arg2)) {
					intValue2 = getInt(arg2);
				} else if (isOperator(arg2)) {
					intValue2 = performOperation2(arg2, workingStack, varValPair);
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
					value = performOperation2(arg2, workingStack, varValPair);
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

	private static Integer performOperation2(String operator, ArrayDeque<String> workingStack,
			Hashtable<String, String> varValPair) {

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
				value = performOperation2(arg2, workingStack, varValPair);
			}

			arg2 = String.valueOf(value);

			varValPair.put(arg1, arg2);

			arg1 = workingStack.pop();

			// do i need to check if this is a

			if (isOperator(arg1)) {
				tempResult = performOperation2(arg1, workingStack, varValPair);
			}

		}

		return tempResult;

	}

}
