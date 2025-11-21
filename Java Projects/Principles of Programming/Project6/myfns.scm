
;Main entry point
(define (plan program)
  (evalExpr (cadr program) '()) ; Start with empty binding environment
)

;Core expression evaluator
(define (evalExpr expr bind)
  (cond
    ((integer? expr) expr) ; Integer constant
    ((symbol? expr) (lookup expr bind)) ; Variable
    ((equal? 'planAdd (car expr)) (evalPlanAdd expr bind))
    ((equal? 'planMul (car expr)) (evalPlanMul expr bind))
    ((equal? 'planSub (car expr)) (evalPlanSub expr bind))
    ((equal? 'planIf  (car expr)) (evalPlanIf  expr bind))
    ((equal? 'planLet (car expr)) (evalPlanLet expr bind))
    (else (evalPlanCall expr bind)) ; Function call
  )
)

;Variable lookup with dynamic scoping
(define (lookup id bind)
  (cond
    ((null? bind) (error "Unbound variable:" id))
    ((equal? id (car (car bind))) (cdr (car bind))) ; Found
    (else (lookup id (cdr bind))) ; Keep looking
  )
)

;Addition
(define (evalPlanAdd expr bind)
  (+ (evalExpr (cadr expr) bind)
     (evalExpr (caddr expr) bind))
)

;Multiplication
(define (evalPlanMul expr bind)
  (* (evalExpr (cadr expr) bind)
     (evalExpr (caddr expr) bind))
)

;Subtraction
(define (evalPlanSub expr bind)
  (- (evalExpr (cadr expr) bind)
     (evalExpr (caddr expr) bind))
)

;Conditional
(define (evalPlanIf expr bind)
  (if (> (evalExpr (cadr expr) bind) 0)
      (evalExpr (caddr expr) bind)
      (evalExpr (cadddr expr) bind))
)

;planLet for both value and function bindings
(define (evalPlanLet expr bind)
  (let* ((id (cadr expr))
      (val-expr (caddr expr))
      (val (if (and (list? val-expr)
              (equal? (car val-expr) 'planFunction))
              val-expr 
              (evalExpr val-expr bind)))
      (new-bind (cons (cons id val) bind)))
    (evalExpr (cadddr expr) new-bind))
)

;Function call: (id arg)
(define (evalPlanCall expr bind)
  (let* ((func-id (car expr))
       (arg-expr (cadr expr))
       (func-val (lookup func-id bind)))
    (if (and (pair? func-val)
          (equal? (car func-val) 'planFunction))
      (let* ((param (cadr func-val))
            (body  (caddr func-val))
            (arg-val (evalExpr arg-expr bind))
            (new-bind (cons (cons param arg-val) bind)))
        (evalExpr body new-bind))
      (error "Invalid function call or not a function:" func-id))))


; Name: Ansh Pachauri
; Course: CSE 3341 - Programming Languages
; Project: Project 6 - PLAN Interpreter (with Extra Credit)
;
; This file defines an interpreter for the PLAN functional language.
; The interpreter supports arithmetic operations, conditional expressions,
; variable bindings with dynamic scoping, and first-class functions.
;
; This implementation includes:
;  - Integer constants and arithmetic expressions: planAdd, planMul, planSub
;  - Conditional expressions: planIf
;  - Local variable bindings with dynamic scoping: planLet
;  - Variable lookup via an environment list
;  - Function definitions and first-class function calls via planFunction and (id expr)
;
; Interpreter Design:
; The interpreter uses recursive evaluation of expressions while maintaining an
; explicit dynamic environment (list of bindings). Each PLAN construct is evaluated
; based on the semantics described in the project specification. Functions are
; represented as tagged lists and stored as values inside the environment, enabling
; first-class behavior.
;
; Entry Point:
; (plan program) - Accepts a PLAN program in the format (planProg <Expr>) and
;                  returns the integer result of evaluating <Expr>.
;
; Environment:
; A binding environment is a list of (id . value) pairs that maps identifiers to
; their most recent active values. This environment is passed explicitly during
; recursive evaluation to support dynamic scoping.
;
; Extra Credit:
; This implementation supports the extended PLAN grammar that includes:
;   - (planLet <id> (planFunction <param> <body>) <expr>) for function definitions
;   - (<id> <expr>) for calling functions bound to <id>
;
; Function bindings are stored as (planFunction <param> <body>) in the environment.
; Function calls look up the function, evaluate the argument, bind it to the
; function’s parameter, and evaluate the function body in the extended environment.
;
; Testing:
; The interpreter has been tested against all 10 provided test cases in cases.scm. All tests pass as expected.
; No known bugs.

