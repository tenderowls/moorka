package moorka

import moorka.rx.collection.ops.BufferOps

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Try

 /**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object rx {

  type Channel[A] = base.Channel[A]
  @deprecated("Use Rx instead State", "0.4.0")
  type State[A] = base.Rx[A]
  type Var[A] = base.Var[A]
  type Lazy[A] = base.Lazy[A]
  type Rx[A] = base.Rx[A]

  val Buffer = collection.Buffer
  val Channel = base.Channel
  val Var = base.Var
  val Val = base.Val
  val Dummy = base.Dummy
  val Lazy = base.Lazy

  val Reaper = death.Reaper

  type Buffer[A] = collection.Buffer[A]
  type BufferView[A] = collection.BufferView[A]

  type Mortal = death.Mortal
  type Reaper = death.Reaper

  implicit def ToRx[T](x: T): Rx[T] = Val(x)
  implicit def ToBufferOps[A](x: BufferView[A]): BufferOps[A] = new BufferOps(x)
  implicit class FutureOps[A](val self: Future[A]) extends AnyVal {
    def toRx(implicit executor: ExecutionContext): Rx[Try[A]] = {
      new base.RxFuture(self)
    }
  }
}
