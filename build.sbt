import Dependencies._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease._

organization := "com.gu"

name := "facia-api-client"

description := "Scala client for The Guardian's Facia JSON API"

val scala211 = "2.11.12"
val scala212 = "2.12.8"

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

val publishSettings = Seq(
  publishArtifact := true,
  publishTo := sonatypePublishTo.value
)

lazy val root = (project in file("."))
  .aggregate(faciaJson, faciaJson_play25, faciaJson_play26, fapiClient, fapiClient_play25, fapiClient_play26)
  .settings(sonatypeReleaseSettings: _*)
  .settings(
    publishArtifact := false,
    skip in publish := true
  )

lazy val faciaJson = project.in(file("facia-json"))
  .settings(
    organization := "com.gu",
    name := "facia-json",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("public"),
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalaVersion := scala211,
    crossScalaVersions:= Seq(scala211),
    scalacOptions := Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      awsSdk,
      commonsIo,
      specs2,
      playJson24,
      scalaLogging
    ),
    publishSettings
  )
  .settings(sonatypeReleaseSettings: _*)

lazy val faciaJson_play25 = project.in(file("facia-json-play25"))
  .settings(
    organization := "com.gu",
    name := "facia-json-play25",
    sourceDirectory := baseDirectory.value / "../facia-json/src",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("public"),
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalaVersion := scala211,
    crossScalaVersions:= Seq(scala211),
    scalacOptions := Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      awsSdk,
      commonsIo,
      specs2,
      playJson25,
      scalaLogging
    ),
    publishSettings
  )
  .settings(sonatypeReleaseSettings: _*)

lazy val faciaJson_play26 = project.in(file("facia-json-play26"))
  .settings(
    organization := "com.gu",
    name := "facia-json-play26",
    sourceDirectory := baseDirectory.value / "../facia-json/src",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("public"),
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalaVersion := scala211,
    crossScalaVersions:= Seq(scala211, scala212),
    releaseCrossBuild := true,
    scalacOptions := Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      awsSdk,
      commonsIo,
      specs2,
      playJson26,
      scalaLogging
    ),
    publishSettings
  )
  .settings(sonatypeReleaseSettings: _*)

lazy val fapiClient = project.in(file("fapi-client"))
  .settings(
    organization := "com.gu",
    name := "fapi-client",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("public"),
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Guardian Frontend Bintray" at "https://dl.bintray.com/guardian/frontend",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalaVersion := scala211,
    crossScalaVersions:= Seq(scala211),
    scalacOptions := Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      contentApi,
      contentApiDefault,
      commercialShared,
      scalaTest,
      mockito
    ),
    publishSettings
  )
  .dependsOn(faciaJson)
  .settings(sonatypeReleaseSettings: _*)

lazy val fapiClient_play25 = project.in(file("fapi-client-play25"))
  .settings(
    organization := "com.gu",
    name := "fapi-client-play25",
    sourceDirectory := baseDirectory.value / "../fapi-client/src",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("public"),
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Guardian Frontend Bintray" at "https://dl.bintray.com/guardian/frontend",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalaVersion := scala211,
    crossScalaVersions:= Seq(scala211),
    scalacOptions := Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      contentApi,
      contentApiDefault,
      commercialShared,
      scalaTest,
      mockito
    ),
    publishSettings
  )
  .dependsOn(faciaJson_play25)
  .settings(sonatypeReleaseSettings: _*)

lazy val fapiClient_play26 = project.in(file("fapi-client-play26"))
  .settings(
    organization := "com.gu",
    name := "fapi-client-play26",
    sourceDirectory := baseDirectory.value / "../fapi-client/src",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("public"),
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Guardian Frontend Bintray" at "https://dl.bintray.com/guardian/frontend",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalaVersion := scala211,
    crossScalaVersions:= Seq(scala211, scala212),
    releaseCrossBuild := true,
    scalacOptions := Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      contentApi,
      contentApiDefault,
      commercialShared,
      scalaTest,
      mockito
    ),
    publishSettings
  )
  .dependsOn(faciaJson_play26)
  .settings(sonatypeReleaseSettings: _*)
