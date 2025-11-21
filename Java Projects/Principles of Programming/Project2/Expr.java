public class Expr {
    private Term term; // Term component
    private Expr nextExpr; // Next expression component (for + or -)
    private Core operator; // Operator (if any)

    void parse() {
        // Parse the term
        term = new Term();
        term.parse();

        // Check for additional terms with + or -
        if (Parser.scanner.currentToken() == Core.ADD || Parser.scanner.currentToken() == Core.SUBTRACT) {
            operator = Parser.scanner.currentToken();
            Parser.scanner.nextToken();
            nextExpr = new Expr();
            nextExpr.parse();
        }
    }

    void print(int indent) {
        term.print(indent);
        if (nextExpr != null) {
            System.out.print(" ");
            switch (operator) {
                case Core.ADD:
                    System.out.print("+");
                    break;
                case Core.SUBTRACT:
                    System.out.print("-");
                    break;
                default:
                    throw new RuntimeException("Unknown operator: " + operator);
            }
            System.out.print(" ");
            nextExpr.print(indent);
        }
    }

    void validate(SymbolTable symbols) {
        term.validate(symbols); // Validate the term
        if (nextExpr != null) {
            nextExpr.validate(symbols); // Validate additional terms (for + or -)
        }
    }
}
