import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Program to generate a glossary with a given input file.
 *
 * @author Ansh Pachauri
 */
public final class Glossary {

    /**
     * Compare {@code String}s in Alphabetical order.
     */
    private static class StringLT implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private Glossary() {
    }

    /**
     * Extracts the terms and their definitions from an input data file. Assumes
     * that each term starts on a new line and is terminated by an empty line.
     * Stores the terms and their definitions in a dictionary-like data
     * structure for easy access, where the terms are used as keys and the
     * definitions are used as values.
     *
     * @param inputFile
     *            The name of the input file containing the terms and their
     *            definitions.
     * @return A Map containing the extracted terms as keys and their
     *         corresponding definitions as values.
     * @ensures termMap contains term -> definition mappings from the input
     *          file.
     */
    public static Map<String, String> termWithDef(String inputFile) {
        Map<String, String> termMap = new Map1L<>();
        SimpleReader in = new SimpleReader1L(inputFile);
        String term = "";
        String definition = "";

        while (!in.atEOS()) {
            String line = in.nextLine();

            if (line.isEmpty()) {
                // Empty line indicates end of a term and its definition
                if (!term.isEmpty()) {
                    termMap.add(term, definition);
                    term = "";
                    definition = "";
                }
            } else {

                // First non-empty line is assumed to be the term
                if (term.isEmpty()) {
                    term = line;
                }
                // Accumulate lines as term definition
                if (definition.isEmpty()) {
                    line = in.nextLine();
                    definition = line;
                } else {
                    definition += "\n" + line;
                }

            }
        }

        in.close();
        return termMap;
    }

    /**
     * Sorts the terms in ascending order alphabetically after storing them in a
     * Queue.
     *
     * @param termMap
     *            The Map containing the terms as keys and their definitions as
     *            values.
     * @return A Queue with the terms sorted in ascending order alphabetically.
     * @ensures The terms in the returned Map are sorted in ascending order
     *          alphabetically in a queue.
     */
    public static Queue<String> sortTermsAlphabetically(
            Map<String, String> termMap) {
        // Create a new Map to store the sorted terms
        Map<String, String> temp = termMap.newInstance();
        temp.transferFrom(termMap);

        // Convert the keys of the input termMap to a Queue for sorting
        Queue<String> termQueue = new Queue1L<>();
        while (temp.size() > 0) {
            Map.Pair<String, String> tempPair = temp.removeAny();
            String key = tempPair.key();
            String value = tempPair.value();
            termQueue.enqueue(key);
            termMap.add(key, value);
        }

        Comparator<String> order = new StringLT();
        termQueue.sort(order);

        return termQueue;
    }

    /**
     * Creates the top-level index file and separate HTML files for each term in
     * the given Map of terms and definitions.
     *
     * @param termMap
     *            The Map containing the terms as keys and their definitions as
     *            values.
     * @param outputFolder
     *            The output folder where the top-level index file and term HTML
     *            files will be created.
     * @param termQueue
     *            Queue with sorted keys
     * @requires termMap and outputFolder are not null.
     * @ensures The top-level index file and term HTML files are created in the
     *          specified output folder.
     */
    public static void createHTMLFiles(Map<String, String> termMap,
            String outputFolder, Queue<String> termQueue) {
        // Create the top-level index file
        String indexFileName = outputFolder + "/index.html";
        SimpleWriter indexWriter = new SimpleWriter1L(indexFileName);

        // Write HTML code for the top-level index file
        indexWriter.println("<html>");
        indexWriter.println("<head><title>Glossary Index</title></head>");
        indexWriter.println("<body>");
        indexWriter.println("<h1>Glossary Index</h1>");
        indexWriter.println("<hr>");
        indexWriter.println("<h3>Index</h3>");
        indexWriter.println("<ul>");

        // Create a separate HTML file for each term

        while (termQueue.length() > 0) {
            String term = termQueue.dequeue();
            String termFileName = outputFolder + "/" + term + ".html";
            SimpleWriter termWriter = new SimpleWriter1L(termFileName);

            // Write HTML code for the term file
            termWriter.println("<html>");
            termWriter.println("<head><title>" + term + "</title></head>");
            termWriter.println("<body>");
            termWriter.println("<h1><font color=\"red\"><b><i>" + term
                    + "</i></b></font></h1>");
            termWriter.println(
                    "<p>" + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp"
                            + termMap.value(term) + "</p>");
            termWriter.println("<hr>");
            // Prints a link to return to the index
            termWriter.println(
                    "<p>Return to <a href=\"index.html\">index</a>.</p>");
            termWriter.println("</body>");
            termWriter.println("</html>");

            termWriter.close();

            // Add a link to the term file in the top-level index file
            indexWriter.println(
                    "<li><a href=\"" + term + ".html\">" + term + "</a></li>");
        }
        indexWriter.println("</ul>");
        indexWriter.println("</body>");
        indexWriter.println("</html>");

        indexWriter.close();
    }

    /**
     * Check each definition for terms that appear in the glossary and replace
     * them with hyperlinks to the corresponding term page.
     *
     * @param termMap
     *            The Map containing the terms as keys and their definitions as
     *            values.
     * @requires termMap and outputFolder are not null.
     * @ensures Definitions in termMap are updated to replace terms with
     *          hyperlinks to the corresponding term pages.
     */
    public static void replaceTermsWithLinks(Map<String, String> termMap) {

        Queue<String> termQueue = sortTermsAlphabetically(termMap);

        for (String term : termQueue) {
            String definition = termMap.value(term);
            String[] words = definition.split(" ");
            for (String word : words) {
                for (String terms : termQueue) {
                    String termPageLink = "<a href=\"" + terms + ".html\">"
                            + terms + "</a>";
                    if (word.equals(terms) || word.equals(terms + ",")) {
                        definition = definition.replace(terms, termPageLink);
                    }
                }
            }
            termMap.replaceValue(term, definition);
        }
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleWriter out = new SimpleWriter1L();
        SimpleReader in = new SimpleReader1L();

        Queue<String> termQueue = new Queue1L<>();

        // Get input file path from user
        out.print("Enter input file path: ");
        String inputFile = in.nextLine();

        // Generate term map from input file
        Map<String, String> termMap = termWithDef(inputFile);

        // Sort terms alphabetically
        termQueue = sortTermsAlphabetically(termMap);

        // Create output folder
        out.print("Enter output folder path: ");
        String outputFolder = in.nextLine();
        // Replace terms with links
        replaceTermsWithLinks(termMap);
        createHTMLFiles(termMap, outputFolder, termQueue);

        // Display success message
        out.println("Glossary generated successfully!");
    }
}
