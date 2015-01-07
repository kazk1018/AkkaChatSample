package sample.protocol

import scala.util.parsing.combinator.RegexParsers

case class MyProtocol(key: String, value: String)

object DataParser extends RegexParsers {
  override val skipWhitespace = true
  def value = "[a-zA-Z0-9]*".r
  def row = (value ~ ':' ~ value) ^^ { res => MyProtocol(res._1._1, res._2) }
  def parse(input: String) = parseAll(row, input)
}
