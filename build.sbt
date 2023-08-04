import Dependencies.*
import sbtrelease.ReleaseStateTransformations.*
import sbtversionpolicy.withsbtrelease.ReleaseVersion.fromAggregatedAssessedCompatibilityWithLatestRelease

name := "facia-api-client"

description := "Scala client for The Guardian's Facia JSON API"

def baseSettings(supportScala3: Boolean) = Seq(
  organization := "com.gu",
  licenses := Seq("Apache V2" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  resolvers ++= Resolver.sonatypeOssRepos("releases"),
  scalaVersion := "2.13.11",
  crossScalaVersions := Seq(scalaVersion.value, "2.12.18") ++ (if (supportScala3) Seq("3.3.1") else Seq.empty),
  scalacOptions := Seq(
    "-release:11",
    "-feature",
    "-deprecation",
    "-Xfatal-warnings"
  ),
  libraryDependencies += scalaTest
)

lazy val root = (project in file(".")).aggregate(
    faciaJson_play27,
    faciaJson_play28,
    faciaJson_play30,
    fapiClient_core,
    fapiClient_s3_sdk_v2,
    fapiClient_play27,
    fapiClient_play28,
    fapiClient_play30
  ).settings(
    publish / skip := true,
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

def playJsonSpecificProject(module: String, playJsonVersion: PlayJsonVersion) = Project(s"$module-${playJsonVersion.projectId}", file(s"$module-${playJsonVersion.projectId}"))
  .settings(
    sourceDirectory := baseDirectory.value / s"../$module/src"
  )

def faciaJson(playJsonVersion: PlayJsonVersion) = playJsonSpecificProject("facia-json", playJsonVersion)
  .dependsOn(fapiClient_core)
  .settings(
    libraryDependencies ++= Seq(
      awsS3SdkV1, // ideally, this would be pushed out to a separate FAPI artifact
      commonsIo,
      playJsonVersion.lib,
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.11.0",
      scalaLogging
    ),
    baseSettings(supportScala3 = playJsonVersion.supportsScala3)
  )

def fapiClient(playJsonVersion: PlayJsonVersion) =  playJsonSpecificProject("fapi-client", playJsonVersion)
  .settings(
    libraryDependencies ++= Seq(
      contentApi,
      contentApiDefault,
      commercialShared,
      scalaTestMockito,
      mockito
    ),
    baseSettings(supportScala3 = false) // currently blocked by contentApi & commercialShared clients
  )

lazy val faciaJson_play27 = faciaJson(PlayJsonVersion.V27)
lazy val faciaJson_play28 = faciaJson(PlayJsonVersion.V28)
lazy val faciaJson_play30 = faciaJson(PlayJsonVersion.V30)

lazy val fapiClient_core = (project in file("fapi-client-core")).settings(
  libraryDependencies ++= Seq(
    eTagCachingS3Base
  ),
  baseSettings(supportScala3 = true)
)

lazy val fapiClient_s3_sdk_v2 = (project in file("fapi-s3-sdk-v2")).dependsOn(fapiClient_core)
  .settings(
    libraryDependencies += eTagCachingS3SdkV2,
    baseSettings(supportScala3 = true)
  )

lazy val fapiClient_play27 = fapiClient(PlayJsonVersion.V27).dependsOn(faciaJson_play27)
lazy val fapiClient_play28 = fapiClient(PlayJsonVersion.V28).dependsOn(faciaJson_play28)
lazy val fapiClient_play30 = fapiClient(PlayJsonVersion.V30).dependsOn(faciaJson_play30)

Test/testOptions += Tests.Argument(
  TestFrameworks.ScalaTest,
  "-u", s"test-results/scala-${scalaVersion.value}", "-o"
)
