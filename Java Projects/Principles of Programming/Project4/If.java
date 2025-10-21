public class If {
    Cond condition;
    StmtSeq thenStmts;
    StmtSeq elseStmts;

    void parse() {
        // Parse 'if'
        Parser.expectedToken(Core.IF);
        Parser.scanner.nextToken();

        // Parse condition
        condition = new Cond();
        condition.parse();

        // Parse 'then'
        Parser.expectedToken(Core.THEN);
        Parser.scanner.nextToken();

        // Parse 'then' statements
        thenStmts = new StmtSeq();
        thenStmts.parse();

        // Check for 'else'
        if (Parser.scanner.currentToken() == Core.ELSE) {
            Parser.scanner.nextToken();
            elseStmts = new StmtSeq();
            elseStmts.parse();
        }

        // Parse 'end'
        Parser.expectedToken(Core.END);
        Parser.scanner.nextToken();
    }

    void print(int indent) {
        // Print indentation
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        System.out.print("if ");
        condition.print(indent);
        System.out.println(" then");
        thenStmts.print(indent + 1);
        if (elseStmts != null) {
            for (int i = 0; i < indent; i++) {
                System.out.print("  ");
            }
            System.out.println("else");
            elseStmts.print(indent + 1);
        }
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
        System.out.println("end");
    }

    // Validates the if statement, including its condition and both statement
    // blocks.
    void validate(SymbolTable symbols) {
        condition.validate(symbols);

        // Create a new scope for the "then" block.
        symbols.enterScope();
        thenStmts.validate(symbols);
        symbols.exitScope();

        // If an "else" block exists, validate it in its own scope.
        if (elseStmts != null) {
            symbols.enterScope();
            elseStmts.validate(symbols);
            symbols.exitScope();
        }
    }

    // Executes the if statement by evaluating the condition and executing the
    // corresponding block.
    void execute(MemoryManager memory) {
        if (condition.evaluate(memory)) {
            memory.enterScope(); // New scope for "then" block.
            thenStmts.execute(memory);
            memory.exitScope();
        } else if (elseStmts != null) {
            memory.enterScope(); // New scope for "else" block.
            elseStmts.execute(memory);
            memory.exitScope();
        }
    }

}
