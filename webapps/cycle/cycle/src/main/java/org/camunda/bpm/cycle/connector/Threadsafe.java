package org.camunda.bpm.cycle.connector;

/**
 * Marks a method on a {@link Connector} as threadsafe.
 *
 * Along with {@link org.camunda.bpm.cycle.aspect.ThreadsafeAspect} this annotation makes sure that
 * annotated connection methods are called in a threadsafe (i.e. one thread at a time) manner.
 *
 * @author nico.rehwaldt
 *
 * @see org.camunda.bpm.cycle.aspect.ThreadsafeAspect
 */
public @interface Threadsafe {

}
