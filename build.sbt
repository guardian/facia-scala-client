import sbtrelease._
import ReleaseStateTransformations._
import Dependencies._

organization := "com.gu"

name := "facia-api-client"

scalaVersion := "2.10.4"

description := "Scala client for The Guardian's Facia JSON API"

val sonatypeReleaseSettings = releaseSettings ++ sonatypeSettings ++ Seq(
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
  ReleaseKeys.crossBuild := true,
  ReleaseKeys.releaseProcess := Seq[ReleaseStep](
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
      },
      enableCrossBuild = true
    ),
    setNextVersion,
    commitNextVersion,
    ReleaseStep(
      action = state => Project.extract(state).runTask(SonatypeKeys.sonatypeReleaseAll, state)._1,
      enableCrossBuild = false
    ),
    pushChanges
  )
)

lazy val root = (project in file("."))
  .aggregate(faciaJson, fapiClient)
  .settings(publishArtifact := false)

lazy val faciaJson = project.in(file("facia-json"))
  .settings(
    crossScalaVersions := Seq("2.10.4", "2.11.4"),
    scalaVersion := "2.11.4",
    organization := "com.gu",
    name := "facia-json",
    libraryDependencies ++= Seq(
      awsSdk,
      commonsIo,
      specs2,
      playJson
    ),
    publishArtifact := true
  )
  .settings(sonatypeReleaseSettings: _*)

lazy val fapiClient = project.in(file("fapi-client"))
  .settings(
    scalaVersion := "2.11.4",
    crossScalaVersions := Seq("2.10.4", "2.11.4"),
    organization := "com.gu",
    name := "fapi-client",
    resolvers ++= Seq(
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    libraryDependencies ++= Seq(
      contentApi,
      scalaTest,
      mockito
    ),
    publishArtifact := true
  )
  .dependsOn(faciaJson)
  .settings(sonatypeReleaseSettings: _*)
