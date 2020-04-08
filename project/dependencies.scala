import sbt._

object Dependencies {
  val capiVersion = "17.1"

  val awsSdk = "com.amazonaws" % "aws-java-sdk-s3" % "1.11.646"
  val commonsIo = "org.apache.commons" % "commons-io" % "1.3.2"
  val contentApi = "com.gu" %% "content-api-client" % capiVersion
  val contentApiDefault = "com.gu" %% "content-api-client-default" % capiVersion % "test"
  val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8" % "test"
  val specs2 = "org.specs2" %% "specs2-core" % "4.7.1" % "test"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  val commercialShared = "com.gu" %% "commercial-shared" % "6.1.6"
}
