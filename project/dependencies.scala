import sbt._

object Dependencies {
  val capiVersion = "19.2.3"

  val awsSdk = "com.amazonaws" % "aws-java-sdk-s3" % "1.11.1034"
  val commonsIo = "org.apache.commons" % "commons-io" % "1.3.2"
  val contentApi = "com.gu" %% "content-api-client" % capiVersion
  val contentApiDefault = "com.gu" %% "content-api-client-default" % capiVersion % Test
  val mockito = "org.mockito" % "mockito-all" % "1.10.19" % Test
  val scalaTestMockito = "org.scalatestplus" %% "mockito-4-11" % "3.2.16.0" % Test
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.15" % Test
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  val commercialShared = "com.gu" %% "commercial-shared" % "6.1.6"
}
