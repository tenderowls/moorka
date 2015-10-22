package moorka.flow

import moorka.death.Mortal

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
trait Source extends Mortal {

  def validate(): Unit
}
