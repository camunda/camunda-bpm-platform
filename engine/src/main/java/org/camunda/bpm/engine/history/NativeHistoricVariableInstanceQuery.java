package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.camunda.bpm.engine.history.HistoricVariableInstanceQuery}s via native (SQL) queries
 * @author Ramona Koch
 */
public interface NativeHistoricVariableInstanceQuery extends NativeQuery<NativeHistoricVariableInstanceQuery, HistoricVariableInstance> {

    /**
     * Disable deserialization of variable values that are custom objects. By default, the query
     * will attempt to deserialize the value of these variables. By calling this method you can
     * prevent such attempts in environments where their classes are not available.
     * Independent of this setting, variable serialized values are accessible.
     */
    NativeHistoricVariableInstanceQuery disableCustomObjectDeserialization();

}
