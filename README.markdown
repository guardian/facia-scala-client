Facia Scala Client
==================

Low-level client for Facia JSON API.

Usage
-----

### Adding the dependency to SBT

Add the following line to your [SBT build file] (http://www.scala-sbt.org/0.13.5/docs/Getting-Started/Basic-Def.html):

    libraryDependencies += "com.gu" %% "facia-api-client" % "0.4"

### Making calls to the API

#### Config

```scala
ApiClient.config
```

Returns the current config.

#### Collection

```scala
ApiClient.collection("id goes here")
```

Returns the collection with the given ID.
