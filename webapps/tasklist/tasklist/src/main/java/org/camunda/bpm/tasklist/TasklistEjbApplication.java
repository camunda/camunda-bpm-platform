package org.camunda.bpm.tasklist;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.EjbProcessApplication;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;

/**
 * @author drobisch
 *
 * Also remove ServletContextListener from web.xml and remove engine tag from processes.xml
 * to switch to EJB Process Application
 *
 */
@Singleton
@ProcessApplication
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class TasklistEjbApplication extends EjbProcessApplication {
}
