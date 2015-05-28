package moorka.ui.event

import moorka.rx._
import moorka.ui._
import vaska.JSObj

import scala.annotation.tailrec
import scala.collection.mutable
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

object EventProcessor {

  final class PropagationStopped extends Throwable

  private[ui] val nativeElementIndex = mutable.Map[String, EventTarget]()

  def registerElement(element: EventTarget) = {
    nativeElementIndex.put(element.ref.id, element)
  }

  def deregisterElement(element: EventTarget) = {
    nativeElementIndex.remove(element.ref.id)
  }

  val listeners = mutable.Map[(String, EventTarget), Channel[SyntheticEvent]]()

  val captures = mutable.Map[(String, EventTarget), Channel[SyntheticEvent]]()
  
  def propagate(tpe: String, element:EventTarget, nativeEvent: JSObj) = {

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

    val event = new SyntheticEvent(nativeEvent, element)

    try {
      // Capturing phase
      event._eventPhase = Capturing
      collectParents(element, Nil).foreach { x =>
        event._currentTarget = x
        captures.get(tpe, x).foreach(_.pull(event))
        if (event._propagationStopped)
          throw new PropagationStopped()
      }
      // At target
      event._eventPhase = AtTarget
      event._currentTarget = element
      listeners.get(tpe, element).foreach(_.pull(event))
      if (event._propagationStopped)
        throw new PropagationStopped()
      // Bubbling
      event._eventPhase = Bubbling
      event._bubbles = true
      var ct = element.parent
      while (ct != null) {
        listeners.get(tpe, ct).foreach(_.pull(event))
        if (event._propagationStopped)
          throw new PropagationStopped()
        ct = ct.parent
      }
    }
    catch {
      case x: PropagationStopped =>
        // Stop event propagation cycle
    }
  }
  
  def addListener(element: EventTarget, tpe: String, listener: (SyntheticEvent) => Unit): Rx[Unit] = {
    listeners.getOrElseUpdate((tpe, element), Channel[SyntheticEvent]()).foreach(listener)
  }

  def addCapture(element: EventTarget, tpe: String, capture: (SyntheticEvent) => Unit): Rx[Unit] = {
    captures.getOrElseUpdate((tpe, element), Channel[SyntheticEvent]()).foreach(capture)
  }

  /**
   * Use this callback to handle events.
   */
  val globalEventListener = jsAccess.registerCallback { nativeEvent: JSObj ⇒
    for {
      tpe ← nativeEvent.get[String]("type")
      target ← nativeEvent.get[JSObj]("target")
      id ← target.get[String]("id")
    } yield {
      val element = EventProcessor.nativeElementIndex.get(id)
      element foreach { target ⇒
        propagate(tpe, target, nativeEvent)
      }
    }
  } 
  
  globalEventListener foreach { listener ⇒
    val eventTypes = Seq(
      "click", "touchstart", "touchend", 
      "mousedown", "mouseup", "dblclick",
      "change", "input"
    )
    eventTypes foreach { eventType ⇒
      document.call[Unit]("addEventListener", eventType, listener)
    }
    // Prevent default behavior for every submit
    document.call[Unit](
      "addEventListenerWhichPreventDefault",
      "submit", 
      listener
    )
  }
}
