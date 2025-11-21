#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

/*
This program prints the all the digits between '0' and '9'.
The program uses the system call write by updating the
character at position 7 of the string msg.
*/

int main()
{
  int ii;
  char msg[10] = "........\n";
  for (ii=0; ii<10; ii++) {
    msg[7] = '0' + ii; 
    write (1, msg, 9);
  }
  return 0;
}
