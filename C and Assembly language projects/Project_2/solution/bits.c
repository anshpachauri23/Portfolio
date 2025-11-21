#include <stdio.h>
#include "a2.h"


// Write and use a function to count the minimum number of bits needed
// to represent an unsigned int.
unsigned int count_bits (unsigned int pat)
{
  unsigned int count = 0;
  while (pat != 0) {
    pat >>= 1; // Shift the number to the right by 1 bit
    count++;   // Increment count for each bit shifted
  }
  return count;
}

// Function should return the number of 'needle' bits found
// in the parameter num.
// Argument needle must always be zero or one. 
unsigned int count_bit_type (unsigned int num, unsigned int needle)
{
  unsigned int count = 0;
  unsigned int i = 0;
  while (i < sizeof(int) * 8) {
    count += num % 2 == needle; // Check if the least significant bit matches 'needle'
    num >>= 1; // Shift the number to the right by 1 bit
    i++;
  }
  return count;
}

// Find the needle pattern in the haystack number.
// Return the number of times the needle pattern repeats.
unsigned int 
find_pattern (unsigned int haystack, unsigned int needle)
{
  unsigned int pattern_size = count_bits(needle);
  unsigned int count = 0;
  unsigned int bit_pos;

  for (bit_pos = 0; bit_pos <= 32 - pattern_size; bit_pos++) {
    unsigned int temp = ((haystack << (32 - bit_pos)) >> (32 - bit_pos)) >> (bit_pos - pattern_size); // Extract 'pattern_size' bits starting from bit 'bit_pos'
    if (needle == temp) {
      count++;
      bit_pos += pattern_size; // Move 'bit_pos' to the next position after the pattern
    }

  }
  return count;
}

// Invert the input number nin as per the instructions of assignment 2.
unsigned int
invert_32bit (unsigned int nin)
{
  unsigned int result;
  result = (nin >> 16) | (nin << 16);
  return result;
}


void
translate_bits (char * filename)
{
  int num_tests;
  int ii;
  FILE * ff = NULL;
  // COMPLETE HERE: Open the file for reading.
  ff = fopen(filename, "r");
  // COMPLETE HERE: Abort the function if NULL is returned.
  if(!ff)
    return;
  // COMPLETE HERE: Read the number of test cases from the file.
  fscanf(ff, "%d", &num_tests);
  // Loop for processing. Process num_tests inputs from the text file.
  for (ii = 0; ii < num_tests; ii++)
  {
    unsigned int val;
    unsigned int needle, min_bits, min_matches;
    // COMPLETE HERE: Read four unsigned int from the file.
    fscanf(ff, "%u %u %u %u", &val, &needle, &min_bits, &min_matches);
    // Your functions should be working at this point.
    unsigned int out_val = val;
    unsigned int nb = count_bit_type (val, 1);
    unsigned int matches = find_pattern (val, needle);

    if (nb >= min_bits && matches >= min_matches)
    {
      out_val = invert_32bit (val);
    }

    // Do not change the line below.
    printf ("translate_bits(testcase=%d,haystack=%u,needle=%u,on_bits=%u,matches=%u) ==> %u\n", ii+1, val, needle, min_bits, min_matches, out_val);

    //NOTE: You can use the below functions, but only for debugging.
    //Once you are done debugging, re-comment any additional printf.

    //print_in_binary (val);
    //print_in_binary (out_val);
    //printf ("\n");
  }

  if (ff)     
    fclose (ff);
}
