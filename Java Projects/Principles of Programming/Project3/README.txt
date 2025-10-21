Name: Ansh Pachauri
Course: CSE 3341 - Programming Languages
Project: Project 3

This submission includes the following files:

Assign.java:
This file implements assignment statements in Core. It supports multiple forms including simple assignments (x = <expr>), array assignments (x['key'] = <expr>), object creation (x = new object('key', <expr>)), and object reference assignments (x : y). The code parses, prints, validates, and executes assignments while ensuring the correct handling of object initialization.

Cmpr.java:
This file handles comparison expressions using the operators "==" and "<". It parses two expressions, prints the comparison in a human‑friendly format, validates that both sides are properly formed, and evaluates the comparison during execution to control flow.

Cond.java:
This file deals with conditional expressions, including support for logical operators like not, and, and or, as well as bracketed conditions. It parses a condition, prints it, validates its structure, and then evaluates it to determine the outcome of control flow constructs.

Expr.java:
This file implements arithmetic expressions that support addition and subtraction. It constructs the parse tree for an expression, prints the result in a readable way, validates its structure, and calculates the evaluated numerical value during execution.

Term.java:
This file represents a term within an arithmetic expression that handles multiplication and division. It parses a factor and any subsequent term linked by '*' or '/', prints the term neatly, validates the components, and computes its final value.

Factor.java:
This file represents the smallest elements of an expression, which can be identifiers, constants, or expressions within parentheses. It is responsible for parsing these basic units, printing them, validating their correctness, and evaluating them to return a number.

Decl.java:
This file handles variable declarations for both integers and objects in Core. It parses a declaration, prints it, and validates that no duplicate identifiers exist within the same scope. When executed, it allocates memory for integers and declares object variables as null until they are explicitly initialized.

DeclSeq.java:
This file manages a sequence of declarations by recursively parsing multiple declarations, printing them in order, validating each one, and executing them to correctly establish the program’s variable scope.

If.java:
This file implements the if-else control structure. It parses an if statement (with an optional else block), prints the structure with proper indentation, validates the contained statements in their own scopes, and executes the correct branch based on the evaluated condition.

Loop.java:
This file implements the for loop construct. It parses the initialization, condition, update expression, and loop body; prints the loop with appropriate formatting; validates the loop ensuring proper scoping and type checking; and executes the loop by repeatedly evaluating the condition and updating the loop variable.

Print.java:
This file implements the print statement for Core. It parses a print command, prints it in a human‑friendly format, validates the contained expression, and executes the command by evaluating the expression and outputting its result to the console.

Read.java:
This file implements the read statement used to input integer values. It parses the read command, prints it, validates that the target variable is declared, and executes the command by reading the next available integer from a data file.

Stmt.java:
This file acts as a wrapper for all types of statements (assignments, conditionals, loops, prints, reads, and declarations). It determines the type of statement based on the current token and delegates parsing, printing, validating, and execution to the appropriate file.

StmtSeq.java:
This file manages a sequence of statements by recursively parsing and printing a list of statements, validating them in order, and executing them sequentially to maintain the correct program flow.

Procedure.java:
This file represents the overall Core program (the procedure). It coordinates parsing the procedure header, optional declarations, and the statement sequence; validates the entire program using a symbol table; and executes the procedure within its own scope.

MemoryManager.java:
This file simulates the program’s memory, managing three regions: global, local (for nested scopes), and heap (for objects). It handles variable creation, updates, and retrieval, and incorporates robust error checking for issues like division by zero, assignment to null object variables, and running out of input data.

SymbolTable.java:
This file implements a stack‑based symbol table to manage variable declarations and their types across multiple scopes. It provides simple methods to enter and exit scopes, add new identifiers, and retrieve variable types, ensuring that each variable is declared only once in its respective scope.

Parser.java:
This file provides helper routines for parsing. It contains methods like expectedToken() to ensure that the current token matches what is anticipated, centralizing error reporting and making the parser code more maintainable.

Scanner.java:
This file tokenizes the input Core program (.code file). Scanner.java reads characters from the file, distinguishes keywords, identifiers, constants, and symbols, and produces tokens for the parser to build the parse tree.

Overall Interpreter Design:

Overall Interpreter Design:
The CORE interpreter is built in distinct phases—lexical analysis, parsing, validation, and execution—to transform a Core program from text to a running process. 
The parser, implemented using recursive descent, builds a parse tree where each class represents a non-terminal from the Core grammar. Each node in the tree provides 
methods for parsing, pretty-printing, and validating the program structure before execution begins.

Variable management is a critical aspect of the design. The interpreter uses a stack-based symbol table to track variable declarations and ensure that variables 
are only accessible within their proper scope. When entering a new block (such as a procedure, loop, or conditional), a new scope is pushed onto the symbol table 
so that any variables declared within hide those from outer scopes. Upon exiting the block, the corresponding scope is popped off, ensuring that temporary variables 
are discarded. During execution, the MemoryManager simulates three regions of memory—global, local, and heap—so that integer variables are stored with an initial value 
of 0, while object variables are declared as null until they are explicitly initialized via a "new object" assignment. This design robustly tracks variable values, 
prevents naming conflicts, and enforces proper initialization, thereby catching runtime errors like assignments to null objects or division by zero.

In summary, the interpreter maintains a clear separation between the phases of processing while using a layered approach to variable scoping and memory management. 
This ensures that each variable’s lifetime and visibility are correctly handled, making the interpreter both robust in execution and easier to maintain.


Special Features / Comments:

The interpreter is built using a recursive descent approach, which makes the parsing logic straightforward.
There is a clear separation between parsing, validation, and execution, making the interpreter easier to maintain.
Memory management is divided into global, local, and heap regions. Notably, object variables are declared as null by default, and a "new object" assignment is required to initialize them.

Testing and Known Bugs:

I tested the parser using the test cases provided with the project. I am not aware of any bugs in this implementaion of the interpreter.


