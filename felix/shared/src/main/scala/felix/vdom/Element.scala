package felix.vdom

import felix.core.{EventTarget, FelixSystem}
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
  
  def refId = ref.id
  
  def append(entries: Seq[Entry]): this.type = {
    val childrenRefs = mutable.Buffer.empty[Any]
    val directives = mutable.Buffer.empty[Directive]
    entries foreach {
      case element: Element ⇒
        reaper.mark(element)
        element.parent = Some(this)
        childrenRefs += element.ref
      case directive: Directive ⇒
        reaper.mark(directive)
        directives += directive
      case TextEntry(s) ⇒ childrenRefs += s
      case Elements(xs) ⇒ childrenRefs ++= xs.map(_.ref)
    }
    if (childrenRefs.nonEmpty) {
      system.utils.call[Unit]("appendChildren", ref, childrenRefs)
    }
    for (directive ← directives) {
      directive.affect(this)
    }
    system.eventProcessor.registerElement(this)
    this
  }

  def kill(): Unit = {
    system.eventProcessor.unregisterElement(this)
    reaper.sweep()
    ref.free()
  }
}
