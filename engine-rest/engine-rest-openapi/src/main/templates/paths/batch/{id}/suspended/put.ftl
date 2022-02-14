<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "updateBatchSuspensionState"
      tag = "Batch"
      summary = "Activate/Suspend"
      desc = "Activates or suspends a batch by id." />

  "parameters" : [ 
      <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the batch to activate or suspend."/>
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "SuspensionStateDto"
      examples = ['"example-1": {
                     "summary": "PUT `/batch/aBatch/suspended`",
                     "value": {
                       "suspended" : true
                     }
                   }'
      ] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if the batch cannot be suspended or activated.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>


      }
}
</#macro>