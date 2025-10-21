

 public class StmtSeq {
    Stmt sm;    // The first statement in the sequence
    StmtSeq ss; // The rest of the statement sequence

    // Parses a sequence of statements.
    void parse() {
        // If the next token is END, then there are no statements.
        if (Parser.scanner.currentToken() == Core.END) {
            System.out.println("ERROR: Procedure body missing (no stmt-seq).");
            System.exit(1);
        }

        // Parse the first statement.
        sm = new Stmt();
        sm.parse();

        // Check if the next token indicates another statement.
        Core currentToken = Parser.scanner.currentToken();
        if (isStartOfStmt(currentToken)) {
            ss = new StmtSeq();
            ss.parse();
        }
    }

    // Prints the sequence of statements with proper indentation.
    void print(int indent) {
        sm.print(indent);
        if (ss != null) {
            ss.print(indent);
        }
    }

    // Determines if a token is a valid beginning of a statement.
    private boolean isStartOfStmt(Core token) {
        return token == Core.ID || token == Core.IF || token == Core.FOR ||
               token == Core.PRINT || token == Core.READ ||
               token == Core.INTEGER || token == Core.OBJECT || token == Core.BEGIN;
    }

    // Validates the statement sequence.
    void validate(SymbolTable symbols) {
        sm.validate(symbols);
        if (ss != null) {
            ss.validate(symbols);
        }
    }

    // Executes the statement sequence.
    void execute(MemoryManager memory) {
        sm.execute(memory);
        if (ss != null) {
            ss.execute(memory);
        }
    }
}
