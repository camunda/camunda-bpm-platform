{
  <@lib.endpointInfo
      id = "getActivityInstanceTree"
      tag = "Process instance"
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
        desc = "Request successful." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "Process instance with given id does not exist."/>

  }
}