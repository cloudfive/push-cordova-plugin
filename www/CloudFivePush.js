var exec = require("cordova/exec");
module.exports = {
  alert: function() {
    exec(function() {alert('success')}, 
                 function() {alert('failure')},
                  'CloudFivePush', 
                  'alert', 
                  []
                );  
  },

  register: function(userIdentifier) {
    exec(function() {alert('successfully registered')}, 
                 function() {alert('failed to register')},
                  'CloudFivePush', 
                  'register', 
                  [userIdentifier]
                );  
  }
};
