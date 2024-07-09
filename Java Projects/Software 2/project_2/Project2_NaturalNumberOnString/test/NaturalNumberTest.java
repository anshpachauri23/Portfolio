import static org.junit.Assert.assertEquals;

import org.junit.Test;

import components.naturalnumber.NaturalNumber;

/**
 * JUnit test fixture for {@code NaturalNumber}'s constructors and kernel
 * methods.
 *
 * @author Ansh Pachauri
 *
 */
public abstract class NaturalNumberTest {

    /**
     * Invokes the appropriate {@code NaturalNumber} constructor for the
     * implementation under test and returns the result.
     *
     * @return the new number
     * @ensures constructorTest = 0
     */
    protected abstract NaturalNumber constructorTest();

    /**
     * Invokes the appropriate {@code NaturalNumber} constructor for the
     * implementation under test and returns the result.
     *
     * @param i
     *            {@code int} to initialize from
     * @return the new number
     * @requires i >= 0
     * @ensures constructorTest = i
     */
    protected abstract NaturalNumber constructorTest(int i);

    /**
     * Invokes the appropriate {@code NaturalNumber} constructor for the
     * implementation under test and returns the result.
     *
     * @param s
     *            {@code String} to initialize from
     * @return the new number
     * @requires there exists n: NATURAL (s = TO_STRING(n))
     * @ensures s = TO_STRING(constructorTest)
     */
    protected abstract NaturalNumber constructorTest(String s);

    /**
     * Invokes the appropriate {@code NaturalNumber} constructor for the
     * implementation under test and returns the result.
     *
     * @param n
     *            {@code NaturalNumber} to initialize from
     * @return the new number
     * @ensures constructorTest = n
     */
    protected abstract NaturalNumber constructorTest(NaturalNumber n);

    /**
     * Invokes the appropriate {@code NaturalNumber} constructor for the
     * reference implementation and returns the result.
     *
     * @return the new number
     * @ensures constructorRef = 0
     */
    protected abstract NaturalNumber constructorRef();

    /**
     * Invokes the appropriate {@code NaturalNumber} constructor for the
     * reference implementation and returns the result.
     *
     * @param i
     *            {@code int} to initialize from
     * @return the new number
     * @requires i >= 0
     * @ensures constructorRef = i
     */
    protected abstract NaturalNumber constructorRef(int i);

    /**
     * Invokes the appropriate {@code NaturalNumber} constructor for the
     * reference implementation and returns the result.
     *
     * @param s
     *            {@code String} to initialize from
     * @return the new number
     * @requires there exists n: NATURAL (s = TO_STRING(n))
     * @ensures s = TO_STRING(constructorRef)
     */
    protected abstract NaturalNumber constructorRef(String s);

    /**
     * Invokes the appropriate {@code NaturalNumber} constructor for the
     * reference implementation and returns the result.
     *
     * @param n
     *            {@code NaturalNumber} to initialize from
     * @return the new number
     * @ensures constructorRef = n
     */
    protected abstract NaturalNumber constructorRef(NaturalNumber n);

    /**
     * Test for Empty constructor.
     */
    @Test
    public final void testConstructorEmpty() {
        NaturalNumber s = this.constructorTest();
        NaturalNumber sExpected = this.constructorRef();

        assertEquals(s, sExpected);
    }

    /**
     * Test for constructor with a String zero.
     */
    @Test
    public final void testConstructorStringZero() {
        NaturalNumber s = this.constructorTest("0");
        NaturalNumber sExpected = this.constructorRef("0");

        assertEquals(s, sExpected);
    }

    /**
     * Test for constructor with String.
     */
    @Test
    public final void testConstructorString1() {
        NaturalNumber s = this.constructorTest("123");
        NaturalNumber sExpected = this.constructorRef("123");

        assertEquals(s, sExpected);
    }

    /**
     * Test for constructor with String.
     */
    @Test
    public final void testConstructorString2() {
        NaturalNumber s = this.constructorTest("123456789");
        NaturalNumber sExpected = this.constructorRef("123456789");

        assertEquals(s, sExpected);
    }

    /**
     * Test for constructor with integer zero.
     */
    @Test
    public final void testConstructorInteger1() {
        NaturalNumber s = this.constructorTest(0);
        NaturalNumber sExpected = this.constructorRef(0);

        assertEquals(s, sExpected);
    }

