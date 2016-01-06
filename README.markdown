Facia Scala Client
==================

Facia's Scala client is split into two parts.

## The Fronts API client

The `fapi-client` project contains the main Fronts API client. This provides ways to fetch Fronts
data and includes high-level logic to convert Facia's low-level internal data structures into more
easily-used types.

### Adding the dependency to SBT

Add the following line to your [SBT build file] (http://www.scala-sbt.org/0.13.5/docs/Getting-Started/Basic-Def.html):

    libraryDependencies += "com.gu" %% "fapi-client" % "0.63"

### Using the library

To use the Fronts API client you will need instances of:

* the content API client
* the underlying Facia JSON library

These instances will need to be configured with your access/API keys. The `FAPI` class contains
the public interface to the behaviour in the library. Examples of how to use the client can be
found in the `demo.sc` file, an IntelliJ worksheet.

## Low-level client for Facia JSON API.

The low-level Facia API client is designed to fetch and manipulate Facia's internal JSON structures.
This library provides underlying behaviour for the main Fronts API client.

### Adding the dependency to SBT

Add the following line to your [SBT build file] (http://www.scala-sbt.org/0.13.5/docs/Getting-Started/Basic-Def.html):

    libraryDependencies += "com.gu" %% "facia-json" % "0.63"

### Making calls to the API

##### Config

```scala
ApiClient.config
```

Returns the current config.

##### Collection

```scala
ApiClient.collection("id goes here")
```

Returns the collection with the given ID.

## Running the tests

To run the tests you need to pass a content api key to the library via your environment.
 
    export CONTENT_API_KEY="<api-key>"
    sbt test
