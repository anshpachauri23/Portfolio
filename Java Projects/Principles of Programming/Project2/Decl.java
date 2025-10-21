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

    void validate(SymbolTable symbols) {
        System.out.println("DEBUG: Attempting to add '" + id + "' as " + type + " in scope level "
                + (symbols.getScopeDepth() - 1));
        symbols.add(id, type); // ✅ FIX: Ensure we are in the correct scope before adding
    }
}
