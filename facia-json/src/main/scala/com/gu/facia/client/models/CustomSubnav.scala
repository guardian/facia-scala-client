package com.gu.facia.client.models

import org.joda.time.DateTime

case class SubnavLink(
  linkText: String,
  dotcomPath: String
)

sealed trait TargetedPageType
object TargetedPageType {
  case object Front extends TargetedPageType
  case object Article extends TargetedPageType
  case object HasTag extends TargetedPageType
}

case class TargetedPage(
  `type`: TargetedPageType,
  path: String
)

sealed trait ImageBreakpoint
object ImageBreakpoint {
  case object Mobile extends ImageBreakpoint
  case object Tablet extends ImageBreakpoint
  case object Web extends ImageBreakpoint
}

case class Image(
  imageSrc: String,
  breakpoint: ImageBreakpoint
)

case class CustomSubnavHeader(
  headerText: String,
  dotcomPath: Option[String],
  copy: String
)

case class Palette(
  text: Option[String],
  header: Option[String],
  link: Option[String]
)

sealed trait CustomSubnavFormat
object CustomSubnavFormat {
  case object Large extends CustomSubnavFormat
  case object Compact extends CustomSubnavFormat
}

case class CustomSubnav(
  id: String,
  header: CustomSubnavHeader,
  format: CustomSubnavFormat,
  links: List[SubnavLink],
  pages: List[TargetedPage],
  images: List[Image],
  palette: Palette,
  lastUpdated: DateTime,
  updatedBy: String,
  updatedEmail: String,
)

case class CustomSubnavConfig(
  live: List[CustomSubnav],
  draft: List[CustomSubnav]
)
