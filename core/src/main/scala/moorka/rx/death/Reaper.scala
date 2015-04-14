package moorka.rx.death

object Reaper {

  /**
   * This nice version of death reaper doesn't
   * care about mortals. Only flowers.
   */
  val nice = new Reaper {
    def sweep(): Unit = {}

    def mark(mortal: Mortal): Unit = {}
  }

  def apply() = new Reaper() {

    @volatile
    private var mortals: List[Mortal] = Nil

    def mark(mortal: Mortal) = mortals ::= mortal

    def sweep() = {
      mortals.foreach(_.kill())
    }
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Reaper {

  def mark(mortals: Mortal*): Unit = {
    for (x ← mortals) {
      mark(x)
    }
  }

  def mark(mortal: Mortal): Unit

  def sweep(): Unit
}
