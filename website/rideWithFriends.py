import json
import requests
import flask
import subprocess
import smtplib

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
from email.mime.text import MIMEText


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

    def __init__(self, uid=None, username=None, password=None, email=None):
        query = {}
        #if uid is not None: query['_id'] = uid 
        if username is not None: query['username'] = username 
        #if password is not None: query['password'] = bcrypt.generate_password_hash(password + 'garlic_salt') 
        existing = db['users'].find_one(query) 

        if existing is not None:
            self.username = existing['username']
            self.id = existing['_id']
            self.password = existing['password']
        else:
            password_hash = bcrypt.generate_password_hash(password + 'garlic_salt')
            self.id = db['users'].insert({'username': username, 'email': email ,'password': password_hash})
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

def send_email(emails, usernames):
    GMAIL_USERNAME = 'ridewithfriendsbot'
    GMAIL_PASSWORD = 'imokwithsharingthis'
    recipients = ", ".join(emails)
    print recipients
    email_subject = "Ride Together!"
    body_of_email = (" and ".join(usernames) + ", \nYou two are riding within 1km of eachother." + 
        "You should meet up and Ride With Friends!" + 
        "\n\nHave Fun!" +
        "\n-RideWithFriends")

    session = smtplib.SMTP('smtp.gmail.com', 587)
    session.ehlo()
    session.starttls()
    session.login(GMAIL_USERNAME, GMAIL_PASSWORD)

    headers = "\r\n".join(["from: " + GMAIL_USERNAME,
                           "subject: " + email_subject,
                           "to: " + recipients,
                           "mime-version: 1.0",
                           "content-type: text/html"])

    content = headers + "\r\n\r\n" + body_of_email
    session.sendmail(GMAIL_USERNAME, emails[0], content)
    session.sendmail(GMAIL_USERNAME, emails[1], content)
    

@app.route("/updatelocation/<location>/<username>", methods=["GET", "POST"])
def update_location(location, username):
    locations = db['locations']
    locations.update({'username': username}, {'location': location, 'username': username})
    [thisLat, thisLon] = map(float, location.split(','))
    thisUser = db['users'].find_one({'username': username})
    print thisUser

    cursor = locations.find({'username': {'$ne': username}})
    documentList = get_list_from_cursor(cursor)

    closenessThreshold = 0.01 #~1km in all directions
    for document in documentList:
        print document
        try:
            [lat, lon] = map(float, document['location'].split(','))
        except:
            print 'bad location data'
            continue            
        if abs(thisLat - lat) < 0.01 and abs(thisLon - lon) < 0.01:
            print "Heyyyy they're close enough"
            closeUser = db['users'].find_one({'username': document['username']})
            print closeUser
            send_email([thisUser['email'], closeUser['email']], [thisUser['username'], closeUser['username']])


    return "submitted"

@app.route("/rides/<username>")
def get_rides(username):
    rides = db['rides']
    cursor = rides.find({'username': username})
    documentList = get_list_from_cursor(cursor)
    print documentList
    return JSONEncoder().encode(documentList) if documentList else '[]'

@app.route("/rides")
def get_all_rides():
    rides = db['rides']
    cursor = rides.find()
    documentList = get_list_from_cursor(cursor)
    print documentList
    return JSONEncoder().encode(documentList) if documentList else '[]'

# Create user loader function
@login_manager.user_loader
def load_user(userid):
    return User(uid=userid)

class LoginForm(Form):
    username = TextField('Username', [validators.Required()])
    email = TextField('Email', [validators.Required()])
    password = PasswordField('Password', [validators.Required()])
    remember = BooleanField('Remember Me', default=False)

@app.route("/login", methods=["GET", "POST"])
def login():
    form = LoginForm()
    if form.validate_on_submit():
        user = User(username=form.username.data, password=form.password.data, email=form.email.data)
        if user.active is not False:
            login_user(user, remember=form.remember.data)
            flash("Welcome to Ride With Friends, %s!" % user.username, 'success')
            return redirect(request.args.get('next') or url_for("show_home"))
        else:
            flash("Login failed", 'danger')

    return render_template("login.html", form=form)

@login_manager.unauthorized_handler
def unauthorized():
    return redirect(url_for("login"))


@app.route("/logout", methods=["GET", "POST"])
@login_required
def logout():
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


