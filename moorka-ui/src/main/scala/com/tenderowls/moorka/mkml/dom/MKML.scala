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
    new BoundComponentContainer(tagNode)
  }

  implicit def componentSequence(seq: Seq[ComponentBase]) = {
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

  def a(id: String)(xs: SyntheticDomNode*) = new StandardElement("a", Some(id),  xs)

  def p(id: String)(xs: SyntheticDomNode*) = new StandardElement("p", Some(id),  xs)

  def h1(id: String)(xs: SyntheticDomNode*) = new StandardElement("h1", Some(id),  xs)

  def ul(id: String)(xs: SyntheticDomNode*) = new StandardElement("ul", Some(id),  xs)

  def li(id: String)(xs: SyntheticDomNode*) = new StandardElement("li", Some(id),  xs)

  def div(id: String)(xs: SyntheticDomNode*) = new StandardElement("div", Some(id),  xs)

  def span(id: String)(xs: SyntheticDomNode*) = new StandardElement("span", Some(id),  xs)

  def button(id: String)(xs: SyntheticDomNode*) = new StandardElement("button", Some(id),  xs)

  def section(id: String)(xs: SyntheticDomNode*) = new StandardElement("section", Some(id),  xs)

  def strong(id: String)(xs: SyntheticDomNode*) = new StandardElement("strong", Some(id),  xs)

  def header(id: String)(xs: SyntheticDomNode*) = new StandardElement("header", Some(id),  xs)

  def footer(id: String)(xs: SyntheticDomNode*) = new StandardElement("footer", Some(id),  xs)

  def input(id: String)(xs: SyntheticDomNode*) = new StandardElement("input", Some(id),  xs)

  def label(id: String)(xs: SyntheticDomNode*) = new StandardElement("label", Some(id),  xs)

  def form(id: String)(xs: SyntheticDomNode*) = new StandardElement("form", Some(id),  xs)

  def a(xs: SyntheticDomNode*) = new StandardElement("a", None, xs)

  def p(xs: SyntheticDomNode*) = new StandardElement("p", None, xs)

  def h1(xs: SyntheticDomNode*) = new StandardElement("h1", None, xs)

  def ul(xs: SyntheticDomNode*) = new StandardElement("ul", None, xs)

  def li(xs: SyntheticDomNode*) = new StandardElement("li", None, xs)

  def div(xs: SyntheticDomNode*) = new StandardElement("div", None, xs)

  def span(xs: SyntheticDomNode*) = new StandardElement("span", None, xs)

  def button(xs: SyntheticDomNode*) = new StandardElement("button", None, xs)

  def section(xs: SyntheticDomNode*) = new StandardElement("section", None, xs)

  def strong(xs: SyntheticDomNode*) = new StandardElement("strong", None, xs)

  def header(xs: SyntheticDomNode*) = new StandardElement("header", None, xs)

  def footer(xs: SyntheticDomNode*) = new StandardElement("footer", None, xs)

  def input(xs: SyntheticDomNode*) = new StandardElement("input", None, xs)

  def label(xs: SyntheticDomNode*) = new StandardElement("label", None, xs)

  def form(xs: SyntheticDomNode*) = new StandardElement("form", None, xs)

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
