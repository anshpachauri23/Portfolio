#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

/*
This program computes the sum of products of two vectors of length 5.
The main function returns the final result.
*/

int main()
{
  int aa[5]={2,0,4,1,8};
  int bb[5]={1,1,0,1,0};
  int ii;
  int sum=0;
  int term;
  for (ii=0; ii<5; ii++){
    term=aa[ii]*bb[ii];
    sum=sum+term;
  }
  return sum;
}
