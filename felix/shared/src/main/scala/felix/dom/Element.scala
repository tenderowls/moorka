package felix.dom

import felix.core.{ElementRef, FelixSystem}
import felix.event.EventTarget
import moorka.rx.Mortal
import moorka.rx.death.Reaper

import scala.collection.mutable

/**
 * DOM element representation
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Element(tag: String, system: FelixSystem) extends Entry with Mortal with EventTarget {

  implicit val reaper = Reaper()

  val ref = new ElementRef(tag)(system)

  def append(entries: Seq[Entry]): Element = {
    val childrenRefs = mutable.Buffer.empty[Any]
    entries foreach {
      case element: Element ⇒
        reaper.mark(element)
        element.parent = Some(this)
        childrenRefs += element.ref
      case directive: Directive ⇒
        reaper.mark(directive)
        directive.affect(this)
      case TextEntry(s) ⇒ childrenRefs += s
      case Elements(xs) ⇒ childrenRefs ++= xs.map(_.ref)
    }
    if (childrenRefs.nonEmpty) {
      system.utils.call[Unit]("appendChildren", ref, childrenRefs)
    }
    //EventProcessor.deregisterElement(this)
    this
  }
  
  def kill(): Unit = {
    //EventProcessor.deregisterElement(this)
    reaper.sweep()
    ref.free()
  }
}
