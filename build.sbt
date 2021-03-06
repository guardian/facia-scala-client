import Dependencies._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease._

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
    faciaJson_play26,
    faciaJson_play27,
    faciaJson_play28,
    fapiClient_play26,
    fapiClient_play27,
    fapiClient_play28
  ).settings(
    publishArtifact := false,
    skip in publish := true,
    sonatypeReleaseSettings
  )

val exactPlayJsonVersions = Map(
  "26" -> "2.6.13",
  "27" -> "2.7.4",
  "28" -> "2.8.1"
)

def baseProject(module: String, majorMinorVersion: String) = Project(s"$module-play$majorMinorVersion", file(s"$module-play$majorMinorVersion"))
  .settings(
    sourceDirectory := baseDirectory.value / s"../$module/src",
    organization := "com.gu",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("public"),
      Resolver.typesafeRepo("releases")
    ),
    scalaVersion := "2.12.12",
    scalacOptions := Seq("-feature", "-deprecation"),
    publishTo := sonatypePublishToBundle.value,
    sonatypeReleaseSettings
  )

def faciaJson_playJsonVersion(majorMinorVersion: String) = baseProject("facia-json", majorMinorVersion)
  .settings(
    libraryDependencies ++= Seq(
      awsSdk,
      commonsIo,
      specs2,
      "com.typesafe.play" %% "play-json" % exactPlayJsonVersions(majorMinorVersion),
      scalaLogging
    )
  )

def fapiClient_playJsonVersion(majorMinorVersion: String) =  baseProject("fapi-client", majorMinorVersion)
  .settings(
    libraryDependencies ++= Seq(
      contentApi,
      contentApiDefault,
      commercialShared,
      scalaTest,
      mockito
    )
  )

lazy val crossCompileScala213 = crossScalaVersions := Seq(scalaVersion.value, "2.13.4")


lazy val faciaJson_play26 = faciaJson_playJsonVersion("26")
lazy val faciaJson_play27 = faciaJson_playJsonVersion("27").settings(crossCompileScala213)
lazy val faciaJson_play28 = faciaJson_playJsonVersion("28").settings(crossCompileScala213)

lazy val fapiClient_play26 = fapiClient_playJsonVersion("26").dependsOn(faciaJson_play26)
lazy val fapiClient_play27 = fapiClient_playJsonVersion("27").dependsOn(faciaJson_play27).settings(crossCompileScala213)
lazy val fapiClient_play28 = fapiClient_playJsonVersion("28").dependsOn(faciaJson_play28).settings(crossCompileScala213)

