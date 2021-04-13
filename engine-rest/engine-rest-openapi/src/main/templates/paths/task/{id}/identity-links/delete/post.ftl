<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "deleteIdentityLink"
      tag = "Task Identity Link"
      summary = "Delete"
      desc = "Removes an identity link from a task by id" />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to remove a link from."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "IdentityLinkDto"
      examples = ['"example-1": {
                       "summary": "POST `/task/anId/identityLinks/delete`",
                       "value": {
                         "groupId": "theOldGroupId",
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
        desc = "Task with given id does not exist.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for
                the error response format." />

  }
}

</#macro>