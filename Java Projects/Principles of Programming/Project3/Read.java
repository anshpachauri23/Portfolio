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

    // Validates that the variable to read into has been declared.
    void validate(SymbolTable symbols) {
        symbols.getType(id); // Will throw an error if the variable is undeclared.
    }

    // Executes the read statement.
    void execute(MemoryManager memory) {
        int value = memory.readInput();
        memory.assignValue(id, value);
    }
}
