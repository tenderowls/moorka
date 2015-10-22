package felix.dsl

import felix.collection.BufferView
import felix.core.FelixSystem
import felix.vdom.{Directive, Element, NodeLike}
import moorka.death.Reaper
import moorka.flow.{Sink, Flow}
import moorka.flow.mutable.Var
import vaska.JSObj

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object DataRepeat {

  def apply[A](dataProvider: BufferView[A], componentFactory: Flow[A] => NodeLike)
              (implicit system: FelixSystem) = {
    new DataRepeat[A](dataProvider, componentFactory)
  }
}

class DataRepeat[A](dataProvider: BufferView[A],
                    factory: Flow[A] => NodeLike)
                   (implicit system: FelixSystem)
  extends Directive {

  implicit val context = system.flowContext
  implicit val reaper = Reaper()

  def affect(element: Element): Unit = {
    val ref = element.ref
    val components = mutable.Buffer[NodeLike]()
    val states = mutable.Buffer[Var[A]]()

    def createAndAppendComponent(x: A): NodeLike = {
      // Create reactive state and state renderer
      val state = Var(x)
      val component = factory(state)
      // Add theirs to internal cache
      states += state
      components += component
      component
    }

    system.utils.call[Unit]("appendChildren", ref,
      dataProvider.toSeq.map { x =>
        val c = createAndAppendComponent(x)
        c.setParent(Some(element))
        c.ref
      }
    )

    dataProvider.added foreach { x =>
      val c = createAndAppendComponent(x)
      c.setParent(Some(element))
      ref.call[Unit]("appendChild", c.ref)
    }

    dataProvider.removed foreach { x =>
      states.remove(x.idx)
      val c = components.remove(x.idx)
      ref.call[Unit]("removeChild", c.ref)
      c.setParent(None)
      c.kill()
    }

    dataProvider.inserted foreach { x =>
      val state = Var(x.e)
      val c = factory(state)
      states.insert(x.idx, state)
      components.insert(x.idx, c)
      c.setParent(Some(element))
      // Insert into DOM
      x.idx + 1 match {
        case idx if idx < components.length =>
          ref.call[JSObj]("insertChild", c.ref, components(idx).ref)
        case _ =>
          ref.call[JSObj]("appendChild", c.ref)
      }
    }

    dataProvider.updated foreach { x =>
      val state = states(x.idx)
      state() = x.e
    }
  }

  def kill(): Unit = {
    reaper.sweep()
  }
}
