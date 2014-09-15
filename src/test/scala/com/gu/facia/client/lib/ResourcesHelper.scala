package com.gu.facia.client.lib

import com.gu.facia.client.FaciaSuccess

trait ResourcesHelper {
  def slurp(path: String): Option[String] =
    Option(getClass.getClassLoader.getResource(path)).map(scala.io.Source.fromURL(_).mkString)

  def slurpOrDie(path: String) = slurp(path).map(_.getBytes).map(FaciaSuccess.apply).getOrElse {
    throw new RuntimeException(s"Required resource $path not on class path")  }
}