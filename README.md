Backend DER
===========

**Based on Modern Web Template from activator**

A backend for DER using MongoDB and Play framework


Requirement
----------

Your development environment will require:
*  Activator / Play.
*  MongoDB.

Getting started
----------

1. Go to directory where the github project located
2. Run the activator from command line (type "activator" in window cmd)
3. enter "run"

Testing
----------

**Highly recommended to backup data from your MongoDB**

1. The test will require "test" database on mongo, if you use it please make backup of the database and drop the database (db.dropDatabase())
2. copy the dump folder in test folder to the mongoDB instalation folder and restore it (mongorestore)
3. (Optional) import many-stream.json in test folder to mongo (mongoimport --db test --collection streams many-stream.json)
4. type "activator test" in shell at project folder