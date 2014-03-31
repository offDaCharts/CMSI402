import json
import requests
import flask

from flask import Flask, render_template, redirect, url_for, request, Response, abort, make_response, flash
from pymongo import Connection
from functools import wraps
from flaskext.bcrypt import Bcrypt

app = Flask(__name__)
app.config.from_object(__name__)
app.secret_key = 'this is the secret'
bcrypt = Bcrypt(app)    
      
@app.route('/')
def show_home():
    return render_template('home.html')



# App Configuration
# This section holds all application specific configuration options.

if __name__ == '__main__':
	app.debug=True
	app.run(host='0.0.0.0', port=5656, processes=1)


# pw_hash = bcrypt.generate_password_hash('hunter2')
# bcrypt.check_password_hash(pw_hash, 'hunter2') # returns True


