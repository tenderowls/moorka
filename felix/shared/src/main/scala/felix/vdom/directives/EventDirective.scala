package felix.vdom.directives

import felix.core.{EventProcessor, FelixSystem}
import felix.vdom.{Directive, Element}
import moorka.rx.death.Reaper

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class EventDirective(eventType: String,
                     f: EventProcessor.EventListener,
                     capture: Boolean,
                     system: FelixSystem) extends Directive {

  system.eventProcessor.registerEventType(eventType, autoPreventDefault = false)
  
  val reaper = Reaper()

  def affect(element: Element): Unit = {
    reaper mark {
      if (capture) system.eventProcessor.addCapture(element, eventType, f)
      else system.eventProcessor.addListener(element, eventType, f)
    }
  }

  def kill(): Unit = {
    reaper.sweep()
  }
}
