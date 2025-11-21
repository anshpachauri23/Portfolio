import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine1L;
import components.utilities.FormatChecker;

/**
 * Program to take a file of text, count each instance of every word, and
 * generate a tag cloud with each word and corresponding size.
 *
 * @author Daniil Gofman
 * @author Ansh Pachauri
 *
 */
public final class TagCloudGenerator {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private TagCloudGenerator() {
    }

    /**
     * The maximum font size.
     */
    private static final int FONT_NUMBER = 37;
    /**
     * The maximum font size.
     */
    private static final int MIN_FONT_SIZE = 11;

    /**
     * Compare {@code String}s in alphabetical order.
     */
    private static class StringLT
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> str1,
                Map.Pair<String, Integer> str2) {
            //lower case and compare alphabetically
            return str1.key().toLowerCase().compareTo(str2.key().toLowerCase());
        }
    }

    /**
     * Compare {@code Integer}i in decreasing order.
     */
    private static class IntegerLT
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> int1,
                Map.Pair<String, Integer> int2) {
            return int2.value().compareTo(int1.value());
        }
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         * at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     * text[position, position + |nextWordOrSeparator|) and
     * if entries(text[position, position + 1)) intersection separators = {} * then
     * entries(nextWordOrSeparator) intersection separators = {} and
     * (position + |nextWordOrSeparator| = |text| or
     * entries(text[position, position + |nextWordOrSeparator| + 1))
     * intersection separators /= {})
     * else
     * entries(nextWordOrSeparator) is subset of separators and
     * (position + |nextWordOrSeparator| = |text| or
     * entries(text[position, position + |nextWordOrSeparator| + 1))
     * is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {

        int end = position;
        if (!separators.contains(text.charAt(position))) {
            //find length of word
            while (end < text.length()
                    && !separators.contains(text.charAt(end))) {
                end++;
            }
        } else {
            //find length of separator
            while (end < text.length()
                    && separators.contains(text.charAt(end))) {
                end++;
            }
        }
        return text.substring(position, end);
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param strSet
     *            the {@code Set} to be replaced
     * @replaces strSet
     * @ensures strSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> strSet) {
        assert str != null : "Violations of: str is not null";
        assert strSet != null : "Violation of: strSet is not null";

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!strSet.contains(c)) {
                strSet.add(c);
            }
        }
    }

    /**
     * Takes the file and makes a map with each key as the word in lower case
     * and the value as the number of occurrences of that word.
     *
     * @param inFile
     *            the SimpleReader file
     * @ensures all words from the file will be in the map with the count of
     *          each word
     * @return Map<String, Integer> of words of the file and their counts
     */
    private static Map<String, Integer> fileToMap(SimpleReader inFile) {
        assert inFile != null : "Violation of: inFile is not null";

        Map<String, Integer> result = new Map1L<>();
        // define separators for the words
        final String separators = "'., ()-_?\"/!@#$%^&*\t1234567890:"
                + ";[]{}+=~`><";
        Set<Character> separatorSet = new Set1L<>();
        generateElements(separators, separatorSet);

        // read file, separate words and compile all words into the map
        while (!inFile.atEOS()) {
            String line = inFile.nextLine();
            int i = 0;
            while (i < line.length()) {
                String word = nextWordOrSeparator(line, i, separatorSet)
                        .toLowerCase();
                boolean isWord = true;
                for (int j = 0; j < word.length(); j++) {
                    char c = word.charAt(j);
                    if (separatorSet.contains(c)) {
                        isWord = false;
                    }
                }
                if (isWord) {
                    //add word to map or increase the count
                    if (result.hasKey(word)) {
                        int count = result.value(word);
                        result.replaceValue(word, count + 1);
                    } else {
                        result.add(word, 1);
                    }
                }
                i += word.length();
            }
        }
        return result;
    }

    /**
     * Takes a map and first sorts the it with sortingMachine by occurrences of
     * each word in decreasing order. Then makes a second sortingMachine, and
     * sorts the map alphabetically. numDisplay is the number of words to be put
     * in first sortingMachine.
     *
     * @param map
     *            map of all of the words and their counts
     * @param numDisplay
     *            the amount of words that will be in the first sortingMachine
     * @requires map is not null
     * @ensures all Map.Pairs is sorted in decreasing order by their values
     * @return SortingMachine<Map.Pair<String, Integer>> of words and their
     *         counts in alphabetical order
     */
    public static SortingMachine<Map.Pair<String, Integer>> mapToSMAlpha(
            Map<String, Integer> map, Integer numDisplay) {
        assert map != null : "Violation of: words is not null";

        Comparator<Map.Pair<String, Integer>> countSort = new IntegerLT();
        Comparator<Map.Pair<String, Integer>> alphaSort = new StringLT();
        SortingMachine<Map.Pair<String, Integer>> countSM = new SortingMachine1L<>(
                countSort);
        SortingMachine<Map.Pair<String, Integer>> alphaSM = new SortingMachine1L<>(
                alphaSort);

        //make the sorting machine with the map
        while (map.size() > 0) {
            Map.Pair<String, Integer> temp = map.removeAny();
            countSM.add(temp);
        }
        countSM.changeToExtractionMode();

        //make the sorting machine in alphabetical order
        for (int i = 0; i < numDisplay; i++) {
            if (countSM.size() != 0) {
                alphaSM.add(countSM.removeFirst());
            }
        }
        alphaSM.changeToExtractionMode();

        return alphaSM;

    }

    /**
     * Output the header of the file.
     *
     * @param out
     *            the output file.
     *
     * @param inFile
     *            the input file.
     *
     * @param numWords
     *            number of words in the HTML file.
     */
    private static void header(SimpleWriter out, String inFile, int numWords) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";
        assert inFile != null : "Violation of: inFile is not null";
        /*
         * Output the index header HTML text.
         */
        out.println("<html>");
        out.println("<head>");
        out.println(
                "<title>Top " + numWords + " words in " + inFile + "</title>");
        out.println("<link href=\"http://web.cse.ohio-state.edu/software"
                + "/2231/web-sw2/assignments/projects/tag-cloud-generator/data/"
                + "tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println(
                "<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Top " + numWords + " words in " + inFile + "</h2>");
        out.println("<hr>");
        out.println("<div class = \"cdiv\">");
        out.println("<p class = \"cbox\">");
    }

    /**
     * Output the ending tags in the generated HTML file.
     *
     * @param output
     *            the output file.
     */
    private static void footer(SimpleWriter output) {
        assert output != null : "Violation of: out is not null";
        assert output.isOpen() : "Violation of: out.is_open";
        //output the closing tags
        output.println("</p>");
        output.println("</div>");
        output.println("</body>");
        output.println("</html>");
    }

    /**
     * Method to generate the main body of 'tag cloud'.
     *
     * @param output
     * @param alphaSort
     */
    private static void generateList(SimpleWriter output,
            SortingMachine<Map.Pair<String, Integer>> alphaSort) {
        int countMax = 0;
        int countMin = 100;
        for (Map.Pair<String, Integer> i : alphaSort) {
            if (i.value() > countMax) {
                countMax = i.value();
            }
            if (i.value() < countMin) {
                countMin = i.value();
            }
        }
        int difference = countMax - countMin;
        int interDifference = difference / FONT_NUMBER;
        if (interDifference == 0) {
            interDifference = 1;
        }

        while (alphaSort.size() > 0) {
            Map.Pair<String, Integer> temp = alphaSort.removeFirst();
            int font = ((temp.value() - countMin) / interDifference)
                    + MIN_FONT_SIZE;
            output.println("<span style=\"cursor:default\" class=\"f" + font
                    + "\"title = \"count:" + temp.value() + "\">" + temp.key()
                    + "</span>");
        }
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
        out.print("Enter the name of the input text file: ");
        String inputFile = in.nextLine();
        SimpleReader input = new SimpleReader1L(inputFile);

        out.print("Enter the name of the output file: ");
        String outputFile = in.nextLine();
        SimpleWriter output = new SimpleWriter1L(outputFile);

        out.print(
                "Enter number of words to be included in the generated tag cloud: ");
        String wordNumStr = in.nextLine();
        while (!FormatChecker.canParseInt(wordNumStr)
                || !(Integer.parseInt(wordNumStr) > 0)) {
            out.print("ERROR: Not Positive Integer \nPlease enter the "
                    + "number of words that will be displayed (integer): ");
            wordNumStr = in.nextLine();
        }
        int wordNum = Integer.parseInt(wordNumStr);

        // Output header of a html-file
        header(output, inputFile, wordNum);
        // Output body of a html-file
        Map<String, Integer> map = fileToMap(input);

        SortingMachine<Map.Pair<String, Integer>> alphaSort = mapToSMAlpha(map,
                wordNum);

        generateList(output, alphaSort);
        // Output footer of a html-file
        footer(output);
        out.println("Program completed");

        /*
         * Close input and output streams
         */
        input.close();
        output.close();
        in.close();
        out.close();
    }

}
