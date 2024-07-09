#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "a3.h"

#ifndef USE_FGETS
int 
read_word_matrix (char wordmat[MAXN][MAXN], int *nrows, int *ncols)
{
  int car;
  *nrows = 0;
  *ncols = 0;
  
  // Return 0 if no error occurred.
  return 0;
}
#endif

// Function to extract a word from a matrix of words starting at a given position and moving in a specified direction.
// Parameters:
// - wordmat: Matrix of words.
// - nrows, ncols: Number of rows and columns in the matrix of words.
// - ii, jj: Coordinates of the starting cell.
// - di, dj: Direction along rows and columns. Each can be one of {-1, 0, 1}.
// - needle: String to search for.
// - candidate: Extracted string from wordmat that starts at (ii, jj) and is found along the direction (di, dj).
int
extract_word (char wordmat[MAXN][MAXN], // Matrix of words.
  int nrows, int ncols, // Number of rows and columns in the matrix of words.
  int ii, int jj, // Coordinates of starting cell.
  int di, int dj, // Direction along rows and columns. Each can be one of {-1,0,1}.
  char needle[MAXN], // String to search for.
  char candidate[MAXN]) // Extracted string from wordmat that starts at <row,col> and found along direction {-1,0,1}.
{
  int column = jj;
  int row = ii;
  int length = strlen(needle);
  int index = 0;
  candidate[0] = 0;
  // Check if the starting position is within the bounds of the matrix
  if (row < 0 || row >= nrows || column < 0 || column >= ncols)
  {
    return -1; // Return -1 if starting position is out of bounds
  }
  // Iterate over each character in the needle
  for (int i = 0; i < length; i++)
  {
    // Check if the next position is within the bounds of the matrix
    if (row < 0 || row >= nrows || column < 0 || column >= ncols)
    {
      candidate[0] = '\0'; // Set candidate to empty string if out of bounds
      return -1;           // Return -1 if out of bounds
    }
    // Extract character from the matrix at the current position
    candidate[index++] = wordmat[row][column];
    // Move to the next position based on the specified direction
    row += di;
    column += dj;
  }
  candidate[index] = '\0'; // Null-terminate the candidate string
  return 0;                // return 0 to indicate success
}

// Function to search for a single word in a matrix of words.
// Parameters:
// - wordmat: Matrix of words to search in.
// - nrows, ncols: Size of the matrix of words.
// - needle: String to search for in the matrix of words.
int
search_one_word (
  char wordmat[MAXN][MAXN], // Matrix of words to search in.
  int nrows, int ncols, // Size of matrix of words.
  char *needle) // String to search for in the matrix of words.
{
  int di, dj;
  int ii, jj;
  int matches = 0; // Counter for number of matches found
  // Iterate over each cell in the matrix
  for (ii = 0; ii < nrows; ii++)
  {
    for (jj = 0; jj < ncols; jj++)
    {
      // Iterate over all possible directions
      for (di = -1; di <= 1; di++)
      {
        for (dj = -1; dj <= 1; dj++)
        {
          // Skip the case when both di and dj are 0, indicating no movement
          if (di != 0 || dj != 0)
          {
            char candidate[MAXN];
            // Extract a word starting from the current position and moving in the specified direction
            int out = extract_word(wordmat, nrows, ncols, ii, jj, di, dj, needle, candidate);
            // If extraction is successful and the extracted word matches the needle, increment the match counter
            if (out >= 0 && !strcmp(candidate, needle))
            {
              matches++;
            }
          }
        }
      }
    }
  }
  return matches; // Return the total number of matches found
}


// Function to search for multiple words in a matrix of words.
// Parameters:
// - wordmat: Matrix of words to search in.
// - nrows, ncols: Size of the matrix of words.
void
search_words (char wordmat[MAXN][MAXN], int nrows, int ncols)
{
  int num_words;
  scanf("%d", &num_words); // Read the number of words to search for
  int i;
  for (i = 0; i < num_words; i++) // Use 'i' as the loop variable, not 'ii'
  {
    char needle[MAXN];
    scanf("%s", needle); // Read the word to search for
    int matches = search_one_word(wordmat, nrows, ncols, needle); // Search for the word

  // Use the below line to print out the needle string and 
  // the number of matches found in the matrix of words.
    printf("search_words(%s) = %d\n", needle, matches);
  }
}
