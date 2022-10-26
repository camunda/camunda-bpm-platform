<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getTaskCountByCandidateGroup"
      tag = "Task"
      summary = "Get Task Count By Candidate Group"
      desc = "Retrieves the number of tasks for each candidate group."
  />

  "responses": {

    <@lib.multiTypeResponse
        code = "200"
        desc = "Request successful."
        types =[
          {
            "dto": "TaskCountByCandidateGroupResultDto",
            "array": true,
            "examples": ['"example-1": { 
                          "value": [
                            {
                              "groupName": null,
                              "taskCount": 1
                            },
                            {
                              "groupName": "aGroupName",
                              "taskCount": 2
                            },
                            {
                              "groupName": "anotherGroupName",
                              "taskCount": 3
                            }
                          ]
              }'
            ]
          },
          {
            "mediaType": "application/csv"
          },
          {
            "mediaType": "text/csv"
          }
        ] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid or mandatory parameters are not supplied. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "If the authenticated user is unauthorized to read the history. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>