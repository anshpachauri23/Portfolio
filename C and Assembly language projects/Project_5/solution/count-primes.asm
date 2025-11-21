.text
.global _start

_start:

/* YOUR CODE STARTS HERE */
movq $2, %rbx      /* rbx <- 2; */
movq $11, %r10
movq $0, %rcx    /* rcx = 0 */
movq $0, %rdx

_loop:
cmpq %r10, %rbx  /* Compare rbx with r10 */
jg _end          /* Jump to _end if rbx is greater than r10 */
movq %rbx, %rax  /* rax = rbx */
movq $0, %rdx    /* rdx = 0 */
divq %rbx        /* Divide rax by rbx, rdx will contain the remainder */
cmpq $0, %rdx    /* is mod 0? */
jne _skip
incq %rcx
_skip:
incq %rbx        /* rbx++ */
jmp _loop
_end:


/* YOUR CODE ENDS HERE */


/* Prepare for return */
movq $60, %rax   /* system call 60: return */
movq %rcx, %rdi   /* May need to change r8 to some other register. */
syscall
