package org.camunda.bpm.engine.impl;

import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.identity.NativeUserQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.db.DbReadOnlyIdentityServiceProvider;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 *  @author Svetlana Dorokhova
 */
public class NativeUserQueryImpl extends AbstractNativeQuery<NativeUserQuery, User> implements NativeUserQuery {

  private static final long serialVersionUID = 1L;

  public NativeUserQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeUserQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }


 //results ////////////////////////////////////////////////////////////////
  
  public List<User> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    final DbReadOnlyIdentityServiceProvider identityProvider = getIdentityProvider(commandContext);
    return identityProvider.findUserByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    final DbReadOnlyIdentityServiceProvider identityProvider = getIdentityProvider(commandContext);
    return identityProvider.findUserCountByNativeQuery(parameterMap);
  }

  private DbReadOnlyIdentityServiceProvider getIdentityProvider(CommandContext commandContext) {
    return (DbReadOnlyIdentityServiceProvider) commandContext.getReadOnlyIdentityProvider();
  }

}
