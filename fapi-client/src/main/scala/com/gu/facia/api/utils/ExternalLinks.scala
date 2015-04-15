package com.gu.facia.api.utils

import java.net.URI

import scala.util.Try

object ExternalLinks {
  val corsOrigins = List(
    "http://www.theguardian.com",
    "https://www.theguardian.com",
    "https://profile.theguardian.com",
    "https://composer.gutools.co.uk",
    "https://composer.release.dev-gutools.co.uk",
    "https://composer.code.dev-gutools.co.uk",
    "http://preview.gutools.co.uk",
    "https://preview.gutools.co.uk")

  val GuardianDomains = corsOrigins flatMap { uri =>
    Try {
      new URI(uri).getHost.stripPrefix("www.")
    }.toOption
  }

  def external(url: String) = Try(Option(new URI(url).getHost).exists({ host => !GuardianDomains.exists({ domain =>
    host == domain || host.endsWith(s".$domain")
  })})).getOrElse(false)

  def internalPath(url: String) = if (external(url)) None else Try {
    Option(new URI(url).getPath)
  }.toOption.flatten
}

