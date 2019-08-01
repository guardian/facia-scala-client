import Dependencies._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease._

organization := "com.gu"

name := "facia-api-client"

description := "Scala client for The Guardian's Facia JSON API"

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
  .aggregate(faciaJson_play26, fapiClient_play26)
  .settings(sonatypeReleaseSettings: _*)
  .settings(
    publishArtifact := false,
    skip in publish := true
  )

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
    scalaVersion := scala212,
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
    scalaVersion := scala212,
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
