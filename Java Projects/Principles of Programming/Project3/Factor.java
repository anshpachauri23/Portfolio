public class Factor {
    private String id; // ID component
    private String strKey; // String key component (for array access)
    private int constValue; // Constant value component
    private Expr expr; // Expression component (for parentheses)
    private FactorType type; // Type of factor

    enum FactorType {
        ID, // id
        ID_ARRAY, // id[string]
        CONST, // const
        EXPR // (<expr>)
    }

    void parse() {
        Core token = Parser.scanner.currentToken();
        if (token == Core.ID) {
            type = FactorType.ID;
            id = Parser.scanner.getId();
            Parser.scanner.nextToken();
            if (Parser.scanner.currentToken() == Core.LSQUARE) {
                type = FactorType.ID_ARRAY;
                Parser.scanner.nextToken(); // Consume "["
                Parser.expectedToken(Core.STRING);
                strKey = Parser.scanner.getString();
                Parser.scanner.nextToken();
                Parser.expectedToken(Core.RSQUARE);
                Parser.scanner.nextToken(); // Consume "]"
            }
        } else if (token == Core.CONST) {
            type = FactorType.CONST;
            constValue = Parser.scanner.getConst();
            Parser.scanner.nextToken();
        } else if (token == Core.LPAREN) {
            type = FactorType.EXPR;
            Parser.scanner.nextToken(); // Consume "("
            expr = new Expr();
            expr.parse();
            Parser.expectedToken(Core.RPAREN);
            Parser.scanner.nextToken(); // Consume ")"
        } else {
            Parser.expectedToken(Core.ID); // Error if none of the above
        }
    }

    void print(int indent) {
        switch (type) {
            case ID:
                System.out.print(id);
                break;
            case ID_ARRAY:
                System.out.print(id + "['" + strKey + "']");
                break;
            case CONST:
                System.out.print(constValue);
                break;
            case EXPR:
                System.out.print("(");
                expr.print(indent);
                System.out.print(")");
                break;
            default:
                throw new RuntimeException("Unknown factor type: " + type);
        }
    }

    void validate(SymbolTable symbols) {
        if (type == FactorType.ID || type == FactorType.ID_ARRAY) {
            symbols.getType(id); // Check if variable is declared
        } else if (type == FactorType.EXPR) {
            expr.validate(symbols); // Validate nested expression
        }
    }

    // Evaluates the factor and returns its computed value.
    int evaluate(MemoryManager memory) {
        if (type == FactorType.ID) {
            return memory.getValue(id);
        } else if (type == FactorType.ID_ARRAY) {
            int value = memory.getObjectValue(id, strKey);
            return value;
        } else if (type == FactorType.CONST) {
            return constValue;
        } else if (type == FactorType.EXPR) {
            return expr.evaluate(memory);
        }
        return 0; // Should not be reached.
    }

}
