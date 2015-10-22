package felix.dsl

import felix.collection.BufferView
import felix.core.FelixSystem
import felix.vdom.{Directive, Element}
import moorka.death.{Reaper, Mortal}
import vaska.JSObj

object Repeat {
  def apply(buffer: BufferView[Element])(implicit system: FelixSystem): Repeat = {
    new Repeat(buffer)
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Repeat(val buffer: BufferView[Element])(implicit system: FelixSystem) extends Directive {

  implicit val reaper = Reaper()
  implicit val context = system.flowContext

  def affect(el: Element): Unit = {

    el.ref.call[Unit]("appendChildren",
      buffer.toSeq.map { x =>
        x.parent = Some(el)
        x.ref
      }
    )

    buffer.added foreach { x ⇒
      x.parent = Some(el)
      el.ref.call[JSObj]("appendChild", x.ref)
    }

    buffer.inserted foreach { x ⇒
      x.e.parent = Some(el)
      x.idx + 1 match {
        case idx if idx < buffer.length =>
          el.ref.call[JSObj]("insertBefore", x.e.ref, buffer(idx).ref)
        case _ =>
          el.ref.call[JSObj]("appendChild", x.e.ref)
      }
    }

    buffer.removed foreach { x ⇒
      x.e.parent = None
      el.ref.call[JSObj]("removeChild", x.e.ref)
    }

    buffer.updated foreach { x ⇒
      x.prevE.parent = None
      x.e.parent = Some(el)
      el.ref.call[JSObj]("removeChild", x.prevE.ref)
      el.ref.call[JSObj]("appendChild", x.e.ref)
    }
  }

  def kill(): Unit = {
    reaper.sweep()
  }
}
