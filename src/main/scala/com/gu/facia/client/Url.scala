package com.gu.facia.client

case class Url(url: String) extends AnyVal {
  def /(that: String) = Url(s"${url.stripSuffix("/")}/${that.stripPrefix("/")}")
}