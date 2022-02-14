<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "updateHistoryTimeToLiveByDecisionDefinitionKey"
      tag = "Decision Definition"
      summary = "Update History Time to Live By Key"
      desc = "Updates the latest version of the decision definition which belongs to no tenant.
              The field is used within [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup)." />

  "parameters" : [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The key of the decision definitions to change history time to live."/>
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoryTimeToLiveDto"
      examples = ['"example-1": {
                     "summary": "PUT `/decision-definition/key/aKey/history-time-to-live`",
                     "value": {
                       "historyTimeToLive" : 5
                     }
                   }'
                 ] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the request parameters are invalid. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Decision definition with given key does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>