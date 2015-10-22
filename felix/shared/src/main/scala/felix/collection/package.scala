package felix

import felix.collection.ops.BufferOps

import scala.language.implicitConversions

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
package object collection {
  implicit def toOps[T](buffer: BufferView[T]): BufferOps[T] = {
    new BufferOps[T](buffer)
  }
}
