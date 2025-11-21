#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int aa[9]={2,0,4,1,8,9,6,3,7};

const char msg_yes[] = "y \n";
const char msg_no[]  = "n \n";

/*
This program computes the sum of products of two vectors of length 5.
The main function returns the final result.
*/

int must_swap (int x, int y)
{
  return y < x;
}

void sort_from_ii (int ii)
{
  int jj;
  for (jj=ii+1; jj<9; jj++)
  {
    int arg1 = aa[ii];
    int arg2 = aa[jj];
    if (must_swap (arg1,arg2))
    {
      aa[ii] = arg2;
      aa[jj] = arg1;
    }
  }
}

void show_array ()
{
  int ii;
  for (ii=0; ii<9; ii++)
  {
    printf ("%d\n", aa[ii]);
  }
}

int main()
{
  int ii;
  int jj;
  int term;

  printf ("Unsorted array\n");
  show_array ();

  // Task 1: sort.
  // Must use sort_from_ii.
  for (ii=0; ii<9-1; ii++)
  {
    sort_from_ii (ii);
  }

  printf ("Sorted array\n");
  show_array ();

  return ii;
}
