public class Term {
    private Factor factor; // Factor component
    private Term nextTerm; // Next term component (for * or /)
    private Core operator; // Operator (if any)

    void parse() {
        // Parse the factor
        factor = new Factor();
        factor.parse();

        // Check for additional factors with * or /
        if (Parser.scanner.currentToken() == Core.MULTIPLY || Parser.scanner.currentToken() == Core.DIVIDE) {
            operator = Parser.scanner.currentToken();
            Parser.scanner.nextToken();
            nextTerm = new Term();
            nextTerm.parse();
        }
    }

    // Prints the term.
    void print(int indent) {
        factor.print(indent);
        if (nextTerm != null) {
            System.out.print(" ");
            switch (operator) {
                case Core.MULTIPLY:
                    System.out.print("*");
                    break;
                case Core.DIVIDE:
                    System.out.print("/");
                    break;
                default:
                    throw new RuntimeException("Unknown operator: " + operator);
            }
            System.out.print(" ");
            nextTerm.print(indent);
        }
    }

    void validate(SymbolTable symbols) {
        factor.validate(symbols); // Validate the factor
        if (nextTerm != null) {
            nextTerm.validate(symbols); // Validate additional terms (for * or /)
        }
    }

    // Evaluates the term and returns its value.
    int evaluate(MemoryManager memory) {
        int value = factor.evaluate(memory);
        if (nextTerm != null) {
            int nextValue = nextTerm.evaluate(memory);
            if (operator == Core.MULTIPLY) {
                value *= nextValue;
            } else if (operator == Core.DIVIDE) {
                if (nextValue == 0) {
                    System.out.println("ERROR: Division by zero.");
                    System.exit(1);
                }
                value /= nextValue;
            }
        }
        return value;
    }

    

}
