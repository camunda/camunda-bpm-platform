<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteTaskMetrics"
      tag = "Metrics"
      summary = "Delete Task Worker Metrics"
      desc = "Deletes all task worker metrics prior to the given date or all if no date is provided." />

  "parameters" : [

    <@lib.parameter
        name = "date"
        location = "query"
        type = "string"
        format = "date-time"
        last = true
        desc = "The date prior to which all task worker metrics should be deleted."/>

  ],

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        last = true
        desc = "If the user who performs the operation is not a <b>camunda-admin</b> user." />

  }
}
</#macro>