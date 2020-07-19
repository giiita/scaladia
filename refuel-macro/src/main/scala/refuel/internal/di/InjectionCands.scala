package refuel.internal.di

import refuel.domination.InjectionPriority.Default
import refuel.domination.{Inject, InjectionPriority}

import scala.reflect.macros.blackbox

sealed abstract class InjectionCands[C <: blackbox.Context](val c: C)(val cands: Vector[C#Symbol]) {

  /* Injection priority config type tag */
  private[this] lazy val InjectionPriorityConfigType = c.weakTypeOf[Inject]

  private[this] type DepWithPriority = (C#Expr[InjectionPriority], C#Symbol)

  def rankingEvaluation: (c.Expr[InjectionPriority], Seq[C#Symbol]) = {
    cands match {
      case _c if _c.size == 1 => c.universe.reify[InjectionPriority](Default) -> _c
      case _ =>
        cands
          .map { sm =>
            val ip = sm.annotations
              .find(_.tree.tpe =:= InjectionPriorityConfigType)
              .flatMap(_.tree.children.tail.headOption)
              .fold[c.Expr[InjectionPriority]](
                c.Expr[InjectionPriority](c.parse("refuel.domination.InjectionPriority.Default"))
              ) {
                case x if x.symbol.isModule =>
                  c.Expr[InjectionPriority](c.parse(x.symbol.fullName))
                case _ =>
                  c.abort(
                    c.enclosingPosition,
                    s"The injection priority setting must be a static module. From [ ${sm.fullName} ]"
                  )
              }
            ip -> sm
          }
          .groupBy(x => c.eval(x._1))
          .toSeq
          .minBy(_._1)(InjectionPriority.Order)
          ._2
          .toList match {
          case x if x.isEmpty => c.universe.reify(Default) -> x.map(_._2)
          case all @ x :: _   => x._1                      -> all.map(_._2)
        }
    }
  }
}

case class ConfirmedCands[C <: blackbox.Context](override val c: C)(cands: Vector[C#Symbol])
    extends InjectionCands[C](c)(cands)

case class ExcludingRuntime[C <: blackbox.Context](override val c: C)(cands: Vector[C#Symbol])
    extends InjectionCands[C](c)(cands)
