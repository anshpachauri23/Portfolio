.text
.global _start

_start:

/* YOUR CODE STARTS HERE */



/* YOUR CODE ENDS HERE */


/* Prepare for return */
movq $60, %rax   /* system call 60: return */
movq $0, %rdi   /* May need to change r8 to some other register. */
syscall
