import Dependencies._
import sbtrelease.ReleaseStateTransformations._

organization := "com.gu"

name := "facia-api-client"

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
  releaseCrossBuild := true, // true if you cross-build the project for multiple Scala versions
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    // For non cross-build projects, use releaseStepCommand("publishSigned")
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

lazy val root = (project in file(".")).aggregate(
    faciaJson_play27,
    faciaJson_play28,
    fapiClient_play27,
    fapiClient_play28
  ).settings(
    publishArtifact := false,
    publish / skip := true,
    sonatypeReleaseSettings
  )

val exactPlayJsonVersions = Map(
  "27" -> "2.7.4",
  "28" -> "2.8.2"
)

def baseProject(module: String, majorMinorVersion: String) = Project(s"$module-play$majorMinorVersion", file(s"$module-play$majorMinorVersion"))
  .settings(
    sourceDirectory := baseDirectory.value / s"../$module/src",
    organization := "com.gu",
    resolvers ++= Resolver.sonatypeOssRepos("releases"),
    scalaVersion := "2.13.11",
    crossScalaVersions := Seq(scalaVersion.value, "2.12.18"),
    scalacOptions := Seq(
        "-feature",
        "-deprecation",
        "-Xfatal-warnings"
    ),
    libraryDependencies += scalaTest,
    publishTo := sonatypePublishToBundle.value,
    sonatypeReleaseSettings
  )

def faciaJson_playJsonVersion(majorMinorVersion: String) = baseProject("facia-json", majorMinorVersion)
  .settings(
    libraryDependencies ++= Seq(
      awsSdk,
      commonsIo,
      "com.typesafe.play" %% "play-json" % exactPlayJsonVersions(majorMinorVersion),
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.11.0",
      scalaLogging
    )
  )

def fapiClient_playJsonVersion(majorMinorVersion: String) =  baseProject("fapi-client", majorMinorVersion)
  .settings(
    libraryDependencies ++= Seq(
      contentApi,
      contentApiDefault,
      commercialShared,
      scalaTestMockito,
      mockito
    )
  )

lazy val faciaJson_play27 = faciaJson_playJsonVersion("27")
lazy val faciaJson_play28 = faciaJson_playJsonVersion("28")

lazy val fapiClient_play27 = fapiClient_playJsonVersion("27").dependsOn(faciaJson_play27)
lazy val fapiClient_play28 = fapiClient_playJsonVersion("28").dependsOn(faciaJson_play28)

Test/testOptions += Tests.Argument(
  TestFrameworks.ScalaTest,
  "-u", s"test-results/scala-${scalaVersion.value}", "-o"
)
