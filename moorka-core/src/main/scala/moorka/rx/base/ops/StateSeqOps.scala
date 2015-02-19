package moorka.rx.base.ops

import moorka.rx._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class StateSeqOps[T](val self: State[Seq[T]]) extends AnyVal {

  implicit def toBuffer: BufferView[T] = {
    val buffer = Buffer.fromSeq(self())
    self subscribe { s =>
      s.length match {
        case l if l > buffer.length() =>
          val startIndex = buffer.length()
          for (i <- 0 until l - startIndex)
            buffer += s(startIndex + i)
        case l if l < buffer.length() =>
          for (i <- 0 until buffer.length() - l)
            buffer.remove(0)
        case _ =>
      }
      for (i <- 0 until s.length) {
        buffer(i) = s(i)
      }
    }
    buffer
  }

}
