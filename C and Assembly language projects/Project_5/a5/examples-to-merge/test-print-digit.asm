.text
.global _start

// Use system call write to print a digit.
_start:
movq $1, %rax    /* System call write=1 expected in rax */
movq $48, %rcx   /* We only print digits. Need to produce ascii character '0':'9'. Add 48+digit_value */
movq $9, %rbx    /* I place the digit to print in rbx */
addq %rcx, %rbx  /* Add ascii code of '0' (48) to current digit value */
movq %rbx, str    /* Copy value in rbx to memory location of str label (a variable) */

movq $1, %rdi    /* Argument to system call write: Use file descriptor 1 = standard output */
movq $str, %rsi  /* Copy address of string to print to rsi */
movq $9, %rdx    /* Need to tell the write system call what is the length of the string to print */
syscall          /* Execute system call */

/* Prepare for return */
movq $60, %rax   /* system call 60: return */
movq $0, %rdi
syscall

.data
str: .ascii "........\n"
