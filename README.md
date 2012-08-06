GCM Sample Client and Server
============================

Sample code for [Google Cloud Messaging for Android][1]. 
The client is an Android app and the server is a python script.

Client
------
You need to change the value of two variables in `Constants.java`:

  - `SENDER_ID`: Change this to the project id from your API project created at `code.google.com`, as shown in the url for your project.
  - `SERVER_URL`: Change this to match your server

When the app starts, it checks if is already registered with GCM. If not, it registers the device with GCM, then send the registration id to the server.

Server
------
You need to change `API_KEY` to the one you generated from your API project at `code.google.com`.

The server has three endpoints:

  - `/`
  Shows the list of registered devices, with a form to send them a message.
  - `/register`
  Stores the registration id sent from the device.
  -  `/send`
  Sends a message to the devices. 

  [1]: http://developer.android.com/guide/google/gcm/index.html
