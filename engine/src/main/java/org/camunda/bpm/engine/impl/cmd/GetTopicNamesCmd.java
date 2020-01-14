package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.impl.ExternalTaskQueryImpl;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.io.Serializable;
import java.util.List;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

public class GetTopicNamesCmd implements Command<List<String>>, Serializable {

    ExternalTaskQuery externalTaskQuery;

    private GetTopicNamesCmd(){
    }

    public GetTopicNamesCmd(ExternalTaskQuery externalTaskQuery){
        this.externalTaskQuery = externalTaskQuery;
    }

    //todo: note that ordering by default is process id [asc]
    @Override
    public List<String> execute(CommandContext commandContext) {
        if (externalTaskQuery == null) throw new NullPointerException("ExternalTasKQuery cannot be null.");
        else {
            ExternalTaskQueryImpl impl = (ExternalTaskQueryImpl) externalTaskQuery;
            commandContext.getAuthorizationManager().configureExternalTaskQuery(impl);
            commandContext.getTenantManager().configureQuery(impl);
            return commandContext
                    .getExternalTaskManager()
                    .selectTopicNamesByQuery(impl);
        }
    }
}
