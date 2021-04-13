<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "setExternalTaskResourcePriority"
      tag = "External Task"
      summary = "Set Priority"
      desc = "Sets the priority of an existing external task by id. The default value of a priority is 0." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the external task to set the priority for."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "PriorityDto"
      examples = ['"example-1": {
                     "summary": "PUT /external-task/anId/priority",
                     "value": {
                      "priority": 5
                     }
                   }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if the task does not exist. This could indicate a wrong task id as well as a cancelled task,
                e.g., due to a caught BPMN boundary event. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>