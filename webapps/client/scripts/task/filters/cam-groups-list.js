define([
  'angular'
], function(
  angular
) {
  'use strict';
  return function() {
    return function(groupsArray) {
      var maxGroups = 2;
      return groupsArray.length > maxGroups ? groupsArray.slice(0, maxGroups).join(", ") + ", ..." : groupsArray.join(", ");

    };
  };
});
