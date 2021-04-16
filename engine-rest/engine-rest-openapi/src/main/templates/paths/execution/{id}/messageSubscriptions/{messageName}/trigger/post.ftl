<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/message-event-subscription/post-message/index.html -->
{
  <@lib.endpointInfo
      id = "triggerEvent"
      tag = "Execution"
      summary = "Trigger Message Event Subscription"
      desc = "Delivers a message to a specific execution by id, to trigger an existing message
              event subscription. Inject process variables as the message's
              payload."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the execution to submit the message to."
      />

      <@lib.parameter
          name = "messageName"
          location = "path"
          type = "string"
          required = true
          desc = "The name of the message that the addressed subscription corresponds to."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ExecutionTriggerDto"
      examples = ['"example-1": {
                     "summary": "POST `/execution/anExecutionId/messageSubscriptions/someMessage/trigger`",
                     "value": {
                       "variables": {
                         "aVariable": {
                           "value": true,
                           "type": "Boolean"
                         },
                         "anotherVariable": {
                           "value": 42,
                           "type": "Integer"
                         }
                       }
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "The variable value or type is invalid, for example if the value could not be parsed
                to an Integer value or the passed variable type is not supported.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "The addressed execution has no pending message subscriptions for the given message.
                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}

</#macro>