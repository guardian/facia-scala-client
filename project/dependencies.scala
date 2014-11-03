import sbt._

object Dependencies {
  val awsSdk = "com.amazonaws" % "aws-java-sdk" % "1.7.9"
  val commonsIo = "org.apache.commons" % "commons-io" % "1.3.2"
  val playJson = "com.typesafe.play" %% "play-json" % "2.3.6"
  val specs2 = "org.specs2" %% "specs2" % "2.3.12" % "test"
}
