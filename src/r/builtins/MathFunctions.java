package r.builtins;

import r.*;
import r.builtins.BuiltIn.AnalyzedArguments;
import r.data.*;
import r.data.internal.*;
import r.errors.*;
import r.nodes.*;
import r.nodes.truffle.*;

import com.oracle.truffle.api.frame.*;

// TODO: complex numbers
public class MathFunctions {

    public abstract static class Operation {
        public abstract double op(ASTNode ast, double value);
    }

    // FIXME: probably should create a more complete framework for simple math function builtins (including type specialization, constant handling)
    public static final class NumericXArgCallFactory extends CallFactory {
        private final Operation op;

        public NumericXArgCallFactory(Operation op) {
            this.op = op;
        }

        public RDouble calc(final ASTNode ast, final RDouble value) {
            final int size = value.size();
            if (size == 1) {
                return RDouble.RDoubleFactory.getScalar(op.op(ast, value.getDouble(0)), value.dimensions(), value.names(), value.attributesRef());
            } else if (size > 0) {
                return new View.RDoubleProxy<RDouble>(value) {

                    @Override public int size() {
                        return size;
                    }

                    @Override public double getDouble(int i) {
                        return op.op(ast, value.getDouble(i));
                    }
                };
            } else {
                return RDouble.EMPTY;
            }
        }

        @Override public RNode create(ASTNode call, RSymbol[] names, RNode[] exprs) {
            BuiltIn.ensureArgName(call, "x", names[0]);

            return new BuiltIn.BuiltIn1(call, names, exprs) {

                @Override public RAny doBuiltIn(Frame frame, RAny value) {
                    if (value instanceof RDouble || value instanceof RInt || value instanceof RLogical) {
                        return calc(ast, value.asDouble());
                    } else {
                        throw RError.getNonNumericMath(ast);
                    }
                }

            };
        }
    }

    public static final CallFactory LOG10_FACTORY = new NumericXArgCallFactory(new Operation() {

        @Override public double op(ASTNode ast, double value) {
            return Math.log10(value);
        }
    });

    public static final CallFactory LOG2_FACTORY = new NumericXArgCallFactory(new Operation() {

        final double rLOG2 = 1 / Math.log(2.0);

        @Override public double op(ASTNode ast, double value) {
            return Math.log(value) * rLOG2;
        }
    });

    public static final CallFactory LN_FACTORY = new NumericXArgCallFactory(new Operation() {

        @Override public double op(ASTNode ast, double value) {
            return Math.log(value);
        }
    });

    private static final String[] paramNames = new String[]{"x", "base"};

    private static final int IX = 0;
    private static final int IBASE = 1;

    public static final CallFactory LOG_FACTORY = new CallFactory() {

        @Override public RNode create(ASTNode call, RSymbol[] names, RNode[] exprs) {

            ArgumentInfo a = BuiltIn.analyzeArguments(names, exprs, paramNames);

            final boolean[] provided = a.providedParams;
            final int[] paramPositions = a.paramPositions;

            if (!provided[IX]) {
                BuiltIn.missingArg(call, paramNames[IX]);
            }

            if (exprs.length == 1) { return LN_FACTORY.create(call, names, exprs); }

            RNode baseExpr = exprs[paramPositions[IBASE]];
            if (BuiltIn.isNumericConstant(baseExpr, 10)) { return LOG10_FACTORY.create(call, names, exprs); }
            if (BuiltIn.isNumericConstant(baseExpr, 2)) { return LOG2_FACTORY.create(call, names, exprs); }
            // TODO: implement the generic case
            throw Utils.nyi("unsupported case");
        }
    };

}
