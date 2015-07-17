package felix.components

import felix.vdom.{Directive, Element}
import moorka.rx._
import vaska.JSObj

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object DataRepeat {

  def apply[A](dataProvider: BufferView[A],
               componentFactory: Rx[A] => Element) = {
    new DataRepeat[A](dataProvider, componentFactory)
  }
}

class DataRepeat[A](dataProvider: BufferView[A],
                factory: Rx[A] => Element)

  extends Directive {
  
  var subscriptions = List.empty[Rx[Any]]
  
  def affect(element: Element): Unit = {
    val ref = element.ref
    val components = mutable.Buffer[Element]()
    val states = mutable.Buffer[Var[A]]()

    def createAndAppendComponent(x: A): Element = {
      // Create reactive state and state renderer
      val state = Var(x)
      val component = factory(state)
      // Add theirs to internal cache
      states += state
      components += component
      component
    }

    ref.call[Unit]("appendChildren",
      dataProvider.toSeq.map { x =>
        val c = createAndAppendComponent(x)
        c.parent = Some(element)
        c.ref
      }
    )

    subscriptions ::= dataProvider.added foreach { x =>
      val c = createAndAppendComponent(x)
      c.parent = Some(element)
      ref.call[Unit]("appendChild", c.ref)
    }

    subscriptions ::= dataProvider.removed foreach { x =>
      states.remove(x.idx)
      val c = components.remove(x.idx)
      ref.call[Unit]("removeChild", c.ref)
      c.parent = null
      c.kill()
    }

    subscriptions ::= dataProvider.inserted foreach { x =>
      val state = Var(x.e)
      val c = factory(state)
      states.insert(x.idx, state)
      components.insert(x.idx, c)
      c.parent = Some(element)
      // Insert into DOM
      x.idx + 1 match {
        case idx if idx < components.length =>
          ref.call[JSObj]("insertChild", c.ref, components(idx).ref)
        case _ =>
          ref.call[JSObj]("appendChild", c.ref)
      }
    }

    subscriptions ::= dataProvider.updated foreach { x =>
      val state = states(x.idx)
      state.modOnce(_ ⇒ x.e)
    }
  }

  def kill(): Unit = {
    subscriptions foreach {
      _.kill()
    }
  }
}
