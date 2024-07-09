import static org.junit.Assert.assertEquals;

import org.junit.Test;

import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 *
 * @author ansh pachauri
 *
 */
public class StringReassemblyTest {

    /**
     * test of combination.
     */

    @Test
    public void testCombination0() {
        String str1 = "abcd";
        String str2 = "cdefghi";
        int overlap = 2;
        String result = StringReassembly.combination(str1, str2, overlap);
        assertEquals("abcdefghi", result);
    }

    /**
     * test of combination.
     */
    @Test
    public void testCombination1() {
        String str1 = "abcdefghi";
        String str2 = "cdefghijklmnopqrst";
        int overlap = 7;
        String result = StringReassembly.combination(str1, str2, overlap);
        assertEquals("abcdefghijklmnopqrst", result);
    }

    /**
     * test of combination.
     */
    @Test
    public void testCombination2() {
        String str1 = "abcd";
        String str2 = "abcd";
        int overlap = 4;
        String result = StringReassembly.combination(str1, str2, overlap);
        assertEquals("abcd", result);
    }

    /**
     * test of addToSetAvoidingSubstrings.
     */

    @Test
    public void testAddToSetAvoidingSubstrings0() {
        Set<String> strSet = new Set1L<String>();
        strSet.add("Hey");
        strSet.add("How are you?");
        strSet.add("I am good");
        Set<String> check = new Set1L<String>();
        check.add("Hey");
        check.add("How are you?");
        check.add("I am good");
        String str = "good";
        StringReassembly.addToSetAvoidingSubstrings(strSet, str);
        assertEquals(check, strSet);
    }

    /**
     * test of addToSetAvoidingSubstrings.
     */
    @Test
    public void testAddToSetAvoidingSubstrings1() {
        Set<String> strSet = new Set1L<String>();
        strSet.add("Hey");
        strSet.add("How are you?");
        strSet.add("I am good");
        Set<String> check = new Set1L<String>();
        check.add("Hey");
        check.add("How are you?");
        check.add("I am good");
        check.add("Nathan");
        String str = "Nathan";
        StringReassembly.addToSetAvoidingSubstrings(strSet, str);
        assertEquals(check, strSet);
    }

    /**
     * test of addToSetAvoidingSubstrings.
     */
    @Test
    public void testAddToSetAvoidingSubstrings2() {
        Set<String> strSet = new Set1L<String>();
        strSet.add("Hey");
        strSet.add("How are you?");
        strSet.add("I am good, Nathaniel");
        Set<String> check = new Set1L<String>();
        check.add("Hey");
        check.add("How are you?");
        check.add("I am good, Nathaniel");
        String str = "Nathan";
        StringReassembly.addToSetAvoidingSubstrings(strSet, str);
        assertEquals(check, strSet);
    }

    /**
     * test of addToSetAvoidingSubstrings.
     */
    @Test
    public void testAddToSetAvoidingSubstrings3() {
        Set<String> strSet = new Set1L<String>();
        strSet.add("Hey");
        strSet.add("How are you?");
        strSet.add("I am good");
        Set<String> check = new Set1L<String>();
        check.add("Hey");
        check.add("How are you?");
        check.add("I am good Nathan");
        String str = "I am good Nathan";
        StringReassembly.addToSetAvoidingSubstrings(strSet, str);
        assertEquals(check, strSet);
    }

    /**
     * test of linesFromInput.
     */
    @Test
    public void testLinesFromInput0() {
        String fileName = "testFile";
        SimpleWriter fileOut = new SimpleWriter1L(fileName);
        SimpleReader input = new SimpleReader1L(fileName);
        String str = "abcdefgh";
        fileOut.print(str);
        Set<String> check = new Set1L<>();
        check.add("abcdefgh");

        Set<String> result = StringReassembly.linesFromInput(input);

        assertEquals(check, result);
    }

    /**
     * test of linesFromInput.
     */
    @Test
    public void testLinesFromInput1() {
        String fileName = "testFile";
        SimpleWriter fileOut = new SimpleWriter1L(fileName);
        SimpleReader input = new SimpleReader1L(fileName);
        String str = "a\nb\nc\ndefgh";
        fileOut.print(str);
        Set<String> check = new Set1L<>();
        check.add("a");
        check.add("b");
        check.add("c");
        check.add("defgh");

        Set<String> result = StringReassembly.linesFromInput(input);

        assertEquals(check, result);
    }

    /**
     * test of linesFromInput.
     */
    @Test
    public void testLinesFromInput2() {
        String fileName = "testFile";
        SimpleWriter fileOut = new SimpleWriter1L(fileName);
        SimpleReader input = new SimpleReader1L(fileName);
        String str = "a\nb\nc\nd\ne\nf\ng";
        fileOut.print(str);
        Set<String> check = new Set1L<>();
        check.add("a");
        check.add("b");
        check.add("c");
        check.add("d");
        check.add("e");
        check.add("f");
        check.add("g");

        Set<String> result = StringReassembly.linesFromInput(input);

        assertEquals(check, result);
    }

    /**
     * test of printWithLineSeparators.
     */
    @Test
    public void printWithLineSeparators0() {

        String fileName = "testFile";
        SimpleWriter fileOut = new SimpleWriter1L(fileName);
        SimpleReader input = new SimpleReader1L(fileName);
        String str = "abcdefg";
        String check = "abcdefg";
        StringReassembly.printWithLineSeparators(str, fileOut);
        String str1 = "";
        while (!input.atEOS()) {
            str1 += input.nextLine();
            if (!input.atEOS()) {
                str1 += "\n";
            }
        }
        assertEquals(check, str1);
    }

    /**
     * test of printWithLineSeparators.
     */
    @Test
    public void printWithLineSeparators1() {

        String fileName = "testFile";
        SimpleWriter fileOut = new SimpleWriter1L(fileName);
        SimpleReader input = new SimpleReader1L(fileName);
        String str = "a~b~c~d~e~f~g";
        String check = "a\nb\nc\nd\ne\nf\ng";
        StringReassembly.printWithLineSeparators(str, fileOut);
        String str1 = "";
        while (!input.atEOS()) {
            str1 += input.nextLine();
            if (!input.atEOS()) {
                str1 += "\n";
            }
        }
        assertEquals(check, str1);
    }

    /**
     * test of printWithLineSeparators.
     */
    @Test
    public void printWithLineSeparators2() {

        String fileName = "testFile";
        SimpleWriter fileOut = new SimpleWriter1L(fileName);
        SimpleReader input = new SimpleReader1L(fileName);
        String str = "a~b~c~~~defg";
        String check = "a\nb\nc\n\n\ndefg";
        StringReassembly.printWithLineSeparators(str, fileOut);
        String str1 = "";
        while (!input.atEOS()) {
            str1 += input.nextLine();
            if (!input.atEOS()) {
                str1 += "\n";
            }
        }
        assertEquals(check, str1);
    }

}
