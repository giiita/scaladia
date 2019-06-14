package com.giitan.scope

import com.giitan.container._
import com.giitan.injector.Injector
import com.giitan.scope.Scope.{ClassScope, ObjectScope}

import scala.collection.mutable.ListBuffer
import scala.reflect._
import scala.reflect.runtime.universe._

object Scope {
  type ClassScope[T] = Class[T]
  type ObjectScope[T] = Wrapper[T]

  def apply[X: TypeTag](value: X): ScopeSet[X] = new ScopeSet[X](
    new TaggedClassScope[X](value),
    new TaggedObjectScope[X](value)
  )
}

case class ScopeSet[T: TypeTag](classSet: Scope[T, ClassScope], objectSet: Scope[T, ObjectScope]) extends Injector {

  /**
    c* Index to global container.
    */
  def indexing(): Unit = {
    classSet.indexing()
    objectSet.indexing()
  }

  /**
    * Extend the allowed type.
    * Only that dynamic class.
    *
    * @param cls Allowed object.
    * @tparam A Allowed type.
    * @return
    */
  def accept[A](cls: A): ScopeSet[T] = copy(objectSet = objectSet.accept(cls))

  /**
    * Extend the allowed type.
    * Only that object.
    *
    * @tparam A Allowed type.
    * @return
    */
  def accept[A: TypeTag : ClassTag]: ScopeSet[T] = copy(classSet = classSet.accept[A])
}

trait Scope[X, ST[_]] extends Injector {

  /**
    * Super class of injected object
    *
    * @return
    */
  val tt: TypeTag[X]
  val instance: X
  val acceptedScope: ListBuffer[ST[_]] = ListBuffer.empty

  /**
    * Extend the allowed type.
    * Only that dynamic class.
    *
    * @param cls Allowed object.
    * @tparam A Allowed type.
    * @return
    */
  def accept[A](cls: A): Scope[X, ST]

  /**
    * Extend the allowed type.
    * Only that type.
    *
    * @tparam A Allowed type.
    * @return
    */
  def accept[A: TypeTag : ClassTag]: Scope[X, ST]

  /**
    * Allow access to all types.
    * Default this.
    */
  def acceptedGlobal(): Scope[X, ST] = {
    this.acceptedScope.clear()
    this
  }

  def indexing(): Unit
}

case class Wrapper[T](v: T)

private[giitan] class TaggedClassScope[X: TypeTag](value: X) extends Scope[X, ClassScope] {
  val tt: TypeTag[X] = typeTag[X]
  val instance: X = value

  /**
    * Extend the allowed type.
    * Only that dynamic class.
    *
    * @param cls Allowed object.
    * @tparam A Allowed type.
    * @return
    */
  def accept[A](cls: A): Scope[X, ClassScope] = {
    acceptedScope += cls.getClass
    this
  }

  /**
    * Extend the allowed type.
    * Only that type.
    *
    * @tparam A Allowed type.
    * @return
    */
  def accept[A: TypeTag : ClassTag]: Scope[X, ClassScope] = {
    acceptedScope += classTag[A].runtimeClass
    this
  }

  def indexing(): Unit = {
    if (acceptedScope.nonEmpty) {
      inject[Indexer[ClassScope]].indexing(tt, instance, this.acceptedScope.toSeq)
    }
  }
}

private[giitan] class TaggedObjectScope[X: TypeTag](value: X) extends Scope[X, ObjectScope] {
  val tt: TypeTag[X] = typeTag[X]
  val instance: X = value

  /**
    * Extend the allowed type.
    * Only that dynamic class.
    *
    * @param cls Allowed object.
    * @tparam A Allowed type.
    * @return
    */
  def accept[A](cls: A): Scope[X, ObjectScope] = {
    acceptedScope += Wrapper(cls)
    this
  }

  /**
    * Extend the allowed type.
    * Only that type.
    *
    * @tparam A Allowed type.
    * @return
    */
  def accept[A: TypeTag : ClassTag]: Scope[X, ObjectScope] = {
    acceptedScope += Wrapper(inject[A].provide)
    this
  }

  def indexing(): Unit = {
    if (acceptedScope.nonEmpty) {
      inject[Indexer[ObjectScope]].indexing(tt, instance, this.acceptedScope.toSeq)
    }
  }
}