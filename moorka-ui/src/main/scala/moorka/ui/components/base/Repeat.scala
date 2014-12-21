package moorka.ui.components.base

import moorka.rx._
import moorka.ui.element._

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object Repeat {

  def apply[A](dataProvider: BufferView[A],
               componentFactory: State[A] => Component[A]) = {
    new Repeat[A](dataProvider, componentFactory)
  }
}

class Repeat[A](dataProvider: BufferView[A],
                factory: State[A] => Component[_])
  extends ElementExtension {
  
  def start(element: ElementBase): Unit = {
    val ref = element.ref
    val components = mutable.Buffer[Component[_]]()
    val states = mutable.Buffer[Var[A]]()

    def createAndAppendComponent(x: A): Component[_] = {
      // Create reactive state and state renderer
      val state = Var(x)
      val component = factory(state)
      // Add theirs to internal cache
      states += state
      components += component
      component
    }

    ref.appendChildren(
      dataProvider.asSeq.map { x =>
        val c = createAndAppendComponent(x)
        c.parent = element
        c.ref
      }
    )

    dataProvider.added subscribe { x =>
      val c = createAndAppendComponent(x)
      c.parent = element
      ref.appendChild(c.ref)
    }

    dataProvider.removed subscribe { x =>
      states.remove(x.idx)
      val c = components.remove(x.idx)
      ref.removeChild(c.ref)
      c.parent = null
      c.kill()
    }

    dataProvider.inserted subscribe { x =>
      val state = Var(x.e)
      val c = factory(state)
      states.insert(x.idx, state)
      components.insert(x.idx, c)
      c.parent = element
      // Insert into DOM
      x.idx + 1 match {
        case idx if idx < components.length =>
          ref.insertChild(c.ref, components(idx).ref)
        case _ =>
          c.parent = element
          ref.appendChild(c.ref)
      }
    }

    dataProvider.updated subscribe { x =>
      val state = states(x.idx)
      state() = x.e
    }
  }
}
