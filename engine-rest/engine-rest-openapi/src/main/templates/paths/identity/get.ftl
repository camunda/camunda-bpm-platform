<#-- Generated From File: camunda-docs-manual/public/reference/rest/identity/get-group-info/index.html -->
{
  <@lib.endpointInfo
      id = "getGroupInfo"
      tag = "Identity"
      summary = "Get a User's Groups"
      desc = "Gets the groups of a user by id and includes all users that share a group with the
              given user."
  />

  "parameters" : [
     "userId": {
       "type": "string",
       "desc": "The id of the user to get the groups for."
     }
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "GroupInfoDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "GET `/identity/groups?userId=aUserId`",
                       "value": {
                         "groups": [
                           {
                             "id": "group1Id",
                             "name": "group1"
                           }
                         ],
                         "groupUsers": [
                           {
                             "firstName": "firstName",
                             "lastName": "lastName",
                             "displayName": "firstName lastName",
                             "id": "anotherUserId"
                           }
                         ]
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "If the `userId` query parameter is missing. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}