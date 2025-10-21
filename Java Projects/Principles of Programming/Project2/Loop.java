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

    void validate(SymbolTable symbols) {
        String loopVarType = null;

        // ✅ FIX: Find the first valid declaration in an **outer scope** (skipping inner
        // redeclarations)
        for (int i = 0; i < symbols.getScopeDepth(); i++) { // Start from **outermost** scope (0)
            loopVarType = symbols.getTypeInScope(loopVar, i);
            if (loopVarType != null) {
                break; // ✅ Stop once we find the **first valid** declaration
            }
        }

        // ✅ If we still couldn't find `loopVar`, report undeclared error
        if (loopVarType == null) {
            System.out.println("ERROR: Undeclared loop variable " + loopVar);
            System.exit(1);
        }

        // ✅ If the **first valid declaration** is not an integer, report error
        if (!"integer".equals(loopVarType)) {
            System.out.println("ERROR: Loop variable " + loopVar + " must be an integer");
            System.exit(1);
        }

        initExpr.validate(symbols);
        cond.validate(symbols);
        updateExpr.validate(symbols);

        symbols.enterScope(); // New scope for loop body
        body.validate(symbols);
        symbols.exitScope();
    }

}
