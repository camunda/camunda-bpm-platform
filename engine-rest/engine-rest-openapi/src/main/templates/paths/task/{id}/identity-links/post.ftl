<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "addIdentityLink"
      tag = "Task Identity Link"
      summary = "Add"
      desc = "Adds an identity link to a task by id. Can be used to link any user or group to a task
              and specify a relation." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to add a link to."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "IdentityLinkDto"
      examples = ['"example-1": {
                       "summary": "POST `/task/anId/identity-links`",
                       "value": {
                         "groupId": "aNewGroupId",
                         "type": "candidate"
                       }
                     }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Task with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format." />

  }
}

</#macro>