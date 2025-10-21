

 public interface ProcDecl {
    void print();
    void validate();
    // Executes the procedure/function with given argument bindings (even if empty).
    void execute(MemoryManager memory, java.util.HashMap<String, String> argumentBindings);
}
