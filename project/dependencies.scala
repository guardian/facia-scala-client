import sbt._

object Dependencies {
  val capiVersion = "35.0.0"
  val eTagCachingVersion = "7.0.0"

  val awsS3SdkV1 = "com.amazonaws" % "aws-java-sdk-s3" % "1.12.765"
  val eTagCachingS3Base = "com.gu.etag-caching" %% "aws-s3-base" % eTagCachingVersion
  val eTagCachingS3SupportForTesting = "com.gu.etag-caching" %% "aws-s3-sdk-v2" % eTagCachingVersion % Test
  val commonsIo = "org.apache.commons" % "commons-io" % "1.3.2"
  val contentApi = "com.gu" %% "content-api-client" % capiVersion
  val contentApiDefault = "com.gu" %% "content-api-client-default" % capiVersion % Test
  val mockito = "org.mockito" % "mockito-all" % "1.10.19" % Test
  val scalaTestMockito = "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.18" % Test
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
  val commercialShared = "com.gu" %% "commercial-shared" % "6.1.8"

  case class PlayJsonVersion(
    majorMinorVersion: String,
    groupId: String,
    exactPlayJsonVersion: String,
    supportsScala3: Boolean = false
  ) {
    val projectId = s"play$majorMinorVersion"

    val lib: ModuleID = groupId %% "play-json" % exactPlayJsonVersion
  }

  object PlayJsonVersion {
    val V28 = PlayJsonVersion("28", "com.typesafe.play", "2.8.2")
    val V29 = PlayJsonVersion("29", "com.typesafe.play", "2.10.4", supportsScala3 = true)
    val V30 = PlayJsonVersion("30", "org.playframework", "3.0.2", supportsScala3 = true)
  }
}
