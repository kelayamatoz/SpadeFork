package spade.config

import spade.node._
import spade.util.typealias._

import prism.collection.immutable._
import scala.language.existentials

import FIMap._
case class FIMap(fmap:OneToOneMap[K,V], bmap:OneToManyMap[V,K]) extends BiManyToOneMapLike[K,V,FIMap] {
  def get(k:PGI[_<:PModule]) = { map.get(k).asInstanceOf[Option[PGO[_<:PModule]]] }
  def apply(v:V):KK = bmap(v)
  def get(v:V):Option[KK] = bmap.get(v)
  def contains(v:V) = bmap.contains(v)
}

object FIMap {
  type K = PI[_<:PModule]
  type V = PO[_<:PModule]
  def empty = FIMap(OneToOneMap.empty, OneToManyMap.empty)
}
