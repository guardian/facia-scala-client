import sbtrelease._
import ReleaseStateTransformations._
import Dependencies._

organization := "com.gu"

name := "facia-api-client"

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.10.4", "2.11.4")

libraryDependencies ++= Seq(
  awsSdk,
  commonsIo,
  playJson,
  specs2
)

releaseSettings

sonatypeSettings

description := "Scala client for The Guardian's Facia JSON API"

scmInfo := Some(ScmInfo(
  url("https://github.com/guardian/facia-scala-client"),
  "scm:git:git@github.com:guardian/facia-scala-client.git"
))

pomExtra := (
  <url>https://github.com/guardian/facia-scala-client</url>
    <developers>
      <developer>
        <id>robertberry</id>
        <name>Robert Berry</name>
        <url>https://github.com/robertberry</url>
      </developer>
    </developers>
)

licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

ReleaseKeys.crossBuild := true

ReleaseKeys.releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(
    action = state => Project.extract(state).runTask(PgpKeys.publishSigned, state)._1,
    enableCrossBuild = true
  ),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(state => Project.extract(state).runTask(SonatypeKeys.sonatypeReleaseAll, state)._1),
  pushChanges
)

