# fsm-pdf-merge

REST service merging Content Version documents (PDF only) to single PDF. Merged document is saved as
new Content Version document and optionally linked to SObjects.

## Running Locally

Make sure you have Java installed.  Also, install the [Heroku Toolbelt](https://toolbelt.heroku.com/).

```sh
$ git clone https://github.com/goforce/fsm-pdf-merge.git
$ cd fsm-pdf-merge
$ ./gradlew stage
$ heroku local web
```

Your app should now be running on [localhost:5000](http://localhost:5000/).

## Deploying to Heroku

```sh
$ heroku create
$ git push heroku master
$ heroku open
```

## Documentation

For more information about using Java on Heroku, see these Dev Center articles:

- [Java on Heroku](https://devcenter.heroku.com/categories/java)
- [Deploying Spring Boot apps to Heroku] (https://devcenter.heroku.com/articles/deploying-spring-boot-apps-to-heroku)
- [Preparing Spring Boot app for production on Heroku] (https://devcenter.heroku.com/articles/preparing-a-spring-boot-app-for-production-on-heroku)