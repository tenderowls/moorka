package felix.dsl

import felix.vdom.{Directive, Element}
import moorka._
import vaska.JSObj

object Repeat {
  def apply(buffer: BufferView[Element]): Repeat = {
    new Repeat(buffer)
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Repeat(val buffer: BufferView[Element]) extends Directive {

  var subscriptions = List.empty[Rx[Any]]

  def affect(el: Element): Unit = {

    el.ref.call[Unit]("appendChildren",
      buffer.toSeq.map { x =>
        x.parent = Some(el)
        x.ref
      }
    )

    subscriptions ::= buffer.added foreach { x ⇒
      x.parent = Some(el)
      el.ref.call[JSObj]("appendChild", x.ref)
    }

    subscriptions ::= buffer.inserted foreach { x ⇒
      x.e.parent = Some(el)
      x.idx + 1 match {
        case idx if idx < buffer.length =>
          el.ref.call[JSObj]("insertBefore", x.e.ref, buffer(idx).ref)
        case _ =>
          el.ref.call[JSObj]("appendChild", x.e.ref)
      }
    }

    subscriptions ::= buffer.removed foreach { x ⇒
      x.e.parent = None
      el.ref.call[JSObj]("removeChild", x.e.ref)
    }

    subscriptions ::= buffer.updated foreach { x ⇒
      x.prevE.parent = None
      x.e.parent = Some(el)
      el.ref.call[JSObj]("removeChild", x.prevE.ref)
      el.ref.call[JSObj]("appendChild", x.e.ref)
    }
  }

  def kill(): Unit = {
    subscriptions foreach {
      _.kill()
    }
  }
}
