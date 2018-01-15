var sinon = require('sinon');

module.exports = {
  get: sinon.stub().returnsThis(),
  set: sinon.stub(),
  remove: sinon.stub()
};
