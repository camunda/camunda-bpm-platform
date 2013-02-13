package com.camunda.fox.cycle.web.jaxrs.ext;

import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;


public class JaxRsUtil {

  public static ResponseBuilderImpl createResponse() {
    // automatic detection of the JAX-RS implementation is broken on WAS 8.5.
    return new ResponseBuilderImpl();
  }
  
}
