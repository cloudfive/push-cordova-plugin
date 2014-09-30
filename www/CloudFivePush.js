var exec = require("cordova/exec");
var _handlerFunction = null;
var _handlerScope = null;
module.exports = {
  register: function(userIdentifier) {
    exec(
      function() { console.log("Succesfully registered for Cloud Five Push") }, 
      function() { console.log("Failed to register for push notifications")},
      'CloudFivePush', 
      'register', 
      [userIdentifier]
    );  
  },
  setHandler: function(handlerFunction, handlerScope) {
    _handlerFunction = handlerFunction;
    _handlerScope = handlerScope;
  },
  _messageCallback: function(data) {
    if (data.event === 'registered') {
      console.log('registered with push service succesfully');
    } else if (data.event === 'message') {
      console.log("got a notification in real time");
    } else if (data.event === 'interaction') {
      console.log("user interacted with a notification");
      if (typeof(_handlerFunction) === 'function') {
        _handlerFunction.apply(_handlerScope, data.payload);
      }
    }
  }
};
