'use strict';

var events = require('./events');

var Delete = function() {

  this.cascade = false;
  this.disable = false;

};

Delete.prototype.cancel = function() {
  events.emit('delete:cancel');
};

Delete.prototype.confirm = function() {
  this.disable = true;
  events.emit('delete:confirm', {
    cascade: this.cascade
  });
};

module.exports = Delete;
