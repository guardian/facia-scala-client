package com.gu.facia.api.utils

sealed trait MediaType

case object Gallery extends MediaType
case object Video extends MediaType
case object Audio extends MediaType
