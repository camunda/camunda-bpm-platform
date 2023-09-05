<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "updateHistoryTimeToLiveByProcessDefinitionKey"
      tag = "Process Definition"
      summary = "Update History Time to Live"
      desc = "Updates history time to live for the latest version of the process definition which belongs to no tenant.
              The field is used within [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup).
              The value of the update is mandatory by default and does not allow `null` values. To enable them, please
              set the feature flag `enforceHistoryTimeToLive` to `false`. Read more in [Configuration Properties]
              (${docsUrl}/reference/deployment-descriptors/tags/process-engine#configuration-properties)" />

  "parameters" : [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The key of the process definition to change history time to live."/>
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoryTimeToLiveDto"
      examples = ['"example-1": {
                     "summary": "PUT `/process-definition/key/aProcessDefinitionKey/history-time-to-live`",
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
        desc = "Process definition with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>