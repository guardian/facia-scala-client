package com.gu.facia

import java.io.PrintWriter

package object api {

  def writeToFile(string: String) = {
    Some(new PrintWriter("/etc/fapi-log.txt")).foreach{p => p.append(string); p.close}
  }

}
