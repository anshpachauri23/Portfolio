import java.util.*;

class Memory {
	//scanner is stored here as a static field so it is avaiable to the execute method for factor
	public static Scanner data;
	
	// Class and data structures to represent variables
	static class Variable {
		Core type;
		int integerVal;
		HeapObject heap;  // Points to a HeapObject for OBJECT variables.
		Variable(Core t) {
			this.type = t;
		}
	}
	// Represents an allocated object on the heap along with its fields and reference count.
	static class HeapObject {
    HashMap<String, Integer> map;  // Stores field values identified by keys.
    String defaultKey;             // The default key for quick field access.
    int refCount;                  // Count of active references to this object.
    
    // Constructor: creates a new object with an initial field (defaultKey, value)
    // and sets its initial reference count to 1.
    HeapObject(String key, int value) {
        this.map = new HashMap<>();
        this.defaultKey = key;
        this.map.put(key, value);
        this.refCount = 1;  
    }
	}
	// Global counter tracking the number of currently reachable (allocated) HeapObjects.
	public static int reachableObjects = 0;
	
	
	public static HashMap<String, Variable> global;
	public static Stack<Stack<HashMap<String, Variable>>> local;

	
	public static HashMap<String, Function> funcMap;
	
	// Helper methods to manage memory
	
	// Inializes and clear the global memory structures
	// Called by Procedure before executing the DeclSeq
	public static void initializeGlobal() {
		global = new HashMap<String, Variable>();
		funcMap = new HashMap<String, Function>();
	}
	
	// Called at end of Procedure
	public static void clearGlobal() {
		for (Variable v : global.values()) {
			if (v.type == Core.OBJECT && v.heap != null) {
				decrementRef(v.heap);
			}
		}
		global = null;
		funcMap = null;
	}
	
	
	
	
	// Initializes the local data structure
	// Called before executing the main StmtSeq
	public static void initializeLocal() {
		local = new Stack<Stack<HashMap<String, Variable>>>();
		local.push(new Stack<HashMap<String, Variable>>());
		local.peek().push(new HashMap<String, Variable>());
	}
	
	// Pushes a "scope" for if/loop stmts
	public static void pushScope() {
		local.peek().push(new HashMap<String, Variable>());
	}
	
	// Pops a "scope"
	public static void popScope() {
		// Get the scope (a HashMap) that is about to be popped.
		HashMap<String, Variable> scope = local.peek().pop();
		for (Variable v : scope.values()) {
			if (v.type == Core.OBJECT && v.heap != null) {
				decrementRef(v.heap);
			}
		}
	}
	
	
	// Handles decl integer
	public static void declareInteger(String id) {
		Variable v = new Variable(Core.INTEGER);
		if (local != null) {
			local.peek().peek().put(id, v);
		} else {
			global.put(id, v);
		}
	}
	
	// Handles decl object
	public static void declareObject(String id) {
		Variable v = new Variable(Core.OBJECT);
		if (local != null) {
			local.peek().peek().put(id, v);
		} else {
			global.put(id, v);
		}
	}
	
	// Retrives a value from memory (integer or array at index 0)
	public static int load(String id) {
		Variable v = getLocalOrGlobal(id);
		if (v.type == Core.INTEGER) {
			return v.integerVal;
		} else {
			if(v.heap == null) {
				System.out.println("ERROR: Object " + id + " is not allocated!");
				System.exit(1);
			}
			return v.heap.map.get(v.heap.defaultKey);
		}
	}
	
	// Retrieves a value using the key
	public static int load(String id, String key) {
		Variable v = getLocalOrGlobal(id);
		if(v.heap == null) {
			System.out.println("ERROR: Object " + id + " is not allocated!");
			System.exit(1);
		}
		return v.heap.map.get(key);
	}
	
	// Stores a value (integer or map at default key)
	public static void store(String id, int value) {
		Variable v = getLocalOrGlobal(id);
		if (v.type == Core.INTEGER) {
			v.integerVal = value;
		} else {
			if(v.heap == null) {
				System.out.println("ERROR: Object " + id + " is not allocated!");
				System.exit(1);
			}
			v.heap.map.put(v.heap.defaultKey, value);
		}
	}
	
	// Stores a value at key
	public static void store(String id, String key, int value) {
		Variable v = getLocalOrGlobal(id);
		if(v.heap == null) {
			System.out.println("ERROR: Object " + id + " is not allocated!");
			System.exit(1);
		}
		v.heap.map.put(key, value);
	}
	
	// Handles "new object" assignment
	public static void allocate(String id, String key, int value) {
		Variable v = getLocalOrGlobal(id);
		// If v already refers to an allocated object, decrement its reference count.
		if (v.heap != null) {
			decrementRef(v.heap);
		}
		// Allocate a new HeapObject and assign it to the variable.
		v.heap = new HeapObject(key, value);
		reachableObjects++; // Increment global count of reachable objects.
    	System.out.println("gc:" + reachableObjects); // Print new gc state.
	}
	
	
	// Handles "id : id" assignment
	public static void alias(String lhs, String rhs) {
		Variable v1 = getLocalOrGlobal(lhs);
		Variable v2 = getLocalOrGlobal(rhs);
		// If v1 was already referencing an object, remove that reference.
		if (v1.heap != null) {
			decrementRef(v1.heap);
		}
		
		// Assign v1 to the same heap object as v2 and increment the new reference.
		v1.heap = v2.heap;
		if (v2.heap != null) {
			v2.heap.refCount++;
		}
	}
	
	// Looks up value of the variables, searches local then global
	private static Variable getLocalOrGlobal(String id) {
		Variable result;
		if (local.peek().size() > 0) {
			if (local.peek().peek().containsKey(id)) {
				result = local.peek().peek().get(id);
			} else {
				HashMap<String, Variable> temp = local.peek().pop();
				result = getLocalOrGlobal(id);
				local.peek().push(temp);
			}
		} else {
			result = global.get(id);
		}
		return result;
	}
	
	
	/*
	 *
	 * New methods for pushing/popping frames
	 *
	 */

	 public static void pushFrameAndExecute(String name, Parameter args) {
		Function f = funcMap.get(name);
		ArrayList<String> formals = f.param.execute();
		ArrayList<String> arguments = args.execute();
		
		// Create a new frame for the function call.
		Stack<HashMap<String, Variable>> frame = new Stack<HashMap<String, Variable>>();
		frame.push(new HashMap<String, Variable>());
		
		for (int i = 0; i < arguments.size(); i++) {
			Variable src = getLocalOrGlobal(arguments.get(i));
			Variable newVar = new Variable(src.type);
			if (src.type == Core.OBJECT) {
				newVar.heap = src.heap;
				if (newVar.heap != null) {
					newVar.heap.refCount++; // New reference in the function frame.
				}
			} else {
				newVar.integerVal = src.integerVal;
			}
			frame.peek().put(formals.get(i), newVar);
		}
		
		local.push(frame);
		
		f.ss.execute();
	}
	
	 
	public static void popFrame() {
		// Pop the entire frame (a Stack of scopes)
		Stack<HashMap<String, Variable>> frame = local.pop();
		// For each scope in the frame, decrement the references
		for (HashMap<String, Variable> scope : frame) {
			for (Variable v : scope.values()) {
				if (v.type == Core.OBJECT && v.heap != null) {
					decrementRef(v.heap);
				}
			}
		}
	}
	
	 public static void decrementRef(HeapObject h) {
		h.refCount--;
		if (h.refCount == 0) {
			reachableObjects--;  // One less reachable object.
			System.out.println("gc:" + reachableObjects); // Print updated gc state.
		}
	}
	
	
	 
	 
}