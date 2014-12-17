 package moorka

import moorka.rx.base.ops.{RxStateOps, RxStreamOps}
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

  type Collection[A] = collection.Buffer[A]
  type BufferView[A] = collection.BufferView[A]

  trait Mortal {
    def kill(): Unit
  }

  implicit def ToRxStreamOps[A](x: Channel[A]): RxStreamOps[A] = new RxStreamOps(x)
  implicit def ToRxStateOps[A](x: State[A]): RxStateOps[A] = new RxStateOps(x)
}
