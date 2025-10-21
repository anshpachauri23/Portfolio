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

    private void parseNewObject() {
        Parser.expectedToken(Core.NEW);
        Parser.scanner.nextToken();
        Parser.expectedToken(Core.OBJECT);
        Parser.scanner.nextToken();
        Parser.expectedToken(Core.LPAREN);
        Parser.scanner.nextToken();

        // Parse string key
        Parser.expectedToken(Core.STRING);
        strKey = Parser.scanner.getString();
        Parser.scanner.nextToken();

        Parser.expectedToken(Core.COMMA);
        Parser.scanner.nextToken();

        // Parse expression
        expr = new Expr();
        expr.parse();

        Parser.expectedToken(Core.RPAREN);
        Parser.scanner.nextToken();
    }

    private void parseArrayAssign() {
        type = AssignmentType.ARRAY_ASSIGN;
        Parser.scanner.nextToken(); // Consume "["

        // Parse string key
        Parser.expectedToken(Core.STRING);
        strKey = Parser.scanner.getString();
        Parser.scanner.nextToken();

        Parser.expectedToken(Core.RSQUARE);
        Parser.scanner.nextToken(); // Consume "]"
        Parser.expectedToken(Core.ASSIGN);
        Parser.scanner.nextToken(); // Consume "="

        // Parse expression
        expr = new Expr();
        expr.parse();
    }

    private void parseObjectRefAssign() {
        type = AssignmentType.OBJECT_REF_ASSIGN;
        Parser.scanner.nextToken(); // Consume ":"

        // Parse right-hand side ID
        Parser.expectedToken(Core.ID);
        rightId = Parser.scanner.getId();
        Parser.scanner.nextToken();
    }

    void print(int indent) {
        // Print indentation
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
                throw new RuntimeException("ERROR: Unexpected assignment type: " + type); // Handle all cases
        }
        System.out.println(";");
    }

    void validate(SymbolTable symbols) {
        String leftType = symbols.getType(leftId);

        // Type checks for object-specific assignments
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
                // No additional checks needed for SIMPLE_ASSIGN
                break;
        }

        // Validate expressions (e.g., exp.validate(symbols))
        if (expr != null) {
            expr.validate(symbols);
        }
    }
}
