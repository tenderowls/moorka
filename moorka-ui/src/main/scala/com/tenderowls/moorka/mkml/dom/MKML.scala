package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.mkml.dom.CreationPolicy.CreationPolicy
import com.tenderowls.moorka.mkml.engine._

/**
 * Definition of HTML tags, attributes and properties
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait MKML {

  //---------------------------------------------------------------------------
  //
  //  Implicit conversions
  //
  //---------------------------------------------------------------------------

  implicit def boundTag(tagNode: Bindable[CreationPolicy]) = {
    new BoundElementContainer(tagNode)
  }

  implicit def componentSequence(seq: Seq[ElementBase]) = {
    new ElementSequence(seq)
  }

  implicit def staticText(text: String): ElementPropertyExtension[String] = {
    `textContent` := text
  }

  implicit def reactiveText(text: Bindable[String]): ElementBoundPropertyExtension[String] = {
    `textContent` := text
  }

  //---------------------------------------------------------------------------
  //
  //  HTML tags
  //
  //---------------------------------------------------------------------------

  def a(xs: Node*) = new Element("a", xs)

  def p(xs: Node*) = new Element("p", xs)

  def h1(xs: Node*) = new Element("h1", xs)

  def ul(xs: Node*) = new Element("ul", xs)

  def li(xs: Node*) = new Element("li", xs)

  def div(xs: Node*) = new Element("div", xs)

  def span(xs: Node*) = new Element("span", xs)

  def button(xs: Node*) = new Element("button", xs)

  def section(xs: Node*) = new Element("section", xs)

  def strong(xs: Node*) = new Element("strong", xs)

  def header(xs: Node*) = new Element("header", xs)

  def footer(xs: Node*) = new Element("footer", xs)

  def input(xs: Node*) = new Element("input", xs)

  def label(xs: Node*) = new Element("label", xs)

  def form(xs: Node*) = new Element("form", xs)

  //---------------------------------------------------------------------------
  //
  //  HTML attributes
  //
  //---------------------------------------------------------------------------

  val `id` = ElementAttributeName("id")
  val `for` = ElementAttributeName("for")
  val `href` = ElementAttributeName("href")
  val `type` = ElementAttributeName("type")
  val `class` = ElementAttributeName("class")
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

  def mkClass(clsName: String, not:Boolean = false) = new BoundExtensionFactory[Boolean](
    x => UseClassExtension(clsName, if (not) !x else x),
    x => UseClassBoundExtension(
      clsName,
      if (not) Bind { !x() } else x
    )
  )

  val mkShow = mkClass("hidden", not = true)

  val mkHide = mkClass("hidden", not = false)
}
