public class Loop {
    private String loopVar; // Loop variable (e.g., "i")
    private Expr initExpr; // Initial value (e.g., "0")
    private Cond cond; // Condition (e.g., "i < 10")
    private Expr updateExpr; // Update expression (e.g., "i = i + 1")
    private StmtSeq body; // Loop body

    void parse() {
        // Parse "for"
        Parser.expectedToken(Core.FOR);
        Parser.scanner.nextToken();

        // Parse "("
        Parser.expectedToken(Core.LPAREN);
        Parser.scanner.nextToken();

        // Parse loop variable (ID)
        Parser.expectedToken(Core.ID);
        loopVar = Parser.scanner.getId();
        Parser.scanner.nextToken();

        // Parse "="
        Parser.expectedToken(Core.ASSIGN);
        Parser.scanner.nextToken();

        // Parse initial expression
        initExpr = new Expr();
        initExpr.parse();

        // Parse ";"
        Parser.expectedToken(Core.SEMICOLON);
        Parser.scanner.nextToken();

        // Parse condition
        cond = new Cond();
        cond.parse();

        // Parse ";"
        Parser.expectedToken(Core.SEMICOLON);
        Parser.scanner.nextToken();

        // Parse update expression
        updateExpr = new Expr();
        updateExpr.parse();

        // Parse ")"
        Parser.expectedToken(Core.RPAREN);
        Parser.scanner.nextToken();

        // Parse "do"
        Parser.expectedToken(Core.DO);
        Parser.scanner.nextToken();

        // Parse loop body (stmt-seq)
        body = new StmtSeq();
        body.parse();

        // Parse "end"
        Parser.expectedToken(Core.END);
        Parser.scanner.nextToken();
    }

    void print(int indent) {
        // Print indentation
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        // Print loop header
        System.out.print("for (" + loopVar + " = ");
        initExpr.print(0);
        System.out.print("; ");
        cond.print(0);
        System.out.print("; ");
        updateExpr.print(0);
        System.out.println(") do");

        // Print loop body with increased indentation
        body.print(indent + 1);

        // Print "end" with original indentation
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
        System.out.println("end");
    }

    // Validates the loop components against the symbol table.
    void validate(SymbolTable symbols) {
        String loopVarType = null;

        // Search for the loop variable in outer scopes.
        for (int i = 0; i < symbols.getScopeDepth(); i++) {
            loopVarType = symbols.getTypeInScope(loopVar, i);
            if (loopVarType != null) {
                break;
            }
        }
        // If the loop variable was not found, report an error.
        if (loopVarType == null) {
            System.out.println("ERROR: Undeclared loop variable " + loopVar);
            System.exit(1);
        }
        // Ensure the loop variable is of a valid type (integer or object).
        if (!("integer".equals(loopVarType) || "object".equals(loopVarType))) {
            System.out.println("ERROR: Loop variable " + loopVar + " must be an integer or an object");
            System.exit(1);
        }

        // Validate the initial expression, condition, and update expression.
        initExpr.validate(symbols);
        cond.validate(symbols);
        updateExpr.validate(symbols);

        // Create a new scope for the loop body.
        symbols.enterScope();
        body.validate(symbols);
        symbols.exitScope();
    }

    // Executes the loop by repeatedly evaluating its condition and body.
    void execute(MemoryManager memory) {
        // Initialize the loop variable.
        int loopValue = initExpr.evaluate(memory);
        if (memory.isObject(loopVar)) {
            memory.updateObject(loopVar, "default", loopValue);
        } else {
            memory.assignValue(loopVar, loopValue);
        }

        // Execute the loop as long as the condition is true.
        while (cond.evaluate(memory)) {
            // Execute the loop body.
            body.execute(memory);

            // Remove any inner redeclaration of the loop variable.
            memory.removeLocalVariable(loopVar);

            // Update the loop variable.
            loopValue = updateExpr.evaluate(memory);
            if (memory.isObject(loopVar)) {
                memory.updateObject(loopVar, "default", loopValue);
            } else {
                memory.assignValue(loopVar, loopValue);
            }
        }
    }

}
