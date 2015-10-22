import moorka.death.Reaper
import moorka.flow.mutable.{Await, Channel, Var}
import moorka.flow.{Context, Flow}
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object FlowSuite extends TestSuite {

  implicit val reaper = Reaper()

  def withContext[U](f: Context ⇒ U): Any = {
    val ctx = Context()
    f(ctx)
    ctx.validate()
  }

  val tests = TestSuite {

    "check standard combinators" - {
      "map() should change type of value" - {
        var calls = 0
        withContext { implicit ctx ⇒
          val emitter = Var(42)
          System.gc()
          val result = emitter.map(_.toString)
          System.gc()
          result foreach { x ⇒
            assert(x == "42")
            calls += 1
          }
        }
        System.gc()
        assert(calls == 1)
      }
      "filter() should drop value not satisfied `f`" - {
        var calls = 0
        withContext { implicit ctx ⇒
          val ch = Channel[Int]
          System.gc()
          ch.filter(_ > 0).foreach { x ⇒
            calls += 1
            assert(x == 1)
          }
          System.gc()
          ch.push(0)
          System.gc()
          ch.push(1)
        }
        System.gc()
        assert(calls == 1)
      }

      "collect() should drop values not processed by `f`" - {
        var calls = 0
        withContext {  implicit ctx ⇒
          val ch = Channel[Int]
          ch collect {
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
          ch.push(0)
          System.gc()
          ch.push(1)
          ch.push(-1)
          ch.push(2)
        }
        assert(calls == 2)
      }
    }

//    "check autokill combinators" - {
//      "once() should kill after fist call" - {
//        var calls = 0
//        withContext {  implicit ctx ⇒
//          val ch = Channel[Int]
//          System.gc()
//          ch once { x ⇒
//            calls += 1
//          }
//          System.gc()
//          ch.push(1)
//          System.gc()
//          ch.push(2)
//          System.gc()
//          ch.push(3)
//          System.gc()
//        }
//        assert(calls == 1)
//      }
//
//      "until() should kill after f returns false" - {
//        var calls = 0
//        withContext {  implicit ctx ⇒
//          val ch = Channel[Int]
//          System.gc()
//          ch until { x ⇒
//            calls += 1
//            x < 2
//          }
//          System.gc()
//          System.gc()
//          ch.push(1)
//          System.gc()
//          ch.push(2)
//          System.gc()
//          ch.push(3)
//        }
//        assert(calls == 2)
//      }
//    }
//
    "check flatMap behavior" - {
      "zip two channels" - {
        var calls = 0
        implicit val ctx = Context()
          val state1 = Channel[String]
          val state2 = Channel[Int]
          val zipped = state1 flatMap { a ⇒
            Await(state2) map { b ⇒
              (a, b)
            }
          }
          System.gc()
          zipped foreach { x =>
            calls += 1
            calls match {
              case 1 => assert(x.toString() == "(Cat,1)")
              case 2 => assert(x.toString() == "(Dog,1)")
            }
          }
          state1.push("Cat")
          ctx.validate()
          state2.push(1)
          ctx.validate()
          state1.push("Dog")
          ctx.validate()
          state2.push(1)
          ctx.validate()
          assert(calls == 2)
      }

      "zip two vars" - {
        var calls = 0
        withContext {  implicit ctx ⇒
          val state1 = Var("Cat")
          val state2 = Var(1)
          val zipped = state1 zip state2
          System.gc()
          System.gc()
          zipped foreach { x =>
            calls += 1
            calls match {
              case 1 => assert(x.toString() == "(Cat,1)")
              case 2 => assert(x.toString() == "(Cat,2)")
              case 3 => assert(x.toString() == "(Cat,3)")
              case 4 => assert(x.toString() == "(Dog,1)")
              case 5 => assert(x.toString() == "(Dog,2)")
              case 6 => assert(x.toString() == "(Dog,3)")
              case 7 => assert(x.toString() == "(Cow,1)")
              case 8 => assert(x.toString() == "(Cow,2)")
              case 9 => assert(x.toString() == "(Cow,3)")
            }
          }
          state1.update("Dog")
          state2.update(2)
          state1.update("Cow")
          state2.update(3)
        }
        assert(calls == 9)
      }

      "bind 3 channels" - {
        var calls = 0
        withContext {  implicit ctx ⇒
          val ch1 = Channel[String]
          val ch2 = Channel[String]
          val ch3 = Channel[String]
          System.gc()
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
          ch1.push("I")
          System.gc()
          ch2.push("am")
          System.gc()
          ch3.push("cow")
        }
        assert(calls == 1)
      }
      "bind two vars and channel" - {
        var calls = 0
        var evalCalls = 0
        withContext {  implicit ctx ⇒
          val vx = Var(0)
          val vy = Var(0)
          val click = Channel[Int]
          System.gc()
          val res = vx flatMap { x ⇒
            vy flatMap { y ⇒
              click map { z ⇒
                evalCalls += 1
                x + y + z
              }
            }
          }

          System.gc()
          res foreach { x ⇒
            calls += 1
            calls match {
              case 1 ⇒ assert(x == 2)
              case 2 ⇒ assert(x == 4)
              case 3 ⇒ assert(x == 6)
            }
          }
          System.gc()
          vy.update(2)
          vx.update(2)
          System.gc()
          click.push(2)
        }
        assert(calls == 3)
        assert(evalCalls == 4)
      }
    }


    "check for-comprehension" - {
      var calls = 0
      withContext {  implicit ctx ⇒
        val vx = Var(2)
        val vy = Var(2)
        System.gc()
        val res = for (x ← vx; y ← vy) yield {
          x + y
        }
        res foreach { x ⇒
          calls += 1
          assert(x == 4)
        }
        System.gc()
      }
      assert(calls == 1)
    }

    "check for-comprehensions with filter" - {
      var calls = 0
      withContext {  implicit ctx ⇒
        val a = Channel[Int]
        val b = for (x ← a if x > 10) yield {
          calls += 1
          x + 1
        }
        System.gc()
        b foreach { x =>
          assert(x == 12)
        }
        System.gc()
        a.push(10)
        a.push(11)
      }
    }

    "check drop()" - {
      var calls = 0
      withContext { implicit ctx ⇒
        val ch = Channel[Int]
        System.gc()
        ch drop 2 foreach { x ⇒
          calls += 1
          assert(x == 2)
        }
        ch.push(0)
        ch.push(1)
        ch.push(2)
      }
      assert(calls == 1)
    }

    "check or() with Channel" - {
      var calls = 0
      withContext { implicit ctx ⇒
        val c1 = Channel[Int]
        val c2 = Channel[String]
        c1 push 10
        c1 or c2 foreach {
          case Left(i) ⇒
            calls += 1
            assert(i == 10)
          case Right(s) ⇒
            calls += 1
            assert(s == "Cat")
        }
        c2 push "Cat"
      }
      assert(calls == 2)
    }

    "check take()" - {
      var calls = 0
      withContext { implicit ctx ⇒
        val x = Channel[Int]
        System.gc()
        x.take(3) foreach { x =>
          calls += 1
          assert(x == Seq(1, 2, 3))
        }
        System.gc()
        x.push(1)
        x.push(2)
        x.push(3)
        x.push(4)
      }
      assert(calls == 1)
    }

    "check fold() on Var" - {
      var calls = 0
      withContext { implicit ctx ⇒
        val state = Var("I")
        val res = state.fold("") {
          case ("", x) ⇒ x
          case (acc, x) ⇒ s"$acc $x"
        }
        state() = "am"
        state() = "cow"
        res.foreach { x =>
          calls += 1
          calls match {
            case 1 ⇒ assert(x == "I")
            case 2 ⇒ assert(x == "I am")
            case 3 ⇒ assert(x == "I am cow")
          }
        }
      }
      assert(calls == 3)
    }

    "check fold() on Channel" - {
      var calls = 0
      withContext { implicit ctx ⇒
        val state = Channel[String]
        val res = state.fold("") {
          case ("", x) ⇒ x
          case (acc, x) ⇒ s"$acc $x"
        }
        state.push("I")
        state.push("am")
        state.push("cow")
        res.foreach { x =>
          calls += 1
          calls match {
            case 1 ⇒ assert(x == "I")
            case 2 ⇒ assert(x == "I am")
            case 3 ⇒ assert(x == "I am cow")
          }
        }
      }
      assert(calls == 3)
    }

    "check switch" - {
      var calls = 0
      withContext {  implicit ctx ⇒
        val a = Channel[Int]
        val b = a switch {
          case x if x % 15 == 0 ⇒ Left("FizzBuzz")
          case x if x % 3 == 0 ⇒ Left("Fizz")
          case x if x % 5 == 0 ⇒ Left("Buzz")
          case x ⇒ Right(x)
        }
        b._1 foreach { x ⇒
          calls += 1
          assert(x == "Fizz")
        }
        b._2 foreach { x ⇒
          calls += 1
          assert(x == 2)
        }
        a.push(3)
        a.push(2)
      }
      assert(calls == 2)
    }

    "check partition" - {
      var calls = 0
      withContext {  implicit ctx ⇒
        val a = Channel[Int]
        val b = a.partition(_ > 10)
        b._1 foreach { x ⇒
          calls += 1
          assert(x == 11)
        }
        b._2 foreach { x ⇒
          calls += 1
          assert(x == 2)
        }
        a.push(11)
        a.push(2)
      }
      assert(calls == 2)
    }

    "check future conversion" - {
      var calls = 0
      withContext {  implicit ctx ⇒
        val p = Promise[Int]()
        import moorka.flow.converters.future
        val flow = p.future.toFlow
        flow foreach { x ⇒
          calls += 1
          assert(x == 10)
        }
        System.gc()
        p.success(10)
      }
      assert(calls == 1)
    }
  }
}
