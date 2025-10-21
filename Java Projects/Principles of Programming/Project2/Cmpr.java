public class Cmpr {
    private Expr leftExpr; // Left-hand side expression
    private Expr rightExpr; // Right-hand side expression
    private Core operator; // Comparison operator (==, <)

    void parse() {
        // Parse left expression
        leftExpr = new Expr();
        leftExpr.parse();

        // Parse operator
        operator = Parser.scanner.currentToken();
        if (operator != Core.EQUAL && operator != Core.LESS) {
            System.out.println("ERROR: Expected == or <, found " + operator);
            System.exit(1);
        }
        Parser.scanner.nextToken();

        // Parse right expression
        rightExpr = new Expr();
        rightExpr.parse();
    }

    void print(int indent) {
        leftExpr.print(indent);
        // Print the operator symbol instead of the enum name
        switch (operator) {
            case EQUAL:
                System.out.print(" == ");
                break;
            case LESS:
                System.out.print(" < ");
                break;
            default:
                throw new RuntimeException("Unexpected operator: " + operator);
        }
        rightExpr.print(indent);
    }

    void validate(SymbolTable symbols) {
        leftExpr.validate(symbols); // Validate left expression
        rightExpr.validate(symbols); // Validate right expression
    }
}
