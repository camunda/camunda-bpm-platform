<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "availableOperations"
      tag = "User"
      summary = "Options"
      desc = "The `/user` resource supports two custom `OPTIONS` requests, one for the resource as such
              and one for individual user instances. The `OPTIONS` request allows checking for the set of
              available operations that the currently authenticated user can perform on the /user resource.
              If the user can perform an operation or not may depend on various things, including the user's
              authorizations to interact with this resource and the internal configuration of the process
              engine. `OPTIONS /user` returns available interactions on the resource."/>

  "responses" : {

    <@lib.response
        mediaType = "application/json"
        code = "200"
        dto = "ResourceOptionsDto"
        last = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/user`",
                       "value": {
                          "links": [
                            {
                              "method": "GET",
                              "href": "http://localhost:8080/engine-rest/user",
                              "rel": "list"
                            },
                            {
                              "method": "GET",
                              "href": "http://localhost:8080/engine-rest/user/count",
                              "rel": "count"
                            },
                            {
                              "method": "POST",
                              "href": "http://localhost:8080/engine-rest/user/create",
                              "rel": "create"
                            }
                          ]
                        }
                     }'] />
    }
}
</#macro>