<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/options/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "filterResourceOptions"
      tag = "Filter"
      summary = "Filter Resource Options"
      desc = "The OPTIONS request allows you to check for the set of available operations 
              that the currently authenticated user can perform on the `/filter` resource.
              Whether the user can perform an operation or not may depend on various
              factors, including the users authorizations to interact with this
              resource and the internal configuration of the process engine."
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "ResourceOptionsDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "request",
                       "description": "OPTIONS `/filter`",
                       "value": {
                         "links": [
                           {
                             "method": "GET",
                             "href": "http://localhost:8080/engine-rest/filter",
                             "rel": "list"
                           },
                           {
                             "method": "GET",
                             "href": "http://localhost:8080/engine-rest/filter/count",
                             "rel": "count"
                           },
                           {
                             "method": "POST",
                             "href": "http://localhost:8080/engine-rest/filter/create",
                             "rel": "create"
                           }
                         ]
                       }
                   }']
        last = true
    />

  }

}
</#macro>