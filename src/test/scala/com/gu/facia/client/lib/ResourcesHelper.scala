package com.gu.facia.client.lib

trait ResourcesHelper {
  def slurp(path: String): Option[String] =
    Option(getClass.getClassLoader.getResource(path)).map(scala.io.Source.fromURL(_).mkString)

  def slurpOrDie(path: String) = slurp(path) getOrElse {
    throw new RuntimeException(s"Required resource $path not on class path")
  }
}