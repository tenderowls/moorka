package moorka.ui.event

import vaska.JSObj

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class SyntheticEvent(val nativeEvent: JSObj,
                           val target: EventTarget) {

  private[moorka] var _bubbles: Boolean = false

  private[moorka] var _currentTarget: EventTarget = null

  private[moorka] var _eventPhase: EventPhase = Idle

  private[moorka] var _propagationStopped = false
  
  def bubbles = _bubbles

  def currentTarget = _currentTarget

  def eventPhase: EventPhase = _eventPhase

  def stopPropagation() = {
    _propagationStopped = true
  }
}
