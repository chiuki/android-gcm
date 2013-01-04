#!/usr/bin/python

import BaseHTTPServer
import cgi
import sys
import httplib
import json
import urllib
import urllib2
import urlparse

API_KEY = None
reg_id_set = set()

class GCMHandler(BaseHTTPServer.BaseHTTPRequestHandler):
  def do_GET(self):
    return self.index()

  def do_POST(self):
    (content_type, dict) = cgi.parse_header(
        self.headers.getheader('content-type'))
    if content_type == 'multipart/form-data':
      params = cgi.parse_multipart(self.rfile, dict)
    elif content_type == 'application/x-www-form-urlencoded':
      length = int(self.headers.getheader('content-length'))
      params = cgi.parse_qs(self.rfile.read(length), keep_blank_values=1)
    else:
      params = {}

    if self.path == "/register":
      return self.register(params)
    if self.path == "/send":
      return self.send(params)

    self.send_response(500)

  def str_param(self, list, name, default):
    try:
      value = list[name][0].strip()
      if len(value) > 0:
        return value
      else:
        return default
    except:
      return default

  def index(self):
    """
      Show all the registered devices, with a form the send them a message.
    """
    global reg_id_set
    self.send_response(200)
    self.send_header('Content-type', 'text/html')
    self.end_headers()

    reg_id_list = list(reg_id_set)

    html = """
<html>
  <head>
    <title>Sample GCM Server</title>
  </head>
  <body>
"""

    if len(reg_id_list) == 0:
      html += "<h3>No registered devices</h3>"
    else:
      html += """<h3>Registered Ids</h3>
      <form action="/send" method="post">
        Message: <input name="msg" size="30" />
        <input type="submit" value="Send" />
        <br />
        Devices:
        <table>"""

      for reg_id in reg_id_list:
        html += """
          <tr>
            <td>
              <input type="checkbox" name="reg_id" value="%s" checked />%s
            </td>
          </tr>
        """ % (reg_id, reg_id)

      html += """
      </table>
    </form>"""

    html += """
  </body>
</html>
"""

    self.wfile.write(html)
    return


  def register(self, params):
    """
      Stores the registration ids sent via the 'reg_id' parameter

      Sample request:
      curl -d "reg_id=test_id" http://localhost:8080/register
    """
    global reg_id_set
    if 'reg_id' in params and len(params['reg_id']) > 0:
      reg_id_set = reg_id_set.union(set(params['reg_id']))
      self.send_response(200)
      return

    self.send_response(400)
    return


  def send(self, params):
    """
      Sends a message to the devices.
      The message is specified by the 'msg' parameter.
      The devices are specified by the 'reg_id' parameter. If the request does
      not contain any registration ids, the message will be sent to all
      devices recorded by /register

      Sample request:
      curl -d "reg_id=test_id&msg=Hello" http://localhost:8080/send
    """
    global API_KEY
    global reg_id_set

    msg = self.str_param(params, 'msg', 'Greetings from the cloud!')

    reg_id_list = None
    if 'reg_id' in params and len(params['reg_id']) > 0:
      reg_id_list = params['reg_id']

    if reg_id_list is None:
      sys.stderr.write('Sending message to all registered devices\n')
      reg_id_list = list(reg_id_set)

    data = {
      'registration_ids' : reg_id_list,
      'data' : {
        'msg' : msg
      }
    }

    headers = {
      'Content-Type' : 'application/json',
      'Authorization' : 'key=' + API_KEY
    }

    url = 'https://android.googleapis.com/gcm/send'
    request = urllib2.Request(url, json.dumps(data), headers)

    try:
      response = urllib2.urlopen(request)
      self.send_response(200)
      self.send_header('Content-type', 'text/html')
      self.end_headers()
      self.wfile.write(self.make_gcm_summary(data, response))
      return
    except urllib2.HTTPError, e:
       print e.code
       print e.read()

    return


  def make_gcm_summary(self, data, response):
    """
      Helper function to display the result of a /send request.
    """
    json_string = response.read()
    json_response = json.loads(json_string)

    html = """
<html>
  <head>
    <title>GCM send result</title>
  </head>
  <body>
    <h2>Request</h2>
    <pre>%s</pre>
    <h2>Response</h2>
    <pre>%s</pre>
    <h3>Per device</h3>
    <ol>""" % (repr(data), json_string)

    reg_id_list = data['registration_ids']
    for i in xrange(len(reg_id_list)):
      reg_id = reg_id_list[i]
      result = json_response['results'][i]

      html += """
        <li>
          reg_id: <code>%s</code><br/>
          <pre>%s</pre>
        </li>""" % (reg_id, json.dumps(result))

      html += """
    </ol>
    <a href="/">Back</a>
  </body>
</html>"""
    return html


def main(argv):
  global API_KEY

  if len(argv) != 1:
    sys.stderr.write('Usage: gcm_server.py\n')
    sys.exit(1)

  if API_KEY is None:
    sys.stderr.write('Missing API_KEY\n')
    sys.exit(1)

  server = BaseHTTPServer.HTTPServer(('', 8080), GCMHandler)
  print 'Starting server on port 8080'
  server.serve_forever()

if __name__ == '__main__':
  main(sys.argv)
