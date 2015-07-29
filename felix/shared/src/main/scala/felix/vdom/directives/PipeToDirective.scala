package felix.vdom.directives

import felix.core.{EventProcessor, FelixSystem}
import felix.vdom.{Directive, Element}
import moorka.death.Reaper
import moorka.rx.{Channel, Val}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class PipeToDirective(eventType: String,
                      channel: Channel[Unit],
                      system: FelixSystem) extends Directive {

  val reaper = Reaper()
  
  def affect(element: Element): Unit = {
    val f: EventProcessor.EventListener = (_, _, _) ⇒ channel.pull(Val(()))
    val mortal = system.eventProcessor.addListener(element, eventType, f)
    reaper.mark(mortal)
  }

  def kill(): Unit = {
    reaper.sweep()
  }
}
