import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class Scanner {
    private BufferedReader readFile;
    private Core currentToken;
    private String currentId;
    private int currentConst;
    private String currentString;
    private boolean isClosed = false;
    private int state = 0;
    private int savedChar = -1;

    // Constructor to initialize the scanner with a file
    Scanner(String filename) {
        try {
            readFile = new BufferedReader(new FileReader(filename));
            nextToken(); // Call nextToken to find the first token
        } catch (IOException e) {
            System.out.println("ERROR: Could not open file.");
            currentToken = Core.ERROR;
        }
    }

    // Method to advance to the next token
    public void nextToken() {
        if (isClosed) {
            System.out.println("ERROR: Scanner is closed.");
            return; // Prevent further processing if closed
        }

        StringBuilder tokenBuilder = new StringBuilder();

        try {
            int character = -1;
            if (savedChar != -1) {
                character = savedChar;
                savedChar = -1; // Clear saved character after using
            } else {
                character = readFile.read();
            }

            // If end of stream is reached
            if (character == -1) {
                currentToken = Core.EOS; // Set to EOS
                close(); // Close the file when done
                return;
            }

            while (Character.isWhitespace((char) character)) {
                character = readFile.read();
            }
            if (character != -1) {
                if (isConstant(Character.toString(character))) {
                    state = 3; // Constant
                } else if (isSymbol(Character.toString(character))) {
                    state = 2; // Symbol
                } else if (Character.toString(character).equals("'")) {
                    state = 4;
                } else {
                    state = 1; // everything else
                }
            }

            while (state == 1) {
                tokenBuilder.append((char) character);
                character = readFile.read();
                if (character != -1) {
                    if (isSymbol(Character.toString(character)) || Character.isWhitespace(character)) {
                        state = 0;
                        savedChar = character;
                    }
                } else {
                    state = 0;
                    savedChar = character;
                }

            }
            while (state == 2) {
                tokenBuilder.append((char) character);
                int nextChar = readFile.read();
                if (nextChar != -1) {
                    if (isSymbol(Character.toString((char) nextChar))) {
                        String potentialSymbol = tokenBuilder.toString() + (char) nextChar;
                        if (isSymbol(potentialSymbol)) {
                            tokenBuilder.append((char) nextChar);
                            character = readFile.read(); // Move past combined symbol
                            savedChar = character;
                            state = 0;
                        } else {
                            character = nextChar;
                            savedChar = character;
                            state = 0;
                        }
                    } else {
                        character = nextChar;
                        savedChar = character;
                        state = 0;
                    }
                } else {
                    character = nextChar;
                    savedChar = character;
                    state = 0;
                }
            }
            while (state == 3) {
                tokenBuilder.append((char) character);
                character = readFile.read();
                if (character != -1) {
                    if (!isConstant(Character.toString(character))) {
                        state = 0;
                        savedChar = character;
                    }
                } else {
                    state = 0;
                    savedChar = character;
                }
            }
            while (state == 4) {
                tokenBuilder.append((char) character);
                character = readFile.read();
                if (character != -1) {
                    if (Character.toString(character).equals("'")) {
                        state = 0;
                        tokenBuilder.append((char) character);
                    }
                } else {
                    state = 0;
                }
            }

            String token = tokenBuilder.toString();

            if (token.isEmpty()) {
                nextToken(); // Skip empty tokens
                return;
            }
            if (isKeyword(token)) {
                currentToken = Core.valueOf(token.toUpperCase());
            } else if (isIdentifier(token)) {
                currentToken = Core.ID;
                currentId = token;
            } else if (isSymbol(token)) {
                currentToken = symbolType(token);
            } else if (isConstant(token)) {
                currentToken = Core.CONST;
                currentConst = Integer.parseInt(token);
            } else if (isString(token)) {
                currentToken = Core.STRING;
                currentString = token.substring(1, token.length() - 1);
            } else {
                System.out.println("ERROR: Invalid token - " + token);
                currentToken = Core.ERROR;
            }

        } catch (IOException e) {
            System.out.println("ERROR: Could not read file.");
            currentToken = Core.ERROR;
        }
    }

    // Method to return the current token
    public Core currentToken() {
        return currentToken;
    }

    // Method to return the identifier string
    public String getId() {
        if (currentToken == Core.ID) {
            return currentId;
        }
        return null;
    }

    // Method to return the constant value
    public int getConst() {
        if (currentToken == Core.CONST) {
            return currentConst;
        }
        return -1;
    }

    // Method to return the character string
    public String getString() {
        if (currentToken == Core.STRING) {
            return currentString;
        }
        return null;
    }

    // Method to check if a token is a keyword
    private boolean isKeyword(String token) {
        try {
            Core.valueOf(swapCase(token)); // Attempt to convert to enum
            return true; // If successful, it's a keyword
        } catch (IllegalArgumentException e) {
            return false; // If an exception is thrown, it's not a keyword
        }
    }

    // Method to check if a token is an identifier
    private boolean isIdentifier(String token) {
        return token.matches("^[a-zA-Z][a-zA-Z0-9]*$");
    }

    // Method to check if a token is a constant
    private boolean isConstant(String token) {
        try {
            char firstNum = token.charAt(0);
            if (firstNum == '0' && token.length() > 1) {
                return false;
            } else {
                int value = Integer.parseInt(token);
                return (value >= 0 && value <= 1000003);
            }

        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Method to check if a token is a symbol
    private boolean isSymbol(String token) {
        switch (token) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "=":
            case "==":
            case "<":
            case ":":
            case ";":
            case ".":
            case ",":
            case "(":
            case ")":
            case "[":
            case "]":
            case "{":
            case "}":
                return true;
            default:
                return false;
        }
    }

    // Method to check if a token is a string
    private boolean isString(String token) {
        return token.startsWith("'") && token.endsWith("'");
    }

    // Method to determine the type of symbol
    private Core symbolType(String token) {
        switch (token) {
            case "+":
                return Core.ADD;
            case "-":
                return Core.SUBTRACT;
            case "*":
                return Core.MULTIPLY;
            case "/":
                return Core.DIVIDE;
            case "=":
                return Core.ASSIGN;
            case "==":
                return Core.EQUAL;
            case "<":
                return Core.LESS;
            case ":":
                return Core.COLON;
            case ";":
                return Core.SEMICOLON;
            case ".":
                return Core.PERIOD;
            case ",":
                return Core.COMMA;
            case "(":
                return Core.LPAREN;
            case ")":
                return Core.RPAREN;
            case "[":
                return Core.LSQUARE;
            case "]":
                return Core.RSQUARE;
            case "{":
                return Core.LCURL;
            case "}":
                return Core.RCURL;
            default:
                return null;
        }
    }

    // Method to release memory (close the file)
    public void close() {
        if (!isClosed) {
            try {
                if (readFile != null) {
                    readFile.close(); // Close the BufferedReader
                }
                isClosed = true; // Mark as closed
            } catch (IOException e) {
                System.out.println("ERROR: Could not close file.");
            }
        }
    }

    // Method to swap the case of a string
    private String swapCase(String token) {
        String x = "";
        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            if (Character.isUpperCase(ch))
                x += Character.toLowerCase(ch);
            else
                x += Character.toUpperCase(ch);
        }
        return x;
    }

}
