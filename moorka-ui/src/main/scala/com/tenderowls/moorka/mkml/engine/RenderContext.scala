package com.tenderowls.moorka.mkml.engine

import com.tenderowls.moorka.core.Event
import com.tenderowls.moorka.core.events.Emitter
import com.tenderowls.moorka.mkml.dom.ElementBase
import org.scalajs.dom

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.Date

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object RenderContext {

  private val operations = new js.Array[DomOperation]
  
  private var inAction = false
  
  def appendOperation(op: DomOperation) = {
    operations.push(op)
    if (!inAction) {
      dom.setTimeout(applyOperations, 1)
      inAction = true
    }
  }

  def applyOperation(op: DomOperation) = op match {
    case AppendChild(to, element) => to.appendChild(element)
    case InsertChild(to, element, ref) => to.insertBefore(element, ref)
    case RemoveChild(from, element) => from.removeChild(element)
    case RemoveChildren(from, elements) => elements.foreach(from.removeChild(_))
    case UpdateAttribute(element, name, value) => element.setAttribute(name, value)
    case CustomOperation(f) => f()
    case UpdateProperty(element, name, value) =>
      val dyn = element.asInstanceOf[js.Dynamic]
      val jsVal = value.asInstanceOf[js.Any]
      dyn.updateDynamic(name)(jsVal)
    case AppendChildren(to, elements) =>
      val fragment = dom.document.createDocumentFragment()
      elements.foreach(fragment.appendChild(_))
      to.appendChild(fragment)
    case ReplaceChild(element, newChild, oldChild) =>
      element.replaceChild(newChild, oldChild)
  }

  // reject implicit conversion to js.Function
  private val applyOperations = { () =>
    val t = new Date().getTime()
    operations.foreach(applyOperation)
    operations.splice(0)
    inAction = false
  } : js.Function
}