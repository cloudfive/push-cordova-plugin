var exec = require("cordova/exec");
module.exports = {
  register: function(userIdentifier) {
    exec(
      function() { console.log("Succesfully registered for Cloud Five Push") }, 
      function() { console.log("Failed to register for push notifications")},
      'CloudFivePush', 
      'register', 
      [userIdentifier]
    );  
  }
};