    /**
     * Test for constructor with integer.
     */
    @Test
    public final void testConstructorInteger2() {
        final int testNum = 12345;
        NaturalNumber s = this.constructorTest(testNum);
        NaturalNumber sExpected = this.constructorRef(testNum);

        assertEquals(s, sExpected);
    }

    /**
     * Test for constructor with MAX value of integer.
     */
    @Test
    public final void testConstructorInteger3() {
        NaturalNumber s = this.constructorTest(Integer.MAX_VALUE);
        NaturalNumber sExpected = this.constructorRef(Integer.MAX_VALUE);

        assertEquals(s, sExpected);
    }

    /**
     * Test for constructor with Natural Number zero.
     */
    @Test
    public final void testConstructorNN1() {
        NaturalNumber test = this.constructorTest(0);
        NaturalNumber s = this.constructorTest(test);
        NaturalNumber sExpected = this.constructorRef(test);

        assertEquals(s, sExpected);
    }

    /**
     * Test for constructor with Natural Number.
     */
    @Test
    public final void testConstructorNN2() {
        final int val = 12345;
        NaturalNumber test = this.constructorTest(val);
        NaturalNumber s = this.constructorTest(test);
        NaturalNumber sExpected = this.constructorRef(test);

        assertEquals(s, sExpected);
    }

    /**
     * Test for constructor with MAX of Natural Number.
     */
    @Test
    public final void testConstructorNN3() {
        NaturalNumber test = this.constructorTest(Integer.MAX_VALUE);
        NaturalNumber s = this.constructorTest(test);
        NaturalNumber sExpected = this.constructorRef(test);

        assertEquals(s, sExpected);
    }

    /**
     * Test for MultiplyBy10 with Zero.
     */
    @Test
    public final void testMultiplyBy10Zero() {
        final int val = 7;
        NaturalNumber s = this.constructorTest(0);
        NaturalNumber sExpected = this.constructorRef(val);
        s.multiplyBy10(val);

        assertEquals(s, sExpected);
    }

    /**
     * Test for Non-Empty MultiplyBy10.
     */
    @Test
    public final void testMultiplyBy10NonEmpty1() {
        final int val = 123;
        final int valEx = 1234;
        final int num = 4;
        NaturalNumber s = this.constructorTest(val);
        NaturalNumber sExpected = this.constructorRef(valEx);
        s.multiplyBy10(num);

        assertEquals(s, sExpected);
    }

    /**
     * Test for Non-Empty MultiplyBy10.
     */
    @Test
    public final void testMultiplyBy10NonEmpty2() {
        final int val = 12345678;
        final int valEx = 123456789;
        final int num = 9;
        NaturalNumber s = this.constructorTest(val);
        NaturalNumber sExpected = this.constructorRef(valEx);

        s.multiplyBy10(num);

        assertEquals(s, sExpected);
    }

    /**
     * Test for DivideBy10 with Zero.
     */
    @Test
    public final void testDivideBy10Zero() {
        final int val = 7;
        NaturalNumber s = this.constructorTest(val);
        NaturalNumber sExpected = this.constructorRef(0);

        s.divideBy10();

        assertEquals(s, sExpected);
    }

    /**
     * Test for Non-Empty DivideBy10.
     */
    @Test
    public final void testDivideBy10NonEmpty1() {
        final int val = 1234;
        final int valEx = 123;
        NaturalNumber s = this.constructorTest(val);
        NaturalNumber sExpected = this.constructorRef(valEx);
        s.divideBy10();

        assertEquals(s, sExpected);
    }

    /**
     * Test for Non-Empty DivideBy10.
     */
    @Test
    public final void testDivideBy10NonEmpty2() {
        final int val = 123456789;
        final int valEx = 12345678;
        NaturalNumber s = this.constructorTest(val);
        NaturalNumber sExpected = this.constructorRef(valEx);
        s.divideBy10();

        assertEquals(s, sExpected);
    }

    /**
     * Test for isZero with zero.
     */
    @Test
    public final void testIsZeroZero() {
        NaturalNumber s = this.constructorTest(0);
        NaturalNumber sExpected = this.constructorRef(0);

        boolean test = s.isZero();

        assertEquals(s, sExpected);
        assertEquals(test, true);
    }

    /**
     * Test for Non-Empty isZero.
     */
    @Test
    public final void testNonEmptyIsZero() {
        final int val = 123;
        NaturalNumber s = this.constructorTest(val);
        NaturalNumber sExpected = this.constructorRef(val);

        boolean test = s.isZero();

        assertEquals(s, sExpected);
        assertEquals(test, false);
    }

}
