package felix.vdom

import felix.core.{EventTarget, FelixSystem}
import moorka.rx.death.{Mortal, Reaper}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Component extends NodeLike with Mortal {

  implicit def system: FelixSystem

  implicit def ec = system.ec

  implicit val reaper = Reaper()

  private lazy val element = start

  def ref = element.ref

  def start: Element

  def kill(): Unit = {
    element.kill()
    reaper.sweep()
  }

  def setParent(value: Option[EventTarget]): Unit = {
    element.parent = value
  }
}
