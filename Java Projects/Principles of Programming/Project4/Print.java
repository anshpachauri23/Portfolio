
 public class Print {
    private Expr expr; // Expression to print

    // Parses a print statement: print(<expr>);
    void parse() {
        Parser.expectedToken(Core.PRINT);
        Parser.scanner.nextToken();

        Parser.expectedToken(Core.LPAREN);
        Parser.scanner.nextToken();

        expr = new Expr();
        expr.parse();

        Parser.expectedToken(Core.RPAREN);
        Parser.scanner.nextToken();

        Parser.expectedToken(Core.SEMICOLON);
        Parser.scanner.nextToken();
    }

    // Prints the print statement with proper indentation.
    void print(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
        System.out.print("print(");
        expr.print(indent);
        System.out.println(");");
    }

    // Validates the print statement by validating its expression.
    void validate(SymbolTable symbols) {
        expr.validate(symbols);
    }

    // Executes the print statement by evaluating the expression and printing its value.
    void execute(MemoryManager memory) {
        int value = expr.evaluate(memory);
        System.out.println(value);
    }
}
