import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Project 10.
 *
 * @author Daniil Gofman
 * @author Ansh Pachauri
 *
 */
public final class TagCloudGeneratorJCF {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private TagCloudGeneratorJCF() {
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
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> str1,
                Map.Entry<String, Integer> str2) {

            String s1 = str1.getKey();
            String s2 = str2.getKey();

            int compare = s1.compareToIgnoreCase(s2);

            if (compare == 0) {
                compare = s1.compareTo(s2);
            }
            return compare;
        }
    }

    /**
     * Compare {@code Integer}i in decreasing order.
     */
    private static class IntegerLT
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> int1,
                Map.Entry<String, Integer> int2) {

            Integer i1 = int1.getValue();
            Integer i2 = int2.getValue();

            int compare = i2.compareTo(i1);

            if (compare == 0) {
                compare = int1.getKey().compareTo(int2.getKey());
            }
            return compare;
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
     * @param input
     *            the BufferedReader file
     * @ensures all words from the file will be in the map with the count of
     *          each word
     * @return Map<String, Integer> of words of the file and their counts
     * @throws IOException
     */
    private static Map<String, Integer> fileToMap(BufferedReader input)
            throws IOException {
        assert input != null : "Violation of: inFile is not null";

        Map<String, Integer> result = new HashMap<>();
        // define separators for the words
        final String separators = "'., ()-_?\"/!@#$%^&*\t1234567890:"
                + ";[]{}+=~`><";
        Set<Character> separatorSet = new HashSet<>();
        generateElements(separators, separatorSet);
        try {
            // read file, separate words and compile all words into the map
            String line = input.readLine();
            while (line != null) {
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
                        if (result.containsKey(word)) {
                            int count = result.get(word);
                            result.replace(word, count + 1);
                        } else {
                            result.put(word, 1);
                        }
                    }
                    i += word.length();
                }
                line = input.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error reading data from the file");
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
     * @ensures all Map.Entrys is sorted in decreasing order by their values
     * @return SortingMachine<Map.Entry<String, Integer>> of words and their
     *         counts in alphabetical order
     */
    public static List<Map.Entry<String, Integer>> mapToListAlpha(
            Map<String, Integer> map, Integer numDisplay) {
        assert map != null : "Violation of: map is not null";

        // Comparator for sorting by count
        Comparator<Map.Entry<String, Integer>> countSort = new IntegerLT();

        // Comparator for sorting alphabetically by keys
        Comparator<Map.Entry<String, Integer>> alphaSort = new StringLT();

        // Create a List to store map entries
        List<Map.Entry<String, Integer>> listInt = new ArrayList<>();

        // Add all entries from the map to the list
        listInt.addAll(map.entrySet());

        // Sort the list by count using the countSort comparator
        Collections.sort(listInt, countSort);

        // Create a sublist containing the specified number of entries to display
        List<Map.Entry<String, Integer>> listAlpha = listInt.subList(0,
                numDisplay);

        // Sort the sublist alphabetically using the alphaSort comparator
        Collections.sort(listAlpha, alphaSort);

        // Return the final sorted list
        return listAlpha;
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
    private static void header(PrintWriter out, String inFile, int numWords) {
        assert out != null : "Violation of: out is not null";
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
    private static void footer(PrintWriter output) {
        assert output != null : "Violation of: out is not null";
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
    private static void generateList(PrintWriter output,
            List<Map.Entry<String, Integer>> alphaSort) {
        // Initialize a variable to keep track of the maximum count
        int countMax = 0;

        // Define a constant for the minimum value
        final int min = 100;

        // Initialize a variable to keep track of the minimum count
        int countMin = min;

        // Iterate through the entries in the 'alphaSort' map
        for (Map.Entry<String, Integer> i : alphaSort) {
            /*
             * Check if the current entry's value is greater than the current
             * maximum count
             */
            if (i.getValue() > countMax) {
                // Update the maximum count if the condition is met
                countMax = i.getValue();
            }

            // Check if the current entry's value is less than the current minimum count
            if (i.getValue() < countMin) {
                // Update the minimum count if the condition is met
                countMin = i.getValue();
            }
        }

        // Calculate the difference between the maximum and minimum counts
        int difference = countMax - countMin;

        // Calculate an intermediate difference
        int interDifference = difference / FONT_NUMBER;

        // If the intermediate difference is zero, set it to 1 to avoid division by zero
        if (interDifference == 0) {
            interDifference = 1;
        }

        // Iterate through entries in alphaSort while it's not empty
        while (alphaSort.size() > 0) {
            // Remove and retrieve the first entry
            Map.Entry<String, Integer> temp = alphaSort.remove(0);

            // Calculate the font size based on the entry's value
            int font = ((temp.getValue() - countMin) / interDifference)
                    + MIN_FONT_SIZE;

            // Print HTML code with the calculated font size and additional information
            output.println("<span style=\"cursor:default\" class=\"f" + font
                    + "\"title=\"count:" + temp.getValue() + "\">"
                    + temp.getKey() + "</span>");
        }
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in))) {
            System.out.print("Enter the name of the input text file: ");
            String inputFile = in.readLine();

            try (BufferedReader input = new BufferedReader(
                    new FileReader(inputFile))) {
                System.out.print("Enter the name of the output file: ");
                String outputFile = in.readLine();

                try (PrintWriter output = new PrintWriter(
                        new BufferedWriter(new FileWriter(outputFile)))) {
                    System.out.print("Enter number of words to be included "
                            + "in the generated tag cloud: ");
                    String wordNumStr = in.readLine();

                    if (wordNumStr != null) {
                        int wordNum = Integer.parseInt(wordNumStr);
                        // Output header of an html-file
                        header(output, inputFile, wordNum);
                        // Output body of an html-file
                        Map<String, Integer> map = fileToMap(input);
                        List<Map.Entry<String, Integer>> alphaSort = mapToListAlpha(
                                map, wordNum);
                        generateList(output, alphaSort);
                        // Output footer of an html-file
                        footer(output);
                        System.out.println("Program completed");
                    }
                }
            } catch (IOException e) {
                System.err.printf("Error %s occurred", e.getMessage());
            }

        } catch (IOException e) {
            System.err.printf("Error %s occurred", e.getMessage());
        }
    }

}
