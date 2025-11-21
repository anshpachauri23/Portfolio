 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Call {
     private String calledName;         // Name of the procedure or function being called
     private ArrayList<String> arguments; // List of argument variable names
 
     // Parses a call statement: begin <ID>(<arguments>);
     void parse() {
         // Parse the "begin" keyword
         Parser.expectedToken(Core.BEGIN);
         Parser.scanner.nextToken();
         
         // Parse the procedure/function name
         Parser.expectedToken(Core.ID);
         calledName = Parser.scanner.getId();
         Parser.scanner.nextToken();
 
         // Parse the parameter list enclosed in parentheses
         Parser.expectedToken(Core.LPAREN);
         Parser.scanner.nextToken();
         arguments = new ArrayList<>();
 
         // Parse arguments if present (argument list may be empty)
         if (Parser.scanner.currentToken() != Core.RPAREN) {
             while (Parser.scanner.currentToken() == Core.ID) {
                 arguments.add(Parser.scanner.getId());
                 Parser.scanner.nextToken();
                 if (Parser.scanner.currentToken() == Core.COMMA) {
                     Parser.scanner.nextToken();
                 } else {
                     break;
                 }
             }
         }
         Parser.expectedToken(Core.RPAREN);
         Parser.scanner.nextToken();
 
         // Consume the trailing semicolon
         Parser.expectedToken(Core.SEMICOLON);
         Parser.scanner.nextToken();
     }
 
     // Prints the call statement with proper indentation.
     void print(int indent) {
         for (int i = 0; i < indent; i++) {
             System.out.print("  ");
         }
         System.out.print("begin " + calledName + "(");
         for (int i = 0; i < arguments.size(); i++) {
             System.out.print(arguments.get(i));
             if (i < arguments.size() - 1)
                 System.out.print(", ");
         }
         System.out.println(");");
     }
 
     // Validates the call by checking the existence of the procedure or function and the correctness of parameters.
     void validate(SymbolTable symbols) {
         if (arguments.size() > 0) {
             // Function call expected
             if (!Function.functionTable.containsKey(calledName)) {
                 System.out.println("ERROR: Calling an undefined procedure or function: '" + calledName + "'.");
                 System.exit(1);
             }
 
             Function func = Function.functionTable.get(calledName);
             if (arguments.size() != func.parameters.params.size()) {
                 System.out.println("ERROR: Function '" + calledName + "' expects " + 
                     func.parameters.params.size() + " arguments, but got " + arguments.size() + ".");
                 System.exit(1);
             }
             for (String arg : arguments) {
                 String type = symbols.getType(arg);
                 if (!"object".equals(type)) {
                     System.out.println("ERROR: Argument '" + arg + "' in function call must be an object.");
                     System.exit(1);
                 }
             }
         } else {
             // Procedure call (no parameters expected)
             if (!Procedure.procedureTable.containsKey(calledName)) {
                 System.out.println("ERROR: Calling an undefined procedure or function: '" + calledName + "'.");
                 System.exit(1);
             }
 
             Procedure proc = Procedure.procedureTable.get(calledName);
             if (!proc.parameters.isEmpty()) {
                 System.out.println("ERROR: Procedure '" + calledName + "' cannot be called without parameters.");
                 System.exit(1);
             }
         }
     }
 
     // Executes the call by dispatching to the corresponding function or procedure.
     void execute(MemoryManager memory) {
         if (arguments.size() > 0) {
             // Function call: bind arguments to formal parameters and execute the function.
             Function func = Function.functionTable.get(calledName);
             HashMap<String, String> binding = new HashMap<>();
             for (int i = 0; i < arguments.size(); i++) {
                 String paramName = func.parameters.params.get(i);
                 String argName = arguments.get(i);
                 binding.put(paramName, argName);
             }
             func.execute(memory, binding);
         } else {
             // Procedure call: execute with an empty argument binding.
             Procedure proc = Procedure.procedureTable.get(calledName);
             HashMap<String, String> emptyBinding = new HashMap<>();
             proc.execute(memory, emptyBinding);
         }
     }
 }
 