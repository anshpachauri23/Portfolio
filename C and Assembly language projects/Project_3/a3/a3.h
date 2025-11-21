
#ifndef MAXN
#define MAXN 16
#endif

int 
read_word_matrix (char wordmat[MAXN][MAXN], int *nrows, int *ncols);


void
show_word_matrix (char wordmat[MAXN][MAXN], int nrows, int ncols);

int
extract_word (char wordmat[MAXN][MAXN],
  int nrows, int ncols,
  int ii, int jj,
  int di, int dj,
  char needle[MAXN],
  char candidate[MAXN]);

int
search_one_word (
  char wordmat[MAXN][MAXN],
  int nrows, int ncols, 
  char *needle);

void
search_words (char wordmat[MAXN][MAXN], int nrows, int ncols);


void
print_ascii_codes ();
