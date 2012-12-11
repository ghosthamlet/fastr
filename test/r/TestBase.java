package r;

import java.io.*;

import org.antlr.runtime.*;
import org.junit.*;

import r.data.*;
import r.nodes.*;
import r.nodes.tools.*;

public class TestBase {

    static RContext global = new RContext(1, true); // use debugging format

    static String evalString(String input) throws RecognitionException {
        return eval(input).pretty();
    }

    static void assertEval(String input, String expectedOutput, String expectedResult) throws RecognitionException {
        // FIXME: can this be made work also with Truffle?

        final PrintStream oldOut = System.out;
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        final PrintStream myOutPS = new PrintStream(myOut);
        System.setOut(myOutPS);
        String result = evalString(input);
        myOutPS.flush();
        System.setOut(oldOut);
        String output = myOut.toString();

        if (!result.equals(expectedResult)) {
            Assert.fail("incorrect result, got " + result + " for " + input + " expecting " + expectedResult);
        }
        if (!output.equals(expectedOutput)) {
            Assert.fail("incorrect output, got " + output + " for " + input + " expecting " + expectedOutput);
        }

    }

    static void assertEval(String input, String expected) throws RecognitionException {

        if (global.getCompiler() != null) {

            final PrintStream oldOut = System.out;
            final PrintStream oldErr = System.err;

            final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
            final PrintStream myOutPS = new PrintStream(myOut);

            System.out.println("Testing " + input + " with captured output.");

            System.setOut(myOutPS);
            System.setErr(myOutPS);
            String result = evalString(input);
            myOutPS.flush();
            System.setOut(oldOut);
            System.setErr(oldErr);

            String output = myOut.toString();
            String verboseOutput = "Captured output of " + input + " is below:\n" + output + "\n" +
                                   "Captured output of " + input + " is above.\n";

            if (!result.equals(expected)) {
                System.err.println(verboseOutput);
                Assert.fail("incorrect result when running with Truffle, got " + result + " for " + input);
            }
            if (output.contains("createOptimizedGraph:") && !output.contains("new specialization]#")) {
                System.err.println("Truffle compilation failed for " + input);
                System.err.println(verboseOutput);
                Assert.fail("Truffle compilation failed");
            }
            if (!input.contains("junitWrapper")) {
                // the test did not trigger compilation
                String newInput = "{ junitWrapper <- function() { " + input + " }; junitWrapper(); junitWrapper() }";
                System.out.println("Converted input " + input + " to " + newInput);
                assertEval(newInput, expected);
            } else {
                System.out.println(verboseOutput);
            }
        } else {
            String result = evalString(input);
            Assert.assertEquals(expected, result);
        }
    }

    static RAny eval(String input) throws RecognitionException {
        ASTNode astNode = TestPP.parse(input);
        return global.eval(astNode);
    }
}
