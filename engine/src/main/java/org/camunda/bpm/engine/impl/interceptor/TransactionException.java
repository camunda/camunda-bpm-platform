package org.camunda.bpm.engine.impl.interceptor;

public class TransactionException extends RuntimeException {
  protected static final long serialVersionUID = 1L;

  protected TransactionException() {
  }

  protected TransactionException(String s) {
    super(s);
  }

  protected TransactionException(String s, Throwable throwable) {
    super(s, throwable);
  }

  protected TransactionException(Throwable throwable) {
    super(throwable);
  }
}