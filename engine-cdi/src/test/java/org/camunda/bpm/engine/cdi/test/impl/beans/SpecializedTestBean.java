package org.camunda.bpm.engine.cdi.test.impl.beans;

import javax.enterprise.inject.Specializes;

import org.camunda.bpm.engine.cdi.test.impl.util.ProgrammaticBeanLookupTest.TestBean;

@Specializes
public class SpecializedTestBean extends TestBean {

}