import static org.junit.Assert.assertEquals;

import java.util.Comparator;

import org.junit.Test;

import components.sortingmachine.SortingMachine;

/**
 * JUnit test fixture for {@code SortingMachine<String>}'s constructor and
 * kernel methods.
 *
 * @author Daniil Gofman, Ansh Pachauri
 *
 */
public abstract class SortingMachineTest {

    /**
     * Invokes the appropriate {@code SortingMachine} constructor for the
     * implesEntation under test and returns the result.
     *
     *
     * @param order
     *            the {@code Comparator} defining the order for {@code String}
     * @return the new {@code SortingMachine}
     * @requires IS_TOTAL_PREORDER([relation computed by order.compare method])
     * @ensures constructorTest = (true, order, {})
     */
    protected abstract SortingMachine<String> constructorTest(
            Comparator<String> order);

    /**
     * Invokes the appropriate {@code SortingMachine} constructor for the
     * reference implementation and returns the result.
     *
     * @param order
     *            the {@code Comparator} defining the order for {@code String}
     * @return the new {@code SortingMachine}
     * @requires IS_TOTAL_PREORDER([relation computed by order.compare method])
     * @ensures constructorRef = (true, order, {})
     */
    protected abstract SortingMachine<String> constructorRef(
            Comparator<String> order);

    /**
     *
     * Creates and returns a {@code SortingMachine<String>} of the
     * implementation under test type with the given entries and mode.
     *
     * @param order
     *            the {@code Comparator} defining the order for {@code String}
     * @param insertionMode
     *            flag indicating the machine mode
     * @param args
     *            the entries for the {@code SortingMachine}
     * @return the constructed {@code SortingMachine}
     * @requires IS_TOTAL_PREORDER([relation computed by order.compare method])
     * @ensures <pre>
     * createFromArgsTest = (insertionMode, order, [multiset of entries in args])
     * </pre>
     */
    private SortingMachine<String> createFromArgsTest(Comparator<String> order,
            boolean insertionMode, String... args) {
        SortingMachine<String> sm = this.constructorTest(order);
        for (int i = 0; i < args.length; i++) {
            sm.add(args[i]);
        }
        if (!insertionMode) {
            sm.changeToExtractionMode();
        }
        return sm;
    }

    /**
     *
     * Creates and returns a {@code SortingMachine<String>} of the reference
     * implementation type with the given entries and mode.
     *
     * @param order
     *            the {@code Comparator} defining the order for {@code String}
     * @param insertionMode
     *            flag indicating the machine mode
     * @param args
     *            the entries for the {@code SortingMachine}
     * @return the constructed {@code SortingMachine}
     * @requires IS_TOTAL_PREORDER([relation computed by order.compare method])
     * @ensures <pre>
     * createFromArgsRef = (insertionMode, order, [multiset of entries in args])
     * </pre>
     */
    private SortingMachine<String> createFromArgsRef(Comparator<String> order,
            boolean insertionMode, String... args) {
        SortingMachine<String> sm = this.constructorRef(order);
        for (int i = 0; i < args.length; i++) {
            sm.add(args[i]);
        }
        if (!insertionMode) {
            sm.changeToExtractionMode();
        }
        return sm;
    }

    /**
     * Comparator<String> implementation to be used in all test cases. Compare
     * {@code String}s in lexicographic order.
     */
    private static class StringLT implements Comparator<String> {

        @Override
        public int compare(String s1, String s2) {
            return s1.compareToIgnoreCase(s2);
        }

    }

    /**
     * Comparator instance to be used in all test cases.
     */
    private static final StringLT ORDER = new StringLT();

    /**
     * Test empty constructor.
     */
    @Test
    public final void testConstructor() {
        SortingMachine<String> s = this.constructorTest(ORDER);
        SortingMachine<String> sExpected = this.constructorRef(ORDER);
        assertEquals(sExpected, s);
    }

