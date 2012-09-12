package r;

import org.antlr.runtime.*;
import org.junit.*;

public class TestSimpleArithmetic extends TestBase {

    @Test
    public void testScalars() throws RecognitionException {
        assertEval("{1L+1}", "2.0");
        assertEval("{1L+1L}", "2L");
        assertEval("(1+1)*(3+2)", "10.0");
        assertEval("{1000000000*100000000000}", "1.0E20");
        assertEval("{1000000000L*1000000000L}", "NA");
        assertEval("{1000000000L*1000000000}", "1.0E18");
    }
}