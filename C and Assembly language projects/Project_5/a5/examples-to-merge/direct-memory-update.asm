.text
.global _start

_start:

movq arr, %rbx
movq $1, %rcx
addq %rcx, %rbx

movq %rbx, arr
movq arr, %rcx

movq $60, %rax   
movq %rcx, %rdi
syscall

.data
arr: 
  .quad 10
  .quad 20 
  .quad 30

