package moorka

import moorka.death.{Mortal, Reaper}
import moorka.flow.mutable.Var
import moorka.flow.ops.{FlowOps, FlowSeqOps, VarOps}

import scala.language.implicitConversions

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
package object flow {

  implicit def ToVarOps[A](x: Var[A]): VarOps[A] = new VarOps(x)

  implicit def ToFlowOps[A](x: Flow[A]): FlowOps[A] = new FlowOps(x)

  implicit def ToFlowSeqOps[A](x: Flow[Seq[A]]): FlowSeqOps[A] = new FlowSeqOps[A](x)

  implicit class MortalOps[T <: Mortal](val self: T) extends AnyVal {
    def mark()(implicit reaper: Reaper): T = {
      reaper.mark(self)
      self
    }
  }

}
