public class StmtSeq {
    Stmt sm;
    StmtSeq ss;

    void parse() {
        // Parse the first statement
        sm = new Stmt();
        sm.parse();

        // Check if the next token is the start of another statement
        Core currentToken = Parser.scanner.currentToken();
        if (isStartOfStmt(currentToken)) {
            ss = new StmtSeq();
            ss.parse();
        }
    }

    void print(int indent) {
        sm.print(indent);
        if (ss != null) {
            ss.print(indent);
        }
    }

    // Helper: Check if a token starts a statement
    private boolean isStartOfStmt(Core token) {
        return token == Core.ID || token == Core.IF || token == Core.FOR ||
                token == Core.PRINT || token == Core.READ ||
                token == Core.INTEGER || token == Core.OBJECT;
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
