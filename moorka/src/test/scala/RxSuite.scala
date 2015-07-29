import moorka._
import utest._

import scala.concurrent.Promise
import scala.util.Success

import utest.ExecutionContext.RunNow

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object RxSuite extends TestSuite {

  val tests = TestSuite {

    "check kill behavior" - {
      "check simple kill" - {
        val emitter = Var(10)
        var calls = 0
        val cb = emitter foreach { _ ⇒
          calls += 1
        }
        cb.kill()
        emitter.pull(1)
        assert(calls == 1)
      }
      "check binding initialized inside flatMap would be killed" - {
        import scala.collection.mutable

        var lst = mutable.Buffer.empty[Rx[_]]
        var gCalls = 0
        var lCalls = 0

        val a = Var(0)
        val b = Var(1)
        val c = Var(2)

        val g = a flatMap { a ⇒
          val binding = b flatMap { b ⇒
            val binding = c map { c ⇒
              lCalls += 1
              a + b + c
            }
            lst += binding
            binding
          }
          lst += binding
          binding
        }
        lst += g

        g foreach { _ ⇒
          gCalls += 1
        }

        a.pull(1)
        b.pull(10)
        c.pull(4)
        b.pull(3)

        val aliveCount = lst.count(_.alive)
        val lstLength = lst.length

        assert(lstLength == 7)
        assert(aliveCount == 3)
        assert(lCalls == 5)
        assert(gCalls == 5)
      }
      "check binding created outside of flatMap wouldn't be killed" - {
        val a = Var(0)
        val b = Var(1)
        val g = a.map(_ + 1)
        b.flatMap(_ ⇒ g)
        b.pull(10)
        assert(g.alive)
      }
    }

    "check standard combinators" - {
      "map() should change type of value" - {
        val emitter = Var(42)
        System.gc()
        val result = emitter.map(_.toString)
        System.gc()
        var calls = 0
        result foreach { x ⇒
          assert(x == "42")
          calls += 1
        }
        System.gc()
        assert(calls == 1)
      }
      "filter() should drop value not satisfied `f`" - {
        var calls = 0
        val ch = Channel[Int]()
        System.gc()
        ch filter (_ > 0) foreach { x ⇒
          calls += 1
          assert(x == 1)
        }
        System.gc()
        ch.pull(0)
        System.gc()
        ch.pull(1)
        System.gc()
        assert(calls == 1)
      }

      "collect() should drop values not processed by `f`" - {
        var calls = 0
        val ch = Channel[Int]()
        ch.collect {
          case x if x > 0 ⇒
            x.toString
        } foreach { x ⇒
          calls match {
            case 0 ⇒ assert(x == "1")
            case 1 ⇒ assert(x == "2")
          }
          calls += 1
        }
        System.gc()
        ch.pull(0)
        System.gc()
        ch.pull(1)
        ch.pull(-1)
        ch.pull(2)
        assert(calls == 2)
      }
    }

    "check autokill combinators" - {
      "once() should kill after fist call" - {
        var calls = 0
        val ch = Channel[Int]()
        System.gc()
        ch once { x ⇒
          calls += 1
        }
        System.gc()
        ch.pull(1)
        System.gc()
        ch.pull(2)
        System.gc()
        ch.pull(3)
        System.gc()
        assert(calls == 1)
      }

      "until() should kill after f returns false" - {
        var calls = 0
        val ch = Channel[Int]()
        System.gc()
        ch until { x ⇒
          calls += 1
          x < 2
        }
        System.gc()
        System.gc()
        ch.pull(1)
        System.gc()
        ch.pull(2)
        System.gc()
        ch.pull(3)
        assert(calls == 2)
      }
    }

    "check flatMap behavior" - {
      "zip two channels" - {
        val state1 = Channel[String]()
        val state2 = Channel[Int]()
        val zipped = state1 zip state2
        System.gc()
        var calls = 0
        zipped foreach { x =>
          calls += 1
          calls match {
            case 1 => assert(x.toString() == "(Cat,1)")
            case 2 => assert(x.toString() == "(Dog,1)")
          }
        }
        System.gc()
        state1.pull("Cat")
        System.gc()
        state2.pull(1)
        System.gc()
        state1.pull("Dog")
        System.gc()
        state2.pull(1)
        assert(calls == 2)
      }
      "zip two vars" - {
        val state1 = Var("Cat")
        val state2 = Var(1)
        val zipped = state1 zip state2
        System.gc()
        var calls = 0
        System.gc()
        zipped foreach { x =>
          calls += 1
          calls match {
            case 1 => assert(x.toString() == "(Cat,1)")
            case 2 => assert(x.toString() == "(Dog,1)")
            case 3 => assert(x.toString() == "(Dog,2)")
            case 4 => assert(x.toString() == "(Cow,2)")
            case 5 => assert(x.toString() == "(Cow,3)")
          }
        }
        System.gc()
        state1.pull("Dog")
        System.gc()
        state2.pull(2)
        System.gc()
        state1.pull("Cow")
        System.gc()
        state2.pull(3)
        assert(calls == 5)
      }
      "bind 3 channels" - {
        val ch1 = Channel[String]()
        val ch2 = Channel[String]()
        val ch3 = Channel[String]()
        System.gc()
        var calls = 0
        ch1 flatMap { v1 ⇒
          ch2 flatMap { v2 ⇒
            ch3 map { v3 ⇒
              calls += 1
              s"$v1 $v2 $v3"
            }
          }
        } foreach { x ⇒
          assert(x == "I am cow")
        }
        System.gc()
        ch1.pull("I")
        System.gc()
        ch2.pull("am")
        System.gc()
        ch3.pull("cow")
        assert(calls == 1)
      }

      "bind two vars and channel" - {
        var calls = 0
        var evalCalls = 0
        val vx = Var(Lazy(0))
        val vy = Var(Lazy(0))
        val click = Channel[Lazy[Int]]()
        val res = Channel[Lazy[Int]]()
        System.gc()
        res <<= {
          vx >>= { x ⇒
            vy >>= { y ⇒
              click >>= { z ⇒
                Lazy {
                  evalCalls += 1
                  x.eval() + y.eval() + z.eval()
                }
              }
            }
          }
        }
        System.gc()
        res foreach { x ⇒
          calls += 1
          assert(x.eval() == 6)
        }
        System.gc()
        vy.pull(Lazy(2))
        vx.pull(Lazy(2))
        System.gc()
        click.pull(Lazy(2))
        assert(calls == 1)
        assert(evalCalls == 1)
      }
    }

    "check for-comprehension" - {
      var calls = 0
      val vx = Var(2)
      val vy = Var(2)
      val res = Channel[Int]()
      res foreach { x ⇒
        calls += 1
        assert(x == 4)
      }
      System.gc()
      res pull {
        for (
          x ← vx;
          y ← vy
        ) yield {
          x + y
        }
      }
      System.gc()
      assert(calls == 1)
    }

    "check for-comprehensions with filter" - {
      val a = Channel[Int]()
      val b = for (x ← a if x > 10) yield x + 1
      System.gc()
      b foreach { x =>
        assert(x == 12)
      }
      System.gc()
      a.pull(10)
      a.pull(11)
    }

    "check drop()" - {
      var calls = 0
      val ch = Channel[Int]()
      System.gc()
      ch drop 2 foreach { x ⇒
        calls += 1
        assert(x == 2)
      }
      System.gc()
      ch.pull(0)
      System.gc()
      ch.pull(1)
      System.gc()
      ch.pull(2)
      assert(calls == 1)
    }

    "check take()" - {
      val x = Channel[Int]()
      System.gc()
      x.take(3) foreach { x =>
        assert(x == Seq(1, 2, 3))
      }
      System.gc()
      x.pull(1)
      x.pull(2)
      x.pull(3)
      x.pull(4)
    }

//    "check fold() on Var" - {
//      val x = Var("I")
//      val res = x.fold("")(_ + " " + _)
//      System.gc()
//      x.pull("am")
//      x.pull("cow")
//      res.foreach { x =>
//        assert(x == " I am cow")
//      }
//      ()
//    }

//    "check fold() on Channel" - {
//      val x = Channel[String]()
//      val res = x.fold("")(_ + " " + _)
//      System.gc()
//      x.pull("I")
//      x.pull("am")
//      x.pull("cow")
//      res.foreach { x =>
//        assert(x == " I am cow")
//      }
//      ()
//    }

    "check switch" - {
      val a = Channel[Int]()
      val b = a switch {
        case x if x % 15 == 0 ⇒ Left("FizzBuzz")
        case x if x % 3 == 0 ⇒ Left("Fizz")
        case x if x % 5 == 0 ⇒ Left("Buzz")
        case x ⇒ Right(x)
      }
      b._1 foreach { x ⇒
        assert(x == "Fizz")
      }
      b._2 foreach { x ⇒
        assert(x == 2)
      }
      a.pull(3)
      a.pull(2)
    }

    "check partition" - {
      val a = Channel[Int]()
      val b = a.partition(_ > 10)
      b._1 foreach { x ⇒
        assert(x == 11)
      }
      b._2 foreach { x ⇒
        assert(x == 2)
      }
      a.pull(11)
      a.pull(2)
    }

    "check future conversion" - {
      var calls = 0
      val p = Promise[Int]()
      val rx = p.future.toRx
      rx foreach { x ⇒
        calls += 1
        assert(x == Success(10))
      }
      System.gc()
      p.success(10)
      assert(calls == 1)
    }

    def complexWithModBehavior() = {
      val promise = Promise[Boolean]()
      val dependency = Var[String]("AIdle")
      val collectDependency = {
        dependency collect {
          case x if x startsWith "AProgress" ⇒ "BProgress"
          case "ASuccess" ⇒ "BSuccess"
        }
      }
      val res = Var.withMod("BIdle") {
        case "BIdle" =>
          promise.future.toRx.flatMap {
            case Success(true) ⇒ collectDependency
            case _ ⇒ Val("BFailure")
          }
        case "BProgress" ⇒ collectDependency
        case "BSuccess" ⇒ Killer
        case "BFailure" ⇒ Killer
      }
      (promise, dependency, res)
    }

    "check complex withMod behavior 1" - {
      val (promise, dependency, res) = complexWithModBehavior()
      promise.success(true)
      dependency.pull(Val("AProgress 10"))
      dependency.pull(Val("AProgress 12"))
      dependency.pull(Val("AProgress 24"))
      dependency.pull(Val("AProgress 100"))
      dependency.pull(Val("ASuccess"))

      res once { x ⇒
        assert(x == "BSuccess")
        assert(!res.alive)
      }
    }

    "check complex withMod behavior 2" - {
      val (promise, dependency, res) = complexWithModBehavior()
      promise.success(false)
      res once { x ⇒
        assert(x == "BFailure")
        assert(!res.alive)
      }
    }
    
    "check Var ignore side effects when state was changed twice" - {
      val state = Var(0d)
      val changesChannel1 = Channel.signal()
      val changesChannel2 = state.stateless
      var bug = false
      Var.withMod(0) {
        case 0 ⇒
          changesChannel1 or changesChannel2 map {
            case Left(_) ⇒ 2
            case Right(_) ⇒ 1
          }
        case 1 ⇒ // Writing
          bug = true
          Dummy
        case 2 ⇒ // Updating
          Val(0)
      } foreach {
        case 2 ⇒
          state.pull(Silent(Math.random()))
        case _ ⇒
      }
      changesChannel1.fire()
      changesChannel1.fire()
      assert(!bug)
    }
  }
}
