import static org.junit.Assert.assertEquals;

import org.junit.Test;

import components.map.Map;
import components.map.Map.Pair;

/**
 * JUnit test fixture for {@code Map<String, String>}'s constructor and kernel
 * methods.
 *
 * @author Daniil Gofman
 *
 */
public abstract class MapTest {

    /**
     * Invokes the appropriate {@code Map} constructor for the implementation
     * under test and returns the result.
     *
     * @return the new map
     * @ensures constructorTest = {}
     */
    protected abstract Map<String, String> constructorTest();

    /**
     * Invokes the appropriate {@code Map} constructor for the reference
     * implementation and returns the result.
     *
     * @return the new map
     * @ensures constructorRef = {}
     */
    protected abstract Map<String, String> constructorRef();

    /**
     *
     * Creates and returns a {@code Map<String, String>} of the implementation
     * under test type with the given entries.
     *
     * @param args
     *            the (key, value) pairs for the map
     * @return the constructed map
     * @requires <pre>
     * [args.length is even]  and
     * [the 'key' entries in args are unique]
     * </pre>
     * @ensures createFromArgsTest = [pairs in args]
     */
    private Map<String, String> createFromArgsTest(String... args) {
        assert args.length % 2 == 0 : "Violation of: args.length is even";
        Map<String, String> map = this.constructorTest();
        for (int i = 0; i < args.length; i += 2) {
            assert !map.hasKey(args[i]) : ""
                    + "Violation of: the 'key' entries in args are unique";
            map.add(args[i], args[i + 1]);
        }
        return map;
    }

    /**
     *
     * Creates and returns a {@code Map<String, String>} of the reference
     * implementation type with the given entries.
     *
     * @param args
     *            the (key, value) pairs for the map
     * @return the constructed map
     * @requires <pre>
     * [args.length is even]  and
     * [the 'key' entries in args are unique]
     * </pre>
     * @ensures createFromArgsRef = [pairs in args]
     */
    private Map<String, String> createFromArgsRef(String... args) {
        assert args.length % 2 == 0 : "Violation of: args.length is even";
        Map<String, String> map = this.constructorRef();
        for (int i = 0; i < args.length; i += 2) {
            assert !map.hasKey(args[i]) : ""
                    + "Violation of: the 'key' entries in args are unique";
            map.add(args[i], args[i + 1]);
        }
        return map;
    }

    /**
     * Test empty constructor.
     */
    @Test
    public final void testEmptyConstructor() {
        Map<String, String> map = this.constructorTest();
        Map<String, String> mapExpected = this.constructorRef();
        assertEquals(map, mapExpected);
    }

