import Dependencies.*
import sbtrelease.ReleaseStateTransformations.*
import sbtversionpolicy.withsbtrelease.ReleaseVersion.fromAggregatedAssessedCompatibilityWithLatestRelease

name := "facia-api-client"

description := "Scala client for The Guardian's Facia JSON API"

val sonatypeReleaseSettings = Seq(
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

def artifactProducingSettings(supportScala3: Boolean) = Seq(
  organization := "com.gu",
  licenses := Seq(License.Apache2),
  resolvers ++= Resolver.sonatypeOssRepos("releases"),
  scalaVersion := "2.13.14",
  crossScalaVersions := Seq(scalaVersion.value) ++ (if (supportScala3) Seq("3.3.3") else Seq.empty),
  scalacOptions := Seq(
    "-release:8",
    "-feature",
    "-deprecation",
    "-Xfatal-warnings"
  ),
  libraryDependencies += scalaTest
)

lazy val root = (project in file(".")).aggregate(
    faciaJson_play28,
    faciaJson_play29,
    faciaJson_play30,
    fapiClient_play28,
    fapiClient_play29,
    fapiClient_play30
  ).settings(
    publish / skip := true,
    sonatypeReleaseSettings
  )

def playJsonSpecificProject(module: String, playJsonVersion: PlayJsonVersion) = Project(s"$module-${playJsonVersion.projectId}", file(s"$module-${playJsonVersion.projectId}"))
  .settings(
    sourceDirectory := baseDirectory.value / s"../$module/src"
  )

def faciaJson(playJsonVersion: PlayJsonVersion) = playJsonSpecificProject("facia-json", playJsonVersion)
  .settings(
    libraryDependencies ++= Seq(
      awsSdk,
      commonsIo,
      playJsonVersion.lib,
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.11.0",
      scalaLogging
    ),
    artifactProducingSettings(supportScala3 = playJsonVersion.supportsScala3)
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
    artifactProducingSettings(supportScala3 = false) // currently blocked by contentApi & commercialShared clients
  )

lazy val faciaJson_play28 = faciaJson(PlayJsonVersion.V28)
lazy val faciaJson_play29 = faciaJson(PlayJsonVersion.V29)
lazy val faciaJson_play30 = faciaJson(PlayJsonVersion.V30)

lazy val fapiClient_play28 = fapiClient(PlayJsonVersion.V28).dependsOn(faciaJson_play28)
lazy val fapiClient_play29 = fapiClient(PlayJsonVersion.V29).dependsOn(faciaJson_play29)
lazy val fapiClient_play30 = fapiClient(PlayJsonVersion.V30).dependsOn(faciaJson_play30)

Test/testOptions += Tests.Argument(
  TestFrameworks.ScalaTest,
  "-u", s"test-results/scala-${scalaVersion.value}", "-o"
)
