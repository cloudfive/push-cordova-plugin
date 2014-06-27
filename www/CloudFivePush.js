var exec = require("cordova/exec");
module.exports = {
  register: function(userIdentifier) {
    exec(
      function() {console.log('successfully registered')},
      function() {console.log('failed to register')},
      'CloudFivePush',
      'register',
      [userIdentifier]
    );
  }
};
