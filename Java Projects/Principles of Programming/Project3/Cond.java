public class Cond {
    private Cmpr cmpr; // Comparison (e.g., "x < 5")
    private Cond cond1; // First condition (for NOT, AND, OR)
    private Cond cond2; // Second condition (for AND, OR)
    private CondType type; // Type of condition (comparison, NOT, AND, OR)

    enum CondType {
        CMPR, // Simple comparison (e.g., "x < 5")
        NOT, // Negation (e.g., "not x < 5")
        AND, // Logical AND (e.g., "x < 5 and y > 10")
        OR, // Logical OR (e.g., "x < 5 or y > 10")
        BRACKETED
    }

    void parse() {
        // Check for NOT or [ <cond> ]
        if (Parser.scanner.currentToken() == Core.NOT) {
            type = CondType.NOT;
            Parser.scanner.nextToken();
            cond1 = new Cond();
            cond1.parse();
        } else if (Parser.scanner.currentToken() == Core.LSQUARE) {
            // Handle bracketed condition: [ <cond> ]
            Parser.scanner.nextToken(); // Consume "["
            cond1 = new Cond();
            cond1.parse();
            Parser.expectedToken(Core.RSQUARE);
            Parser.scanner.nextToken(); // Consume "]"
            type = CondType.BRACKETED; // NEW TYPE FOR BRACKETED CONDITIONS
        } else {
            // Parse a comparison
            type = CondType.CMPR;
            cmpr = new Cmpr();
            cmpr.parse();

            // Check for AND/OR
            Core token = Parser.scanner.currentToken();
            if (token == Core.AND) {
                type = CondType.AND;
                Parser.scanner.nextToken();
                cond2 = new Cond();
                cond2.parse();
            } else if (token == Core.OR) {
                type = CondType.OR;
                Parser.scanner.nextToken();
                cond2 = new Cond();
                cond2.parse();
            }
        }
    }

    void print(int indent) {
        switch (type) {
            case CMPR:
                cmpr.print(indent);
                break;
            case NOT:
                System.out.print("not ");
                cond1.print(indent);
                break;
            case BRACKETED: // Handle bracketed conditions
                System.out.print("[");
                cond1.print(indent);
                System.out.print("]");
                break;
            case AND:
                cmpr.print(indent);
                System.out.print(" and ");
                cond2.print(indent);
                break;
            case OR:
                cmpr.print(indent);
                System.out.print(" or ");
                cond2.print(indent);
                break;
            default:
                throw new RuntimeException("ERROR: Unexpected condition type: " + type);
        }
    }

    void validate(SymbolTable symbols) {
        switch (type) {
            case CMPR:
                cmpr.validate(symbols);
                break;
            case NOT:
            case BRACKETED:
                cond1.validate(symbols); // Validate the nested condition
                break;
            case AND:
            case OR:
                cmpr.validate(symbols);
                cond2.validate(symbols);
                break;
            default:
                throw new RuntimeException("Unexpected condition type: " + type);
        }
    }

    // Evaluates the condition and returns a boolean result.
    boolean evaluate(MemoryManager memory) {
        switch (type) {
            case CMPR:
                return cmpr.evaluate(memory);
            case NOT:
                return !cond1.evaluate(memory);
            case AND:
                return cmpr.evaluate(memory) && cond2.evaluate(memory);
            case OR:
                return cmpr.evaluate(memory) || cond2.evaluate(memory);
            case BRACKETED:
                return cond1.evaluate(memory);
            default:
                System.out.println("ERROR: Unexpected condition type in evaluate().");
                System.exit(1);
                return false; // Unreachable.
        }
    }

}