    /**
     * Test constructor with parameters.
     */
    @Test
    public final void testConstructor() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota", "3", "Chevrolet", "4", "Lexus");
        Map<String, String> mapExpected = this.createFromArgsRef("1", "Tesla",
                "2", "Toyota", "3", "Chevrolet", "4", "Lexus");
        assertEquals(map, mapExpected);
    }

    /**
     * Test Add starting with empty containers.
     */
    @Test
    public final void testAddEmpty() {
        Map<String, String> map = this.constructorTest();
        Map<String, String> mapExpected = this.constructorRef();

        map.add("1", "Steak");
        mapExpected.add("1", "Steak");
        assertEquals(map, mapExpected);
    }

    /**
     * Test for Add not empty containers.
     */
    @Test
    public final void testAddNotEmpty() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota");
        Map<String, String> mapExpected = this.createFromArgsRef("1", "Tesla",
                "2", "Toyota", "3", "Chevrolet", "4", "Lexus");

        map.add("3", "Chevrolet");
        map.add("4", "Lexus");

        assertEquals(map, mapExpected);
    }

    /**
     * Test for Add not empty containers adding values to both.
     */
    @Test
    public final void testAddNotEmpty2() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota");
        Map<String, String> mapExpected = this.createFromArgsRef("1", "Tesla",
                "2", "Toyota");

        map.add("3", "Chevrolet");
        map.add("4", "Lexus");
        mapExpected.add("3", "Chevrolet");
        mapExpected.add("4", "Lexus");

        assertEquals(map, mapExpected);
    }

    /**
     * Test Remove leaving empty with empty and not empty maps.
     */
    @Test
    public final void testRemoveLeaveEmpty() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota");
        Map<String, String> mapExpected = this.constructorRef();
        Pair<String, String> firstVal = map.remove("1");
        Pair<String, String> secondVal = map.remove("2");
        assertEquals("Tesla", firstVal.value());
        assertEquals("Toyota", secondVal.value());
        assertEquals(map, mapExpected);
    }

    /**
     * Test Remove leaving empty with two not empty maps.
     */
    @Test
    public final void testRemoveLeaveEmpty2() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota");
        Map<String, String> mapExpected = this.createFromArgsRef("2", "Toyota");
        map.remove("1");
        map.remove("2");
        mapExpected.remove("2");
        assertEquals(map, mapExpected);
    }

    /**
     * Test Remove leaving not empty.
     */
    @Test
    public final void testRemoveLeaveNotEmpty() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota");
        Map<String, String> mapExpected = this.createFromArgsRef("1", "Tesla",
                "2", "Toyota");
        map.remove("1");
        mapExpected.remove("1");
        assertEquals(map, mapExpected);
    }

    /**
     * Test RemoveAny leaving empty.
     */
    @Test
    public final void testRemoveAnyLeavingEmpty() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota");
        Map<String, String> mapExpected = this.constructorRef();
        map.removeAny();
        map.removeAny();
        assertEquals(map, mapExpected);
    }

    /**
     * Test RemoveAny leaving empty.
     */
    @Test
    public final void testRemoveAnyLeavingEmpty2() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota");
        Map<String, String> mapExpected = this.createFromArgsRef("1", "Tesla");
        map.removeAny();
        map.removeAny();
        mapExpected.removeAny();
        assertEquals(map, mapExpected);
    }

    /**
     * Test RemoveAny leaving empty, compare values.
     */
    @Test
    public final void testRemoveAnyNotEmpty() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla");
        Pair<String, String> result = map.removeAny();
        String expectedRes = "Tesla";
        assertEquals(result.value(), expectedRes);
    }

    /**
     * Test Value with two maps.
     */
    @Test
    public final void testValueTwoMaps() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota");
        Map<String, String> mapExpected = this.createFromArgsRef("1", "Tesla",
                "2", "Toyota");

        assertEquals(map, mapExpected);
        assertEquals(map.value("1"), mapExpected.value("1"));
        assertEquals(map.value("2"), mapExpected.value("2"));
    }

    /**
     * Test Value with two maps different size.
     */
    @Test
    public final void testValueTwoMaps2() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota", "3", "Chevrolet", "4", "Lexus");
        Map<String, String> mapExpected = this.createFromArgsRef("1", "Tesla",
                "2", "Toyota", "3", "Chevrolet", "4", "Lexus");

        assertEquals(map, mapExpected);
        assertEquals(map.value("1"), mapExpected.value("1"));
        assertEquals(map.value("2"), mapExpected.value("2"));
        assertEquals(map.value("3"), mapExpected.value("3"));
        assertEquals(map.value("4"), mapExpected.value("4"));
    }

    /**
     * Test HasKey with two maps.
     */
    @Test
    public final void testHasKey() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota");
        Map<String, String> mapExpected = this.createFromArgsRef("1", "Tesla",
                "2", "Toyota");

        assertEquals(map, mapExpected);
        assertEquals(map.hasKey("1"), mapExpected.hasKey("1"));
        assertEquals(map.hasKey("2"), mapExpected.hasKey("2"));
    }

    /**
     * Test HasKey maps of different size.
     */
    @Test
    public final void testHasKey2() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota", "3", "Chevrolet", "4", "Lexus");
        Map<String, String> mapExpected = this.createFromArgsRef("1", "Tesla",
                "2", "Toyota", "3", "Chevrolet", "4", "Lexus");

        assertEquals(map, mapExpected);
        assertEquals(map.hasKey("1"), mapExpected.hasKey("1"));
        assertEquals(map.hasKey("2"), mapExpected.hasKey("2"));
        assertEquals(map.hasKey("3"), mapExpected.hasKey("3"));
        assertEquals(map.hasKey("4"), mapExpected.hasKey("4"));
    }

    /**
     * Test Size with not empty maps.
     */
    @Test
    public final void testSizeNotEmpty() {
        Map<String, String> map = this.createFromArgsTest("1", "Tesla", "2",
                "Toyota", "3", "Chevrolet", "4", "Lexus");
        Map<String, String> mapExpected = this.createFromArgsRef("1", "Tesla",
                "2", "Toyota", "3", "Chevrolet", "4", "Lexus");

        assertEquals(map.size(), mapExpected.size());
    }

    /**
     * Test Size with empty maps.
     */
    @Test
    public final void testSizeEmpty() {
        Map<String, String> map = this.constructorTest();
        Map<String, String> mapExpected = this.constructorRef();

        assertEquals(map.size(), mapExpected.size());
    }
}
