package com.gu.facia.scalacompat

object Implicits {
  implicit class LazyZipList[+El1](coll1: Iterable[El1]) {
    def lazyZip[B](that: List[B]): LazyZip2[El1, B] = {
      LazyZip2(coll1, that)
    }
  }
}
