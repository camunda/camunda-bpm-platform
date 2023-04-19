<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getActivityInstanceTree"
      tag = "Process Instance"
      summary = "Get Activity Instance"
      desc = "Retrieves an Activity Instance (Tree) for a given process instance by id." />

  "parameters" : [
    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the process instance for which the activity instance should be retrieved."/>
  ],
  "responses": {

    <@lib.response
        code = "200"
        dto = "ActivityInstanceDto"
        desc = "Request successful."
        examples = ['"example-1": {
                     "summary": "GET `/process-instance/aProcessInstanceId/activity-instances`",
                     "value": {
                       "id": "eca75c6b-f70c-11e9-8777-e4a7a094a9d6",
                       "parentActivityInstanceId": null,
                       "activityId": "invoice:2:e9d77375-f70c-11e9-8777-e4a7a094a9d6",
                       "activityType": "processDefinition",
                       "processInstanceId": "eca75c6b-f70c-11e9-8777-e4a7a094a9d6",
                       "processDefinitionId": "invoice:2:e9d77375-f70c-11e9-8777-e4a7a094a9d6",
                       "childActivityInstances": [{
                         "id": "approveInvoice:eca89509-f70c-11e9-8777-e4a7a094a9d6",
                         "parentActivityInstanceId": "eca75c6b-f70c-11e9-8777-e4a7a094a9d6",
                         "activityId": "approveInvoice",
                         "activityType": "userTask",
                         "processInstanceId": "eca75c6b-f70c-11e9-8777-e4a7a094a9d6",
                         "processDefinitionId": "invoice:2:e9d77375-f70c-11e9-8777-e4a7a094a9d6",
                         "childActivityInstances": [],
                         "childTransitionInstances": [],
                         "executionIds": [
                         "eca75c6b-f70c-11e9-8777-e4a7a094a9d6"
                         ],
                         "activityName": "Approve Invoice",
                         "incidentIds": [
                         "648d7e21-f71c-11e9-a725-e4a7a094a9d6"
                         ],
                         "incidents": [{
                           "id": "648d7e21-f71c-11e9-a725-e4a7a094a9d6",
                           "activityId": "AttachedTimerBoundaryEvent"
                         }
                         ],
                         "name": "Approve Invoice"
                       }
                       ],
                       "childTransitionInstances": [],
                       "executionIds": [
                       "eca75c6b-f70c-11e9-8777-e4a7a094a9d6"
                       ],
                       "activityName": "Invoice Receipt",
                       "incidentIds": null,
                       "incidents": null,
                       "name": "Invoice Receipt"
                     }
                   }'
      ] />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "Process instance with given id does not exist."/>

  }
}
</#macro>
