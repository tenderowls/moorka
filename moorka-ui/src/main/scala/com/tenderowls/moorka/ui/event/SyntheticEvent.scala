package com.tenderowls.moorka.ui.event

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
sealed trait SyntheticEvent {

  private[moorka] var _bubbles: Boolean = false

  private[moorka] var _currentTarget: EventTarget = null

  private[moorka] var _target: EventTarget = null

  private[moorka] var _timestamp: Long = 0l

  private[moorka] var _defaultPrevented: Boolean = false

  private[moorka] var _eventPhase: EventPhase = Idle

  private[moorka] var _propagationStopped: Boolean = false

  def bubbles: Boolean = _bubbles

  def currentTarget: EventTarget = _currentTarget

  def target: EventTarget = _target

  def timestamp: Long = _timestamp

  def eventPhase: EventPhase = _eventPhase

  def stopPropagation() = {
    _propagationStopped = true
  }
}

final class FormEvent extends SyntheticEvent {

}

sealed class MouseEvent extends SyntheticEvent {

  private[moorka] var _altKey: Boolean = false

  def altKey: Boolean = _altKey

  private[moorka] var _button: Int = 0

  def button: Int = _button

  private[moorka] var _buttons: Int = 0

  def buttons: Int = _buttons

  private[moorka] var _clientX: Int = 0

  def clientX: Int = _clientX

  private[moorka] var _clientY: Int = 0

  def clientY: Int = _clientY

  private[moorka] var _ctrlKey: Boolean = false

  def ctrlKey: Boolean = _ctrlKey

  private[moorka] var _metaKey: Boolean = false

  def metaKey: Boolean = _metaKey

  private[moorka] var _pageX: Int = 0

  def pageX: Int = _pageX

  private[moorka] var _pageY: Int = 0

  def pageY: Int = _pageY

  private[moorka] var _relatedTarget: EventTarget = null

  def relatedTarget: EventTarget = _relatedTarget

  private[moorka] var _screenX: Int = 0

  def screenX: Int = _screenX

  private[moorka] var _screenY: Int = 0

  def screenY: Int = _screenY

  private[moorka] var _shiftKey: Boolean = false

  def shiftKey: Boolean = _shiftKey
}
