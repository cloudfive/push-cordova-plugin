# Cloud Five Push

## Cordova/Phonegap plugin
 This is a phonegap plugin that makes integration with Cloud Five Push extremely simple. It was designed on Phonegap 3.5 but it probably works on lower versions down to 3.0. 

## Installation

The plugin conforms to plugman standards so installation is easy:

    $ cordova plugin add https://github.com/tenforwardconsulting/cloud-five-push-plugin.git

To register/activate push notifications all you need to do is put this in your javascript:

    CloudFivePush.register();

This will register the user anonymously which is useful if you only need to send occasionally "broadcast" messages to your entire userbase.  If you want to send messages to individual users, you can specify an identifier, i.e. something like an e-mail address, user id or some other way your application identifies users.  Then you can target those individual users. 

    CloudFivePush.register('user-identifer');

If you are sending custom/arbitrary payloads, you should register a callback for when the user acknowledges your notification:
   
    CloudFivePush.onPushActivated(function(payload) {
        // This will receive the payload specified in the push message
    });


## Removal

To remove this plugin, simply execute 
    
    $ cordova plugin remove com.cloudfiveapp.push


## Setup for Apple

### Background Notifications

If you push a notification having ```content-available: 1``` and configure your app to use ```UIBackgroundModes: "location"```, your app will awaken to receive a push notifiction in the background.  iOS will provide your app exactly 30s of background-running.  In your ```#onPushReceived``` callback, you must execute the ```#finish``` method to signal to the native plugin that your background-running is complete so the plugin can gracefully kill the background-thread before 30s expires.  If you don't, ios will kill your app.

```
CloudFivePush.onPushReceived(function(payload) {
  // A push notification has arrived.  Let's talk to our server about something.
  $.get({
    url: 'my/server',
    success: function() {
      CloudFivePush.finish();   // <-- Signal to plugin that your background-running is complete.
    }
  });
});
```


## Setup for Android

This best reference for instructions are here: http://developer.android.com/google/gcm/gs.html  Briefly, the steps are: 

  * Open the Google Developers Console. - https://cloud.google.com/console

  * Create/select your project and enable cloud messaging

  * Create an API key

  * put your SenderID (project number) in the <gcmSenderId> tag in config.xml and re-run `cordova prepare android`

Add ```<gcmSenderId>YOUR_PROJECT_ID</gcmSenderId>``` to your main config.xml and run cordova prepare.
This should be the project id from the Google Developers Console.

## Sending messages



## LICENSE

    The MIT License

    Copyright (c) 2014 Ten Forward Consulting, Inc.
    portions Copyright (c) 2012 Adobe Systems, inc.
    portions Copyright (c) 2012 Olivier Louvignes

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
