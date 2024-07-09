#include <stdio.h>
#include <math.h>

// All user-defined headers must appear here.
double rectangle_area(double left, double right);
double approx_area(double upper_limit, double lower_limit, int intervals);
double area_x2(double d_prec, double d_lower, double d_upper);

// From here on, no headers are accepted. Only function definitions.

//This function calculates the area of a rectangle given the left and right bounds.
double rectangle_area(double left, double right){
  double midpoint = ((left*left) + (right*right)) / 2.0;
  return midpoint * (right - left);
}
//This function estimates the area under the curve by dividing it into rectangles and summing their areas.
double approx_area(double upper_limit, double lower_limit, int intervals){
  double width = (upper_limit - lower_limit) / intervals;
  double area = 0.0;
  for(int i = 0; i < intervals; i++){
    area += rectangle_area(lower_limit + (i * width), lower_limit + ((i+1)*width));
  }
  return area;
}
//takes the precision, lower and upper bounds of integration. It iteratively calculates the area under the curve
//using approx_area until the difference between successive estimates is less than the specified precision.
//It prints the iteration count and the final and penultimate area estimates.
double area_x2 (double d_prec, double d_lower, double d_upper)
{
  int i_intervals;
  double d_ret = 10000000;
  double d_ret_prev = 2;
  double d_left, d_right;

  // Example of useful comment:
  // Handle the two illegal cases mentioned in the instructions
  // of assignment 1.
  // If any of the two illegal cases are triggered, then we 
  // assign the expected values and jump straight to printing
  // the output.

  if(d_lower <= d_upper || d_prec <= 0){
    d_ret_prev = -1.0;
    d_ret = -2.0;
    goto print_result;
  }
  for(i_intervals = 1; fabs (d_ret_prev - d_ret) > d_prec; i_intervals++){
    if(i_intervals >= 10000000){
      d_ret_prev = -1.0;
      d_ret = -2.0;
      goto print_result;
    }
    d_ret_prev = d_ret;
    d_ret = approx_area(d_upper, d_lower, i_intervals);
  }  
  
  
  print_result:
  // Warning to all students: DO NOT CHANGE THE LINE BELOW.
  printf ("%d %.10lf %.10lf %.10lf\n", i_intervals, d_prec, d_ret_prev, d_ret);

  return d_ret;
}

int main ()
{
  double d_lower;
  double d_upper;
  double d_prec;

  //printf ("Enter lower and upper values for x, and desired precision: ");
  scanf ("%lf %lf %lf", &d_lower, &d_upper, &d_prec);
  double d_area = area_x2 (d_prec, d_lower, d_upper);
  //printf ("Area under square curve between [%lf,%lf]: %lf\n", d_lower, d_upper, d_area);

  return 0;
}
