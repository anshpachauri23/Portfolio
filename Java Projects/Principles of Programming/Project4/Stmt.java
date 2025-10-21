

 public class Stmt {
    Assign asn;  // Assignment statement
    If ifsm;    // If statement
    Loop lp;    // Loop statement
    Print pt;   // Print statement
    Read rd;    // Read statement
    Decl dc;    // Declaration statement
    Call call;  // Procedure call statement

    // Parses a statement based on the current token.
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
        } else if (token == Core.BEGIN) { // Procedure call
            call = new Call();
            call.parse();
        } else {
            System.out.println("ERROR: Unexpected token " + token + " in statement.");
            System.exit(1);
        }
    }

    // Prints the statement.
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
        } else if (call != null) {
            call.print(indent);
        }
    }

    // Validates the statement.
    // Note: Compound statements (if, loop) handle their own scopes.
    void validate(SymbolTable symbols) {
        if (asn != null) {
            asn.validate(symbols);
        } else if (ifsm != null) {
            ifsm.validate(symbols);
        } else if (lp != null) {
            lp.validate(symbols);
        } else if (pt != null) {
            pt.validate(symbols);
        } else if (rd != null) {
            rd.validate(symbols);
        } else if (dc != null) {
            dc.validate(symbols);
        } else if (call != null) {
            call.validate(symbols);
        }
    }

    // Executes the statement.
    void execute(MemoryManager memory) {
        if (asn != null) {
            asn.execute(memory);
        } else if (ifsm != null) {
            ifsm.execute(memory);
        } else if (lp != null) {
            lp.execute(memory);
        } else if (pt != null) {
            pt.execute(memory);
        } else if (rd != null) {
            rd.execute(memory);
        } else if (dc != null) {
            dc.execute(memory);
        } else if (call != null) {
            call.execute(memory);
        }
    }
}
