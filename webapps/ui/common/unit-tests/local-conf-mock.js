var sinon = require('sinon');

module.exports = {
  get: sinon.stub().returnsArg(2),
  set: sinon.stub(),
  remove: sinon.stub()
};
