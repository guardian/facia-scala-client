import sbt._

object Dependencies {
  val capiVersion = "19.2.3"

  val awsSdk = "com.amazonaws" % "aws-java-sdk-s3" % "1.11.646"
  val commonsIo = "org.apache.commons" % "commons-io" % "1.3.2"
  val contentApi = "com.gu" %% "content-api-client" % capiVersion
  val contentApiDefault = "com.gu" %% "content-api-client-default" % capiVersion % Test
  val mockito = "org.mockito" % "mockito-all" % "1.10.19" % Test
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.9" % Test
  val specs2 = "org.specs2" %% "specs2-core" % "4.20.0" % Test
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
  val commercialShared = "com.gu" %% "commercial-shared" % "6.1.8"
}
