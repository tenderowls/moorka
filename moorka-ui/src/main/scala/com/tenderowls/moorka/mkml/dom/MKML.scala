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

  def a(id: String)(xs: Node*) = new Element("a", Some(id),  xs)

  def p(id: String)(xs: Node*) = new Element("p", Some(id),  xs)

  def h1(id: String)(xs: Node*) = new Element("h1", Some(id),  xs)

  def ul(id: String)(xs: Node*) = new Element("ul", Some(id),  xs)

  def li(id: String)(xs: Node*) = new Element("li", Some(id),  xs)

  def div(id: String)(xs: Node*) = new Element("div", Some(id),  xs)

  def span(id: String)(xs: Node*) = new Element("span", Some(id),  xs)

  def button(id: String)(xs: Node*) = new Element("button", Some(id),  xs)

  def section(id: String)(xs: Node*) = new Element("section", Some(id),  xs)

  def strong(id: String)(xs: Node*) = new Element("strong", Some(id),  xs)

  def header(id: String)(xs: Node*) = new Element("header", Some(id),  xs)

  def footer(id: String)(xs: Node*) = new Element("footer", Some(id),  xs)

  def input(id: String)(xs: Node*) = new Element("input", Some(id),  xs)

  def label(id: String)(xs: Node*) = new Element("label", Some(id),  xs)

  def form(id: String)(xs: Node*) = new Element("form", Some(id),  xs)

  def a(xs: Node*) = new Element("a", None, xs)

  def p(xs: Node*) = new Element("p", None, xs)

  def h1(xs: Node*) = new Element("h1", None, xs)

  def ul(xs: Node*) = new Element("ul", None, xs)

  def li(xs: Node*) = new Element("li", None, xs)

  def div(xs: Node*) = new Element("div", None, xs)

  def span(xs: Node*) = new Element("span", None, xs)

  def button(xs: Node*) = new Element("button", None, xs)

  def section(xs: Node*) = new Element("section", None, xs)

  def strong(xs: Node*) = new Element("strong", None, xs)

  def header(xs: Node*) = new Element("header", None, xs)

  def footer(xs: Node*) = new Element("footer", None, xs)

  def input(xs: Node*) = new Element("input", None, xs)

  def label(xs: Node*) = new Element("label", None, xs)

  def form(xs: Node*) = new Element("form", None, xs)

  //---------------------------------------------------------------------------
  //
  //  HTML attributes
  //
  //---------------------------------------------------------------------------

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
