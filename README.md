# README #



### What is this repository for? ###

* Address Index is a Play Framework application which resolves address query strings to actual addresses.
* Version 0.0.1

### How do I get set up? ###

* Install `java`
* Install `sbt`
* Run `sbt -Des.path.home="location/to/store/es/data/if/local"` or just `sbt` if you're using a remote elasticsearch.
* For Windows put double quotes around complete args string e.g. 'sbt "-Des.path.home=C:\\\es\\\data"'
* `project address-index-server`
* `run`
* Go to web browser [localhost:9000](localhost:9000)

### Contribution guidelines ###

* Standard Scala style
* Testing: FlatSpec > FreeSpec
* Style: Infix > Not Infix, named params when param count > 1
* Scaladoc
* GitFlow
* Pull requests must have tests

### Who do I talk to? ###

* Rhys Bradbury
* Flavian Alexandru
