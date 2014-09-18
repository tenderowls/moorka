package com.tenderowls.moorka.mkml.engine

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.core.events.Emitter
import com.tenderowls.moorka.mkml.dom.{ElementBase, MKML}
import org.scalajs.dom

import scala.annotation.tailrec
import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object SyntheticEventProcessor extends MKML {

  private var lastId = 0

  private[engine] val nativeElementIndex = new mutable.HashMap[String, ElementBase]()

  def registerElement(element: ElementBase) = {
    var id = element.nativeElement.id
    if (id == "") {
      lastId += 1
      id = "_mk" + lastId.toString
      element.nativeElement.id = id
    }
    nativeElementIndex.put(id, element)
  }

  def deregisterElement(element: ElementBase) = {
    nativeElementIndex.remove(element.nativeElement.id)
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

  def fillEvent(element: ElementBase, nativeEvent: dom.Event) = {
    event._target = element
    event._bubbles = nativeEvent.bubbles
    event._cancelable = nativeEvent.cancelable
  }

  def propagate(element:ElementBase, nativeEvent: dom.Event) = {
    
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
  
  def topLevelListener(e: dom.Event) = {
    val nativeTarget = e.target.asInstanceOf[dom.Element]
    SyntheticEventProcessor.nativeElementIndex.get(nativeTarget.id).foreach { syntheticTarget =>
      propagate(syntheticTarget, e)
    }
  }

  def addListener(element: ElementBase, listener: (A) => Unit): Slot[A] = {
    listeners.getOrElseUpdate(element, Emitter[A]).subscribe(listener)
  }

  def addCapture(element: ElementBase, capture: (A) => Unit): Slot[A] = {
    captures.getOrElseUpdate(element, Emitter[A]).subscribe(capture)
  }
  
}

sealed trait MouseEventProcessor extends SyntheticEventProcessor[MouseEvent] {

  val event: MouseEvent = new MouseEvent()

  override def fillEvent(element: ElementBase, nativeEvent: dom.Event): Unit = {
    super.fillEvent(element, nativeEvent)
    nativeEvent match {
      case x: dom.MouseEvent =>
        val body = dom.document.body
        event._nativeEvent = x
        event._altKey = x.altKey
        event._ctrlKey = x.ctrlKey
        event._metaKey = x.metaKey
        //event._buttons = x.buttons
        event._button = x.button
        event._clientX = x.clientX
        event._clientY = x.clientY
        event._pageX = x.clientX + body.scrollLeft - body.clientLeft
        event._pageY = x.clientY + body.scrollTop - body.clientTop
        event._screenX = x.screenX
        event._screenY = x.screenY
        val rt = x.relatedTarget.asInstanceOf[dom.Element]
        if (rt != null) {
          event._relatedTarget = SyntheticEventProcessor.nativeElementIndex(rt.id)
        }
      case _ =>
        // todo throw something
    }
  }
}

sealed trait FormEventProcessor extends SyntheticEventProcessor[FormEvent] {
  val event: FormEvent = new FormEvent()
}

object ClickEventProcessor extends MouseEventProcessor {
  val eventType: String = "click"
  dom.document.addEventListener(eventType, { (e: dom.Event) =>
    topLevelListener(e)
  })
}

object DoubleClickEventProcessor extends MouseEventProcessor {
  val eventType: String = "dblclick"
  dom.document.addEventListener(eventType, { (e: dom.Event) =>
    topLevelListener(e)
  })
}

object SubmitEventProcessor extends FormEventProcessor {
  val eventType: String = "submit"
  dom.document.addEventListener(eventType, { (e: dom.Event) =>
    topLevelListener(e)
  })
}