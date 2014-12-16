package com.gu.facia.api.contentapi

import com.gu.contentapi.client.model.{SearchQuery, ItemQuery}

trait QueryConverter {
  def itemQuery(itemQuery: ItemQuery): ItemQuery
  def searchQuery(searchQuery: SearchQuery): SearchQuery
}
object IdentityQueryConverter extends QueryConverter {
  override def itemQuery(itemQuery: ItemQuery): ItemQuery = itemQuery
  override def searchQuery(searchQuery: SearchQuery): SearchQuery = searchQuery
}
