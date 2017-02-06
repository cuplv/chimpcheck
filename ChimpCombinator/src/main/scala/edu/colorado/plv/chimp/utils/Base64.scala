package edu.colorado.plv.chimp.utils

import java.util.{Base64 => B}

/**
  * Created by edmund on 2/6/17.
  */
object Base64 {

  val encoder = B.getEncoder()

  def encode(bytes: Array[Byte]): String = {
    encoder.encodeToString(bytes)
  }

  val decoder = B.getDecoder()

  def decode(str: String): Array[Byte] = {
    decoder.decode(str)
  }

}
