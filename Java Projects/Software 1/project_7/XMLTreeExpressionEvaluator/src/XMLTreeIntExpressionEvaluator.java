import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to evaluate XMLTree expressions of {@code int}.
 *
 * @author Ansh Pachauri
 *
 */
public final class XMLTreeIntExpressionEvaluator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private XMLTreeIntExpressionEvaluator() {
    }

    /**
     * Evaluate the given expression.
     *
     * @param exp
     *            the {@code XMLTree} representing the expression
     * @return the value of the expression
     * @requires <pre>
     * [exp is a subtree of a well-formed XML arithmetic expression]  and
     *  [the label of the root of exp is not "expression"]
     * </pre>
     * @ensures evaluate = [the value of the expression]
     */
    private static int evaluate(XMLTree exp) {
        assert exp != null : "Violation of: exp is not null";

        // TODO - fill in body
        int noReOccur = 0;
        // when the node is a number
        if (exp.label().equals("number")) {
            noReOccur = Integer.parseInt(exp.attributeValue("value"));
            // when the node is not a number
        } else {
            String action = exp.label();

            XMLTree one = exp.child(0);
            XMLTree two = exp.child(1);

            noReOccur = evaluate(one);
            // calculating the expression
            if (action.equals("plus")) {
                noReOccur += evaluate(two);
            } else if (action.equals("divide")) {
                noReOccur /= evaluate(two);
            } else if (action.equals("multiply")) {
                noReOccur *= evaluate(two);
            } else {
                noReOccur -= evaluate(two);
            }
        }
        return noReOccur;
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        out.print("Enter the name of an expression XML file: ");
        String file = in.nextLine();
        while (!file.equals("")) {
            XMLTree exp = new XMLTree1(file);
            out.println(evaluate(exp.child(0)));
            out.print("Enter the name of an expression XML file: ");
            file = in.nextLine();
        }

        in.close();
        out.close();
    }

}
