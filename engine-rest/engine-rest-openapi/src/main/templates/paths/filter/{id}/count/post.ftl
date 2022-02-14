<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/post-execute-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "postExecuteFilterCount"
      tag = "Filter"
      summary = "Execute Filter Count (POST)"
      desc = "Executes the saved query of the filter by id and returns the count. This method is
              slightly more powerful then the [Get Execute Filter Count](${docsUrl}/reference/rest/filter/get-execute-count/) 
              method because it allows to extend the saved query of the filter."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the filter to execute."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      flatType = "object"
      requestDesc = "A JSON object which corresponds to the type of the saved query of the filter, i.e., if the resource type of the filter is Task the body should form a valid task query corresponding to the Task resource."
      examples = ['"example-1": {
                     "summary": "request",
                     "description": "POST `filter/aTaskFilterId/singleResult`. Note: The examples show a task filter. So the request body corresponds to a task query. For other resource types the request body will differ.",
                     "value": {
                       "assignee": "jonny1",
                       "taskDefinitionKey": "aTaskKey"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "request",
                       "description": "POST `filter/aTaskFilterId/singleResult`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "
                The extending query was invalid. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "
                The authenticated user is unauthorized to read this filter. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "
                Filter with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
        last = true
    />

  }

}
</#macro>