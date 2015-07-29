import moorka.rx.ops.{RxOps, RxSeqOps, VarOps}
import moorka.collection.ops.BufferOps

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Try

 /**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object moorka {

  type Channel[A] = moorka.rx.Channel[A]
  @deprecated("Use Rx instead State", "0.4.0")
  type State[A] = moorka.rx.Rx[A]
  type Silent[A] = moorka.rx.Silent[A]
  type Var[A] = moorka.rx.Var[A]
  type Lazy[A] = moorka.rx.Lazy[A]
  type Rx[A] = moorka.rx.Rx[A]

  val Buffer = collection.Buffer
  val Channel = rx.Channel
  val Var = rx.Var
  val Val = rx.Val
  val Dummy = rx.Dummy
  val Silent = rx.Silent
  val Killer = rx.Killer
  val Lazy = rx.Lazy

  val Reaper = death.Reaper

  type Buffer[A] = moorka.collection.Buffer[A]
  type BufferView[A] = moorka.collection.BufferView[A]

  type Mortal = moorka.death.Mortal
  type Reaper = moorka.death.Reaper

  implicit def ToBufferOps[A](x: BufferView[A]): BufferOps[A] = new BufferOps(x)

  implicit def ToVarOps[A](x: Var[A]): VarOps[A] = new VarOps(x)

  implicit def ToRxOps[A](x: Rx[A]): RxOps[A] = new RxOps(x)

  implicit def ToRxSeqOps[A](x: Rx[Seq[A]]): RxSeqOps[A] = new RxSeqOps[A](x)

  implicit class MortalOps[T <: Mortal](val self: T) extends AnyVal {
    def mark()(implicit reaper: Reaper): T = {
      reaper.mark(self)
      self
    }
  }
  
  implicit class FutureOps[A](val self: Future[A]) extends AnyVal {
    def toRx(implicit executor: ExecutionContext): Rx[Try[A]] = {
      if (self.isCompleted) {
        Val(self.value.get)
      }
      else {
        new moorka.rx.RxFuture(self)
      }
    }
  }
}
