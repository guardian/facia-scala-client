import Dependencies.*
import sbtrelease.ReleaseStateTransformations.*
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
    faciaJson_play29,
    faciaJson_play30,
    fapiClient_play27,
    fapiClient_play28,
    fapiClient_play29,
    fapiClient_play30
  ).settings(
    publish / skip := true,
    sonatypeReleaseSettings
  )

def baseProject(module: String, playJsonVersion: PlayJsonVersion) = Project(s"$module-${playJsonVersion.projectId}", file(s"$module-${playJsonVersion.projectId}"))
  .settings(
    sourceDirectory := baseDirectory.value / s"../$module/src",
    organization := "com.gu",
    resolvers ++= Resolver.sonatypeOssRepos("releases"),
    scalaVersion := "2.13.11",
    crossScalaVersions := Seq(scalaVersion.value, "2.12.19"), // ++ (if (playJsonVersion.supportsScala3) Seq("3.3.1") else Seq.empty),
    scalacOptions := Seq(
        "-release:11",
        "-feature",
        "-deprecation",
        "-Xfatal-warnings"
    ),
    libraryDependencies += scalaTest,
    sonatypeReleaseSettings
  )

def faciaJson_playJsonVersion(playJsonVersion: PlayJsonVersion) = baseProject("facia-json", playJsonVersion)
  .settings(
    libraryDependencies ++= Seq(
      awsSdk,
      commonsIo,
      playJsonVersion.lib,
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.11.0",
      scalaLogging
    )
  )

def fapiClient_playJsonVersion(playJsonVersion: PlayJsonVersion) =  baseProject("fapi-client", playJsonVersion)
  .settings(
    libraryDependencies ++= Seq(
      contentApi,
      contentApiDefault,
      commercialShared,
      scalaTestMockito,
      mockito
    )
  )

lazy val faciaJson_play27 = faciaJson_playJsonVersion(PlayJsonVersion.V27)
lazy val faciaJson_play28 = faciaJson_playJsonVersion(PlayJsonVersion.V28)
lazy val faciaJson_play29 = faciaJson_playJsonVersion(PlayJsonVersion.V29)
lazy val faciaJson_play30 = faciaJson_playJsonVersion(PlayJsonVersion.V30)

lazy val fapiClient_play27 = fapiClient_playJsonVersion(PlayJsonVersion.V27).dependsOn(faciaJson_play27)
lazy val fapiClient_play28 = fapiClient_playJsonVersion(PlayJsonVersion.V28).dependsOn(faciaJson_play28)
lazy val fapiClient_play29 = fapiClient_playJsonVersion(PlayJsonVersion.V29).dependsOn(faciaJson_play29)
lazy val fapiClient_play30 = fapiClient_playJsonVersion(PlayJsonVersion.V30).dependsOn(faciaJson_play30)

Test/testOptions += Tests.Argument(
  TestFrameworks.ScalaTest,
  "-u", s"test-results/scala-${scalaVersion.value}", "-o"
)
