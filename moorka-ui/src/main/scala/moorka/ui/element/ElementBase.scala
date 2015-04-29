package moorka.ui.element

import moorka.rx._
import moorka.ui.event.{EventProcessor, EventTarget}
import vaska.JSObj

/**
 * DOM element representation
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait ElementBase extends ElementEntry with Mortal with EventTarget {

  private[moorka] var parent: EventTarget = null

  val ref: JSObj

  implicit val reaper = Reaper()

  def fill(children: ElementEntry*) = fillSeq(children)

  def fillSeq(children: Seq[ElementEntry]): Unit = {
    children.foreach {
      case e: ElementBase =>
        reaper.mark(e)
        e.parent = this
        ref.call[JSObj]("appendChild", e.ref)
      case sequence: ElementSequence =>
        sequence.value.foreach { x =>
          x.parent = this
          reaper.mark(x)
        }
        ref.call[Unit]("appendChildren", sequence.value.map(_.ref))
      case processor: ElementExtension =>
        reaper.mark(processor)
        processor.start(this)
    }

    EventProcessor.registerElement(this)
  }
  
  def kill(): Unit = {
    EventProcessor.deregisterElement(this)
    reaper.sweep()
    ref.free()
  }
}

class ElementSequence(val value: Seq[ElementBase]) extends ElementEntry
