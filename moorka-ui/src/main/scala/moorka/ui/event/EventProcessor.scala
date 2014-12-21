package moorka.ui.event

import moorka.rx._
import moorka.ui.RenderAPI

import scala.annotation.tailrec
import scala.collection.mutable
import scala.scalajs.js

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object EventProcessor {

  private[ui] val nativeElementIndex = new mutable.HashMap[String, EventTarget]()

  def registerElement(element: EventTarget) = {
    nativeElementIndex.put(element.ref.id, element)
  }

  def deregisterElement(element: EventTarget) = {
    nativeElementIndex.remove(element.ref.id)
  }
}

sealed trait EventProcessor[A <: SyntheticEvent ] {

  /**
   * Type of native event
   */
  val eventType:String

  object PropagationStopped extends Throwable

  val event: A

  val listeners = new mutable.HashMap[EventTarget, Channel[A]]()

  val captures = new mutable.HashMap[EventTarget, Channel[A]]()

  def fillEvent(element: EventTarget, nativeEvent: js.Dynamic) = {
    event._target = element
    event._bubbles = false
  }

  def propagate(element:EventTarget, nativeEvent: js.Dynamic) = {

    // todo: May be it's more effective to use js.Array instead of scala's
    // todo: immutable collection. They are produce a lot of garbage.
    @tailrec
    def collectParents(e: EventTarget, xs: List[EventTarget]): List[EventTarget] = {
      val ep = e.parent
      if (ep != null) {
        collectParents(ep, ep :: xs)
      }
      else {
        xs
      }
    }

    fillEvent(element, nativeEvent)

    try {
      // Capturing phase
//      event._eventPhase = Capturing
//      collectParents(element, Nil).foreach { x =>
//        event._currentTarget = x
//        captures.get(x).foreach(_.emit(event))
//        if (event._propagationStopped)
//          throw PropagationStopped
//      }
      // At target
      event._eventPhase = AtTarget
      event._currentTarget = element
      listeners.get(element).foreach(_.emit(event))
      if (event._propagationStopped)
        throw PropagationStopped
      // Bubbling
//      event._eventPhase = Bubbling
//      event._bubbles = true
//      var ct = element.parent
//      while (ct != null) {
//        listeners.get(ct).foreach(_.emit(event))
//        if (event._propagationStopped)
//          throw PropagationStopped
//        ct = ct.parent
//      }
    }
    catch {
      case PropagationStopped =>
        // Stop event propagation cycle
    }
    if (event._defaultPrevented)
      nativeEvent.preventDefault()
  }
  
  def topLevelListener(nativeEvent: js.Dynamic) = {
    val targetId = nativeEvent.target.asInstanceOf[String]
    EventProcessor.nativeElementIndex.get(targetId).foreach { syntheticTarget =>
      propagate(syntheticTarget, nativeEvent)
    }
  }

  def addListener(element: EventTarget, listener: (A) => Unit): Channel[A] = {
    listeners.getOrElseUpdate(element, Channel[A]).subscribe(listener)
  }

  def addCapture(element: EventTarget, capture: (A) => Unit): Channel[A] = {
    captures.getOrElseUpdate(element, Channel[A]).subscribe(capture)
  }

  RenderAPI.onMessage subscribe { x =>
    if (x(0) == "event") {
      val e = x(1).asInstanceOf[js.Dynamic]
      if (e.`type`.asInstanceOf[String] == eventType) {
        topLevelListener(e)
      }
    }
  }
}

sealed trait MouseEventProcessor extends EventProcessor[MouseEvent] {

  val event: MouseEvent = new MouseEvent()

  override def fillEvent(element: EventTarget, x: js.Dynamic): Unit = {
    super.fillEvent(element, x)
    event._altKey = x.altKey.asInstanceOf[Boolean]
    event._ctrlKey = x.ctrlKey.asInstanceOf[Boolean]
    event._metaKey = x.metaKey.asInstanceOf[Boolean]
    event._button = x.button.asInstanceOf[Int]
    event._clientX = x.clientX.asInstanceOf[Int]
    event._clientY = x.clientY.asInstanceOf[Int]
    event._screenX = x.screenX.asInstanceOf[Int]
    event._screenY = x.screenY.asInstanceOf[Int]
  }
}

sealed trait FormEventProcessor extends EventProcessor[FormEvent] {
  val event: FormEvent = new FormEvent()
}

object ClickEventProcessor extends MouseEventProcessor {
  val eventType: String = "click"
}

object TouchendEventProcessor extends EventProcessor[TouchEvent] {
  val eventType: String = "touchend"
  val event = new TouchEvent()
}

object DoubleClickEventProcessor extends MouseEventProcessor {
  val eventType: String = "dblclick"
}

object SubmitEventProcessor extends FormEventProcessor {
  val eventType: String = "submit"
}

object ChangeEventProcessor extends FormEventProcessor {
  val eventType: String = "change"
}

object InputEventProcessor extends FormEventProcessor {
  val eventType: String = "input"
}
