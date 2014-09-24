package com.tenderowls.moorka.mkml.engine

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.core.events.Emitter
import com.tenderowls.moorka.mkml.dom.ElementBase
import org.scalajs.dom

import scala.scalajs.js
import scala.annotation.tailrec
import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object SyntheticEventProcessor {

  private[engine] val nativeElementIndex = new mutable.HashMap[String, ElementBase]()

  def registerElement(element: ElementBase) = {
    nativeElementIndex.put(element.ref.id, element)
  }

  def deregisterElement(element: ElementBase) = {
    nativeElementIndex.remove(element.ref.id)
  }
}

sealed abstract class SyntheticEventProcessor[A <: SyntheticEvent ] {

  /**
   * Type of native event
   */
  val eventType:String

  object PropagationStopped extends Throwable

  val event: A

  val listeners = new mutable.HashMap[ElementBase, Emitter[A]]()

  val captures = new mutable.HashMap[ElementBase, Emitter[A]]()

  def fillEvent(element: ElementBase, nativeEvent: js.Dynamic) = {
    event._target = element
    event._bubbles = false
  }

  def propagate(element:ElementBase, nativeEvent: js.Dynamic) = {

    // todo: May be it's more effective to use js.Array instead of scala's
    // todo: immutable collection. They are produce a lot of garbage.
    @tailrec
    def collectParents(e: ElementBase, xs: List[ElementBase]): List[ElementBase] = {
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
      event._eventPhase = Capturing
      collectParents(element, Nil).foreach { x =>
        event._currentTarget = x
        captures.get(x).foreach(_.emit(event))
        if (event._propagationStopped)
          throw PropagationStopped
      }
      // At target
      event._eventPhase = AtTarget
      event._currentTarget = element
      listeners.get(element).foreach(_.emit(event))
      if (event._propagationStopped)
        throw PropagationStopped
      // Bubbling
      event._eventPhase = Bubbling
      event._bubbles = true
      var ct = element.parent
      while (ct != null) {
        listeners.get(ct).foreach(_.emit(event))
        if (event._propagationStopped)
          throw PropagationStopped
        ct = ct.parent
      }
    }
    catch {
      case PropagationStopped =>
        // Stop event propagation cycle
    }
    if (event._defaultPrevented)
      nativeEvent.preventDefault()
  }
  
  def topLevelListener(e: js.Dynamic) = {
    val targetId = e.target.asInstanceOf[String]
    SyntheticEventProcessor.nativeElementIndex.get(targetId).foreach { syntheticTarget =>
      propagate(syntheticTarget, e)
    }
  }

  def addListener(element: ElementBase, listener: (A) => Unit): Slot[A] = {
    listeners.getOrElseUpdate(element, Emitter[A]).subscribe(listener)
  }

  def addCapture(element: ElementBase, capture: (A) => Unit): Slot[A] = {
    captures.getOrElseUpdate(element, Emitter[A]).subscribe(capture)
  }

  RenderBackendApi.onMessage subscribe { x =>
    if (x(0) == "event") {
      val e = x(1).asInstanceOf[js.Dynamic]
      if (e.`type` == eventType) {
        topLevelListener(e)
      }
    }
  }
}

sealed trait MouseEventProcessor extends SyntheticEventProcessor[MouseEvent] {

  val event: MouseEvent = new MouseEvent()

  override def fillEvent(element: ElementBase, x: js.Dynamic): Unit = {
    super.fillEvent(element, x)
    event._altKey = x.altKey.asInstanceOf[Boolean]
    event._ctrlKey = x.ctrlKey.asInstanceOf[Boolean]
    event._metaKey = x.metaKey.asInstanceOf[Boolean]
    //event._buttons = x.buttons
    event._button = x.button.asInstanceOf[Int]
    event._clientX = x.clientX.asInstanceOf[Int]
    event._clientY = x.clientY.asInstanceOf[Int]
    //event._pageX = x.clientX + body.scrollLeft - body.clientLeft
    //event._pageY = x.clientY + body.scrollTop - body.clientTop
    event._screenX = x.screenX.asInstanceOf[Int]
    event._screenY = x.screenY.asInstanceOf[Int]
    
  }
}

sealed trait FormEventProcessor extends SyntheticEventProcessor[FormEvent] {
  val event: FormEvent = new FormEvent()
}

object ClickEventProcessor extends MouseEventProcessor {
  val eventType: String = "click"
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