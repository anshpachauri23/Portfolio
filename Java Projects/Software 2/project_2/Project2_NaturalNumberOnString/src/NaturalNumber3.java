import components.naturalnumber.NaturalNumber;
import components.naturalnumber.NaturalNumberSecondary;

/**
 * {@code NaturalNumber} represented as a {@code String} with implementations of
 * primary methods.
 *
 * @convention <pre>
 * [all characters of $this.rep are '0' through '9']  and
 * [$this.rep does not start with '0']
 * </pre>
 * @correspondence <pre>
 * this = [if $this.rep = "" then 0
 *         else the decimal number whose ordinary depiction is $this.rep]
 * </pre>
 *
 * @author Daniil Gofman
 *
 */
public class NaturalNumber3 extends NaturalNumberSecondary {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Representation of {@code this}.
     */
    private String rep;

    /**
     * Creator of initial representation.
     */
    private void createNewRep() {
        // Create the representation the corresponds to the data representation
        this.rep = "";
    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public NaturalNumber3() {
        this.rep = "";
    }

    /**
     * Constructor from {@code int}.
     *
     * @param i
     *            {@code int} to initialize from
     */
    public NaturalNumber3(int i) {
        assert i >= 0 : "Violation of: i >= 0";
        // Check parameter i and set up proper data representation
        if (i == 0) {
            this.rep = "";
        } else {
            this.rep = Integer.toString(i);
        }

    }

    /**
     * Constructor from {@code String}.
     *
     * @param s
     *            {@code String} to initialize from
     */
    public NaturalNumber3(String s) {
        assert s != null : "Violation of: s is not null";
        assert s.matches("0|[1-9]\\d*") : ""
                + "Violation of: there exists n: NATURAL (s = TO_STRING(n))";
        // Check parameter s and set up proper data representation
        if (s.equals("0")) {
            this.rep = "";
        } else {
            this.rep = s;
        }
    }

    /**
     * Constructor from {@code NaturalNumber}.
     *
     * @param n
     *            {@code NaturalNumber} to initialize from
     */
    public NaturalNumber3(NaturalNumber n) {
        assert n != null : "Violation of: n is not null";
        // Check parameter n and set up proper data representation
        if (n.isZero()) {
            this.rep = "";
        } else {
            this.rep = n.toString();
        }
    }

    /*
     * Standard methods -------------------------------------------------------
     */

    @Override
    public final NaturalNumber newInstance() {
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
    public final void transferFrom(NaturalNumber source) {
        assert source != null : "Violation of: source is not null";
        assert source != this : "Violation of: source is not this";
        assert source instanceof NaturalNumber3 : ""
                + "Violation of: source is of dynamic type NaturalNumberExample";
        /*
         * This cast cannot fail since the assert above would have stopped
         * execution in that case.
         */
        NaturalNumber3 localSource = (NaturalNumber3) source;
        this.rep = localSource.rep;
        localSource.createNewRep();
    }

    /*
     * Kernel methods ---------------------------------------------------------
     */

    @Override
    public final void multiplyBy10(int k) {
        // Check the preconditions: k should be non-negative and less than 10 (RADIX)
        assert 0 <= k : "Violation of: 0 <= k";
        assert k < RADIX : "Violation of: k < 10";

        /*
         * Concatenate the integer k to the current representation, effectively
         * multiplying by 10
         */
        this.rep = this.rep + Integer.toString(k);
    }

    @Override
    public final int divideBy10() {
        // Variable to store current value
        String numberStr = this.rep;
        // Variable to store new representation
        String newStr = "";
        // Variable to store result
        int result = 0;

        if (!this.rep.isEmpty()) {
            // Get the new value
            newStr = numberStr.substring(0, numberStr.length() - 1);
            // Get the remainder
            result = Character
                    .getNumericValue(numberStr.charAt(numberStr.length() - 1));
            // Update the number to contain only the quotient
            this.rep = newStr;
        }
        // Return the result (remainder)
        return result;
    }

    @Override
    public final boolean isZero() {
        // A natural number is zero if its representation is "".
        return this.rep.equals("");
    }

}
