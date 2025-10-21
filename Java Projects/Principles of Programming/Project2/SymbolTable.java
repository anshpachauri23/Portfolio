import java.util.Stack;
import java.util.HashMap;

public class SymbolTable {
    private Stack<HashMap<String, String>> scopes;

    public SymbolTable() {
        scopes = new Stack<>();
        enterScope(); // Initialize with global scope
    }

    public void enterScope() {
        System.out.println("DEBUG: Entering new scope. Depth: " + scopes.size());
        scopes.push(new HashMap<>()); // New scope
    }

    public void exitScope() {
        if (scopes.size() > 1) {
            System.out.println("DEBUG: Exiting scope. Depth: " + (scopes.size() - 1));
            scopes.pop(); // Remove the latest scope
        } else {
            System.out.println("ERROR: Cannot exit the global scope.");
            System.exit(1);
        }
    }

    public void add(String id, String type) {
        HashMap<String, String> currentScope = scopes.peek();

        if (currentScope.containsKey(id)) {
            System.out.println("ERROR: Duplicate declaration of '" + id + "' in the same scope.");
            System.exit(1);
        }

        System.out.println("DEBUG: Adding '" + id + "' as " + type + " in scope level " + (scopes.size() - 1));
        currentScope.put(id, type);
    }

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

    public int getScopeDepth() {
        return scopes.size();
    }

    public String getTypeInScope(String id, int scopeLevel) {
        if (scopeLevel < scopes.size() && scopes.get(scopeLevel).containsKey(id)) {
            return scopes.get(scopeLevel).get(id);
        }
        return null; // ✅ If not found in this specific scope, return null
    }

}
