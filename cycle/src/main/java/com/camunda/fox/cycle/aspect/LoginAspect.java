package com.camunda.fox.cycle.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoginAspect {
  
  @Before("@annotation(com.camunda.fox.cycle.api.connector.Secured)")
  private void aroundSecured(JoinPoint jp) throws Throwable {
  }
}
