package com.camunda.fox.platform.impl.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.UserTransaction;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.LogInterceptor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.spring.SpringTransactionInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.camunda.fox.platform.impl.AbstractProcessEngineService;
import com.camunda.fox.platform.impl.jobexecutor.commonj.WorkManagerJobExecutor;
import com.camunda.fox.platform.impl.jobexecutor.simple.SimpleJobExecutor;
import com.camunda.fox.platform.impl.transactions.spi.TransactionManagerFactory;
import com.camunda.fox.platform.impl.util.Services;

/**
 * <p>Configuration for a CMPE (Container-Managed Process Engine) running in a Java EE 6+ 
 * compliant application server</p>
 * 
 * <p><b>Transaction Management:</b> this configuration uses the spring transaction abstraction layer.
 * Some application servers like Websphere or Web Logic provide non-standard interfaces to the 
 * JTA transaction manager. We use the {@link JtaTransactionManager} provided by the Spring 
 * framework to abstract the underlying transaction manager API.</p>
 * 
 * <p>(Note: we cannot fall back to {@link UserTransaction} as we need REQUIRES_NEW semantics 
 * for some activiti {@link Command Commands}.)</p>
 * 
 * <p>We use externally-managed transactions to enable the process engine to participate in 
 * client transactions (and vice versa).</p>
 * 
 * <p><b>Work Management:</b> this configuration performs autodetection of the 
 * right {@link JobExecutor} implementation for the application server we are running in.
 * <ul>
 * <li>On IBM WebSphere / Oracle WebLogic we use the {@link WorkManagerJobExecutor}, delegating 
 * to a CommonJ work manager and thereby using container-managed JEE-Threads.</li>
 * <li>On other application servers we use the {@link SimpleJobExecutor} which uses a 
 * {@link java.util.concurrent.ThreadPoolExecutor} and self-managed threads.</li>
 * </ul>  
 * </p>
 * 
 * <p>Note: This configuration uses some classes provided by the spring framework while not building 
 * a Spring application context.</p>
 *  
 * <p>Note: this configuration conforms to IBM and Oracle application server support requirements.</p>
 * 
 * @author Daniel Meyer
 * @author nico.rehwaldt@camunda.com
 */
public class SpringCmpeProcessEngineConfiguration extends CmpeProcessEngineConfiguration {
  
  protected PlatformTransactionManager transactionManager;
  
  public SpringCmpeProcessEngineConfiguration(AbstractProcessEngineService processEngineServiceBean) {
    super(processEngineServiceBean);
  }
    
  @Override
  protected void init() {
    initSpringTransactionManager();
    super.init();    
  }

  protected void initSpringTransactionManager() {
    if(transactionManager != null) {
      return;
    }
    TransactionManagerFactory transactionManagerFactory = Services.getService(TransactionManagerFactory.class);
    transactionManager = transactionManagerFactory.getTransactionManager();
  }

  @Override
  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {   
    List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
    defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequired.add(new SpringTransactionInterceptor(transactionManager, TransactionTemplate.PROPAGATION_REQUIRED));
    CommandContextInterceptor commandContextInterceptor = new CommandContextInterceptor(commandContextFactory, this);
    defaultCommandInterceptorsTxRequired.add(commandContextInterceptor);
    return defaultCommandInterceptorsTxRequired;
  }
  
  @Override
  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
    List<CommandInterceptor> defaultCommandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
    defaultCommandInterceptorsTxRequiresNew.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequiresNew.add(new SpringTransactionInterceptor(transactionManager, TransactionTemplate.PROPAGATION_REQUIRES_NEW));
    CommandContextInterceptor commandContextInterceptor = new CommandContextInterceptor(commandContextFactory, this);
    defaultCommandInterceptorsTxRequiresNew.add(commandContextInterceptor);
    return defaultCommandInterceptorsTxRequiresNew;
  }
  
  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
  
  public PlatformTransactionManager getTransactionManager() {
    return transactionManager;
  }
  
}
