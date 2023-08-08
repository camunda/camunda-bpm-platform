<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "updateHistoryTimeToLiveByDecisionDefinitionKeyAndTenant"
      tag = "Decision Definition"
      summary = "Update History Time to Live By Key And Tenant"
      desc = "Updates the latest version of the decision definition for tenant.
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
        desc = "The key of the decision definitions to change history time to live."/>

    <@lib.parameter
        name = "tenant-id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the tenant the decision definition belongs to." />
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoryTimeToLiveDto"
      examples = ['"example-1": {
                     "summary": "PUT `/decision-definition/key/aKey/tenant-id/aTenantId/history-time-to-live`",
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