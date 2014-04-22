import json
import requests
import flask

from bson import json_util, ObjectId
from flask import Flask, render_template, redirect, url_for, request, Response, abort, make_response, flash
from flask import abort, flash, Flask, g, make_response, render_template, redirect, request, Response, session, url_for
from pymongo import Connection
from functools import wraps
from flaskext.bcrypt import Bcrypt
from flask.ext import admin, login
from flask.ext.login import LoginManager, login_user, UserMixin, login_required, logout_user, current_user
from flask.ext.wtf import Form
from wtforms import BooleanField, TextField, PasswordField, validators


app = Flask(__name__)
app.config.from_object(__name__)
app.secret_key = 'this is the secret'
bcrypt = Bcrypt(app)    

login_manager = LoginManager()
login_manager.init_app(app)

connection = Connection()
db = connection['rideWithFriends']

@login_manager.user_loader
def load_user(userid):
    return User.get(userid)

class JSONEncoder(json.JSONEncoder):
    def default(self, o):
        return (isinstance(o, ObjectId) or isinstance(o, datetime.timedelta)
            ) and str(o) or json.JSONEncoder.default(self, o)

# # Create user model
class User(UserMixin):

    def __init__(self, uid=None, username=None, password=None):
        query = {}
        if uid is not None: query['_id'] = uid 
        if username is not None: query['username'] = username 
        if password is not None: query['password'] = bcrypt.generate_password_hash(password + 'garlic_salt') 
        existing = db['users'].find_one(query) 

        if existing is not None:
            self.username = existing['username']
            self.id = existing['_id']
            self.password = existing['password']
        else:
            password_hash = bcrypt.generate_password_hash(password + 'garlic_salt')
            self.id = db['users'].insert({'username': username, 'password': password_hash})
            self.username = username
            self.password = password_hash
        self.active = True

    # Flask-Login integration
    def is_authenticated(self):
        return True

    def is_active(self):
        return self.active

    def is_anonymous(self):
        return False

    def get_id(self):
        return self.id

    def get_username(self):
        return self.username

    def get_password(self):
        return self.password

@app.route('/')
def show_home():
    return render_template('home.html')

@app.route('/loginPage')
def show_login():
    return render_template('login.html')

@app.route('/stats')
def stats():
    return render_template('stats.html')

@app.route('/routes')
def routes():
    return render_template('routes.html')

@app.route('/profile/<username>')
def profile(username):
    return render_template('profile.html')

@app.route("/saveride/<time>/<distance>/<maxspeed>/<username>", methods=["GET", "POST"])
def save_ride(time, distance, maxspeed, username):
    rides = db['rides']
    rides.insert({'time': time, 'distance': distance, 'maxspeed': maxspeed, 'username': username})
    return "submitted"

@app.route("/rides/<username>")
def get_rides(username):
    rides = db['rides']
    cursor = rides.find({'username': username})
    documentList = get_list_from_cursor(cursor)
    print documentList
    return JSONEncoder().encode(documentList) if documentList else '[]'

# Create user loader function
@login_manager.user_loader
def load_user(userid):
    return User(uid=userid)

class LoginForm(Form):
    username = TextField('Username', [validators.Required()])
    password = PasswordField('Password', [validators.Required()])
    remember = BooleanField('Remember Me', default=False)

@app.route("/login", methods=["GET", "POST"])
def login():
    form = LoginForm()
    if form.validate_on_submit():
        user = User(username=form.username.data, password=form.password.data)
        if user.active is not False:
            login_user(user, remember=form.remember.data)
            flash("Welcome to PLAnet, %s!" % user.username, 'success')
            return redirect(request.args.get('next') or url_for("show_home"))
        else:
            flash("Sorry, but your login attempt did not succeed.", 'danger')

    return render_template("login.html", form=form)

@login_manager.unauthorized_handler
def unauthorized():
    return redirect(url_for("login"))


@app.route("/logout", methods=["GET", "POST"])
@login_required
def logout():
    flash("Thank you, come again!")
    logout_user()
    return redirect(url_for("show_home"))

## Internal Functions
@app.before_request
def before_request():
    g.user = current_user

def get_list_from_cursor(cursor):
    list_of_documents = []
    for document in cursor:
        list_of_documents.append(document)
    return list_of_documents

# App Configuration
# This section holds all application specific configuration options.

if __name__ == '__main__':
	app.debug=True
	app.run(host='0.0.0.0', port=5656, processes=1)


# pw_hash = bcrypt.generate_password_hash('hunter2')
# bcrypt.check_password_hash(pw_hash, 'hunter2') # returns True


