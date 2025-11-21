public class Print {
    private Expr expr; // Expression to print

    void parse() {
        // Parse "print"
        Parser.expectedToken(Core.PRINT);
        Parser.scanner.nextToken();

        // Parse "("
        Parser.expectedToken(Core.LPAREN);
        Parser.scanner.nextToken();

        // Parse the expression
        expr = new Expr();
        expr.parse();

        // Parse ")"
        Parser.expectedToken(Core.RPAREN);
        Parser.scanner.nextToken();

        // Parse ";"
        Parser.expectedToken(Core.SEMICOLON);
        Parser.scanner.nextToken();
    }

    void print(int indent) {
        // Print indentation
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        // Print the statement
        System.out.print("print(");
        expr.print(indent);
        System.out.println(");");
    }

    void validate(SymbolTable symbols) {
        expr.validate(symbols); // Validate the expression (e.g., variables in expr must be declared)
    }
}
