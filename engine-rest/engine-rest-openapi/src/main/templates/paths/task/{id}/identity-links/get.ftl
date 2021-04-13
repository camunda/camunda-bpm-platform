<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getIdentityLinks"
      tag = "Task Identity Link"
      summary = "Get List"
      desc = "Gets the identity links for a task by id, which are the users and groups that are in
              *some* relation to it (including assignee and owner)." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the task to retrieve the identity links for."/>

    <@lib.parameter
        name = "type"
        location = "query"
        type = "string"
        last = true
        desc = "Filter by the type of links to include."/>
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "IdentityLinkDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /task/anId/identityLinks",
                       "value": [
                         {
                           "userId": "userId",
                           "groupId": null,
                           "type": "assignee"
                         },
                         {
                           "userId": null,
                           "groupId": "groupId1",
                           "type": "candidate"
                         },
                         {
                           "userId": null,
                           "groupId": "groupId2",
                           "type": "candidate"
                         }
                       ]
                     }']/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Task with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format." />

  }
}

</#macro>