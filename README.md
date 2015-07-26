# REDIS silly example

Just a first attemp to connect to **REDIS** db though *java*. That java code was the typical *heroku* example.

As well as "template" on how to use different remote GIT repositories to use different *"cloud"* platforms. Let's have a look at its *.git/config* file:

      [core]
	repositoryformatversion = 0
	filemode = true
	bare = false
	logallrefupdates = true
	ignorecase = true
	precomposeunicode = true
      [remote "origin"]
	url = https://github.com/heroku/java-getting-started.git
	fetch = +refs/heads/*:refs/remotes/origin/*
      [branch "master"]
	remote = origin
	merge = refs/heads/master
      [remote "heroku"]
	url = https://git.heroku.com/limitless-lake-1088.git
	fetch = +refs/heads/*:refs/remotes/heroku/*
      [remote "github"]
	url = git@github.com:xue2sheng/redis_silly_example.git
	fetch = +refs/heads/*:refs/remotes/github/*


## java-getting-started

A barebones Java app, which can easily be deployed to Heroku.  

This application support the [Getting Started with Java on Heroku](https://devcenter.heroku.com/articles/getting-started-with-java) article - check it out.

### Running Locally

Make sure you have Java and Maven installed.  Also, install the [Heroku Toolbelt](https://toolbelt.heroku.com/).

```sh
$ git clone https://github.com/heroku/java-getting-started.git
$ cd java-getting-started
$ mvn install
$ foreman start web
```

Your app should now be running on [localhost:5000](http://localhost:5000/).

If you're going to use a database, ensure you have a local `.env` file that reads something like this:

```
DATABASE_URL=postgres://localhost:5432/java_database_name
```

### Deploying to Heroku

```sh
$ heroku create
$ git push heroku master
$ heroku open
```

### Documentation

For more information about using Java on Heroku, see these Dev Center articles:

- [Java on Heroku](https://devcenter.heroku.com/categories/java)
