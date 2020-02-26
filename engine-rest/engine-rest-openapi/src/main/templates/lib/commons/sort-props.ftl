{
  "type": "object",
  "properties": {
    <@lib.property
        name = "sortBy"
        type = "string"
        enumValues = sortByValues
        description = "Sort the results lexicographically by a given criterion. Must be used in conjunction with the sortOrder parameter."/>

    <@lib.property
        name = "sortOrder"
        type = "string"
        enumValues=['"asc"', '"desc"']
        last = last
        description = "Sort the results in a given order. Values may be asc for ascending order or desc for descending order. Must be used in conjunction with the sortBy parameter."/>

  }
}