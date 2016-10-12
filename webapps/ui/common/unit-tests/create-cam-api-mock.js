'use strict';

var sinon = require('sinon');

var defaultResourceMethods = ['create', 'list', 'count', 'update', 'delete', 'get'];

/**
 * Creates mocked camAPI instance, that has resource method that always returns fakeResource property of mocked camAPI.
 *
 * @param resourceMethods   methods that should be mocked on resource.
 * @returns {{resource: *, fakeResource: *}}
 */
function createCamApiMock(resourceMethods) {
  resourceMethods = resourceMethods || defaultResourceMethods;

  var fakeResource = resourceMethods.reduce(function(fakeResource, method) {
    fakeResource[method] = noop;

    sinon.stub(fakeResource, method, function() {
      var args = Array.prototype.slice.call(arguments);
      var callback = args[args.length - 1];

      if (typeof callback === 'function') {
        callback(null, args.slice(0, -1));
      }
    });

    return fakeResource;
  }, {});

  var camAPI = {
    resource: sinon.stub(),
    fakeResource: fakeResource
  };

  camAPI.resource.returns(fakeResource);

  return camAPI;
}

createCamApiMock.defaultResourceMethods = defaultResourceMethods.slice(); // Copy for safety

module.exports = createCamApiMock;

function noop() {}
