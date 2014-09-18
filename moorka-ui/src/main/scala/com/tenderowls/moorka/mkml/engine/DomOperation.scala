package com.tenderowls.moorka.mkml.engine

import org.scalajs.dom

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
sealed trait DomOperation

case class ReplaceChild(element: dom.Element, newChild: dom.Element, oldChild: dom.Element) extends DomOperation

case class AppendChild(to: dom.Element, element: dom.Element) extends DomOperation

case class AppendChildren(to: dom.Element, elements: Seq[dom.Element]) extends DomOperation

case class RemoveChild(from: dom.Element, element: dom.Element) extends DomOperation

case class RemoveChildren(from: dom.Element, elements: Seq[dom.Element]) extends DomOperation

case class UpdateAttribute(element: dom.Element, name: String, value: String) extends DomOperation

case class UpdateProperty[A](element: dom.Element, name: String, value: A) extends DomOperation

case class InsertChild(to: dom.Element, element: dom.Element, ref: dom.Node)  extends DomOperation

case class CustomOperation(f: () => Unit) extends DomOperation
