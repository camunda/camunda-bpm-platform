'use strict';

var events = require('./events');

var Delete = function() {

  this.cascade = false;

};

Delete.prototype.cancel = function() {
  events.emit('delete:cancel');
};

Delete.prototype.confirm = function() {
  events.emit('delete:confirm', {
    cascade: this.cascade
  });
};

module.exports = Delete;
