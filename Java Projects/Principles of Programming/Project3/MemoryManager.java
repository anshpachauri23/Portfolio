import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

class MemoryManager {
    // Stores local variables for nested scopes.
    private Stack<HashMap<String, Integer>> localMemory;
    // Stores global variables.
    private HashMap<String, Integer> globalMemory;
    // Stores objects (heap), where each object is a map from keys to integer
    // values.
    private HashMap<String, HashMap<String, Integer>> heapMemory;
    // Scanner for reading integer input from the .data file.
    private Scanner dataScanner;

    // Constructor: Initializes memory and opens the .data file.
    public MemoryManager(String dataFile) {
        localMemory = new Stack<>();
        globalMemory = new HashMap<>();
        heapMemory = new HashMap<>();

        try {
            dataScanner = new Scanner(new File(dataFile));
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Unable to open data file '" + dataFile + "'.");
            System.exit(1);
        }
    }

    // Enters a new local scope.
    public void enterScope() {
        localMemory.push(new HashMap<>());
    }

    // Exits the current local scope.
    public void exitScope() {
        if (!localMemory.isEmpty()) {
            localMemory.pop();
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

    // Assigns a value to a variable, searching local scopes first.
    public void assignValue(String name, int value) {
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
        // If the variable is an object, do not allow direct integer assignment.
        if (heapMemory.containsKey(name)) {
            System.out.println("ERROR: Cannot assign an integer directly to object '" + name + "'");
            System.exit(1);
        }
        System.out.println("ERROR: Variable '" + name + "' not declared.");
        System.exit(1);
    }

    // Retrieves the value of a variable.
    public int getValue(String name) {
        for (int i = localMemory.size() - 1; i >= 0; i--) {
            if (localMemory.get(i).containsKey(name)) {
                return localMemory.get(i).get(name);
            }
        }
        if (globalMemory.containsKey(name)) {
            return globalMemory.get(name);
        }
        // If the variable is an object, return its default key value.
        if (heapMemory.containsKey(name)) {
            return getObjectValue(name, "default");
        }
        System.out.println("ERROR: Variable '" + name + "' not found.");
        System.exit(1);
        return -1; // Unreachable.
    }

    // Creates a new object with a default key.
    public void createObject(String objName, String key, int value) {
        HashMap<String, Integer> newObject = new HashMap<>();
        newObject.put("default", value);
        heapMemory.put(objName, newObject);
    }

    // Updates an existing object with a new key-value pair.
    public void updateObject(String objName, String key, int value) {
        if (heapMemory.containsKey(objName)) {
            HashMap<String, Integer> object = heapMemory.get(objName);
            if (object == null) {
                System.out.println("ERROR: Assignment to null object variable '" + objName + "'");
                System.exit(1);
            }
            object.put(key, value); // Update or add the key-value pair
            heapMemory.put(objName, object); // Save back the updated object
        } else {
            System.out.println("ERROR: Object '" + objName + "' does not exist in heapMemory.");
            System.exit(1);
        }
    }

    // Retrieves the value associated with a key in an object.
    public int getObjectValue(String objName, String key) {
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

    // Reads the next integer from the .data file.
    public int readInput() {
        if (dataScanner.hasNextInt()) {
            return dataScanner.nextInt();
        } else {
            System.out.println("ERROR: No more data available for reading.");
            System.exit(1);
            return -1; // Unreachable.
        }
    }

    // Checks if we are in the global scope.
    public boolean isGlobalScope() {
        return localMemory.isEmpty();
    }

    // Determines if the given name represents an object.
    public boolean isObject(String name) {
        for (int i = localMemory.size() - 1; i >= 0; i--) {
            if (localMemory.get(i).containsKey(name)) {
                return false;
            }
        }
        if (globalMemory.containsKey(name)) {
            return false;
        }
        return heapMemory.containsKey(name);
    }

    // Prints the current state of memory.
    public void printMemoryState() {
        System.out.println("Global Memory: " + globalMemory);
        System.out.println("Local Memory: " + localMemory);
        System.out.println("Heap Memory: " + heapMemory);
    }

    public HashMap<String, HashMap<String, Integer>> getHeapMemory() {
        return heapMemory;
    }

    // Sets the target object to reference the same object as the source.
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

    public void declareObject(String name, boolean isGlobal) {
        // For object variable declaration, store null to indicate that the object is
        // uninitialized.
        heapMemory.put(name, null);
    }

}
