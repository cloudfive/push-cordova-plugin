=== Cloud Five Push ===
== Cordova/Phonegap plugin ==
 
 This is a phonegap plugin that makes integration with Cloud Five Push extremely simple. It was designed on Phonegap 3.5 but it probably works on lower versions. 

== Installation ==

The plugin conforms to plugman standards so installation is easy: 

    $ cordova plugin install https://github.com/tenforwardconsulting/cloud-five-push-plugin.git

To register/activate push notifications all you need to do is put this in your javascript:
    
    CloudFivePush.register('user-identifer');

Where `user-identifier` is something like 

This will register the user anonymously which is useful if you only need to send occasionally "broadcast" messages to your entire userbase.  This probably isn't 


== Setup for Apple ==

== Setup for Android == 

http://developer.android.com/google/gcm/gs.html

Open the Google Developers Console. - https://cloud.google.com/console
Create/select your project and enable cloud messaging


==LICENSE == 

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