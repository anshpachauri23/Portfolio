.text
.global _start

_start:

/* YOUR CODE STARTS HERE */
_loop_start:

movq $0, %rbx


_loop_condition:

movq $48, %rcx   /* ASCII value of '0' */
addq %rbx, %rcx /* Add current value of rbx to get the digit character */
movq %rcx, str /* Store the digit character in the string */

movq $1, %rax /* syscall number for sys_write */
movq $1, %rdi      /* Argument to system call write: Use file descriptor 1 = standard output */
movq $str, %rsi    /* pointer to the string */
movq $9, %rdx      /* number of bits to write */
syscall            /* Execute system call */

incq %rbx

movq $10, %rcx
cmpq %rbx, %rcx /* Compare rbx with 10 */
je _loop_end

jmp _loop_condition

_loop_end:

/* YOUR CODE ENDS HERE */


/* Prepare for return */
movq $60, %rax   /* system call 60: return */
movq $0, %rdi   /* May need to change r8 to some other register. */
syscall

.data
str:.ascii "........\n"
