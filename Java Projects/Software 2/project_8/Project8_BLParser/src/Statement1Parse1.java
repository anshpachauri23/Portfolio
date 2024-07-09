import components.queue.Queue;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.statement.Statement1;
import components.utilities.Reporter;
import components.utilities.Tokenizer;

/**
 * Layered implementation of secondary methods {@code parse} and
 * {@code parseBlock} for {@code Statement}.
 *
 * @author Daniil Gofman and Ansh Pachauri
 *
 */
public final class Statement1Parse1 extends Statement1 {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Converts {@code c} into the corresponding {@code Condition}.
     *
     * @param c
     *            the condition to convert
     * @return the {@code Condition} corresponding to {@code c}
     * @requires [c is a condition string]
     * @ensures parseCondition = [Condition corresponding to c]
     */
    private static Condition parseCondition(String c) {
        assert c != null : "Violation of: c is not null";
        assert Tokenizer
                .isCondition(c) : "Violation of: c is a condition string";
        return Condition.valueOf(c.replace('-', '_').toUpperCase());
    }

    /**
     * Parses an IF or IF_ELSE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"IF"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [an if string is a proper prefix of #tokens] then
     *  s = [IF or IF_ELSE Statement corresponding to if string at start of #tokens]  and
     *  #tokens = [if string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseIf(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("IF") : ""
                + "Violation of: <\"IF\"> is proper prefix of tokens";

        String tokenIf = tokens.dequeue();
        Reporter.assertElseFatalError(tokenIf.equals("IF"),
                "Error: " + tokenIf + " is not equal to \"IF\"");
        //give error if next token is not a condition.
        Reporter.assertElseFatalError(Tokenizer.isCondition(tokens.front()),
                "Error: " + tokens.front() + " is not a valid condition");
        //parse the condition.
        Condition condIf = parseCondition(tokens.dequeue());
        //check for keyword THEN.
        Reporter.assertElseFatalError(tokens.front().equals("THEN"),
                "Error: " + tokens.front() + " is not equal to \"THEN\"");
        //dequeue "THEN"
        tokens.dequeue();
        //parse block under if
        Statement blockIf = s.newInstance();
        blockIf.parseBlock(tokens);
        //check for keyword END or ELSE
        Reporter.assertElseFatalError(
                tokens.front().equals("ELSE") || tokens.front().equals("END"),
                "Error: " + tokens.front()
                        + " is not equal to \"ELSE\" or \"END\"");

        if (tokens.front().equals("ELSE")) {
            //dequeue "ELSE"
            tokens.dequeue();
            //parse block under else
            Statement blockElse = s.newInstance();
            blockElse.parseBlock(tokens);
            //assemble if_else
            s.assembleIfElse(condIf, blockIf, blockElse);
            //check for keyword END
            Reporter.assertElseFatalError(tokens.front().equals("END"),
                    "Error: " + tokens.front() + " is not equal to \"END\"");
            //dequeue "END"
            tokens.dequeue();
        } else {
            //assemble if
            s.assembleIf(condIf, blockIf);
            //check for keyword END
            Reporter.assertElseFatalError(tokens.front().equals("END"),
                    "Error: " + tokens.front() + " is not equal to \"END\"");
            //dequeue "END"
            tokens.dequeue();
        }
        //check for keyword IF
        Reporter.assertElseFatalError(tokens.front().equals(tokenIf),
                "Error: " + tokens.front() + " is not equal to \"IF\"");
        //dequeue "IF"
        tokens.dequeue();

    }

    /**
     * Parses a WHILE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"WHILE"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [a while string is a proper prefix of #tokens] then
     *  s = [WHILE Statement corresponding to while string at start of #tokens]  and
     *  #tokens = [while string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseWhile(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("WHILE") : ""
                + "Violation of: <\"WHILE\"> is proper prefix of tokens";
        //check for keyword WHILE and dequeue WHILE
        String tokenWhile = tokens.dequeue();
        Reporter.assertElseFatalError(tokenWhile.equals("WHILE"),
                "Error: " + tokenWhile + " is not equal to \"WHILE\"");
        //give error if next token is not a condition
        Reporter.assertElseFatalError(Tokenizer.isCondition(tokens.front()),
                "Error: " + tokens.front() + " is not a valid condition");
        //parse the condition
        Condition condWhile = parseCondition(tokens.dequeue());
        //check and dequeue keyword DO
        Reporter.assertElseFatalError(tokens.front().equals("DO"),
                "Error: " + tokens.front() + " is not equal to \"DO\"");

        tokens.dequeue();
        //parse block under while
        Statement blockWhile = s.newInstance();
        blockWhile.parseBlock(tokens);
        //assemble while
        s.assembleWhile(condWhile, blockWhile);
        //check and dequeue for keyword END
        Reporter.assertElseFatalError(tokens.front().equals("END"),
                "Error: " + tokens.front() + " is not equal to \"END\"");

        tokens.dequeue();
        //check for keyword WHILE and dequeue WHILE
        Reporter.assertElseFatalError(tokens.front().equals(tokenWhile),
                "Error: " + tokens.front() + " is not equal to \"WHILE\"");

        tokens.dequeue();

    }

    /**
     * Parses a CALL statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires [identifier string is a proper prefix of tokens]
     * @ensures <pre>
     * s =
     *   [CALL Statement corresponding to identifier string at start of #tokens]  and
     *  #tokens = [identifier string at start of #tokens] * tokens
     * </pre>
     */
    private static void parseCall(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0
                && Tokenizer.isIdentifier(tokens.front()) : ""
                        + "Violation of: identifier string is proper prefix of tokens";
        //dequeue call and assemble the call
        String call = tokens.dequeue();
        s.assembleCall(call);

    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Statement1Parse1() {
        super();
    }

    /*
     * Public methods ---------------------------------------------------------
     */

    @Override
    public void parse(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";
        //check if next token is not "IF", "WHILE", or an identifier
        Reporter.assertElseFatalError(
                Tokenizer.isIdentifier(tokens.front())
                        || tokens.front().equals("IF")
                        || tokens.front().equals("WHILE"),
                tokens.front() + " is not IF, IF_ELSE, WHILE, or CALL");

        if (tokens.front().equals("IF")) {
            //parse the if
            parseIf(tokens, this);
        } else if (tokens.front().equals("WHILE")) {
            //parse the while
            parseWhile(tokens, this);
        } else {
            //parse call
            parseCall(tokens, this);
        }

    }

    @Override
    public void parseBlock(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        Statement stmt = this.newInstance();
        //check if next token is not "IF", "WHILE", or an identifier
        Reporter.assertElseFatalError(Tokenizer.isIdentifier(tokens.front())
                || tokens.front().equals("IF") || tokens.front().equals("WHILE")
                || tokens.front().equals("END"),
                tokens.front() + " is not IF, IF_ELSE, WHILE, END, or CALL");

        int i = 0;
        while (Tokenizer.isIdentifier(tokens.front())
                || tokens.front().equals("IF")
                || tokens.front().equals("WHILE")) {
            //parse the statement
            stmt.parse(tokens);
            this.addToBlock(i, stmt);
            i++;
        }

    }

    /*
     * Main test method -------------------------------------------------------
     */

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        /*
         * Get input file name
         */
        out.print("Enter valid BL statement(s) file name: ");
        String fileName = in.nextLine();
        /*
         * Parse input file
         */
        out.println("*** Parsing input file ***");
        Statement s = new Statement1Parse1();
        SimpleReader file = new SimpleReader1L(fileName);
        Queue<String> tokens = Tokenizer.tokens(file);
        file.close();
        s.parse(tokens); // replace with parseBlock to test other method
        /*
         * Pretty print the statement(s)
         */
        out.println("*** Pretty print of parsed statement(s) ***");
        s.prettyPrint(out, 0);

        in.close();
        out.close();
    }

}
