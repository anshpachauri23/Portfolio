import components.sequence.Sequence;
import components.statement.Statement;
import components.statement.StatementSecondary;
import components.tree.Tree;
import components.tree.Tree1;
import components.utilities.Tokenizer;

/**
 * {@code Statement} represented as a {@code Tree<StatementLabel>} with
 * implementations of primary methods.
 *
 * @convention [$this.rep is a valid representation of a Statement]
 * @correspondence this = $this.rep
 *
 * @author Ansh Pachauri and Daniil Gofman
 *
 */
public class Statement2 extends StatementSecondary {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Label class for the tree representation.
     */
    private static final class StatementLabel {

        /**
         * Statement kind.
         */
        private Kind kind;

        /**
         * IF/IF_ELSE/WHILE statement condition.
         */
        private Condition condition;

        /**
         * CALL instruction name.
         */
        private String instruction;

        /**
         * Constructor for BLOCK.
         *
         * @param k
         *            the kind of statement
         *
         * @requires k = BLOCK
         * @ensures this = (BLOCK, ?, ?)
         */
        private StatementLabel(Kind k) {
            assert k == Kind.BLOCK : "Violation of: k = BLOCK";
            this.kind = k;
        }

        /**
         * Constructor for IF, IF_ELSE, WHILE.
         *
         * @param k
         *            the kind of statement
         * @param c
         *            the statement condition
         *
         * @requires k = IF or k = IF_ELSE or k = WHILE
         * @ensures this = (k, c, ?)
         */
        private StatementLabel(Kind k, Condition c) {
            assert k == Kind.IF || k == Kind.IF_ELSE || k == Kind.WHILE : ""
                    + "Violation of: k = IF or k = IF_ELSE or k = WHILE";
            this.kind = k;
            this.condition = c;
        }

        /**
         * Constructor for CALL.
         *
         * @param k
         *            the kind of statement
         * @param i
         *            the instruction name
         *
         * @requires k = CALL and [i is an IDENTIFIER]
         * @ensures this = (CALL, ?, i)
         */
        private StatementLabel(Kind k, String i) {
            assert k == Kind.CALL : "Violation of: k = CALL";
            assert i != null : "Violation of: i is not null";
            assert Tokenizer
                    .isIdentifier(i) : "Violation of: i is an IDENTIFIER";
            this.kind = k;
            this.instruction = i;
        }

        @Override
        public String toString() {
            String condition = "?", instruction = "?";
            if ((this.kind == Kind.IF) || (this.kind == Kind.IF_ELSE)
                    || (this.kind == Kind.WHILE)) {
                condition = this.condition.toString();
            } else if (this.kind == Kind.CALL) {
                instruction = this.instruction;
            }
            return "(" + this.kind + "," + condition + "," + instruction + ")";
        }

    }

    /**
     * The tree representation field.
     */
    private Tree<StatementLabel> rep;

