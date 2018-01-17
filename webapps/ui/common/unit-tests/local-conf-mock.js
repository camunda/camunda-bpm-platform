var sinon = require('sinon');

module.exports = {
  get: sinon.stub().returnsArg(1),
  set: sinon.stub(),
  remove: sinon.stub()
};
