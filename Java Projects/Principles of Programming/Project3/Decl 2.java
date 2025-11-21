public class Decl {
    String type; // "integer" or "object"
    String id;

    void parse() {
        // Parse declaration type (integer/object)
        if (Parser.scanner.currentToken() == Core.INTEGER) {
            type = "integer";
        } else if (Parser.scanner.currentToken() == Core.OBJECT) {
            type = "object";
        } else {
            Parser.expectedToken(Core.INTEGER); // Error if neither
        }
        Parser.scanner.nextToken();

        // Parse ID
        Parser.expectedToken(Core.ID);
        id = Parser.scanner.getId();
        Parser.scanner.nextToken();

        // Parse semicolon
        Parser.expectedToken(Core.SEMICOLON);
        Parser.scanner.nextToken();
    }

    void print(int indent) {
        // Print indentation
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        System.out.println(type + " " + id + ";");
    }

    // Validates the declaration by adding the variable to the symbol table.
    void validate(SymbolTable symbols) {
        symbols.add(id, type);
    }

    // Executes the declaration by registering the variable in the memory manager.
    void execute(MemoryManager memory) {
        boolean isGlobal = memory.isGlobalScope(); // Check if we are in global scope

        if (type.equals("integer")) {
            memory.declareVariable(id, isGlobal); // Add integer variable to memory (initialized to 0)
        } else if (type.equals("object")) {
            memory.declareObject(id, isGlobal); // Declare object variable as null (uninitialized)
        }
    }

}
