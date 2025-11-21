#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

/*
Count the number of prime numbers between 2 and 10.
The program returns the number of prime numbers found.
A prime number is a number whose only exact divisors are 1 and the same number.
*/
int main()
{
  int nn;
  int dd;
  int count;
  int nprimes=0;
  for (nn=2; nn<=10; nn++) {
    count=0;
    for (dd=1; dd<=nn; dd++){
      if (nn % dd == 0) {
        count++;
      }
    }
    if (count == 2)
      nprimes++;
  }
  return nprimes;
}
