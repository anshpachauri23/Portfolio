public class Assign {
    private String leftId; // Left-side ID (common to all cases)
    private String rightId; // Right-side ID (for "id : id")
    private String strKey; // String key (for "id[string]" or "new object")
    private Expr expr; // Expression (for "= expr", "= new object(...)", etc.)
    private AssignmentType type; // Type of assignment

    enum AssignmentType {
        SIMPLE_ASSIGN, // id = <expr>;
        ARRAY_ASSIGN, // id[string] = <expr>;
        NEW_OBJECT_ASSIGN, // id = new object(string, <expr>);
        OBJECT_REF_ASSIGN // id : id;
    }

    void parse() {
        // Parse left-hand side ID
        Parser.expectedToken(Core.ID);
        leftId = Parser.scanner.getId();
        Parser.scanner.nextToken();

        // Determine assignment type based on next token
        Core token = Parser.scanner.currentToken();
        if (token == Core.ASSIGN) {
            parseAssignOrNewObject();
        } else if (token == Core.LSQUARE) {
            parseArrayAssign();
        } else if (token == Core.COLON) {
            parseObjectRefAssign();
        } else {
            Parser.expectedToken(Core.ASSIGN); // Throw error
        }

        // Consume semicolon
        Parser.expectedToken(Core.SEMICOLON);
        Parser.scanner.nextToken();
    }

    // Parses either a simple assignment or a new object assignment.
    private void parseAssignOrNewObject() {
        Parser.scanner.nextToken(); // Consume "="
        if (Parser.scanner.currentToken() == Core.NEW) {
            type = AssignmentType.NEW_OBJECT_ASSIGN;
            parseNewObject();
        } else {
            type = AssignmentType.SIMPLE_ASSIGN;
            expr = new Expr();
            expr.parse();
        }
    }

    // Parses a new object assignment: new object("string", <expr>)
    private void parseNewObject() {
        Parser.expectedToken(Core.NEW);
        Parser.scanner.nextToken();
        Parser.expectedToken(Core.OBJECT);
        Parser.scanner.nextToken();
        Parser.expectedToken(Core.LPAREN);
        Parser.scanner.nextToken();

        // Parse string key for the new object
        Parser.expectedToken(Core.STRING);
        strKey = Parser.scanner.getString();
        Parser.scanner.nextToken();

        Parser.expectedToken(Core.COMMA);
        Parser.scanner.nextToken();

        // Parse the expression that initializes the object
        expr = new Expr();
        expr.parse();

        Parser.expectedToken(Core.RPAREN);
        Parser.scanner.nextToken();
    }

    // Parses an array assignment: id["string"] = <expr>
    private void parseArrayAssign() {
        type = AssignmentType.ARRAY_ASSIGN;
        Parser.scanner.nextToken(); // Consume "["

        // Parse the string key for the array access
        Parser.expectedToken(Core.STRING);
        strKey = Parser.scanner.getString();
        Parser.scanner.nextToken();

        Parser.expectedToken(Core.RSQUARE);
        Parser.scanner.nextToken(); // Consume "]"
        Parser.expectedToken(Core.ASSIGN);
        Parser.scanner.nextToken(); // Consume "="

        // Parse the expression assigned to the array element
        expr = new Expr();
        expr.parse();
    }

    // Parses an object reference assignment: id : id
    private void parseObjectRefAssign() {
        type = AssignmentType.OBJECT_REF_ASSIGN;
        Parser.scanner.nextToken(); // Consume ":"

        // Parse the right-hand side identifier
        Parser.expectedToken(Core.ID);
        rightId = Parser.scanner.getId();
        Parser.scanner.nextToken();
    }

    // Prints the assignment statement with proper indentation.
    void print(int indent) {
        // Print indentation spaces
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        switch (type) {
            case SIMPLE_ASSIGN:
                System.out.print(leftId + " = ");
                expr.print(0);
                break;
            case ARRAY_ASSIGN:
                System.out.print(leftId + "['" + strKey + "'] = ");
                expr.print(0);
                break;
            case NEW_OBJECT_ASSIGN:
                System.out.print(leftId + " = new object('" + strKey + "', ");
                expr.print(0);
                System.out.print(")");
                break;
            case OBJECT_REF_ASSIGN:
                System.out.print(leftId + " : " + rightId);
                break;
            default:
                throw new RuntimeException("ERROR: Unexpected assignment type: " + type);
        }
        System.out.println(";");
    }

    // Performs semantic validation of the assignment.
    void validate(SymbolTable symbols) {
        String leftType = symbols.getType(leftId);

        // Perform type checks for object-related assignments.
        switch (type) {
            case ARRAY_ASSIGN:
            case NEW_OBJECT_ASSIGN:
                if (!"object".equals(leftType)) {
                    System.out.println("ERROR: " + leftId + " must be an object");
                    System.exit(1);
                }
                break;
            case OBJECT_REF_ASSIGN:
                String rightType = symbols.getType(rightId);
                if (!"object".equals(leftType) || !"object".equals(rightType)) {
                    System.out.println("ERROR: Both " + leftId + " and " + rightId + " must be objects");
                    System.exit(1);
                }
                break;
            default:
                // No extra checks needed for SIMPLE_ASSIGN.
                break;
        }

        // Validate the expression if it exists.
        if (expr != null) {
            expr.validate(symbols);
        }
    }

    // Executes the assignment by updating the memory manager.
    void execute(MemoryManager memory) {
        switch (type) {
            case OBJECT_REF_ASSIGN:
                memory.setObjectReference(leftId, rightId);
                break;
            case NEW_OBJECT_ASSIGN: {
                int value = expr.evaluate(memory);
                // If leftId is a formal parameter alias, rebind it; otherwise, create a new object.
                if (memory.isFormalAlias(leftId)) {
                    memory.rebindFormal(leftId, value);
                } else {
                    memory.createObject(leftId, strKey, value);
                }
                break;
            }
            case ARRAY_ASSIGN: {
                int value = expr.evaluate(memory);
                memory.updateObject(leftId, strKey, value);
                break;
            }
            case SIMPLE_ASSIGN: {
                int value = expr.evaluate(memory);
                if (memory.isObject(leftId)) {
                    // For objects, update the default key.
                    memory.updateObject(leftId, "default", value);
                } else {
                    memory.assignValue(leftId, value);
                }
                break;
            }
            default:
                System.out.println("ERROR: Unexpected assignment type: " + type);
                System.exit(1);
        }
    }
    
}
