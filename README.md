# fsm-pdf-merge

Merge list of ContentVersion pdf documents into single pdf. All ContentVersions should be of type pdf.
REST service accepting list of ContentVersion Ids and returning single Id of merged ContentVersion document.

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