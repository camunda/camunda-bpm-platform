package org.camunda.bpm.cycle.connector;

/**
 * Marks a method on a {@link Connector} as secured.
 *
 * Along with {@link org.camunda.bpm.cycle.aspect.LoginAspect} this annotation makes sure that
 * connectors are logged into the backend during a method invocation.
 *
 * @author nico.rehwaldt
 *
 * @see org.camunda.bpm.cycle.aspect.LoginAspect
 */
public @interface Secured {

}
