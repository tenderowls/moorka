package moorka.ui.element

import moorka.rx._
import moorka.ui.event.{EventProcessor, EventTarget}
import moorka.ui.Ref

/**
 * DOM element representation
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait ElementBase extends ElementEntry with Mortal with EventTarget {

  private[moorka] var parent: EventTarget = null

  val ref: Ref

  implicit val reaper = Reaper()

  def fill(children: ElementEntry*) = fillSeq(children)

  def fillSeq(children: Seq[ElementEntry]): Unit = {
    children.foreach {
      case e: ElementBase =>
        e.mark()
        e.parent = this
        ref.appendChild(e.ref)
      case sequence: ElementSequence =>
        sequence.value.foreach { x =>
          x.parent = this
          x.mark()
        }
        ref.appendChildren(sequence.value.map(_.ref))
      case processor: ElementExtension =>
        processor.mark()
        processor.start(this)
    }

    EventProcessor.registerElement(this)
  }
  
  def kill(): Unit = {
    EventProcessor.deregisterElement(this)
    reaper.sweep()
    ref.kill()
  }
}

class ElementSequence(val value: Seq[ElementBase]) extends ElementEntry
