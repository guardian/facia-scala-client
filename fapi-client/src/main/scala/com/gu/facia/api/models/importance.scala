package com.gu.facia.api.models

trait Importance
case object Critical extends Importance
case object Important extends Importance
case object DefaultImportance extends Importance

