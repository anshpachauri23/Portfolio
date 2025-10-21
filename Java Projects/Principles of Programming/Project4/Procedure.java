
 import java.util.HashMap;

 public class Procedure implements ProcDecl {
     String name;                     // Procedure name
     DeclSeq ds;                      // Optional declaration sequence (local declarations)
     StmtSeq ss;                      // Statement sequence (procedure body)
     java.util.ArrayList<String> parameters; // Empty for procedures
     static java.util.HashMap<String, Procedure> procedureTable = new java.util.HashMap<>();
 
     // Parses a procedure declaration.
     public void parse() {
         Parser.expectedToken(Core.PROCEDURE);
         Parser.scanner.nextToken();
 
         Parser.expectedToken(Core.ID);
         name = Parser.scanner.getId();
         Parser.scanner.nextToken();
 
         // Check for duplicate procedure names.
         if (procedureTable.containsKey(name)) {
             System.out.println("ERROR: Duplicate procedure '" + name + "' is not allowed.");
             System.exit(1);
         }
 
         // Procedures should not have a parameter list.
         if (Parser.scanner.currentToken() == Core.LPAREN) {
             System.out.println("ERROR: Procedure '" + name + "' should not have a parameter list.");
             System.exit(1);
         }
 
         parseAfterName();
     }
 
     // Parses the remainder of the procedure declaration after the name.
     public void parseAfterName() {
         parameters = new java.util.ArrayList<>();
 
         Parser.expectedToken(Core.IS);
         Parser.scanner.nextToken();
 
         // Optionally parse local declarations if they exist.
         if (Parser.scanner.currentToken() != Core.BEGIN) {
             ds = new DeclSeq();
             ds.parse();
         }
 
         Parser.expectedToken(Core.BEGIN);
         Parser.scanner.nextToken();
 
         // Ensure that there is a statement sequence in the procedure body.
         if (Parser.scanner.currentToken() == Core.END) {
             System.out.println("ERROR: Procedure body missing (no stmt-seq).");
             System.exit(1);
         }
 
         ss = new StmtSeq();
         ss.parse();
 
         Parser.expectedToken(Core.END);
         Parser.scanner.nextToken();
 
         // Register this procedure in the procedure table.
         procedureTable.put(name, this);
     }
 
     // Prints the procedure declaration.
     public void print() {
         System.out.print("procedure " + name + " is");
         if (ds != null) ds.print(1);
         System.out.println("begin");
         ss.print(1);
         System.out.println("end");
     }
 
     // Validates the procedure by checking local declarations and the statement sequence.
     public void validate() {
         SymbolTable symbols = new SymbolTable();
         if (ds != null) ds.validate(symbols);
         symbols.enterScope();
         ss.validate(symbols);
         symbols.exitScope();
     }
 
     // Executes the procedure.
     public void execute(MemoryManager memory, HashMap<String, String> argumentBindings) {
         memory.enterScope();
         if (ds != null) ds.execute(memory);
         ss.execute(memory);
         memory.exitScope();
     }
 }
 