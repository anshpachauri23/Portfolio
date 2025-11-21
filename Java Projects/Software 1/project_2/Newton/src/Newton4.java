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
public final class Newton4 {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private Newton4() {
    }

    /**
     * Computes estimate of square root of x to within relative error 0.01%.
     *
     * @param x
     *            positive number to compute square root of
     * @param epsilon
     *            value of epsilon
     * @return estimate of square root
     */
    private static double sqrt(double x, double epsilon) {
        // initializing r which is the initial guess for x^(1/2)

        double r = x;
        if (r != 0) {
            // changing r until its relative error is less than epsilon

            while ((Math.abs((r * r) - x) / x) > (epsilon * epsilon)) {
                r = (r + x / r) / 2.0;
            }
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
        // taking the value of epsilon as the input

        out.print("Enter the value of epsilon: ");
        double epsilon = in.nextDouble();
        boolean choice = true;
        while (choice) {
            // asking the user to enter the number to be square rooted

            out.print("Enter a number: ");
            double num = in.nextDouble();
            // ending the loop if the user enters a negative value

            if (num < 0) {
                choice = false;
            }
            if (num >= 0) {
                double root = sqrt(num, epsilon);
                out.println("Square root of the number is " + root);
            }
        }
        /*
         * Close input and output streams
         */
        in.close();
        out.close();
    }

}
