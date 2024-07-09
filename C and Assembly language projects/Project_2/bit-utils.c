#include <stdio.h>
#include "a2.h"

void
print_in_binary (unsigned int n)
{
  unsigned int div = 1;
  unsigned nbits = 1;
  while ((div << 1) < n) {
    div = div << 1;
    nbits ++;
  }

  int ii;

  printf ("%u ==> ", n);
  // NOTE: Old, buggy way of printing the binary.

  //#define USE_BUGGY
  #ifdef USE_BUGGY
  for (ii = 0; ii < 32 - nbits; ii++) {
    if (ii % 8 == 0)
      printf ("| ");
    printf ("0 ");
  }

  while (1)
  {
    unsigned int digit = n / div;
    if (ii % 8 == 0)
      printf ("| ");
    printf ("%d ", digit);
    n = n % div;
    div = div / 2;
    ii++;
    if (div == 0)
      break;
  }
  printf ("\n");
  #else
  // NOTE:
  // Bug reported on 02/07/24.
  // Code fixed on 02/08/24. 
  // Courtesy of a student.
  // Exercise for everyone: Contrast the below code with the one above.
  for (ii=32-1; ii>=0; ii--){
    if ((ii+1) % 8 == 0)
      printf ("| ");
    printf ("%d ", n & (1 << ii) ? 1 : 0);
  }
  printf ("\n");
  #endif
}
