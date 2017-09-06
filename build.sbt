import Dependencies._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease._

organization := "com.gu"

name := "facia-api-client"

scalaVersion := "2.11.11"

scalaVersion in ThisBuild := "2.11.11"

description := "Scala client for The Guardian's Facia JSON API"

val sonatypeReleaseSettings = Seq(
  licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/guardian/facia-scala-client"),
    "scm:git:git@github.com:guardian/facia-scala-client.git"
  )),
  pomExtra := (
    <url>https://github.com/guardian/facia-scala-client</url>
      <developers>
        <developer>
          <id>robertberry</id>
          <name>Robert Berry</name>
          <url>https://github.com/robertberry</url>
        </developer>
        <developer>
          <id>janua</id>
          <name>Francis Carr</name>
          <url>https://github.com/janua</url>
        </developer>
        <developer>
          <id>adamnfish</id>
          <name>Adam Fisher</name>
          <url>https://github.com/adamnfish</url>
        </developer>
      </developers>
    ),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    ReleaseStep(
      action = { state =>
        val extracted = Project.extract(state)
        val ref = extracted.get(Keys.thisProjectRef)
        extracted.runAggregated(PgpKeys.publishSigned in Global in ref, state)
      }
    ),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
)

lazy val root = (project in file("."))
  .aggregate(faciaJson, faciaJson_play25, faciaJson_play26, fapiClient, fapiClient_play25, fapiClient_play26)
  .settings(publishArtifact := false)
  .settings(sonatypeReleaseSettings: _*)

lazy val faciaJson = project.in(file("facia-json"))
  .settings(sonatypeReleaseSettings: _*)
  .settings(
    organization := "com.gu",
    name := "facia-json",
    resolvers ++= Seq(
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalacOptions := Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      awsSdk,
      commonsIo,
      specs2,
      playJson,
      scalaLogging
    ),
    publishArtifact := true
  )
  .settings(sonatypeReleaseSettings: _*)

lazy val faciaJson_play25 = project.in(file("facia-json-play25"))
  .settings(sonatypeReleaseSettings: _*)
  .settings(
    organization := "com.gu",
    name := "facia-json-play25",
    sourceDirectory := baseDirectory.value / "../facia-json/src",
    resolvers ++= Seq(
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalacOptions := Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      awsSdk,
      commonsIo,
      specs2,
      playJson25,
      scalaLogging
    ),
    publishArtifact := true
  )
  .settings(sonatypeReleaseSettings: _*)

lazy val faciaJson_play26 = project.in(file("facia-json-play26"))
  .settings(sonatypeReleaseSettings: _*)
  .settings(
    organization := "com.gu",
    name := "facia-json-play26",
    sourceDirectory := baseDirectory.value / "../facia-json/src",
    resolvers ++= Seq(
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalacOptions := Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      awsSdk,
      commonsIo,
      specs2,
      playJson26,
      scalaLogging
    ),
    publishArtifact := true
  )
  .settings(sonatypeReleaseSettings: _*)

lazy val fapiClient = project.in(file("fapi-client"))
  .settings(sonatypeReleaseSettings: _*)
  .settings(
    organization := "com.gu",
    name := "fapi-client",
    resolvers ++= Seq(
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Guardian Frontend Bintray" at "https://dl.bintray.com/guardian/frontend",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalacOptions := Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      contentApi,
      commercialShared,
      scalaTest,
      mockito
    ),
    publishArtifact := true
  )
  .dependsOn(faciaJson)

lazy val fapiClient_play25 = project.in(file("fapi-client-play25"))
  .settings(sonatypeReleaseSettings: _*)
  .settings(
    organization := "com.gu",
    name := "fapi-client-play25",
    sourceDirectory := baseDirectory.value / "../fapi-client/src",
    resolvers ++= Seq(
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Guardian Frontend Bintray" at "https://dl.bintray.com/guardian/frontend",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalacOptions := Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      contentApi,
      commercialShared,
      scalaTest,
      mockito
    ),
    publishArtifact := true
  )
  .dependsOn(faciaJson_play25)

lazy val fapiClient_play26 = project.in(file("fapi-client-play26"))
  .settings(sonatypeReleaseSettings: _*)
  .settings(
    organization := "com.gu",
    name := "fapi-client-play26",
    sourceDirectory := baseDirectory.value / "../fapi-client/src",
    resolvers ++= Seq(
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Guardian Frontend Bintray" at "https://dl.bintray.com/guardian/frontend",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalacOptions := Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      contentApi,
      commercialShared,
      scalaTest,
      mockito
    ),
    publishArtifact := true
  )
  .dependsOn(faciaJson_play26)
