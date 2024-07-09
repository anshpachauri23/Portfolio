import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Put a short phrase describing the program here.
 *
 * @author Put your name here
 *
 */
public final class Newton1 {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private Newton1() {
    }

    /**
     * Computes estimate of square root of x to within relative error 0.01%.
     *
     * @param x
     *            positive number to compute square root of
     * @return estimate of square root
     */
    private static double sqrt(double x) {
        // initializing r which is the initial guess for x^(1/2)
        double r = x;
        double epsilon = 0.0001;
        // changing r until its relative error is less than epsilon
        while ((Math.abs((r * r) - x) / x) > (epsilon * epsilon)) {
            r = (r + x / r) / 2.0;
        }
        return r;
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
        /*
         * Put your main program code here; it may call myMethod as shown
         */

        String choice = "y";
        while (choice.charAt(0) == 'y') {
            // asking the user to enter the number to be square rooted
            out.print("Enter a positive number: ");
            double num = in.nextDouble();
            // making sure the number entered is positive
            if (num < 0) {
                out.print("Enter a POSITIVE number: ");
                num = in.nextDouble();
            }
            // adding a second if statement so that there is no problem even if the user enters multiple negative numbers
            if (num > 0) {
                double root = sqrt(num);
                out.println("Square root of the number is " + root);
                out.print("Would you like to find another square root? (y/n) ");
                choice = in.nextLine();
            }

        }
        /*
         * Close input and output streams
         */
        in.close();
        out.close();
    }

}
