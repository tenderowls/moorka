package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.mkml.dom.DomOperation._
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.Date

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object RenderContext {

  private var operations = new js.Array[DomOperation]
  
  private var inAction = false
  
  def appendOperation(op: DomOperation) = {
    operations.push(op)
    if (!inAction) {
      dom.setTimeout(applyOperations, 1)
      inAction = true
    }
  }
  
  val applyOperations = { () =>
    val t = new Date().getTime()
    operations.foreach {
      case AppendChild(to, element) => to.appendChild(element)
      case AppendChildren(to, elements) =>
        val fragment = dom.document.createDocumentFragment()
        elements.foreach(fragment.appendChild(_))
        to.appendChild(fragment)
      case InsertChild(to, element, ref) => to.insertBefore(element, ref)
      case RemoveChild(from, element) => from.removeChild(element)
      case RemoveChildren(from, elements) => elements.foreach(from.removeChild(_))
      case UpdateAttribute(element, name, value) => element.setAttribute(name, value)
      case UpdateProperty(element, name, value) =>
        element.asInstanceOf[js.Dynamic].updateDynamic(name)(value.asInstanceOf[js.Any])
      case ReplaceChild(element, newChild, oldChild) =>
        element.replaceChild(newChild, oldChild)
      case CustomOperation(f) => f()
    }
    println("operations executed in " + (new Date().getTime() - t))
    operations.splice(0)
    inAction = false
  } : js.Function
}

object DomOperation {

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
}