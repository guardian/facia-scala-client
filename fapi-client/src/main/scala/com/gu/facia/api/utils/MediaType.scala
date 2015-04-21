package com.gu.facia.api.utils

import com.gu.facia.api.models.FaciaContent
import FaciaContentImplicits._

object MediaType {
  def fromFaciaContent(faciaContent: FaciaContent): Option[MediaType] = faciaContent.mediaType
}

sealed trait MediaType

case object Gallery extends MediaType
case object Video extends MediaType
case object Audio extends MediaType
