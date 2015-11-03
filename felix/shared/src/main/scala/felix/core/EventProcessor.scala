package felix.core

import moorka.death.Mortal
import vaska.{JSAccess, JSObj}

import scala.collection.mutable
import scala.concurrent.ExecutionContext

object EventProcessor {

  private[core] object PropagationStopped extends Throwable

  /**
   * objectId@eventType
   */
  private type ListenerKey = String

  /**
   * Fist arg is current target, second arg is target.
   * If return value is EventAction, event processor will execute it.
   */
  type EventListener = (EventTarget, EventTarget, JSObj) ⇒ Any

  private[core] def makeKey(eventType: String, target: EventTarget): ListenerKey = {
    eventType + "@" + target.refId
  }
}

/**
 * Event propagation mechanism for felix virtual DOM.
 */
final class EventProcessor(jsAccess: JSAccess,
                           document: JSObj,
                           utils: JSObj)
                          (implicit executionContext: ExecutionContext) {

  import EventProcessor._

  private[this] val index = mutable.Map.empty[String, EventTarget]

  private[this] val listeners = mutable.Map.empty[ListenerKey, List[EventListener]]

  private[this] val captures = mutable.Map.empty[ListenerKey, List[EventListener]]

  private[this] val types = mutable.Set.empty[String]

  private def propagate(eventType: String, target: EventTarget, nativeEvent: JSObj): Unit = {
    def broadcast(listeners: mutable.Map[ListenerKey, List[EventListener]],
                  elements: List[EventTarget]): Unit = {
      for (currentTarget ← elements) {
        val key = makeKey(eventType, currentTarget)
        val xs = listeners.getOrElse(key, Nil) filter { listener ⇒
          listener(currentTarget, target, nativeEvent) match {
            case EventAction.RemoveListener ⇒ false
            case EventAction.StopPropagation ⇒ throw PropagationStopped
            case _ ⇒ true
          }
        }
        listeners.update(key, xs)
      }
    }

    try {
      val parents = target.parents
      // Capturing phase
      broadcast(captures, parents)
      // At Target + Bubbling phases
      broadcast(listeners, target :: parents.reverse)
    }
    catch {
      case PropagationStopped ⇒
      // Stop event propagation cycle
    }
  }

  def addListener(element: EventTarget,
                  eventType: String,
                  f: EventListener): Mortal = {
    val key = makeKey(eventType, element)
    val tail = listeners.getOrElse(key, Nil)
    listeners(key) = f :: tail
    new Mortal {
      def kill(): Unit = {
        listeners(key) = listeners(key).filter(_ != f)
      }
    }
  }

  def addCapture(element: EventTarget,
                 eventType: String,
                 f: EventListener): Mortal = {
    val key = makeKey(eventType, element)
    val tail = captures.getOrElse(key, Nil)
    captures(key) = f :: tail
    new Mortal {
      def kill(): Unit = {
        captures(key) = captures(key).filter(_ != f)
      }
    }
  }

  /**
   * Adds element to event processor
   */
  def registerElement(element: EventTarget): Unit = {
    index.put(element.refId, element)
  }

  /**
   * Remove element from event processor
   */
  def unregisterElement(element: EventTarget): Unit = {
    index.remove(element.refId)
  }

  /**
   * Register event type. Use this for all native event types.
   * If you want to enable [[EventProcessor]] for custom events that
   * not dispatches from document [[globalEventListener]]
   * @param autoPreventDefault Immediately call preventDefault()
   * @param eventType name of native event. click, mousedown, etc.
   */
  def registerEventType(eventType: String, autoPreventDefault: Boolean): Unit = {
    if (!types(eventType)) {
      globalEventListener foreach { gel ⇒
        if (autoPreventDefault) {
          utils.call[Unit]("autoPreventDefault", eventType, gel)
        }
        else {
          document.call[Unit]("addEventListener", eventType, gel)
        }
      }
      types += eventType
    }
  }

  /**
   * Use this callback to handle events.
   */
  val globalEventListener = jsAccess.registerCallback { nativeEvent: JSObj ⇒
    nativeEvent.get[String]("type") foreach { tpe ⇒
      nativeEvent.get[JSObj]("target") foreach { target ⇒
        target.call[String]("getAttribute", "data-felix-id") foreach { id ⇒
          index.get(id) foreach { target ⇒
            propagate(tpe, target, nativeEvent)
          }
        }
      }
    }
  }

  // This is it.
  registerEventType("submit", autoPreventDefault = true)
}
