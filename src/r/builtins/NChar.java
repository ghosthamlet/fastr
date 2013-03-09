package r.builtins;

import r.*;
import r.builtins.BuiltIn.AnalyzedArguments;
import r.data.*;
import r.nodes.*;
import r.nodes.truffle.*;

import com.oracle.truffle.api.frame.*;

// FIXME: only partial semantics
public class NChar {

    private static final String[] paramNames = new String[]{"x", "type", "allowNA"};

    private static final int IX = 0;
    private static final int ITYPE = 1;
    private static final int IALLOW_NA = 2;

    public static RInt nchar(RString s) {
        int size = s.size();
        int[] content = new int[size];
        for (int i = 0; i < size; i++) {
            content[i] = s.getString(i).length();
        }
        return RInt.RIntFactory.getFor(content, s.dimensions(), s.names());
    }

    public static final CallFactory FACTORY = new CallFactory() {

        @Override public RNode create(ASTNode call, RSymbol[] names, RNode[] exprs) {
            ArgumentInfo a = BuiltIn.analyzeArguments(names, exprs, paramNames);

            final boolean[] provided = a.providedParams;

            if (provided[ITYPE] || provided[IALLOW_NA]) {
                Utils.nyi();
            }
            if (names.length == 1) {
                if (!provided[IX]) {
                    BuiltIn.missingArg(call, paramNames[IX]);
                }
                return new BuiltIn.BuiltIn1(call, names, exprs) {
                    @Override public final RAny doBuiltIn(Frame frame, RAny x) {
                        return nchar(x.asString());
                    }
                };
            }
            throw Utils.nyi();
        }
    };
}
