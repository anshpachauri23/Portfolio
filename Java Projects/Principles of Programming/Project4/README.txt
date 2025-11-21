Name: Ansh Pachauri
Course: CSE 3341 - Programming Languages
Project: Project 4 - Procedure Calls

This submission includes the following files:

Assign.java:
  Implements assignment statements in Core. Supports simple assignments (id = <expr>), 
  array assignments (id['key'] = <expr>), object creation (id = new object('key', <expr>)), 
  and object reference assignments (id : id). It handles parsing, pretty-printing, semantic 
  validation, and execution, including special handling for formal parameter aliasing.

Call.java:
  Implements procedure and function calls. It parses a call statement, validates the procedure 
  or function name and parameter list, and dispatches execution by mapping actual arguments to 
  formal parameters.

Cmpr.java:
  Handles comparison expressions using the operators "==" and "<". It is used for conditional 
  evaluation in if statements and loops.

Cond.java:
  Implements conditional expressions, including support for negation (not), logical AND and OR, 
  and bracketed conditions. It supports parsing, printing, semantic checks, and evaluation.

Core.java:
  Defines the enumeration of tokens (keywords, symbols, and special tokens) used throughout the 
  interpreter.

Decl.java:
  Implements variable declarations for integers and objects. It parses and prints declarations, 
  adds identifiers to the symbol table, and allocates memory when executed.

DeclSeq.java:
  Manages a sequence of declarations and procedure/function declarations. It also enforces the 
  rule that each procedure must have a unique name.

Expr.java:
  Implements arithmetic expressions that support addition and subtraction. It builds the parse tree 
  for an expression, validates it, and evaluates its numeric value during execution.

Factor.java:
  Represents the smallest elements of an expression – identifiers, constants, and expressions in 
  parentheses. Responsible for parsing, printing, validation, and evaluation.

Function.java:
  Represents a function (a procedure with parameters) in Core. It implements parsing (after the 
  name and parameter list are parsed by DeclSeq), validation, and execution (including binding of 
  actual parameters using call-by-sharing). Functions are registered in a function table.

If.java:
  Implements the if-else control structure. It parses the if statement (with an optional else), 
  validates each branch in its own scope, and executes the appropriate branch based on the evaluated 
  condition.

Loop.java:
  Implements the for loop construct. It parses the loop initialization, condition, update expression, 
  and body; validates the loop (including type checks for the loop variable); and executes the loop 
  by iteratively updating the variable and executing the body.

Print.java:
  Implements the print statement. It parses a print command, validates the contained expression, 
  and executes by printing the evaluated integer value on a new line.

Read.java:
  Implements the read statement. It parses and prints the statement, validates that the target 
  variable is declared, and executes by reading an integer from the input data file.

Stmt.java:
  Serves as a wrapper for all kinds of statements (assignments, conditionals, loops, prints, 
  reads, declarations, and procedure calls). It delegates parsing, printing, validation, and 
  execution to the appropriate statement type.

StmtSeq.java:
  Manages a sequence of statements. It recursively parses, prints, validates, and executes the list 
  of statements to ensure correct program flow.

Procedure.java:
  Represents a procedure declaration (with no parameters) and serves as the overall Core program. 
  It coordinates parsing of the procedure header, local declarations, and statement sequence; 
  validates the entire procedure using a symbol table; and executes the procedure in its own scope.

MemoryManager.java:
  Simulates the program’s memory, managing three regions:
    - Global and local variable storage (using a stack for scoping)
    - Heap memory for objects
    - Aliasing for call-by-sharing parameter passing
  It provides methods for variable creation, assignment, object updates, input reading, and error checking.

SymbolTable.java:
  Implements a stack-based symbol table for managing variable declarations and their types across 
  nested scopes. It provides methods to enter and exit scopes, add identifiers, and retrieve types.

Parameters.java:
  Represents the parameter list for functions. It parses the parameter list, validates that parameters 
  are unique, and supports binding formal parameters to actual arguments.

Parser.java:
  Contains helper routines for parsing, including methods such as expectedToken() and lookaheadToken(), 
  to ensure the input adheres to the Core grammar.

Scanner.java:
  Performs lexical analysis of the Core source file (.code). It reads characters, distinguishes 
  between keywords, identifiers, constants, and symbols, and produces tokens for the parser.

Term.java:
  Represents a term in an arithmetic expression (handling multiplication and division). It parses, 
  prints, validates, and evaluates the term.

Overall Interpreter Design:

The CORE interpreter is structured in distinct phases: lexical analysis, parsing, semantic validation,
and execution. The recursive descent parser constructs a parse tree where each non-terminal in the Core grammar 
is represented by a dedicated class. Each node in the tree is responsible for parsing its part of the program, 
printing a human-friendly representation, validating its structure, and executing its semantics.

Variable management is handled by a stack-based symbol table that enforces proper scoping. When entering a 
new block (e.g., procedure, loop, conditional), a new scope is pushed onto the symbol table, and upon exiting, 
the scope is removed. MemoryManager simulates memory with three regions:
  - Global memory for variables declared outside any scope.
  - Local memory (via a stack) for nested scopes.
  - Heap memory for objects, with special aliasing for call-by-sharing.

The interpreter simulates a call stack using a combination of scope and alias stacks within the MemoryManager class. 
Every procedure or function call triggers enterScope(), which pushes a new local variable map and an alias map onto their 
respective stacks. For functions, formal parameters are aliased to actual argument names using bindParameterAlias, enabling 
call-by-sharing, meaning the callee references the same object as the caller. All variable and object access resolve through 
the top of the alias and memory stacks, maintaining scope integrity. After the call completes, exitScope() cleans up the frame. 
This design supports recursion, nested calls, and clean variable isolation while preserving shared object semantics.

Special Features / Comments:

- Recursive descent parsing with clear separation of parsing, validation, and execution.
- Memory management with distinct handling for integers and objects.
- Call-by-sharing parameter passing is implemented with aliasing and rebinding.
- Comprehensive error handling ensures that syntactic and semantic errors are caught early.

Testing and Known Bugs:

I tested the parser using the test cases provided with the project. I am not aware of any bugs in this implementation of the interpreter.

