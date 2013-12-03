package org.camunda.bpm.cycle.aspect;

import java.util.concurrent.Semaphore;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * Ensures that a {@link org.camunda.bpm.cycle.connector.Threadsafe} annotated connector method
 * can only be accessed in a single-threaded manner.
 *
 * @author nico.rehwaldt
 *
 * @see org.camunda.bpm.cycle.connector.Threadsafe
 */
@Component
@Aspect
public class ThreadsafeAspect {

  private Semaphore semaphore = new Semaphore(1);

  @Before("@annotation(org.camunda.bpm.cycle.connector.Threadsafe)")
  private void acquire(JoinPoint jp) throws Throwable {
    this.semaphore.acquire();
  }

  @After("@annotation(org.camunda.bpm.cycle.connector.Threadsafe)")
  private void release(JoinPoint jp) throws Throwable {
    this.semaphore.release();
  }

}
