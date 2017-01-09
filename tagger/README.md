# CRFSuite Tagger

## Overview

We are using a number of open source libraries to enable the machine learning model to be built in Python. These libraries are:

* CRFSuite: [github](https://github.com/chokkan/crfsuite) and [project page](http://www.chokkan.org/software/crfsuite/)
* CQDB: [project page](http://www.chokkan.org/software/cqdb/)
* libLBFGS: [github](https://github.com/chokkan/liblbfgs) and [project page](http://www.chokkan.org/software/liblbfgs/)

These libraries are written in C for performance and portability.

The machine learning model we create is in CQDB format. The model is used when parsing raw address strings into tokens with probabilities. In CRFSuite, this process is called tagging.

We created a C / JNI shim, the CRFSuite Tagger, to enable tagging from Scala code.

This sub-project contains the C code for the shim and a simple Scala test harness.

The purpose of this page is to document the build process for:
* macOS for local development
* Windows for local development
* CentOS for deployment to a server environment (e.g. Cloud Foundry)

## Building on macOS

### Installing the tools

* Install Xcode from the App Store
* Install the Xcode [command line tools](https://developer.apple.com/library/content/technotes/tn2339/_index.html) from a terminal window:
```
$ xcode-select --install
```

### Building the shared library
```
$ cd /path/to/address-index-api/tagger/crftagger

# build libbfgs first
$ cd libbfgs
$ ./configure --prefix=$HOME/local
$ make clean && make
$ make install

# then build crfsuite (depends on libbfgs)
$ cd ../crfsuite
$ ./configure --prefix=$HOME/local --with-liblbfgs=$HOME/local
$ make clean && make # you will see some warnings re inline assignments but these can be ignored
$ make install

# finally, build the tagger (depends on crfsuite and libbfgs)
$ cd ../crftagger
$ make
$ make install
```

### Testing on macOS
```
$ cd /path/to/address-index-api/tagger/crftagger/test

# build the test harness
$ scalac uk/gov/ons/addressIndex/crfscala/CrfScalaJniImpl.scala test.scala

# run the simple multi-threading test
$ scala Test
```

## Building on Windows
TODO

## Building on CentOS
TODO
