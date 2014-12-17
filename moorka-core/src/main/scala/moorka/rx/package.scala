 package moorka

import moorka.rx.base.ops.{StateOps, ChannelOps}
import scala.language.implicitConversions

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object rx {

  val Bind = binding.BindingMacro
  val Var = base.Var

  type Channel[A] = base.Channel[A]
  type State[A] = base.State[A]
  type Var[A] = base.Var[A]

  val Buffer = collection.Buffer
  val Channel = base.Channel
  val Reaper = death.Reaper

  type Buffer[A] = collection.Buffer[A]
  type BufferView[A] = collection.BufferView[A]

  type Mortal = death.Mortal
  type Reaper = death.Reaper

  implicit def ToRxStreamOps[A](x: Channel[A]): ChannelOps[A] = new ChannelOps(x)
  implicit def ToRxStateOps[A](x: State[A]): StateOps[A] = new StateOps(x)
}
