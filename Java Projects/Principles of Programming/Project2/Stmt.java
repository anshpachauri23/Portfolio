public class Stmt {
    Assign asn;
    If ifsm;
    Loop lp;
    Print pt;
    Read rd;
    Decl dc;

    void parse() {
        Core token = Parser.scanner.currentToken();
        if (token == Core.ID) {
            asn = new Assign();
            asn.parse();
        } else if (token == Core.IF) {
            ifsm = new If();
            ifsm.parse();
        } else if (token == Core.FOR) {
            lp = new Loop();
            lp.parse();
        } else if (token == Core.PRINT) {
            pt = new Print();
            pt.parse();
        } else if (token == Core.READ) {
            rd = new Read();
            rd.parse();
        } else if (token == Core.INTEGER || token == Core.OBJECT) {
            dc = new Decl();
            dc.parse();
        } else {
            System.out.println("ERROR: Unexpected token " + token + " in statement sequence");
            System.exit(1);
        }
    }

    void print(int indent) {
        if (asn != null) {
            asn.print(indent);
        } else if (ifsm != null) {
            ifsm.print(indent);
        } else if (lp != null) {
            lp.print(indent);
        } else if (pt != null) {
            pt.print(indent);
        } else if (rd != null) {
            rd.print(indent);
        } else if (dc != null) {
            dc.print(indent);
        }
    }

    void validate(SymbolTable symbols) {
        if (asn != null) {
            asn.validate(symbols);
        } else if (ifsm != null) {
            symbols.enterScope(); // ✅ NEW: Create a scope before validating IF block
            ifsm.validate(symbols);
            symbols.exitScope(); // ✅ NEW: Exit scope after IF block
        } else if (lp != null) {
            symbols.enterScope(); // ✅ NEW: Create a scope before validating FOR loop
            lp.validate(symbols);
            symbols.exitScope(); // ✅ NEW: Exit scope after loop
        } else if (pt != null) {
            pt.validate(symbols);
        } else if (rd != null) {
            rd.validate(symbols);
        } else if (dc != null) {
            dc.validate(symbols);
        }
    }
}
