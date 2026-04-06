package com.company.portal.common.exception;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String fromState, String toState) {
        super("Invalid state transition from " + fromState + " to " + toState);
    }
}
