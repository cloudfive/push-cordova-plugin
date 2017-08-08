var exec = require("cordova/exec");

var emptyFn = function() {};

var callbacks = {
  registration: {
    success: emptyFn,
    failure: emptyFn,
    scope: this
  },
  received: {
    success: emptyFn,
    failure: emptyFn,
    scope: this
  },
  activated: {
    success: emptyFn,
    failure: emptyFn,
    scope: this
  }
};

module.exports = {
  register: function(userIdentifier, onSuccess, onFailure) {
    callbacks.registration.failure = onFailure || emptyFn;
    callbacks.registration.success = onSuccess || emptyFn;

    var me = this;
    exec(
      function() {
        me._messageCallback.apply(me, arguments);
      },
      function() {
        console.log("Failed attempt to register for push notifications -- check your configuration");
      },
      'CloudFivePush',
      'register',
      [userIdentifier]
    );
  },
  onPushReceived: function(callback, scope) {
    callbacks.received = {
      success: callback,
      scope: scope || this
    };
  },
  onPushActivated: function(callback, scope) {
    callbacks.activated = {
      success: callback,
      scope: scope || this
    };
  },
  finish: function() {
    exec(
      emptyFn,
      emptyFn,
      'CloudFivePush',
      'finish',
      []
    );
  },
  _messageCallback: function(data) {
    console.log('[CloudFivePush callback]', JSON.stringify(data));
    if (data.event === 'registration') {
      var handler = callbacks.registration;
      if (data.success) {
        handler.success.call(handler.scope, data);
      } else {
        handler.failure.apply(handler.scope, arguments);
      }
    } else if (data.event === 'message') {
      var handler = callbacks.received;
      handler.success.call(handler.scope, data.payload);
    } else if (data.event === 'interaction') {
      var handler = callbacks.activated;
      handler.success.call(handler.scope, data.payload);
    }
  }
};
