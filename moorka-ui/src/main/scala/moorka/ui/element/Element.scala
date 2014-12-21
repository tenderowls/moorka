package moorka.ui.element

import moorka.rx._
import moorka.ui.Ref
import moorka.ui.event.EventProcessor

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Element(tagName: String, children: Seq[ElementEntry]) extends ElementBase {

  val ref = Ref(tagName)

  var observers:List[Mortal] = Nil

  children.foreach {
    case e: ElementBase =>
      observers ::= e
      e.parent = this
      ref.appendChild(e.ref)
    case sequence: ElementSequence =>
      sequence.value.foreach { x =>
        x.parent = this
        observers ::= x
      }
      ref.appendChildren(sequence.value.map(_.ref))
    case processor: ElementExtension =>
      observers ::= processor
      processor.start(this)
  }

  override def kill(): Unit = {
    super.kill()
    observers.foreach(_.kill())
    observers = Nil
  }

  EventProcessor.registerElement(this)
}
