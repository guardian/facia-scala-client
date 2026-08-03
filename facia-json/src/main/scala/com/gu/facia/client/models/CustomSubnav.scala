package com.gu.facia.client.models

import org.joda.time.DateTime
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Json, OFormat}

case class SubnavLink(
  linkText: String,
  dotcomPath: String
)
object SubnavLink {
  implicit val subnavLinkFormat: OFormat[SubnavLink] = Json.format[SubnavLink]
}

sealed trait TargetedPageType
object TargetedPageType {
  case object Front extends TargetedPageType
  case object Article extends TargetedPageType
  case object HasTag extends TargetedPageType
  implicit val targetedPageTypeFormat: Format[TargetedPageType] = new Format[TargetedPageType] {
    override def reads(json: JsValue): JsResult[TargetedPageType] = json match {
      case JsString("front") => JsSuccess(Front)
      case JsString("article") => JsSuccess(Article)
      case JsString("hasTag") => JsSuccess(HasTag)
      case _ => JsError("Invalid TargetedPageType")
    }

    override def writes(o: TargetedPageType): JsValue = o match {
      case Front => JsString("front")
      case Article => JsString("article")
      case HasTag => JsString("hasTag")
    }
  }
}

case class TargetedPage(
  `type`: TargetedPageType,
  path: String
)
object TargetedPage {
  implicit val targetedPageFormat: OFormat[TargetedPage] = Json.format[TargetedPage]
}

sealed trait ImageBreakpoint
object ImageBreakpoint {
  case object Mobile extends ImageBreakpoint
  case object Tablet extends ImageBreakpoint
  case object Web extends ImageBreakpoint

  implicit val imageBreakpointFormat: Format[ImageBreakpoint] = new Format[ImageBreakpoint] {
    override def reads(json: JsValue): JsResult[ImageBreakpoint] = json match {
      case JsString("mobile") => JsSuccess(Mobile)
      case JsString("tablet") => JsSuccess(Tablet)
      case JsString("web") => JsSuccess(Web)
      case _ => JsError("Invalid ImageBreakpoint")
    }

    override def writes(o: ImageBreakpoint): JsValue = o match {
      case Mobile => JsString("mobile")
      case Tablet => JsString("tablet")
      case Web => JsString("web")
    }
  }
}

case class Image(
  imageSrc: String,
  breakpoint: ImageBreakpoint
)
object Image {
  implicit val imageFormat: OFormat[Image] = Json.format[Image]
}

case class CustomSubnavHeader(
  headerText: String,
  dotcomPath: Option[String],
  copy: String
)
object CustomSubnavHeader {
  implicit val customSubnavHeaderFormat: OFormat[CustomSubnavHeader] = Json.format[CustomSubnavHeader]
}

case class Palette(
  text: Option[String],
  header: Option[String],
  link: Option[String]
)
object Palette {
  implicit val paletteFormat: OFormat[Palette] = Json.format[Palette]
}

sealed trait CustomSubnavFormat
object CustomSubnavFormat {
  case object Large extends CustomSubnavFormat
  case object Compact extends CustomSubnavFormat
  implicit val customSubnavFormatFormat: Format[CustomSubnavFormat] = new Format[CustomSubnavFormat] {
    override def reads(json: JsValue): JsResult[CustomSubnavFormat] = json match {
      case JsString("large") => JsSuccess(Large)
      case JsString("compact") => JsSuccess(Compact)
      case _ => JsError("Invalid CustomSubnavFormat")
    }
    override def writes(o: CustomSubnavFormat): JsValue = o match {
      case Large => JsString("large")
      case Compact => JsString("compact")
    }
  }
}

case class CustomSubnav(
  id: String,
  header: CustomSubnavHeader,
  format: CustomSubnavFormat,
  links: List[SubnavLink],
  pages: List[TargetedPage],
  images: Option[List[Image]],
  palette: Option[Palette],
  lastUpdated: DateTime,
  updatedBy: String,
  updatedEmail: String,
)

object CustomSubnav {
  import com.gu.facia.client.json.JodaFormat._
  implicit val customSubnavFormat: OFormat[CustomSubnav] = Json.format[CustomSubnav]
}

case class CustomSubnavConfig(
  live: List[CustomSubnav],
  draft: List[CustomSubnav]
)

object CustomSubnavConfig {
  implicit val customSubnavConfigFormat: OFormat[CustomSubnavConfig] = Json.format[CustomSubnavConfig]
}
