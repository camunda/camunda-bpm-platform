'use strict';

/**
 * Promisified camAPI
 *
 * ATTENTION: it make assumption that there is somewhere camAPI service defined
 */
module.exports = [
  'camAPI', 'promisify',
  function(camAPI, promisify) {
    var promisifiedCamAPI = Object.create(camAPI);

    promisifiedCamAPI.resource = function() {
      var args = Array.prototype.slice.call(arguments);
      var resource = camAPI.resource.apply(camAPI, args);

      return promisify.promisifyObject(resource);
    };

    return promisifiedCamAPI;
  }
];
