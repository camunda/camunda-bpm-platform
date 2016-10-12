var sinon = require('sinon');

module.exports = {
  when: sinon.stub().returnsThis(),
  $get: sinon.stub().returnsThis()
};
