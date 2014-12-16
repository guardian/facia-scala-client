import sbtrelease._
import ReleaseStateTransformations._
import Dependencies._

organization := "com.gu"

name := "facia-api-client"

scalaVersion := "2.10.4"

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

lazy val root = (project in file(".")).
  aggregate(faciajson, fapiclient)


lazy val faciajson = project.in(file("facia-json"))
  .settings(
    crossScalaVersions := Seq("2.10.4", "2.11.4"),
    scalaVersion := "2.11.4",
    libraryDependencies ++= Seq(
      awsSdk,
      commonsIo,
      specs2,
      playJson
    )
  )

lazy val fapiclient = project.in(file("fapi-client"))
  .settings(
    scalaVersion := "2.11.4",
    crossScalaVersions := Seq("2.10.4", "2.11.4"),
    resolvers ++= Seq(
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    libraryDependencies ++= Seq(
      "com.gu" %% "content-api-client" % "3.8-SNAPSHOT",
      "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
      "org.mockito" % "mockito-all" % "1.10.8" % "test"
    )
  )
  .dependsOn(faciajson)
