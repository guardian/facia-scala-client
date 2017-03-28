import sbt._

object Dependencies {
  val awsSdk = "com.amazonaws" % "aws-java-sdk-s3" % "1.11.7"
  val commonsIo = "org.apache.commons" % "commons-io" % "1.3.2"
  val contentApi = "com.gu" %% "content-api-client" % "11.12"
  val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"
  val playJson = "com.typesafe.play" %% "play-json" % "2.4.6"
  val playJson25 = "com.typesafe.play" %% "play-json" % "2.5.4"
  val scalaTest = "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  val specs2 = "org.specs2" %% "specs2" % "3.7" % "test"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"
}
