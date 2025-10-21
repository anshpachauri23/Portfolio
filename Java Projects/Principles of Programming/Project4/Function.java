
 import java.util.HashMap;

 public class Function implements ProcDecl {
     String name;             // Function name
     Parameters parameters;   // Parsed parameter list
     DeclSeq ds;              // Optional local declaration sequence
     StmtSeq ss;              // Statement sequence (function body)
     static java.util.HashMap<String, Function> functionTable = new java.util.HashMap<>();
 
     // Not used directly since parsing is handled in DeclSeq.
     public void parse() {
         // This method is intentionally left empty.
     }
 
     // Parses the function body after the name and parameter list have been consumed.
     public void parseAfterName() {
         Parser.expectedToken(Core.IS);
         Parser.scanner.nextToken(); // Consume "is"
         
         // Optionally parse local declarations if present.
         if (Parser.scanner.currentToken() == Core.INTEGER || Parser.scanner.currentToken() == Core.OBJECT) {
             ds = new DeclSeq();
             ds.parse();
         }
         
         // Parse the function's statement sequence (body)
         ss = new StmtSeq();
         ss.parse();
         
         Parser.expectedToken(Core.END);
         Parser.scanner.nextToken(); // Consume "end"
         
         // Register the function in the function table
         functionTable.put(name, this);
     }
 
     // Prints the function declaration.
     public void print() {
         System.out.print("procedure " + name + "(");
         System.out.print("object ");
         parameters.print();
         System.out.println(") is");
         if (ds != null) {
             ds.print(1);
         }
         ss.print(1);
         System.out.println("end");
     }
 
     // Validates the function by checking its parameters, local declarations, and body.
     public void validate() {
         SymbolTable symbols = new SymbolTable();
         parameters.validate(symbols); // Add parameters to the symbol table
         if (ds != null) {
             ds.validate(symbols); // Validate local declarations
         }
         ss.validate(symbols); // Validate the function body
     }
 
     // Executes the function with the provided argument bindings using call-by-sharing.
     public void execute(MemoryManager memory, HashMap<String, String> argumentBindings) {
         memory.enterScope();
         // Bind each formal parameter to the corresponding actual argument
         for (String param : parameters.params) {
             String arg = argumentBindings.get(param);
             if (arg != null) {
                 memory.bindParameterAlias(param, arg);
             } else {
                 System.exit(1); // Error: missing argument
             }
         }
         if (ds != null) {
             ds.execute(memory);
         }
         ss.execute(memory);
         memory.exitScope();
     }
 }
 