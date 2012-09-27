package com.camunda.fox.cycle.aspect;

import java.util.concurrent.Semaphore;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ThreadsafeAspect {
  
  private Semaphore semaphore = new Semaphore(1);

  @Before("@annotation(com.camunda.fox.cycle.connector.Threadsafe)")
  private void acquire(JoinPoint jp) throws Throwable {
    this.semaphore.acquire();
  }
  
  @After("@annotation(com.camunda.fox.cycle.connector.Threadsafe)")
  private void release(JoinPoint jp) throws Throwable {
    this.semaphore.release();
  }
  
}
