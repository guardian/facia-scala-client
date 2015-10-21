import sbt._

object Dependencies {
  val awsSdk = "com.amazonaws" % "aws-java-sdk" % "1.7.9"
  val commonsIo = "org.apache.commons" % "commons-io" % "1.3.2"
  val contentApi = "com.gu" %% "content-api-client" % "6.3"
  val mockito = "org.mockito" % "mockito-all" % "1.10.8" % "test"
  val playJson = "com.typesafe.play" %% "play-json" % "2.3.6"
  val scalaTest = "org.scalatest" %% "scalatest" % "2.2.1" % "test"
  val specs2 = "org.specs2" %% "specs2" % "2.3.12" % "test"
}
