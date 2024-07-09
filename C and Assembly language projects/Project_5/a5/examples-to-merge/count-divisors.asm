.text
.global _start

_start:

_loop_start:
movq $1, %rbx  /* rbx <- 1; */
movq $16, %r10
movq $0, %rcx    /* rcx = 0 */


_LC1:
cmpq %r10, %rbx  /* EFLAGS = rbx - r10. Sets flags. */
jg _LE1          /* jump to LE1 if rbx > r10 */
movq %r10, %rax  /* rax = r10 */
movq $0, %rdx    /* rdx = 0 */
divq %rbx        /* [rdx:rax] divide by rbx: rdx = mod res, rax = div res. */
cmpq $0, %rdx    /* is mod 0? */
jne _SKIPIF1
incq %rcx
_SKIPIF1:
incq %rbx        /* rbx++ */
jmp _LC1
_LE1:

/* Prepare for return */
movq $60, %rax   /* system call 60: return */
movq %rcx, %rdi
syscall
