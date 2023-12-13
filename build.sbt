import Dependencies._
import sbtrelease.ReleaseStateTransformations._
import sbtversionpolicy.withsbtrelease.ReleaseVersion.fromAggregatedAssessedCompatibilityWithLatestRelease

organization := "com.gu"

name := "facia-api-client"

description := "Scala client for The Guardian's Facia JSON API"

val sonatypeReleaseSettings = Seq(
  licenses := Seq("Apache V2" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  releaseVersion := fromAggregatedAssessedCompatibilityWithLatestRelease().value,
  releaseCrossBuild := true, // true if you cross-build the project for multiple Scala versions
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    setNextVersion,
    commitNextVersion
  )
)

lazy val root = (project in file(".")).aggregate(
    faciaJson_play27,
    faciaJson_play28,
    fapiClient_play27,
    fapiClient_play28
  ).settings(
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
        "-release:11",
        "-feature",
        "-deprecation",
        "-Xfatal-warnings"
    ),
    libraryDependencies += scalaTest,
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
