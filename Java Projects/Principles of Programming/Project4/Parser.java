

 class Parser {
    public static Scanner scanner;

    // Checks if the current token matches the expected token.
    static void expectedToken(Core expected) {
        Core current = scanner.currentToken();
        if (current != expected) {
            System.out.println("SYNTAX ERROR: Expected " + expected + ", but found " + current);
            System.exit(1);
        }
    }
    
    // Returns the next token without consuming the current token.
    static Core lookaheadToken() {
        return scanner.peekToken();
    }
}
