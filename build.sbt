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

lazy val root = (project in file(".")).aggregate(
    faciaJson_play26,
    faciaJson_play27,
    fapiClient_play26,
    fapiClient_play27
  ).settings(
    publishArtifact := false,
    skip in publish := true,
    sonatypeReleaseSettings
  )

val exactPlayJsonVersions = Map(
  "26" -> "2.6.13",
  "27" -> "2.7.4"
)

def baseProject(module: String, majorMinorVersion: String) = Project(s"$module-play$majorMinorVersion", file(s"$module-play$majorMinorVersion"))
  .settings(
    sourceDirectory := baseDirectory.value / s"../$module/src",
    organization := "com.gu",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("public"),
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    scalaVersion := "2.12.8",
    scalacOptions := Seq("-feature", "-deprecation"),
    publishTo := sonatypePublishTo.value,
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

lazy val faciaJson_play26 = faciaJson_playJsonVersion("26")
lazy val faciaJson_play27 = faciaJson_playJsonVersion("27")

lazy val fapiClient_play26 = fapiClient_playJsonVersion("26").dependsOn(faciaJson_play26)
lazy val fapiClient_play27 = fapiClient_playJsonVersion("27").dependsOn(faciaJson_play27)