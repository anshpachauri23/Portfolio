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
  ff = fopen(filename, "r");
  if (!ff) {
   printf("Error opening file %s\n", filename);
   return NULL;
  } 

  int num_courses;
  // Read the number of courses from the file
  if (fscanf(ff, "%d", &num_courses) != 1) {
   printf("Error reading number of courses from file\n");
   fclose(ff);
   return NULL;
  }
  // Allocate memory for storing course information
  ret = (t_course_info **)malloc_wrapper((num_courses + 1) * sizeof(t_course_info *));
  if (!ret) {
   printf("Error allocating memory for course info\n");
   fclose(ff);
   return NULL;
  }
  // Read course information for each course
  int i;
  for (i = 0; i < num_courses; i++) {
   ret[i] = (t_course_info *)malloc_wrapper(sizeof(t_course_info));
   if (!ret[i]) {
    printf("Error allocating memory for course info\n");
    fclose(ff);
    return NULL;
   }
   // Allocate memory for storing course code
   ret[i]->code = (char *)malloc_wrapper(32 * sizeof(char)); // Assuming maximum length of course code is 32 characters
   if (!ret[i]->code) {
    printf("Error allocating memory for course code\n");
    fclose(ff);
    return NULL;
   }
   // Read course code and number of grades
   if (fscanf(ff, "%s %d", ret[i]->code, &(ret[i]->n_grades)) != 2) {
    printf("Error reading course info from file\n");
    fclose(ff);
    return NULL;
   }
   // Allocate memory for storing grades
   ret[i]->grades = (int *)malloc_wrapper(ret[i]->n_grades * sizeof(int));
   if (!ret[i]->grades) {
    printf("Error allocating memory for grades\n");
    fclose(ff);
    return NULL;
   }
   // Read grades for the course
   int j;
   for (j = 0; j < ret[i]->n_grades; j++) {
    if (fscanf(ff, "%d", &(ret[i]->grades[j])) != 1) {
     printf("Error reading grades from file\n");
     fclose(ff);
     return NULL;
    }
   }
  }

  ret[num_courses] = NULL; // Null-terminate the array 

  fclose(ff);
  return ret;
}


void free_structure (t_course_info ** info)
{
  int bytes;
  if (!info)
    return;
  // Free memory for each course entry and associated data
  int i = 0;
  for (i = 0; info[i]; i++) {
      free_wrapper(info[i]->code, strlen(info[i]->code) + 1);
      free_wrapper(info[i]->grades, info[i]->n_grades * sizeof(int));
      free_wrapper(info[i], sizeof(t_course_info));
  }
  free_wrapper(info, allocated_total); // Free memory for the array of course info pointers
  // Must use many times:
  // free_wrapper (something, bytes);
}

void sort (t_course_info ** info)
{
  t_course_info * ci, * cj, * temp;
  int i = 0;
  while (info[i] != NULL) {
    int j = i + 1;
    while (info[j] != NULL) {
      if (strcmp(info[i]->code, info[j]->code) > 0) {
        temp = info[i];
        info[i] = info[j];
        info[j] = temp;
      }
      j++;
    }
    i++;
  }
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
