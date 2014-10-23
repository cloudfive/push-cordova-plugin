var exec = require("cordova/exec");
var _receivedFunction = null;
var _receivedScope = null;
var _activatedFunction = null;
var _activatedScope = null;

var _registrationSuccess = null;
var _registrationFailure = null;

module.exports = {
  register: function(userIdentifier, onSuccess, onFailure) {
    _registrationFailure = onFailure;
    _registrationSuccess = onSuccess;
    exec(
      function() {
        console.log("Succesfully kicked off registration for Cloud Five Push");
      },
      function() {
        console.log("Failed attempt to register for push notifications -- check your configuration");
      },
      'CloudFivePush',
      'register',
      [userIdentifier]
    );
  },
  onPushReceived: function(handlerFunction, handlerScope) {
    _receivedFunction = handlerFunction;
    _receivedScope = handlerScope;
  },
  onPushActivated: function(handlerFunction, handlerScope) {
    _activatedFunction = handlerFunction;
    _activatedScope = handlerScope;
  },
  _messageCallback: function(data) {
    if (data.event === 'registration') {
      if (data.success) {
        if (typeof(_registrationSuccess) === 'function') { _registrationSuccess(data); }
        console.log('registered with push service succesfully');
      } else {
        if (typeof(_registrationFailure) === 'function') { _registrationFailure(data); }
        console.log('failed to register with push service');
      }
    } else if (data.event === 'message') {
      console.log("got a notification in real time");
      if (typeof(_receivedFunction) === 'function') {
        _receivedFunction.call(_receivedScope, data.payload);
      }
    } else if (data.event === 'interaction') {
      console.log("user interacted with a notification");
      if (typeof(_activatedFunction) === 'function') {
        _activatedFunction.call(_activatedScope, data.payload);
      }
    }
  }
};
