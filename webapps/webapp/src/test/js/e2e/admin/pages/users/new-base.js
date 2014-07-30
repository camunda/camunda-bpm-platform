'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  userId: function() {
    return element(by.model('profile.id'));
  },

  password: function() {
    return element(by.model('credentials.password'));
  },

  passwordRepeat: function() {
    return element(by.model('credentials.password2'));
  },

  userFirstName: function() {
    return element(by.model('profile.firstName'));
  },

  userLastName: function() {
    return element(by.model('profile.lastName'));
  },

  userEmail: function() {
    return element(by.model('profile.email'));
  }

});