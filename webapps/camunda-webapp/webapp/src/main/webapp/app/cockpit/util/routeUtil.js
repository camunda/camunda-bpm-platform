define([], function() {

  return {
    redirectToLive: function(params, currentPath, currentSearch) {
      var redirectUrl = currentPath + '/live',
          search = [],
          key;

      for (key in currentSearch) {
        search.push(key + '=' + currentSearch[key]);
      }

      return redirectUrl + (search.length ? '?' + search.join('&') : '');
    }
  };
});