<#macro dto_macro docsUrl="">
  <@lib.dto >
    <#assign
        dateFormatDescription = "Default [format](${docsUrl}/reference/rest/overview/date-format/)
                                 `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />
    <#assign
        noteHistoricFormFiled = "**Note:** This property is only set for a `HistoricVariableUpdate` historic details.
                                 In these cases, the value of the `type` property is `formField`."
    />
    <#assign
        noteHistoricVariableUpdate = "**Note:** This property is only set for a `HistoricVariableUpdate` historic details.
                                 In these cases, the value of the `type` property is `variableUpdate`."
    />

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the historic detail."
    />

    <@lib.property
        name = "type"
        type = "string"
        desc = "The type of the historic detail. Either `formField` for a submitted form field
                value or `variableUpdate` for variable updates."
    />

    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The key of the process definition that this historic detail belongs to."
    />

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition that this historic detail belongs to."
    />

    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The id of the process instance the historic detail belongs to."
    />

    <@lib.property
        name = "activityInstanceId"
        type = "string"
        desc = "The id of the activity instance the historic detail belongs to."
    />

    <@lib.property
        name = "executionId"
        type = "string"
        desc = "The id of the execution the historic detail belongs to."
    />

    <@lib.property
        name = "caseDefinitionKey"
        type = "string"
        desc = "The key of the case definition that this historic detail belongs to."
    />

    <@lib.property
        name = "caseDefinitionId"
        type = "string"
        desc = "The id of the case definition that this historic detail belongs to."
    />

    <@lib.property
        name = "caseInstanceId"
        type = "string"
        desc = "The id of the case instance the historic detail belongs to."
    />

    <@lib.property
        name = "caseExecutionId"
        type = "string"
        desc = "The id of the case execution the historic detail belongs to."
    />

    <@lib.property
        name = "taskId"
        type = "string"
        desc = "The id of the task the historic detail belongs to."
    />

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The id of the tenant that this historic detail belongs to."
    />

    <@lib.property
        name = "userOperationId"
        type = "string"
        desc = "The id of user operation which links historic detail with
                [user operation log](${docsUrl}/reference/rest/history/user-operation-log/)
                entries."
    />

    <@lib.property
        name = "time"
        type = "string"
        format = "date-time"
        desc = "The time when this historic detail occurred. ${dateFormatDescription}"
    />

    <@lib.property
        name = "removalTime"
        type = "string"
        format = "date-time"
        desc = "The time after which the historic detail should be removed by the History Cleanup job.
                ${dateFormatDescription}"
    />

    <@lib.property
        name = "rootProcessInstanceId"
        type = "string"
        desc = "The process instance id of the root process instance that initiated the process
                containing this historic detail."
    />

    <@lib.property
        name = "fieldId"
        type = "string"
        desc = "The id of the form field.

                ${noteHistoricFormFiled}"
    />

    <@lib.property
        name = "fieldValue"
        type = "object"
        desc = "The submitted form field value. The value differs depending on the form field's type
                and on the `deserializeValue` parameter.

                ${noteHistoricFormFiled}"
    />

  <@lib.property
      name = "variableName"
      type = "string"
      desc = "The name of the variable which has been updated.

              ${noteHistoricVariableUpdate}"
  />

  <@lib.property
      name = "variableInstanceId"
      type = "string"
      desc = "The id of the associated variable instance.

              ${noteHistoricVariableUpdate}"
  />

  <@lib.property
      name = "variableType"
      type = "string"
      desc = "The value type of the variable.

              ${noteHistoricVariableUpdate}"
  />

  <@lib.property
      name = "value"
      type = "object"
      desc = "The variable's value. Value differs depending on the variable's type
              and on the deserializeValues parameter.

              ${noteHistoricVariableUpdate}"
  />

  <@lib.property
      name = "valueInfo"
      type = "object"
      addProperty = "\"additionalProperties\": true"
      desc = "A JSON object containing additional, value-type-dependent properties.
              For variables of type `Object`, the following properties are returned:

              * `objectTypeName`: A string representation of the object's type name.
              * `serializationDataFormat`: The serialization format used to store the variable.

              ${noteHistoricVariableUpdate}"
  />

  <@lib.property
      name = "initial"
      type = "boolean"
      desc = "Returns `true` for variable updates that contains the initial values of the variables.

              ${noteHistoricVariableUpdate}"
  />

  <@lib.property
      name = "revision"
      type = "integer"
      format = "int32"
      desc = "The revision of the historic variable update.

              ${noteHistoricVariableUpdate}"
  />

  <@lib.property
      name = "errorMessage"
      type = "string"
      last = true
      desc = "An error message in case a Java Serialized Object
              could not be de-serialized.

              ${noteHistoricVariableUpdate}"
  />

</@lib.dto>
</#macro>