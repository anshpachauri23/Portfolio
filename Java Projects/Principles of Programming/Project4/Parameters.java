

 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.HashMap;
 
 public class Parameters {
     ArrayList<String> params;
 
     public Parameters() {
         params = new ArrayList<>();
     }
 
     // Parses the parameter list.
     // Grammar: <parameters> ::= ID | ID , <parameters>
     public void parse() {
         // Use a HashSet to track duplicate parameter names.
         HashSet<String> paramSet = new HashSet<>();
 
         // Expect the first parameter (identifier).
         Parser.expectedToken(Core.ID);
         String param = Parser.scanner.getId();
 
         // Check for duplicate parameters.
         if (paramSet.contains(param)) {
             System.out.println("ERROR: Duplicate parameter '" + param + "' in function definition.");
             System.exit(1);
         }
 
         paramSet.add(param);
         params.add(param);
         Parser.scanner.nextToken();
 
         // Parse additional parameters if present.
         while (Parser.scanner.currentToken() == Core.COMMA) {
             Parser.scanner.nextToken();  // Consume comma
             Parser.expectedToken(Core.ID); // Expect next parameter
             param = Parser.scanner.getId();
 
             if (paramSet.contains(param)) {
                 System.out.println("ERROR: Duplicate parameter '" + param + "' in function definition.");
                 System.exit(1);
             }
 
             paramSet.add(param);
             params.add(param);
             Parser.scanner.nextToken();
         }
     }
 
     // Prints the parameter list.
     public void print() {
         for (int i = 0; i < params.size(); i++) {
             System.out.print(params.get(i));
             if (i < params.size() - 1) {
                 System.out.print(", ");
             }
         }
     }
 
     // Validates parameters by adding each as an object variable to the symbol table.
     public void validate(SymbolTable symbols) {
         for (String param : params) {
             symbols.addParameter(param);
         }
     }
 
     // Binds parameters to actual arguments using call-by-sharing.
     public void bind(MemoryManager memory, HashMap<String, String> argumentBindings) {
         for (String param : params) {
             String arg = argumentBindings.get(param);
             if (arg != null) {
                 memory.bindParameterAlias(param, arg);
             } else {
                 System.out.println("ERROR: No argument provided for parameter " + param);
                 System.exit(1);
             }
         }
     }
 }
 