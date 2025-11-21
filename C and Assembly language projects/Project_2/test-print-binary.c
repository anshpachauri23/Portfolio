#include <stdio.h>
#include "a2.h"


int main ()
{
  print_in_binary (5);
  print_in_binary (10);
  print_in_binary (1 << 31); // = print_in_binary (2147483648);
  print_in_binary (2);
  print_in_binary ((15 << 8) + (15 << 16));

  return 0;
}
