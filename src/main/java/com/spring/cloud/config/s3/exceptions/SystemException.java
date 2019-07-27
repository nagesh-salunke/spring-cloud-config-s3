package com.spring.cloud.config.s3.exceptions;

/**
 * SystemException.
 *
 * @author Nagesh Salunke
 */
public class SystemException extends Exception {

  /**
   * Constructor.
   *
   * @param throwable {@link Throwable}
   */
  public SystemException(Throwable throwable) {
    super(throwable);
  }

  /**
   * Constructor.
   *
   * @param message error message
   */
  public SystemException(String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message error message
   * @param throwable {@link Throwable}
   */
  public SystemException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
