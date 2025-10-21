Name: Ansh Pachauri
Course: CSE 3341 - Programming Languages
Project: Project 5 - Garbage Collection

This submission includes the following files:

Assign.java:
  Implements assignment statements in Core. Supports simple assignments (id = <expr>), 
  array assignments (id['key'] = <expr>), object creation (id = new object('key', <expr>)), 
  and object reference assignments (id : id). It handles parsing, pretty-printing, and execution,
  leveraging Memory’s garbage collection methods for object assignments and aliasing.

Call.java:
  Implements procedure calls. It validates and dispatches execution by mapping actual arguments 
  to formal parameters. Call-by-sharing semantics are supported through reference counting in Memory.

Cmpr.java:
  Handles comparison expressions using "==" and "<". It is used in conditional evaluations for if 
  statements and loops.

Cond.java:
  Implements conditional expressions, including support for logical operators (not, and, or) and 
  bracketed conditions. It manages parsing, printing, and execution of conditions.

Core.java:
  Defines the token enumeration (keywords, symbols, and special tokens) used throughout the interpreter.

Decl.java:
  Implements variable declarations for integer and object types. It directs the parsing and execution 
  of declarations to the specific modules for integer and object declarations.

DeclInteger.java:
  Implements integer variable declarations by parsing the declaration syntax and recording the variable 
  in the symbol table.

DeclObject.java:
  Implements object variable declarations by parsing object declarations and adding them to the symbol table.

DeclSeq.java:
  Manages a sequence of declarations and function definitions. It enforces the uniqueness of procedure 
  names and properly sequences declarations.

Expr.java:
  Implements arithmetic expressions supporting addition and subtraction. It builds and evaluates expression trees.

Factor.java:
  Represents the smallest elements in an expression – identifiers, constants, and parenthesized expressions.
  Responsible for parsing and evaluating expression components.

Function.java:
  Represents function (procedure with parameters) declarations. Implements parsing, validation, and registration 
  in Memory’s function table. Supports call-by-sharing through proper aliasing and reference counting.

Id.java:
  Implements identifier parsing and printing. Provides access to variable names used in declarations and expressions.

If.java:
  Implements the if-then[-else] control structure. It parses conditional statements, validates branches 
  in separate scopes, and executes the appropriate branch based on the evaluated condition.

Loop.java:
  Implements loop constructs as defined in Core. It parses loop initialization, condition, update expressions, 
  and the loop body, then executes them iteratively.

Main.java:
  Contains the main entry point for the interpreter. It initializes the scanner (for both code and data),
  triggers parsing of the top-level procedure, and starts execution.

Memory.java:
  Implements memory management for the interpreter. It handles global and local variable storage, heap allocation 
  for objects, and introduces garbage collection via reference counting. New/modified methods include:
    - allocate(): Allocates a HeapObject for object creation, increments the global reachable count, and prints the gc message.
    - alias(): Implements aliasing (id : id) by decrementing any previous reference and incrementing the new reference.
    - decrementRef(): Decrements a HeapObject’s reference count and prints a gc message when the count reaches zero.
    - popFrame(): Releases all object references stored in a function call frame by iterating over all scopes in the frame.
    - clearGlobal(): Iterates over all global variables, releasing object references to ensure that garbage collection 
      messages (e.g., "gc:0") are printed as globals go out of scope.
  These methods ensure that as variables are updated or go out of scope, their HeapObjects’ reference counts 
  are adjusted accordingly and the correct garbage collection output is produced.

Parameter.java:
  Represents and processes parameter lists for functions. It parses the parameter list, ensures uniqueness, 
  and supports binding of formal parameters to actual arguments for call-by-sharing.

Parser.java:
  Implements recursive descent parsing routines. It verifies that the input program adheres to the Core grammar 
  using helper methods such as expectedToken().

Print.java:
  Implements the print statement. It parses the print command and prints the evaluated integer result to stdout.

Procedure.java:
  Represents the main procedure of the program. Coordinates the parsing of declaration sequences and statement 
  sequences, initializes global and local memory, executes the procedure, and finally triggers global cleanup 
  (invoking clearGlobal()) to ensure all object references are properly released.

Read.java:
  Implements the read statement. It parses the statement, reads an integer value from the data file, and assigns 
  it to the specified variable.

Scanner.java:
  Performs lexical analysis on the source (.code) file. It tokenizes the input by distinguishing identifiers, 
  constants, keywords, and symbols as defined in the Core grammar.

Stmt.java:
  An interface for all statement types. It enables polymorphic handling of different kinds of statements (assignments, 
  prints, conditionals, loops, reads, declarations, and procedure calls).

StmtSeq.java:
  Manages a sequence of statements. It recursively parses, prints, and executes statements in the correct program order.

Term.java:
  Implements the term component of arithmetic expressions, supporting multiplication and division.

Overall Interpreter Design:

The Core interpreter is structured into distinct phases: lexical analysis, parsing, semantic validation, and execution. 
Each non-terminal in the Core grammar is implemented as a dedicated class that is responsible for its own parsing, printing, 
validation, and execution. Variable management is achieved through a stack-based symbol table for scoping. 

A key addition in this project is garbage collection using reference counting. Heap objects maintain a reference count that 
is incremented on object allocation and aliasing, and decremented when variables are reassigned or go out of scope. 
When a HeapObject’s reference count drops to zero, a gc message is printed (e.g., "gc:0"), allowing us to simulate garbage 
collection without actually reclaiming memory.

Special Features / Comments:
- Uses recursive descent parsing with clear separation of parsing, validation, and execution responsibilities.
- Memory is managed via distinct global and local regions with a simulated heap for objects.
- Implements call-by-sharing for procedure calls via aliasing and reference count management.
- Comprehensive error handling for syntax and runtime issues (e.g., unallocated objects, division by zero).
- Garbage collection messages are printed as specified whenever object reference counts change.

Testing and Known Bugs:
I tested the interpreter using the test cases provided with the project. I am not aware of any bugs in this implementation of the interpreter.

