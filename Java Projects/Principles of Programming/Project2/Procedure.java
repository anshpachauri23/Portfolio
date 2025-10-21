
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

    void validate() {
        SymbolTable symbols = new SymbolTable();
        symbols.enterScope(); // ✅ FIX: Create a new scope at procedure level

        if (ds != null) {
            ds.validate(symbols);
        }

        symbols.enterScope(); // ✅ FIX: Create a scope for 'begin' block
        ss.validate(symbols);
        symbols.exitScope(); // ✅ FIX: Exit 'begin' block scope

        symbols.exitScope(); // ✅ FIX: Exit procedure-level scope
    }

}