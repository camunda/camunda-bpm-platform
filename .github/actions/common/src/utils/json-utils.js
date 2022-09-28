exports.parseJSONToMap = function(json) {
  return JSON.parse(json, (key, value) => {
    if(typeof value === 'object' && value !== null) {
      if (value.dataType === 'Map') {
        return new Map(value.value);
      }
    }
    return value;
  });
}

exports.stringifyMapToJSON = function(map) {
  return JSON.stringify(map, (key, value) => {
    if(value instanceof Map) {
      return {
        dataType: 'Map',
        value: Array.from(value.entries()), // or with spread: value: [...value]
      };
    } else {
      return value;
    }
  });
}