
class Procedure {

    String name;
    DeclSeq ds;
    StmtSeq ss;

    void parse() {
        Parser.expectedToken(Core.PROCEDURE);
        Parser.scanner.nextToken();
        Parser.expectedToken(Core.ID);
        name = Parser.scanner.getId();
        Parser.scanner.nextToken();
        Parser.expectedToken(Core.IS);
        Parser.scanner.nextToken();
        if (Parser.scanner.currentToken() != Core.BEGIN) {
            ds = new DeclSeq();
            ds.parse();
        }
        Parser.expectedToken(Core.BEGIN);
        Parser.scanner.nextToken();
        ss = new StmtSeq();
        ss.parse();
        Parser.expectedToken(Core.END);
        Parser.scanner.nextToken();
        Parser.expectedToken(Core.EOS);
    }

    void print() {
        System.out.println("procedure " + name + " is");
        if (ds != null) {
            ds.print(1); // Start with indentation level 1 for declarations
        }
        System.out.println("begin");
        ss.print(1); // Start with indentation level 1 for statements
        System.out.println("end");
    }

    // Validates the procedure using a symbol table.
    void validate() {
        SymbolTable symbols = new SymbolTable();
        symbols.enterScope(); // Create a new scope at the procedure level.

        if (ds != null) {
            ds.validate(symbols);
        }

        symbols.enterScope(); // Create a scope for the 'begin' block.
        ss.validate(symbols);
        symbols.exitScope(); // Exit the 'begin' block scope.

        symbols.exitScope(); // Exit the procedure-level scope.
    }

    // Executes the procedure.
    void execute(MemoryManager memory) {
        memory.enterScope(); // Start execution in a new scope.

        if (ds != null) {
            ds.execute(memory); // Execute declarations.
        }
        ss.execute(memory); // Execute statements.

        memory.exitScope(); // End procedure scope.
    }

}