    /**
     * Creator of initial representation.
     */
    private void createNewRep() {

        this.rep = new Tree1<StatementLabel>();
        StatementLabel start = new StatementLabel(Kind.BLOCK);
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();
        this.rep.assemble(start, children);

    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Statement2() {
        this.createNewRep();
    }

    /*
     * Standard methods -------------------------------------------------------
     */

    @Override
    public final Statement2 newInstance() {
        try {
            return this.getClass().getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(
                    "Cannot construct object of type " + this.getClass());
        }
    }

    @Override
    public final void clear() {
        this.createNewRep();
    }

    @Override
    public final void transferFrom(Statement source) {
        assert source != null : "Violation of: source is not null";
        assert source != this : "Violation of: source is not this";
        assert source instanceof Statement2 : ""
                + "Violation of: source is of dynamic type Statement2";
        /*
         * This cast cannot fail since the assert above would have stopped
         * execution in that case: source must be of dynamic type Statement2.
         */
        Statement2 localSource = (Statement2) source;
        this.rep = localSource.rep;
        localSource.createNewRep();
    }

    /*
     * Kernel methods ---------------------------------------------------------
     */

    @Override
    public final Kind kind() {

        // Return kind of a root
        return this.rep.root().kind;
    }

    @Override
    public final void addToBlock(int pos, Statement s) {
        assert s != null : "Violation of: s is not null";
        assert s != this : "Violation of: s is not this";
        assert s instanceof Statement2 : "Violation of: s is a Statement2";
        assert this
                .kind() == Kind.BLOCK : "Violation of: [this is a BLOCK statement]";
        assert 0 <= pos : "Violation of: 0 <= pos";
        assert pos <= this
                .lengthOfBlock() : "Violation of: pos <= [length of this BLOCK]";
        assert s.kind() != Kind.BLOCK : "Violation of: [s is not a BLOCK statement]";

        // Cast the input Statement 's' to Statement2 (specific type) as sLocal
        Statement2 sLocal = (Statement2) s;

        // Create a sequence of tree nodes to represent
        // the children of 'this' BLOCK statement
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();

        // Disassemble 'this' BLOCK statement into its children and get its label
        StatementLabel label = this.rep.disassemble(children);

        // Add the Statement 'sLocal' to the specified position
        // pos in the children sequence
        children.add(pos, sLocal.rep);

        // Assemble this BLOCK statement with the updated children
        // sequence and the original label
        this.rep.assemble(label, children);

        // Clear the content of Statement s to prevent any further use or confusion
        s.clear();
    }

    @Override
    public final Statement removeFromBlock(int pos) {
        assert 0 <= pos : "Violation of: 0 <= pos";
        assert pos < this
                .lengthOfBlock() : "Violation of: pos < [length of this BLOCK]";
        assert this
                .kind() == Kind.BLOCK : "Violation of: [this is a BLOCK statement]";

        // Create a new instance of Statement2 to represent the removed statement
        Statement2 s = this.newInstance();

        // Create a sequence of tree nodes to represent
        // the children of this BLOCK statement
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();

        // Disassemble this BLOCK statement into its children and get its label
        StatementLabel label = this.rep.disassemble(children);

        // Remove the statement at the specified position pos from the children sequence
        s.rep = children.remove(pos);

        // Assemble this BLOCK statement with the updated
        // children sequence and the original label
        this.rep.assemble(label, children);

        // Return the removed statement
        return s;
    }

    @Override
    public final int lengthOfBlock() {
        assert this.kind() == Kind.BLOCK : ""
                + "Violation of: [this is a BLOCK statement]";

        // Returns length of a block
        return this.rep.numberOfSubtrees();
    }

    @Override
    public final void assembleIf(Condition c, Statement s) {
        assert c != null : "Violation of: c is not null";
        assert s != null : "Violation of: s is not null";
        assert s != this : "Violation of: s is not this";
        assert s instanceof Statement2 : "Violation of: s is a Statement2";
        assert s.kind() == Kind.BLOCK : ""
                + "Violation of: [s is a BLOCK statement]";
        Statement2 sLocal = (Statement2) s;
        StatementLabel label = new StatementLabel(Kind.IF, c);
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();
        children.add(0, sLocal.rep);
        this.rep.assemble(label, children);
        sLocal.createNewRep();
    }

    @Override
    public final Condition disassembleIf(Statement s) {
        assert s != null : "Violation of: s is not null";
        assert s != this : "Violation of: s is not this";
        assert s instanceof Statement2 : "Violation of: s is a Statement2";
        assert this.kind() == Kind.IF : ""
                + "Violation of: [this is an IF statement]";
        Statement2 locals = (Statement2) s;
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();
        StatementLabel label = this.rep.disassemble(children);
        locals.rep = children.remove(0);
        this.createNewRep(); // clears this
        return label.condition;
    }

    @Override
    public final void assembleIfElse(Condition c, Statement s1, Statement s2) {
        assert c != null : "Violation of: c is not null";
        assert s1 != null : "Violation of: s1 is not null";
        assert s2 != null : "Violation of: s2 is not null";
        assert s1 != this : "Violation of: s1 is not this";
        assert s2 != this : "Violation of: s2 is not this";
        assert s1 != s2 : "Violation of: s1 is not s2";
        assert s1 instanceof Statement2 : "Violation of: s1 is a Statement2";
        assert s2 instanceof Statement2 : "Violation of: s2 is a Statement2";
        assert s1
                .kind() == Kind.BLOCK : "Violation of: [s1 is a BLOCK statement]";
        assert s2
                .kind() == Kind.BLOCK : "Violation of: [s2 is a BLOCK statement]";

        // Cast Statement s1 and s2 to Statement2 as s1Local and s2Local
        Statement2 s1Local = (Statement2) s1;
        Statement2 s2Local = (Statement2) s2;

        // Create a new StatementLabel representing
        // the IF_ELSE kind with the provided Condition c
        StatementLabel label = new StatementLabel(Kind.IF_ELSE, c);

        // Create a sequence of tree nodes to represent the children of this statement
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();

        // Add s2Local to the children sequence at position 0
        children.add(0, s2Local.rep);

        // Add s1Local to the children sequence at position 0, pushing s2Local down
        children.add(0, s1Local.rep);

        // Assemble this statement with the new label and the updated children sequence
        this.rep.assemble(label, children);

        // Create a new representation for s1Local and s2Local
        s1Local.createNewRep();
        s2Local.createNewRep();
    }

    @Override
    public final Condition disassembleIfElse(Statement s1, Statement s2) {
        assert s1 != null : "Violation of: s1 is not null";
        assert s2 != null : "Violation of: s1 is not null";
        assert s1 != this : "Violation of: s1 is not this";
        assert s2 != this : "Violation of: s2 is not this";
        assert s1 != s2 : "Violation of: s1 is not s2";
        assert s1 instanceof Statement2 : "Violation of: s1 is a Statement2";
        assert s2 instanceof Statement2 : "Violation of: s2 is a Statement2";
        assert this
                .kind() == Kind.IF_ELSE : "Violation of: [this is an IF_ELSE statement]";

        // Cast Statement s1 and s2 to Statement2 as localIf and localElse
        Statement2 localIf = (Statement2) s1;
        Statement2 localElse = (Statement2) s2;

        // Create a sequence of tree nodes to represent the children of this statement
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();

        // Disassemble 'this' IF_ELSE statement into its children and get its root label
        StatementLabel root = this.rep.disassemble(children);

        // Assign the first child (if-branch) to localIf.rep
        localIf.rep = children.remove(0);

        // Assign the second child (else-branch) to localElse.rep
        localElse.rep = children.remove(0);

        // Assemble IF_ELSE statement with the updated root label and children sequence
        this.rep.assemble(root, children);

        // Create a new representation for this IF_ELSE statement
        this.createNewRep();

        // Return the Condition associated with the root label
        return root.condition;
    }

    @Override
    public final void assembleWhile(Condition c, Statement s) {
        assert c != null : "Violation of: c is not null";
        assert s != null : "Violation of: s is not null";
        assert s != this : "Violation of: s is not this";
        assert s instanceof Statement2 : "Violation of: s is a Statement2";
        assert s.kind() == Kind.BLOCK : "Violation of: [s is a BLOCK statement]";

        // Cast Statement s to Statement2 as sLocal
        Statement2 sLocal = (Statement2) s;

        // Create a new StatementLabel representing
        // the WHILE kind with the provided Condition c
        StatementLabel label = new StatementLabel(Kind.WHILE, c);

        // Create a sequence of tree nodes to represent the children of this statement
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();

        // Add sLocal to the children sequence at position 0
        children.add(0, sLocal.rep);

        // Assemble this statement with the new label and the updated children sequence
        this.rep.assemble(label, children);

        // Create a new representation for sLocal
        sLocal.createNewRep();
    }

    @Override
    public final Condition disassembleWhile(Statement s) {
        assert s != null : "Violation of: s is not null";
        assert s != this : "Violation of: s is not this";
        assert s instanceof Statement2 : "Violation of: s is a Statement2";
        assert this
                .kind() == Kind.WHILE : "Violation of: [this is a WHILE statement]";

        // Cast Statement s to Statement2 as sLocal
        Statement2 sLocal = (Statement2) s;

        // Create a sequence of tree nodes to represent the children of this statement
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();

        // Disassemble this WHILE statement into its children and get its label
        StatementLabel label = this.rep.disassemble(children);

        // Assign the child (the BLOCK statement) to sLocal.rep
        sLocal.rep = children.remove(0);

        // Create a new representation for this WHILE statement
        this.createNewRep();

        // Return the Condition associated with the label
        return label.condition;
    }

    @Override
    public final void assembleCall(String inst) {
        assert inst != null : "Violation of: inst is not null";
        assert Tokenizer.isIdentifier(
                inst) : "Violation of: inst is a valid IDENTIFIER";

        // Create a new StatementLabel representing
        // the CALL kind with the provided instruction inst
        StatementLabel label = new StatementLabel(Kind.CALL, inst);

        // Create a sequence of tree nodes to represent the children of this statement
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();

        // Assemble this statement with the new label and an empty children sequence
        this.rep.assemble(label, children);
    }

    @Override
    public final String disassembleCall() {
        assert this
                .kind() == Kind.CALL : "Violation of: [this is a CALL statement]";

        // Create a sequence of tree nodes to represent the children of this statement
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();

        // Disassemble this CALL statement into its children and get its label
        StatementLabel label = this.rep.disassemble(children);

        // Create a new representation for this CALL statement
        this.createNewRep();

        // Return the instruction associated with the label
        return label.instruction;
    }

}
