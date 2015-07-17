package felix.core

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
sealed trait EventAction

object EventAction {

  case object StopPropagation extends EventAction

  case object RemoveListener extends EventAction

  case object DoNothing extends EventAction

}
