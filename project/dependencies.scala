import sbt._

object Dependencies {
  val awsSdk = "com.amazonaws" % "aws-java-sdk-s3" % "1.10.58"
  val commonsIo = "org.apache.commons" % "commons-io" % "1.3.2"
  val contentApi = "com.gu" %% "content-api-client" % "7.29"
  val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"
  val playJson = "com.typesafe.play" %% "play-json" % "2.4.6"
  val scalaTest = "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  val specs2 = "org.specs2" %% "specs2" % "3.7" % "test"
}
