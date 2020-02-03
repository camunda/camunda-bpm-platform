{
  "type" : "object",
  "properties" : {
    "type": {
      "description": "Mandatory. One of the following values: cancel, startBeforeActivity, startAfterActivity, startTransition.\n*A cancel instruction requests cancellation of a single activity instance or all instances of one activity.\n*A startBeforeActivity instruction requests to enter a given activity.\n*A startAfterActivity instruction requests to execute the single outgoing sequence flow of a given activity.\n*A startTransition instruction requests to execute a specific sequence flow.",
      "type": "string",
      "enum": [
        "cancel",
        "startBeforeActivity",
        "startAfterActivity",
        "startTransition"
      ]
    },
    "variables" : {
      "$ref" : "#/components/schemas/TriggerVariableValueDto"
    },
    "activityId" : {
      "type" : "string",
      "description": "Can be used with instructions of types startTransition. Specifies the sequence flow to start."
    },
    "transitionId" : {
      "type" : "string",
      "description": "Can be used with instructions of types startTransition. Specifies the sequence flow to start."
    },
    "activityInstanceId" : {
      "type" : "string",
      "description": "Can be used with instructions of type cancel. Specifies the activity instance to cancel. Valid values are the activity instance IDs supplied by the Get Activity Instance request.(https://docs.camunda.org/manual/develop/reference/rest/process-instance/get-activity-instances/)"
    },
    "transitionInstanceId" : {
      "type" : "string",
      "description": "Can be used with instructions of type cancel. Specifies the transition instance to cancel. Valid values are the transition instance IDs supplied by the Get Activity Instance request.(https://docs.camunda.org/manual/develop/reference/rest/process-instance/get-activity-instances/)"
    },
    "ancestorActivityInstanceId" : {
      "type" : "string",
      "description": "Can be used with instructions of type startBeforeActivity, startAfterActivity, and startTransition. Valid values are the activity instance IDs supplied by the Get Activity Instance request.\nIf there are multiple parent activity instances of the targeted activity, this specifies the ancestor scope in which hierarchy the activity/transition is to be instantiated.\nExample: When there are two instances of a subprocess and an activity contained in the subprocess is to be started, this parameter allows to specifiy under which subprocess instance the activity should be started."
    },
    "cancelCurrentActiveActivityInstances" : {
      "type" : "boolean",
      "description": "Can be used with instructions of type cancel. Prevents the deletion of new created activity instances."
    }
  },
  "discriminator" : {
    "propertyName" : "type"
  },
  "required": [
    "type"
  ]
}