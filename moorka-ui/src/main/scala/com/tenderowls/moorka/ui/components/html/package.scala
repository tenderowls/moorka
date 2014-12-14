package com.tenderowls.moorka.ui.components

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.ui.element._
import com.tenderowls.moorka.ui.event._

import scala.language.implicitConversions

/**
 * Definition of HTML tags, attributes and properties
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object html {

  //---------------------------------------------------------------------------
  //
  //  Implicit conversions
  //
  //---------------------------------------------------------------------------

  implicit def _bound(node: RxState[ElementBase]): BoundComponentContainer = {
    new BoundComponentContainer(node)
  }

  implicit def _sequence(seq: Seq[ElementBase]): ElementSequence = {
    new ElementSequence(seq)
  }

  implicit def _text(text: String): ElementPropertyExtension[String] = {
    `textContent` := text
  }

  implicit def _reactiveText(text: RxState[String]): ElementBoundPropertyExtension[String] = {
    `textContent` := text
  }

  //---------------------------------------------------------------------------
  //
  //  HTML tags
  //
  //---------------------------------------------------------------------------

  def a(xs: ElementEntry*) = new Element("a", xs)

  def p(xs: ElementEntry*) = new Element("p", xs)

  def h1(xs: ElementEntry*) = new Element("h1", xs)

  def ul(xs: ElementEntry*) = new Element("ul", xs)

  def li(xs: ElementEntry*) = new Element("li", xs)

  def div(xs: ElementEntry*) = new Element("div", xs)

  def span(xs: ElementEntry*) = new Element("span", xs)

  def button(xs: ElementEntry*) = new Element("button", xs)

  def section(xs: ElementEntry*) = new Element("section", xs)

  def strong(xs: ElementEntry*) = new Element("strong", xs)

  def header(xs: ElementEntry*) = new Element("header", xs)

  def footer(xs: ElementEntry*) = new Element("footer", xs)

  def input(xs: ElementEntry*) = new Element("input", xs)

  def label(xs: ElementEntry*) = new Element("label", xs)

  def form(xs: ElementEntry*) = new Element("form", xs)

  //---------------------------------------------------------------------------
  //
  //  HTML attributes
  //
  //---------------------------------------------------------------------------

  val `for` = ElementAttributeName("for")

  val `href` = ElementAttributeName("href")

  val `type` = ElementAttributeName("type")

  val `style` = ElementAttributeName("style")

  val `placeholder` = ElementAttributeName("placeholder")

  //---------------------------------------------------------------------------
  //
  //  HTML properties
  //
  //---------------------------------------------------------------------------

  val `textContent` = ElementPropertyName[String]("textContent")

  val `value` = ElementPropertyName[String]("value")

  val `checked` = ElementPropertyName[Boolean]("checked")

  val `autofocus` = ElementPropertyName[Boolean]("autofocus")

  val `className` = ElementPropertyName[String]("className")

  //---------------------------------------------------------------------------
  //
  //  HTML events
  //
  //---------------------------------------------------------------------------

  val `change` = ElementEventName(ChangeEventProcessor)

  val `double-click` = ElementEventName(DoubleClickEventProcessor)

  val `click` = ElementEventName(ClickEventProcessor)

  val `submit` = ElementEventName(SubmitEventProcessor)

  //---------------------------------------------------------------------------
  //
  //  Custom extensions
  //
  //---------------------------------------------------------------------------

  def useClassName(clsName: String, not: Boolean = false) = new BoundExtensionFactory[Boolean](
    x => UseClassExtension(clsName, if (not) !x else x),
    x => UseClassBoundExtension(
      clsName,
      if (not) Bind {
        !x()
      }
      else x
    )
  )

  val `show` = useClassName("hidden", not = true)

  val `hide` = useClassName("hidden", not = false)
}
