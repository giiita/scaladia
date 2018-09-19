package com.giitan.container

import scala.reflect.runtime.universe._
import com.giitan.box.Container
import com.giitan.scope.Scope.{ClassScope, ObjectScope}

object TaggedClassIndexer extends Indexer[ClassScope] {

  /**
    * Resist dependencies.
    *
    * @param tag   Dependency object typed tag.
    * @param value Dependency object.
    * @param scope Acceptable scope.
    * @tparam T
    */
  def indexing[T](tag: TypeTag[T], value: T, scope: Seq[ClassScope[_]]): Unit = {
    implicitly[Container[ClassScope]].indexing(tag, value, scope)
  }
}


object TaggedObjectIndexer extends Indexer[ObjectScope] {

  /**
    * Resist dependencies.
    *
    * @param tag   Dependency object typed tag.
    * @param value Dependency object.
    * @param scope Acceptable scope.
    * @tparam T
    */
  def indexing[T](tag: TypeTag[T], value: T, scope: Seq[ObjectScope[_]]): Unit = {
    implicitly[Container[ObjectScope]].indexing(tag, value, scope)
  }
}
