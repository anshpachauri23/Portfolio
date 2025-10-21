

 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 
 class Scanner {
     // Regular expression patterns for identifiers and constants.
     String id = "[a-zA-Z][a-zA-Z0-9]*";
     String constant = "[0-9]|[1-9][0-9]*";
     
     BufferedReader in;   // Reader for the input file
     StringBuilder token; // Buffer for the current token
     Core t;              // Current token type
 
     // Constructs a Scanner with the given filename.
     Scanner(String filename) {
         try {
             this.in = new BufferedReader(new FileReader(filename));
         } catch (FileNotFoundException e) {
             System.out.println("ERROR: File not found");
             this.t = Core.ERROR;
         }
         this.t = this.nextToken();
     }
 
     // Reads and returns the next token from the input.
     public Core nextToken() {
         try {
             int c = this.in.read();
             // Skip whitespace
             while (Character.isWhitespace(c) && c != -1) {
                 c = this.in.read();
             }
             if (c == -1) {
                 this.t = Core.EOS;
             } else {
                 switch ((char) c) {
                     case '+': this.t = Core.ADD; break;
                     case '-': this.t = Core.SUBTRACT; break;
                     case '*': this.t = Core.MULTIPLY; break;
                     case '/': this.t = Core.DIVIDE; break;
                     case ':': this.t = Core.COLON; break;
                     case '<': this.t = Core.LESS; break;
                     case ';': this.t = Core.SEMICOLON; break;
                     case '.': this.t = Core.PERIOD; break;
                     case ',': this.t = Core.COMMA; break;
                     case '(': this.t = Core.LPAREN; break;
                     case ')': this.t = Core.RPAREN; break;
                     case '[': this.t = Core.LSQUARE; break;
                     case ']': this.t = Core.RSQUARE; break;
                     case '{': this.t = Core.LCURL; break;
                     case '}': this.t = Core.RCURL; break;
                     case '=': {
                         this.in.mark(1);
                         int nextChar = this.in.read();
                         if ((char) nextChar == '=') {
                             this.t = Core.EQUAL;
                         } else {
                             this.in.reset();
                             this.t = Core.ASSIGN;
                         }
                         break;
                     }
                     case '\'': {
                         this.t = Core.STRING;
                         this.token = new StringBuilder();
                         int nextChar = this.in.read();
                         // Read until closing quote is found
                         while (nextChar != -1 && nextChar != '\'') {
                             this.token.append((char) nextChar);
                             nextChar = this.in.read();
                         }
                         if (nextChar == -1) {
                             System.out.println("ERROR: Ended file with unclosed string!");
                             this.t = Core.ERROR;
                         }
                         break;
                     }
                     default: {
                         boolean continued = true;
                         this.token = new StringBuilder();
                         if (Character.isDigit((char)c)) {
                             // Read a numeric constant
                             while (continued) {
                                 this.token.append((char)c);
                                 this.in.mark(1);
                                 c = this.in.read();
                                 continued = c != -1 && Character.isDigit((char)c);
                                 if (!continued) {
                                     this.in.reset();
                                 }
                             }
                         } else if (Character.isLetter((char)c)) {
                             // Read an identifier or keyword
                             while (continued) {
                                 this.token.append((char)c);
                                 this.in.mark(1);
                                 c = this.in.read();
                                 continued = c != -1 && Character.isLetterOrDigit((char)c);
                                 if (!continued) {
                                     this.in.reset();
                                 }
                             }
                         } else {
                             this.token.append((char)c);
                         }
                         // Determine the token type from the string value.
                         switch (this.token.toString()) {
                             case "and": this.t = Core.AND; break;
                             case "begin": this.t = Core.BEGIN; break;
                             case "case": this.t = Core.CASE; break;
                             case "do": this.t = Core.DO; break;
                             case "else": this.t = Core.ELSE; break;
                             case "end": this.t = Core.END; break;
                             case "for": this.t = Core.FOR; break;
                             case "if": this.t = Core.IF; break;
                             case "in": this.t = Core.IN; break;
                             case "integer": this.t = Core.INTEGER; break;
                             case "is": this.t = Core.IS; break;
                             case "new": this.t = Core.NEW; break;
                             case "not": this.t = Core.NOT; break;
                             case "object": this.t = Core.OBJECT; break;
                             case "or": this.t = Core.OR; break;
                             case "print": this.t = Core.PRINT; break;
                             case "procedure": this.t = Core.PROCEDURE; break;
                             case "read": this.t = Core.READ; break;
                             case "return": this.t = Core.RETURN; break;
                             case "then": this.t = Core.THEN; break;
                             default: {
                                 if (this.token.toString().matches(this.id)) {
                                     this.t = Core.ID;
                                 } else if (this.token.toString().matches(this.constant) &&
                                            Integer.parseInt(this.token.toString()) <= 1000003) {
                                     this.t = Core.CONST;
                                 } else {
                                     throw new Exception();
                                 }
                                 break;
                             }
                         }
                         break;
                     }
                 }
             }
         } catch (Exception e) {
             System.out.println("ERROR: Invalid input " + this.token.toString());
             this.t = Core.ERROR;
         }
         return this.t;
     }
 
     // Returns the current token type.
     public Core currentToken() {
         return this.t;
     }
 
     // Returns the identifier of the current token.
     public String getId() {
         return this.token.toString();
     }
 
     // Returns the integer value of the current token.
     public int getConst() {
         return Integer.parseInt(this.token.toString());
     }
     
     // Returns the string literal of the current token.
     public String getString() {
         return this.token.toString();
     }
     
     // Peeks at the next token without consuming the current one.
     public Core peekToken() {
         try {
             in.mark(1024);
             Core next = nextToken();
             in.reset();
             return next;
         } catch (Exception e) {
             System.out.println("ERROR in peekToken");
             return Core.ERROR;
         }
     }
 }
 