module.exports = function() {
  return function(query) {
    return Object
      .keys(query)
      .reduce(function(queryStr, key) {
        const value = query[key];
        const part = key + '=' + encodeURIComponent(value);

        return queryStr.length ? queryStr + '&' + part : part;
      }, '');
  };
};
