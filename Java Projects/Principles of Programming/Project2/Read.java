public class Read {
    private String id; // Variable to store the input

    void parse() {
        // Parse "read"
        Parser.expectedToken(Core.READ);
        Parser.scanner.nextToken();

        // Parse "("
        Parser.expectedToken(Core.LPAREN);
        Parser.scanner.nextToken();

        // Parse ID
        Parser.expectedToken(Core.ID);
        id = Parser.scanner.getId();
        Parser.scanner.nextToken();

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
        System.out.println("read(" + id + ");");
    }

    void validate(SymbolTable symbols) {
        // Ensure the variable being read into is declared
        symbols.getType(id); // Will throw error if undeclared
    }
}