    /**
     * Test add boundary case.
     */
    @Test
    public final void testAddEmpty() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, true);
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, true,
                "apple");
        s.add("apple");
        assertEquals(sExpected, s);
    }

    /**
     * Test add routine case.
     */
    @Test
    public final void testAddRoutine1() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, true,
                "apples");
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, true,
                "apples", "avacado");
        s.add("avacado");
        assertEquals(sExpected, s);
    }

    /**
     * Test add routine case.
     */
    @Test
    public final void testAddRoutine2() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, true,
                "Apples", "Avacado");
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, true,
                "Apples", "Avacado", "Orange");
        s.add("Orange");
        assertEquals(sExpected, s);
    }

    /**
     * Test changeToExtractionMode Empty case.
     */
    @Test
    public final void testchangeToExtractionModeEmpty() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, true);
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, false);
        s.changeToExtractionMode();
        assertEquals(sExpected, s);
    }

    /**
     * Test changeToExtractionMode routine case.
     */
    @Test
    public final void testchangeToExtractionModeRoutine1() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, true,
                "Apples", "Avacado");
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, false,
                "Apples", "Avacado");
        s.changeToExtractionMode();
        assertEquals(sExpected, s);
    }

    /**
     * Test changeToExtractionMode routine case.
     */
    @Test
    public final void testchangeToExtractionModeRoutine2() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, true,
                "Apples", "Avacado", "Orange");
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, false,
                "Apples", "Avacado", "Orange");
        s.changeToExtractionMode();
        assertEquals(sExpected, s);
    }

    /**
     * Test removeFirst Empty test case.
     */
    @Test
    public final void testRemoveFirstEmpty() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, false,
                "Apples");
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, false);
        String expectedVal = "Apples";
        String val = s.removeFirst();
        assertEquals(expectedVal, val);
        assertEquals(sExpected, s);
    }

    /**
     * Test removeFirst routine test case.
     */
    @Test
    public final void testRemoveFirstRoutine() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, false,
                "Apples", "Avacado", "Orange");
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, false,
                "Avacado", "Orange");
        String expectedVal = "Apples";
        String val = s.removeFirst();
        assertEquals(expectedVal, val);
        assertEquals(sExpected, s);
    }

    /**
     * Test isInInsertionMode routine case.
     */
    @Test
    public final void testIsInInsertionModeRoutine1() {
        SortingMachine<String> m = this.createFromArgsTest(ORDER, true, "apple",
                "banana");

        boolean insertionMode = m.isInInsertionMode();
        assertEquals(true, insertionMode);
    }

    /**
     * Test isInInsertionMode routine case.
     */
    @Test
    public final void testIsInInsertionModeRotuine2() {
        SortingMachine<String> m = this.createFromArgsTest(ORDER, false,
                "apple", "banana");

        boolean insertionMode = m.isInInsertionMode();
        assertEquals(false, insertionMode);
    }

    /**
     * Test isInInsertionMode Empty case.
     */
    @Test
    public final void testIsInInsertionModeEmpty1() {
        SortingMachine<String> m = this.createFromArgsTest(ORDER, true);

        boolean insertionMode = m.isInInsertionMode();
        assertEquals(true, insertionMode);
    }

    /**
     * Test isInInsertionMode Empty case.
     */
    @Test
    public final void testIsInInsertionModeEmpty2() {
        SortingMachine<String> m = this.createFromArgsTest(ORDER, false);

        boolean insertionMode = m.isInInsertionMode();
        assertEquals(false, insertionMode);
    }

    /**
     * Test order routine test case.
     */
    @Test
    public final void testOrderRoutine() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, false,
                "Apples", "Avacado", "Orange");
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, false,
                "Apples", "Avacado", "Orange");
        Comparator<String> orderTest = s.order();
        Comparator<String> expectedOrderTest = ORDER;
        assertEquals(expectedOrderTest, orderTest);
        assertEquals(sExpected, s);
    }

    /**
     * Test order empty test case.
     */
    @Test
    public final void testOrderEmpty() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, false);
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, false);
        Comparator<String> orderTest = s.order();
        Comparator<String> expectedOrderTest = ORDER;
        assertEquals(expectedOrderTest, orderTest);
        assertEquals(sExpected, s);
    }

    /**
     * Test size empty case in extraction mode.
     */
    @Test
    public final void testSizeEmpty1() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, false);
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, false);
        assertEquals(0, s.size());
        assertEquals(sExpected, s);
    }

    /**
     * Test size empty case in insertion mode.
     */
    @Test
    public final void testSizeEmpty2() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, true);
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, true);
        assertEquals(0, s.size());
        assertEquals(sExpected, s);
    }

    /**
     * Test size routine test case in extraction mode.
     */
    @Test
    public final void testSizeNotEmpty1() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, false,
                "Apples", "Avacado", "Orange");
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, false,
                "Apples", "Avacado", "Orange");
        assertEquals(3, s.size());
        assertEquals(sExpected, s);
    }

    /**
     * Test size routine test case in extraction mode.
     */
    @Test
    public final void testSizeNotEmpty2() {
        SortingMachine<String> s = this.createFromArgsTest(ORDER, true,
                "Apples", "Avacado", "Orange");
        SortingMachine<String> sExpected = this.createFromArgsRef(ORDER, true,
                "Apples", "Avacado", "Orange");
        assertEquals(3, s.size());
        assertEquals(sExpected, s);
    }
}
