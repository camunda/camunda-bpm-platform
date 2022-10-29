exports.sortIntArray = function(array) {
  return array.sort((val1, val2) => val1 - val2);
}

exports.sortMapByStringKeys = function(map) {
  return new Map([...map].sort((entry1, entry2) => entry1[0].localeCompare(entry2[0])));
}

exports.sortMapByIntKeys = function(map) {
  return new Map([...map].sort((entry1, entry2) => entry1[0] - entry2[0]));
}

// reverses a multi-map (i.e. values are arrays) and returns a multi-map too
exports.revertMultiMap = function(map) {
  const result = new Map();
  map.forEach((mapValues, mapKey) => {
    mapValues.forEach((mapValue) => {
      
      var resultValues = result.get(mapValue);
      
      if (resultValues === undefined) {
        resultValues = [];
        result.set(mapValue, resultValues);
      }
      
      resultValues.push(mapKey);
    });
  });
  
  return result;
}

// returns an array [key, value] that represents the map entry with the highest value
exports.getMaximumKeyValuePair = function(map) {
  return [...map.entries()].reduce((entry1, entry2) => entry1[1] > entry2[1] ? entry1 : entry2);
}
