import sbt._

object Dependencies {
  val capiVersion = "12.18"

  val awsSdk = "com.amazonaws" % "aws-java-sdk-s3" % "1.11.154"
  val commonsIo = "org.apache.commons" % "commons-io" % "1.3.2"
  val contentApi = "com.gu" %% "content-api-client" % capiVersion
  val contentApiDefault = "com.gu" %% "content-api-client-default" % capiVersion % "test"
  val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"
  val playJson24 = "com.typesafe.play" %% "play-json" % "2.4.6"
  val playJson25 = "com.typesafe.play" %% "play-json" % "2.5.4"
  val playJson26 = "com.typesafe.play" %% "play-json" % "2.6.3"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  val specs2 = "org.specs2" %% "specs2-core" % "4.0.0" % "test"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  val commercialShared = "com.gu" %% "commercial-shared" % "6.1.2"
}
