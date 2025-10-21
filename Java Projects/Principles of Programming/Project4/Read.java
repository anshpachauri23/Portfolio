
 public class Read {
    private String id; // Identifier to store the input value

    // Parses a read statement: read(<id>);
    void parse() {
        Parser.expectedToken(Core.READ);
        Parser.scanner.nextToken();

        Parser.expectedToken(Core.LPAREN);
        Parser.scanner.nextToken();

        Parser.expectedToken(Core.ID);
        id = Parser.scanner.getId();
        Parser.scanner.nextToken();

        Parser.expectedToken(Core.RPAREN);
        Parser.scanner.nextToken();

        Parser.expectedToken(Core.SEMICOLON);
        Parser.scanner.nextToken();
    }

    // Prints the read statement with proper indentation.
    void print(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
        System.out.println("read(" + id + ");");
    }

    // Validates the read statement by ensuring the variable is declared.
    void validate(SymbolTable symbols) {
        symbols.getType(id);
    }

    // Executes the read statement by reading an integer from input and assigning it.
    void execute(MemoryManager memory) {
        int value = memory.readInput();
        // If the variable is an object, update its default key; otherwise, assign directly.
        if (memory.isObject(id)) {
            memory.updateObject(id, "default", value);
        } else {
            memory.assignValue(id, value);
        }
    }
}
