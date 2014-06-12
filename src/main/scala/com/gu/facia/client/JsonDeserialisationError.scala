package com.gu.facia.client

case class JsonDeserialisationError(errorMessage: String) extends RuntimeException(errorMessage)