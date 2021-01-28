{
  <@lib.endpointInfo
      id = "deleteTaskMetrics"
      tag = "Metrics"
      summary = "Delete Task Worker Metrics"
      desc = "Deletes all task worker metrics prior to the given date or all if no date is provided." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "DeleteTaskMetricsDto"
      examples = ['"example-1": {
                     "summary": "DELETE `/metrics/task-worker`",
                     "value": {
                       "date": "2020-01-13T18:43:28.000+0200"
                     }
                   }']
  />

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