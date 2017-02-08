module.exports = function() {
  return function(query) {
    return Object
      .keys(query)
      .reduce(function(queryStr, key) {
        var value = query[key];
        var part = key + '=' + encodeURIComponent(value);

        return queryStr.length ? queryStr + '&' + part : part;
      }, '');
  };
};
