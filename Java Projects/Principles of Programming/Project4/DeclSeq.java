 

 import java.util.HashSet;

 class DeclSeq {
     Decl dc;            // Variable declaration (integer or object)
     ProcDecl procDecl;  // Procedure or function declaration
     DeclSeq ds;         // Next declaration in the sequence
     static HashSet<String> procedureTable = new HashSet<>(); // Global set to track procedure names
 
     // Parses a declaration sequence.
     void parse() {
         Core currentToken = Parser.scanner.currentToken();
         
         if (currentToken == Core.INTEGER || currentToken == Core.OBJECT) {
             dc = new Decl();
             dc.parse();
         } else if (currentToken == Core.PROCEDURE) {
             Parser.scanner.nextToken(); // Consume "procedure"
             Parser.expectedToken(Core.ID);
             String name = Parser.scanner.getId();
             Parser.scanner.nextToken(); // Consume procedure name
 
             // Check for duplicate procedure names
             if (procedureTable.contains(name)) {
                 System.out.println("SEMANTIC ERROR: Duplicate procedure '" + name + "' is not allowed.");
                 System.exit(1);
             }
             procedureTable.add(name); // Add procedure name to the set
 
             if (Parser.scanner.currentToken() == Core.LPAREN) {
                 // Function declaration (procedure with parameters)
                 Function func = new Function();
                 func.name = name;
                 Parser.expectedToken(Core.LPAREN);
                 Parser.scanner.nextToken(); // Consume "("
                 Parser.expectedToken(Core.OBJECT);
                 Parser.scanner.nextToken(); // Consume "object"
                 func.parameters = new Parameters();
                 func.parameters.parse();
                 Parser.expectedToken(Core.RPAREN);
                 Parser.scanner.nextToken(); // Consume ")"
                 func.parseAfterName();
                 procDecl = func;
             } else {
                 // Procedure declaration (no parameters)
                 Procedure proc = new Procedure();
                 proc.name = name;
                 proc.parseAfterName();
                 procDecl = proc;
             }
         } else {
             System.out.println("ERROR: Expected a declaration or procedure, found " + currentToken);
             System.exit(1);
         }
         
         currentToken = Parser.scanner.currentToken();
         if (currentToken == Core.INTEGER || currentToken == Core.OBJECT || currentToken == Core.PROCEDURE) {
             ds = new DeclSeq();
             ds.parse();
         }
     }
 
     // Prints the declaration sequence.
     void print(int indent) {
         if (dc != null) dc.print(indent);
         if (procDecl != null) procDecl.print();
         if (ds != null) ds.print(indent);
     }
 
     // Validates the declarations.
     void validate(SymbolTable symbols) {
         if (dc != null) dc.validate(symbols);
         if (procDecl != null) procDecl.validate();
         if (ds != null) ds.validate(symbols);
     }
 
     // Executes variable declarations.
     void execute(MemoryManager memory) {
         if (dc != null) dc.execute(memory);
         if (ds != null) ds.execute(memory);
     }
 }
 