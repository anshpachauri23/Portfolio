.text
.global _start


print_digit:
  pushq %rbp
  movq %rsp, %rbp
  movq %rdi, %r10
  addq $48, %r10
  movq %r10, %rbx
  movw %bx, msg_digit
  movq $1, %rax
  movq $1, %rdi
  movq $msg_digit, %rsi
  movq $3, %rdx
  syscall 
  leave
  ret


print_array:
  pushq %rbp
  movq %rsp, %rbp
  movq ii, %r12
  movq $0, %r12
  movq $0, ii

  _LOOP_START_PRINT_ARRAY:
    movq ii, %r12
    movq $8, %rbx
    cmpq %rbx, %r12
    jg _END_LOOP_PRINT_ARRAY
    movq %r12, %rdi
    leaq arr, %rbx
    movq (%rbx, %r12, 8), %rdi

    call print_digit

    movq ii, %r12
    incq %r12
    movq %r12, ii
    jmp _LOOP_START_PRINT_ARRAY
  _END_LOOP_PRINT_ARRAY:

  leave
  ret


print_message:
  pushq %rbp
  movq %rsp, %rbp
  movq %rdi, %r10

  movq $1, %rax
  movq $1, %rdi
  movq %r10, %rsi
  movq $16, %rdx
  syscall 

  leave
  ret



/* Place your code here */


/* Auxiliary function: must_swap */
must_swap:



/* Auxiliary function: sort_from_ii */
sort_from_ii:


/* Start of main function */
_start:

pushq %rbp
movq %rsp, %rbp

/* NOTE: Do not modify the next 3 lines */
leaq msg_unsorted, %rdi
call print_message
call print_array

/* Initialize registers and outer induction variable. */


_MAIN_LOOP_START:
  
  /* Loop control and preparation of parameter passing to sort_from_ii. */

  /* Invoke sort_from_ii */

  /* Update induction variables. */


_END_MAIN_LOOP:

/* NOTE: Do not modify the next 3 lines */
leaq msg_sorted, %rdi
call print_message
call print_array

/* Return the final value of the induction variable via the exit system call. */

/* Your code ends here */


/* Prepare for return */
movq $60, %rax   /* system call 60: return */
movq %r12, %rdi
syscall

.data

arr: 
  .quad 2
  .quad 0 
  .quad 4
  .quad 1 
  .quad 8 
  .quad 9 
  .quad 6
  .quad 3 
  .quad 7 
ii:
  .quad 0
ii2:
  .quad 0
jj:
  .quad 0
tmp1:
  .quad 0
tmp2:
  .quad 0
msg_yes: .ascii "y \n"
msg_no: .ascii "n \n"
msg_digit: .ascii "..\n"
msg_unsorted: .ascii "Unsorted array \n"
msg_sorted:   .ascii "Sorted array   \n"
ret_must_swap: .quad 2
