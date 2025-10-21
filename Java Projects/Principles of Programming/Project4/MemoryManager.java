
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.Scanner;
 import java.util.Stack;
 
 class MemoryManager {
     // Stack for local variable scopes (maps variable names to their integer values)
     private Stack<HashMap<String, Integer>> localMemory;
     // Global variables
     private HashMap<String, Integer> globalMemory;
     // Heap memory for objects (each object is a map from keys to integer values)
     private HashMap<String, HashMap<String, Integer>> heapMemory;
     // Scanner for reading integer input from the data file
     private Scanner dataScanner;
     // Stack for alias mappings (used for call-by-sharing)
     private Stack<HashMap<String, String>> aliasStack;
 
     // Constructor: Initializes memory structures and opens the data file.
     public MemoryManager(String dataFile) {
         localMemory = new Stack<>();
         globalMemory = new HashMap<>();
         heapMemory = new HashMap<>();
         aliasStack = new Stack<>();
         try {
             dataScanner = new Scanner(new File(dataFile));
         } catch (FileNotFoundException e) {
             System.out.println("ERROR: Unable to open data file '" + dataFile + "'.");
             System.exit(1);
         }
     }
 
     // Enters a new local scope and updates the alias stack.
     public void enterScope() {
         localMemory.push(new HashMap<>());
         // Copy the current alias mapping if available; otherwise, start with an empty map.
         if (aliasStack.isEmpty()) {
             aliasStack.push(new HashMap<>());
         } else {
             aliasStack.push(new HashMap<>(aliasStack.peek()));
         }
     }
 
     // Exits the current local scope and removes the corresponding alias mapping.
     public void exitScope() {
         if (!localMemory.isEmpty()) {
             localMemory.pop();
             aliasStack.pop();
         } else {
             System.out.println("ERROR: Trying to exit global scope!");
             System.exit(1);
         }
     }
 
     // Declares a new variable, initializing it to 0.
     public void declareVariable(String name, boolean isGlobal) {
         if (isGlobal || localMemory.isEmpty()) {
             globalMemory.put(name, 0);
         } else {
             localMemory.peek().put(name, 0);
         }
     }
 
     // Assigns a value to a variable by searching local scopes first.
     public void assignValue(String name, int value) {
         String actual = resolveAlias(name);
         // Search local scopes from innermost to outermost.
         for (int i = localMemory.size() - 1; i >= 0; i--) {
             if (localMemory.get(i).containsKey(name)) {
                 localMemory.get(i).put(name, value);
                 return;
             }
         }
         if (globalMemory.containsKey(name)) {
             globalMemory.put(name, value);
             return;
         }
         // If the resolved name is an object, this is an error for simple assignment.
         if (heapMemory.containsKey(actual)) {
             System.out.println("ERROR: Cannot assign an integer directly to object '" + actual + "'");
             System.exit(1);
         }
         System.out.println("ERROR: Variable '" + name + "' not declared.");
         System.exit(1);
     }
 
     // Retrieves the value of a variable, checking aliases and scopes.
     public int getValue(String name) {
         if (!aliasStack.isEmpty() && aliasStack.peek().containsKey(name)) {
             String actual = aliasStack.peek().get(name);
             return getValue(actual);  // Resolve alias recursively.
         }
         // Search local scopes from innermost to outermost.
         for (int i = localMemory.size() - 1; i >= 0; i--) {
             if (localMemory.get(i).containsKey(name)) {
                 return localMemory.get(i).get(name);
             }
         }
         if (globalMemory.containsKey(name)) {
             return globalMemory.get(name);
         }
         // If the variable is an object, return its default value.
         if (heapMemory.containsKey(name)) {
             return getObjectValue(name, "default");
         }
         System.out.println("ERROR: Variable '" + name + "' not declared.");
         System.exit(1);
         return -1; // Unreachable.
     }
 
     // Creates a new object with a default key initialized to a given value.
     public void createObject(String objName, String key, int value) {
         HashMap<String, Integer> newObject = new HashMap<>();
         newObject.put("default", value);
         heapMemory.put(objName, newObject);
     }
 
     // Updates an existing object by setting a key to a new value.
     public void updateObject(String name, String key, int value) {
         String actual = resolveAlias(name);
         if (heapMemory.containsKey(actual)) {
             HashMap<String, Integer> object = heapMemory.get(actual);
             if (object == null) {
                 System.out.println("ERROR: Assignment to null object variable '" + actual + "'");
                 System.exit(1);
             }
             object.put(key, value);
             heapMemory.put(actual, object);
         } else {
             System.out.println("ERROR: Object '" + actual + "' does not exist in heapMemory.");
             System.exit(1);
         }
     }
 
     // Retrieves the value associated with a key in an object.
     public int getObjectValue(String objName, String key) {
         if (!aliasStack.isEmpty() && aliasStack.peek().containsKey(objName)) {
             String actual = aliasStack.peek().get(objName);
             return getObjectValue(actual, key);
         }
         if (!heapMemory.containsKey(objName)) {
             System.out.println("ERROR: Object '" + objName + "' not found.");
             System.exit(1);
         }
         HashMap<String, Integer> object = heapMemory.get(objName);
         if (object == null) {
             System.out.println("ERROR: Assignment to null object variable '" + objName + "'");
             System.exit(1);
         }
         if (!object.containsKey(key)) {
             System.out.println("ERROR: Key '" + key + "' not found in object '" + objName + "'.");
             System.exit(1);
         }
         return object.get(key);
     }
 
     // Reads the next integer from the data file.
     public int readInput() {
         if (dataScanner.hasNextInt()) {
             return dataScanner.nextInt();
         } else {
             System.out.println("ERROR: No more data available for reading.");
             System.exit(1);
             return -1; // Unreachable.
         }
     }
 
     // Checks if the current scope is global.
     public boolean isGlobalScope() {
         return localMemory.isEmpty();
     }
 
     // Determines if a variable name corresponds to an object, accounting for aliasing.
     public boolean isObject(String name) {
         String actual = resolveAlias(name);
         // If the name exists as a local variable, it's not an object.
         for (int i = localMemory.size() - 1; i >= 0; i--) {
             if (localMemory.get(i).containsKey(name)) {
                 return false;
             }
         }
         if (globalMemory.containsKey(name)) {
             return false;
         }
         return heapMemory.containsKey(actual);
     }
 
     // Prints the current state of global, local, and heap memory (for debugging).
     public void printMemoryState() {
         System.out.println("Global Memory: " + globalMemory);
         System.out.println("Local Memory: " + localMemory);
         System.out.println("Heap Memory: " + heapMemory);
     }
 
     public HashMap<String, HashMap<String, Integer>> getHeapMemory() {
         return heapMemory;
     }
 
     // Sets one object to reference the same underlying object as another.
     public void setObjectReference(String target, String source) {
         if (heapMemory.containsKey(source)) {
             heapMemory.put(target, heapMemory.get(source));
         } else {
             System.out.println("ERROR: Cannot reference undeclared object '" + source + "'");
             System.exit(1);
         }
     }
 
     // Removes a variable from the current local scope.
     public void removeLocalVariable(String name) {
         if (!localMemory.isEmpty() && localMemory.peek().containsKey(name)) {
             localMemory.peek().remove(name);
         }
     }
 
     // Declares a new object variable. Initially uninitialized (null).
     public void declareObject(String name, boolean isGlobal) {
         heapMemory.put(name, null);
     }
 
     // Binds a formal parameter to an actual argument using aliasing.
     public void bindParameterAlias(String formal, String actual) {
         // If the actual argument is an alias, resolve it to the original object name.
         if (!aliasStack.isEmpty() && aliasStack.peek().containsKey(actual)) {
             actual = aliasStack.peek().get(actual);
         }
         if (heapMemory.containsKey(actual)) {
             aliasStack.peek().put(formal, actual);
         } else {
             System.out.println("ERROR: Cannot pass undeclared object '" + actual + "' as parameter.");
             System.exit(1);
         }
     }
      
     // Helper method to resolve a name if it is an alias.
     private String resolveAlias(String name) {
         if (!aliasStack.isEmpty() && aliasStack.peek().containsKey(name)) {
             return aliasStack.peek().get(name);
         }
         return name;
     }
 
     // Returns true if 'name' is bound as a formal parameter alias.
     public boolean isFormalAlias(String name) {
         return (!aliasStack.isEmpty() && aliasStack.peek().containsKey(name));
     }
 
     // Rebinds a formal parameter to a new object with a default value.
     // This simulates creating a new local binding for a reassigned formal parameter.
     public void rebindFormal(String formal, int value) {
         // Create a new unique name for the binding.
         String newName = "formal_" + formal + "_" + System.nanoTime();
         // Update the alias mapping so that the formal parameter now refers to the new object.
         aliasStack.peek().put(formal, newName);
         // Create a new object with the given default value.
         HashMap<String, Integer> newObj = new HashMap<>();
         newObj.put("default", value);
         heapMemory.put(newName, newObj);
     }
 
 }
 