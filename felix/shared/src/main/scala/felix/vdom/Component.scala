package felix.vdom

import felix.core.{EventTarget, FelixSystem}
import moorka.death.{Mortal, Reaper}

import scala.concurrent.ExecutionContext

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Component extends NodeLike with Mortal {

  implicit def system: FelixSystem

  implicit def executionContext: ExecutionContext = system.executionContext

  implicit val reaper = Reaper()

  private lazy val element = start

  def ref: ElementRef = element.ref

  def start: NodeLike

  def kill(): Unit = {
    element.kill()
    reaper.sweep()
  }

  private[felix] def setParent(value: Option[RefHolder with EventTarget]): Unit = {
    element.setParent(value)
  }
}
