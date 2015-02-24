package moorka.ui.components.base

import moorka.rx._
import moorka.ui.element.{ElementBase, ElementExtension}

object Repeat {
  def apply(buffer: BufferView[ElementBase]): Repeat = {
    new Repeat(buffer)
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Repeat(val buffer: BufferView[ElementBase]) extends ElementExtension {

  def start(el: ElementBase): Unit = {
    
    el.ref.appendChildren {
      buffer.asSeq.map { x =>
        x.parent = el
        x.ref
      }
    }
    
    buffer.added subscribe { x ⇒
      x.parent = el
      el.ref.appendChild(x.ref)
    }
    
    buffer.inserted subscribe { x ⇒
      x.e.parent = el
      x.idx + 1 match {
        case idx if idx < buffer.rxLength() =>
          el.ref.insertChild(x.e.ref, buffer(idx).ref)
        case _ =>
          el.ref.appendChild(x.e.ref)
      }
    }
    
    buffer.removed subscribe { x ⇒
      x.e.parent = null
      el.ref.removeChild(x.e.ref)
    }
    
    buffer.updated subscribe { x ⇒
      x.prevE.parent = null
      x.e.parent = el
      el.ref.removeChild(x.prevE.ref)
      el.ref.appendChild(x.e.ref)
    }
  }
}
