package com.gu.facia.scalacompat

case class LazyZip2[+El1, +El2](coll1: Iterable[El1], coll2: Iterable[El2]) {
  def toList: List[(El1, El2)] = (coll1, coll2).zipped.toList

  def lazyZip[B](that: Iterable[B]): LazyZip3[El1, El2, B] =
    LazyZip3(coll1, coll2, that)
}

case class LazyZip3[+El1, +El2, +El3](coll1: Iterable[El1], coll2: Iterable[El2], coll3: Iterable[El3]) {
  def toList: List[(El1, El2, El3)] = (coll1, coll2, coll3).zipped.toList
}