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

    void validate(SymbolTable symbols) {
        condition.validate(symbols);

        symbols.enterScope(); // ✅ FIX: Create a scope for the THEN block
        thenStmts.validate(symbols);
        symbols.exitScope(); // ✅ FIX: Exit THEN block scope

        if (elseStmts != null) {
            symbols.enterScope(); // ✅ FIX: Create a scope for ELSE block
            elseStmts.validate(symbols);
            symbols.exitScope(); // ✅ FIX: Exit ELSE block scope
        }
    }
}
