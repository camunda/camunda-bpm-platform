{
  "type": "object",
  "properties": {
    "value": {
      "type": "object",
       "description": "The variable's value. Value differs depending on the variable's type and on the deserializeValues parameter."
    },
    "type": {
      "type": "string",
      "description": "The value type of the variable."
    },
    "valueInfo": {
      "description": "A JSON object containing additional, value-type-dependent properties.
For serialized variables of type Object, the following properties can be provided:

* `objectTypeName`: A string representation of the object's type name.
* `serializationDataFormat`: The serialization format used to store the variable.

For serialized variables of type File, the following properties can be provided:

* `filename`: The name of the file. This is not the variable name but the name that will be used when downloading the file again.
* `mimetype`: The MIME type of the file that is being uploaded.
* `encoding`: The encoding of the file that is being uploaded.",
      "type": "object",
      "additionalProperties": true
    }
  }
}