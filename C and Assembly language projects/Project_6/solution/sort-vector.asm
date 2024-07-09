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
/* Determines whether a swap is necessary based on the given values. */
must_swap:
  pushq %rbp
  movq %rsp, %rbp

  /* Compare the values to determine if a swap is needed. */
  cmpq %rdi, %rsi
  jl _SWAP
  movq $0, %rax  /* Set return value to 0 to indicate no swap is needed. */
  jmp _END_SWAP
_SWAP:
  movq $1, %rax  /* Set return value to 1 to indicate a swap is needed. */
_END_SWAP:

  leave
  ret


/* Auxiliary function: sort_from_ii */
sort_from_ii:
  pushq %rbp
  movq %rsp, %rbp

  movq %rdi, %r12  /* Initialize outer loop counter i with the given index. */
  movq ii2, %r13   /* Initialize inner loop counter j with the given index. */

_LOOP_START:
    /* Check if j < 8 to ensure we haven't reached the end of the array. */
    movq $8, %rbx
    cmpq %rbx, %r13
    jg _END_SORT_FROM_II

    /* Load values of arr[j] and arr[j+1] for comparison. */
    leaq arr, %r14
    movq (%r14,%r12,8), %rdi
    movq (%r14,%r13,8), %rsi

    /* Call must_swap to determine if a swap is needed. */
    call must_swap
    cmpq $0, %rax
    je _SKIP  /* If no swap is needed, skip to the next iteration. */

    /* Perform the swap of arr[j] and arr[j+1]. */
    movq %rsi, (%r14,%r12,8)
    movq %rdi, (%r14,%r13,8)
  _SKIP:
    incq %r13  /* Increment inner loop counter j. */
    jmp _LOOP_START  /* Repeat the inner loop. */

  _END_SORT_FROM_II:
  leave
  ret

/* Start of main function */
_start:

pushq %rbp
movq %rsp, %rbp

/* NOTE: Do not modify the next 3 lines */
leaq msg_unsorted, %rdi
call print_message
call print_array

/* Initialize registers and outer induction variable. */
 incq ii2


_MAIN_LOOP_START:
  
  /* Loop control and preparation of parameter passing to sort_from_ii. */
    cmpq $7, jj
    jg _END_MAIN_LOOP
    movq jj, %rdi

  /* Invoke sort_from_ii */
  call sort_from_ii
  /* Update induction variables. */
  
  incq jj
  incq ii2
  jmp _MAIN_LOOP_START
_END_MAIN_LOOP:

/* NOTE: Do not modify the next 3 lines */
leaq msg_sorted, %rdi
call print_message
call print_array

/* Return the final value of the induction variable via the exit system call. */
movq jj, %r12
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
