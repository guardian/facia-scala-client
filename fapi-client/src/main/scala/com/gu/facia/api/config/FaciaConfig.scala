package com.gu.facia.api.config

trait FaciaConfig {
  def stage: String
  def accessKey: String
  def secretKey: String
  def bucket: String

  def root: String = s"https://s3-eu-west-1.amazonaws.com/$bucket/$stage/frontsapi"
}
