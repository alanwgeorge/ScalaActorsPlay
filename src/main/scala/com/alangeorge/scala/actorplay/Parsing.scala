package com.alangeorge.scala.actorplay

import scala.util.parsing.combinator._
import scala.util.parsing.input.CharSequenceReader


case class WordFreq(word: String, count: Int) {
  override def toString = "Word <" + word + "> " + "occurs with frequency " + count
}
class SimpleParser extends RegexParsers {
  def word: Parser[String] =  """[a-z]+""".r ^^ { _.toString }
  def number: Parser[Int] = """(0|[1-9]\d*)""".r ^^ { _.toInt }
  def freq: Parser[WordFreq] = word ~ number ^^ { case wd ~ fr => WordFreq(wd,fr) }
  def foo: Parser[String] = "foo" | "alan"
}

object SimpleParsing extends SimpleParser {
  def main(args: Array[String]) {
    val parsed: SimpleParsing.ParseResult[WordFreq] = parse(freq, "alan 123")

    parsed match {
      case Success(matched, _) => println(s"$matched")
      case Failure(msg, _) => println(s"FAILED: $msg")
      case Error(msg, _) => println(s"ERROR: $msg")
    }

    val foop = foo
    val r: SimpleParsing.ParseResult[String] = foop(new CharSequenceReader("alan"))

    r match {
      case Success(matched, _) => println(s"$matched")
      case Failure(msg, _) => println(s"FAILED: $msg")
      case Error(msg, _) => println(s"ERROR: $msg")
    }
  }
}

object Calculator extends RegexParsers {
  def number: Parser[Double] = """\d+(\.\d*)?""".r ^^ { _.toDouble }
  def factor: Parser[Double] = number | "(" ~> expr <~ ")"
  def term  : Parser[Double] = factor ~ rep( "*" ~ factor | "/" ~ factor) ^^ {
    case number ~ list => (number /: list) {
      case (x, "*" ~ y) => x * y
      case (x, "/" ~ y) => x / y
    }
  }
  def expr  : Parser[Double] = term ~ rep("+" ~ log(term)("Plus term") | "-" ~ log(term)("Minus term")) ^^ {
    case number ~ list => list.foldLeft(number) { // same as before, using alternate name for /:
      case (x, "+" ~ y) => x + y
      case (x, "-" ~ y) => x - y
    }
  }

  def apply(input: String): Double = parseAll(expr, input) match {
    case Success(result, _) => result
    case failure : NoSuccess => scala.sys.error(failure.msg)
  }
}

object Calculating extends App {
  println(Calculator("1+2*4"))
}