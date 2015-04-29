package moorka.ui.components.base

import moorka.rx._
import moorka.ui.element.{ElementBase, ElementExtension}
import vaska.JSObj

object Repeat {
  def apply(buffer: BufferView[ElementBase]): Repeat = {
    new Repeat(buffer)
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Repeat(val buffer: BufferView[ElementBase]) extends ElementExtension {

  var subscribtions = List.empty[Rx[Any]]

  def start(el: ElementBase): Unit = {

    el.ref.call[Unit]("appendChildren",
      buffer.asSeq.map { x =>
        x.parent = el
        x.ref
      }
    )

    subscribtions ::= buffer.added foreach { x ⇒
      x.parent = el
      el.ref.call[JSObj]("appendChild", x.ref)
    }

    subscribtions ::= buffer.inserted foreach { x ⇒
      x.e.parent = el
      x.idx + 1 match {
        case idx if idx < buffer.length =>
          el.ref.call[JSObj]("insertBefore", x.e.ref, buffer(idx).ref)
        case _ =>
          el.ref.call[JSObj]("appendChild", x.e.ref)
      }
    }

    subscribtions ::= buffer.removed foreach { x ⇒
      x.e.parent = null
      el.ref.call[JSObj]("removeChild", x.e.ref)
    }

    subscribtions ::= buffer.updated foreach { x ⇒
      x.prevE.parent = null
      x.e.parent = el
      el.ref.call[JSObj]("removeChild", x.prevE.ref)
      el.ref.call[JSObj]("appendChild", x.e.ref)
    }
  }

  override def kill(): Unit = {
    super.kill()
    subscribtions foreach {
      _.kill()
    }
  }
}
