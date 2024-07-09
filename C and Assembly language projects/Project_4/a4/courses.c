#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <malloc.h>

int allocated_total = 0;

struct s_course_info {
  char * code;
  int * grades;
  int n_grades;
};

typedef struct s_course_info t_course_info;

void * malloc_wrapper (int nbytes)
{
  allocated_total += nbytes;
  void * ret = malloc (nbytes);
  return ret;
}

void
free_wrapper (void * mem, int bytes_to_free)
{
  allocated_total -= bytes_to_free;
  free (mem);
}


void show_structure (t_course_info ** info)
{
  if (!info)
    return;
  printf ("\nShowing course info: \n");
  t_course_info * entry;
  int ii;
  for (ii = 0; info[ii]; ii++)
  {
    entry = info[ii];
    printf ("Course [%s] : ", entry->code);
    int gg;
    for (gg = 0; gg < entry->n_grades; gg++)
    {
      printf ("%d ", entry->grades[gg]);
    }
    printf ("\n");
  }
}


t_course_info **
read_from_file (const char * filename)
{
  FILE * ff;
  t_course_info ** ret = NULL;


  return ret;
}


void free_structure (t_course_info ** info)
{
  int bytes;
  if (!info)
    return;

  // Must use many times:
  // free_wrapper (something, bytes);
}

void sort (t_course_info ** info)
{
  t_course_info * ci, * cj, * temp;
}

int main ()
{
  t_course_info ** info = NULL;

  printf ("Before malloc : %d\n", allocated_total);

  printf ("Read input file name: ");
  char filename[32];
  scanf ("%s", filename);

  info = read_from_file (filename);
  if (!info)
    return 42;

  show_structure (info);

  sort (info);

  show_structure (info);

  // Simpler test:
  //int * arr = malloc_wrapper (4 * 10);

  printf ("After malloc : %d\n", allocated_total);

  // Simpler test:
  //free_wrapper (arr, 40);
  free_structure (info);

  printf ("After free : %d\n", allocated_total);

  return 0;
}
