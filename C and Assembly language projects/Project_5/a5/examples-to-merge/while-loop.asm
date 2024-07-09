.text
.global _start

_start:

_loop_start:
movq $0, %rbx  /* rbx <- 0; */
movq $20, %rcx

_loop_condition:
cmpq %rcx, %rbx  
jg _loop_end
incq %rbx  
jmp _loop_condition

_loop_end:

/* Prepare for return */
movq $60, %rax   /* system call 60: return */
movq %rbx, %rdi
syscall
