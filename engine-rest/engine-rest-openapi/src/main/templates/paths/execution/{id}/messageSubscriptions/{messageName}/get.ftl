<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/message-event-subscription/get-message-subscription/index.html -->
{
  <@lib.endpointInfo
      id = "getMessageEventSubscription"
      tag = "Execution"
      summary = "Get Message Event Subscription"
      desc = "Retrieves a message event subscription for a given execution by id and a message
              name."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the execution that holds the subscription."
      />

      <@lib.parameter
          name = "messageName"
          location = "path"
          type = "string"
          required = true
          desc = "The name of the message that the subscription corresponds to."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "EventSubscriptionDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "GET `/execution/anExecutionId/messageSubscriptions/someMessage`",
                       "value": {
                         "id": "anEventSubscriptionId",
                         "eventType": "message",
                         "eventName": "anEvent",
                         "executionId": "anExecutionId",
                         "processInstanceId": "aProcInstId",
                         "activityId": "anActivity",
                         "tenantId": null,
                         "createdDate": "2013-01-23T13:59:43.000+0200"
                       }
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "A message subscription for the given name and execution does not exist.
                This may either mean that the execution does not exist, or that
                it is not subscribed on such a message.
                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>