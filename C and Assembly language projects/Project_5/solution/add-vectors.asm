.text
.global _start

_start:

/* YOUR CODE STARTS HERE */
movq $1, %rbx      /* rbx = 1; */
movq $0, %rcx    /* rcx = 0 */
leaq aa, %rsi    /* Load the address of array aa into rsi */
_loop:
cmpq $5, %rbx  /* Compare the value in rbx with 5 */
jg _end          /* If rbx > 5, jump to the end */
movq (%rsi), %rax /* Load the value at address (rsi) into rax (from array aa) */
movq 40(%rsi), %rdx /* Load the value at address (rsi + 40) into rdx (from array bb) */
imul %rax, %rdx  /* Multiply rax with rdx and store the result in rdx */
addq %rdx, %rcx    /* Add rdx to rcx */
addq $8, %rsi   /* rsi = rsi + 8 (next element of aa and bb) */
incq %rbx        /* rbx++ */
jmp _loop
_end:

/* YOUR CODE ENDS HERE */


/* Prepare for return */
movq $60, %rax   /* system call 60: return */
movq %rcx, %rdi   /* May need to change r8 to some other register. */
syscall

.data
aa: 
  .quad 2
  .quad 1
  .quad 4
  .quad 1
  .quad 8
bb:
  .quad 1
  .quad 1
  .quad 0
  .quad 1
  .quad 2
  
