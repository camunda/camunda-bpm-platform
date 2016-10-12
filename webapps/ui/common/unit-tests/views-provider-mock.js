var sinon = require('sinon');

var ViewsProvider = {};

ViewsProvider.registerDefaultView = sinon.stub();
ViewsProvider.$get = function() {};

module.exports = ViewsProvider;
