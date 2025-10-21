class DeclSeq {
    Decl dc;
    DeclSeq ds;

    void parse() {
        dc = new Decl();
        dc.parse();
        // Check if next token starts a new <decl> (integer/object).
        if (Parser.scanner.currentToken() == Core.INTEGER ||
                Parser.scanner.currentToken() == Core.OBJECT) {
            ds = new DeclSeq();
            ds.parse(); // Parse the next <decl-seq>.
        }
    }

    void print(int indent) {
        dc.print(indent);
        if (ds != null) {
            ds.print(indent);
        }
    }

    void validate(SymbolTable symbols) {
        dc.validate(symbols); // Validate the first declaration
        if (ds != null) {
            ds.validate(symbols); // Validate subsequent declarations
        }
    }

    // Executes each declaration in the sequence.
    void execute(MemoryManager memory) {
        dc.execute(memory);
        if (ds != null) {
            ds.execute(memory);
        }
    }

}
