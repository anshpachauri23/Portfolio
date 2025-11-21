import java.util.Stack;
import java.util.HashMap;

public class SymbolTable {
    // Manages variable declarations and their types across multiple scopes.
    private Stack<HashMap<String, String>> scopes;

    // Constructor: Initializes the symbol table with a global scope.
    public SymbolTable() {
        scopes = new Stack<>();
        enterScope(); // Begin with the global scope.
    }

    // Enters a new scope by pushing a new hash map onto the stack.
    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    // Exits the current scope by popping the top hash map off the stack.
    public void exitScope() {
        if (scopes.size() > 1) {
            scopes.pop();
        } else {
            System.out.println("ERROR: Cannot exit the global scope.");
            System.exit(1);
        }
    }

    // Adds a new identifier and its type to the current scope.
    public void add(String id, String type) {
        HashMap<String, String> currentScope = scopes.peek();

        if (currentScope.containsKey(id)) {
            System.out.println("ERROR: Duplicate declaration of '" + id + "' in the same scope.");
            System.exit(1);
        }
        currentScope.put(id, type);
    }

    // Retrieves the type of an identifier by searching from the innermost to
    // outermost scope.
    public String getType(String id) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(id)) {
                return scopes.get(i).get(id);
            }
        }
        System.out.println("ERROR: Undeclared variable '" + id + "'.");
        System.exit(1);
        return null;
    }

    // Returns the number of scopes currently in the symbol table.
    public int getScopeDepth() {
        return scopes.size();
    }

    // Retrieves the type of an identifier from a specific scope level, or returns
    // null if not found.
    public String getTypeInScope(String id, int scopeLevel) {
        if (scopeLevel < scopes.size() && scopes.get(scopeLevel).containsKey(id)) {
            return scopes.get(scopeLevel).get(id);
        }
        return null;
    }
}
