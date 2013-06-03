package org.camunda.bpm.container.impl.threading.ra;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.camunda.bpm.container.impl.threading.ra.commonj.CommonJWorkManagerExecutorService;
import org.camunda.bpm.container.impl.threading.ra.inflow.JobExecutionHandler;
import org.camunda.bpm.container.impl.threading.ra.inflow.JobExecutionHandlerActivation;
import org.camunda.bpm.container.impl.threading.ra.inflow.JobExecutionHandlerActivationSpec;


/**
 * <p>The {@link ResourceAdapter} responsible for bootstrapping the JcaExecutorService</p>
 *
 * @author Daniel Meyer
 */
@Connector(
    reauthenticationSupport = false,
    transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction
  )
public class JcaExecutorServiceConnector implements ResourceAdapter, Serializable {

  public static final String ORG_CAMUNDA_BPM_ENGINE_PROCESS_ENGINE = "org.camunda.bpm.engine.ProcessEngine";

  /**
   * This class must be free of engine classes to make it possible to install
   * the resource adapter without shared libraries. Some deployments scenarios might
   * require that.
   *
   * The wrapper class was introduced to provide more meaning to a otherwise
   * unspecified property.
   */
  public class ExecutorServiceWrapper {
    /**
     * will hold a org.camunda.bpm.container.ExecutorService reference
     */
    protected Object executorService;

    public Object getExecutorService() {
      return executorService;
    }

    private void setExecutorService(Object executorService) {
      this.executorService = executorService;
    }

  }

  protected ExecutorServiceWrapper executorServiceWrapper = new ExecutorServiceWrapper();

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(JcaExecutorServiceConnector.class.getName());

  protected JobExecutionHandlerActivation jobHandlerActivation;

  // no arg-constructor
  public JcaExecutorServiceConnector() {
  }

  // Configuration Properties //////////////////////////////////////////

  @ConfigProperty(
      type = Boolean.class,
      defaultValue = "false",
      description = "If set to 'true', the CommonJ WorkManager is used instead of the Jca Work Manager."
      + "Can only be used on platforms where a CommonJ Workmanager is available (such as IBM & Oracle)"
  )
  protected Boolean isUseCommonJWorkManager = false;


  @ConfigProperty(
      type=String.class,
      defaultValue = "wm/camunda-bpm-workmanager",
      description="Allows specifying the name of a CommonJ Workmanager."
  )
  protected String commonJWorkManagerName = "wm/camunda-bpm-workmanager";


  // RA-Lifecycle ///////////////////////////////////////////////////

  public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {

    try {
      Class.forName(ORG_CAMUNDA_BPM_ENGINE_PROCESS_ENGINE);
    } catch (Exception e) {
      log.info("ProcessEngine classes not found in shared libraries. Not initializing camunda Platform JobExecutor Resource Adapter.");
      return;
    }

    // initialize the ExecutorService (CommonJ or JCA, depending on configuration)
    if(isUseCommonJWorkManager) {
      if(commonJWorkManagerName != null & commonJWorkManagerName.length() > 0) {
        executorServiceWrapper.setExecutorService(new CommonJWorkManagerExecutorService(this, commonJWorkManagerName));
      } else {
        throw new RuntimeException("Resource Adapter configuration property 'isUseCommonJWorkManager' is set to true but 'commonJWorkManagerName' is not provided.");
      }

    } else {
      executorServiceWrapper.setExecutorService(new JcaWorkManagerExecutorService(this, ctx.getWorkManager()));
    }

    log.log(Level.INFO, "camunda BPM executor service started.");
  }

  public void stop() {
    try {
      Class.forName(ORG_CAMUNDA_BPM_ENGINE_PROCESS_ENGINE);
    } catch (Exception e) {
      return;
    }

    log.log(Level.INFO, "camunda BPM executor service stopped.");

  }

  // JobHandler activation / deactivation ///////////////////////////

  public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
    if(jobHandlerActivation != null) {
      throw new ResourceException("The camunda BPM job executor can only service a single MessageEndpoint for job execution. " +
      		"Make sure not to deploy more than one MDB implementing the '"+JobExecutionHandler.class.getName()+"' interface.");
    }
    JobExecutionHandlerActivation activation = new JobExecutionHandlerActivation(this, endpointFactory, (JobExecutionHandlerActivationSpec) spec);
    activation.start();
    jobHandlerActivation = activation;
  }

  public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
    try {
      if(jobHandlerActivation != null) {
        jobHandlerActivation.stop();
      }
    } finally {
      jobHandlerActivation = null;
    }
  }

  // unsupported (No TX Support) ////////////////////////////////////////////

  public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
    log.finest("getXAResources()");
    return null;
  }

  // getters ///////////////////////////////////////////////////////////////

  public ExecutorServiceWrapper getExecutorServiceWrapper() {
    return executorServiceWrapper;
  }

  public JobExecutionHandlerActivation getJobHandlerActivation() {
    return jobHandlerActivation;
  }

  public Boolean getIsUseCommonJWorkManager() {
    return isUseCommonJWorkManager;
  }

  public void setIsUseCommonJWorkManager(Boolean isUseCommonJWorkManager) {
    this.isUseCommonJWorkManager = isUseCommonJWorkManager;
  }

  public String getCommonJWorkManagerName() {
    return commonJWorkManagerName;
  }

  public void setCommonJWorkManagerName(String commonJWorkManagerName) {
    this.commonJWorkManagerName = commonJWorkManagerName;
  }


  // misc //////////////////////////////////////////////////////////////////


  @Override
  public int hashCode() {
    return 17;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }
    if (!(other instanceof JcaExecutorServiceConnector)) {
      return false;
    }
    return true;
  }

}
