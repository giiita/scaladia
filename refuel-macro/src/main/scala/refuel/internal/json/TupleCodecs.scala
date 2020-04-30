package refuel.internal.json

import refuel.json.Codec

trait TupleCodecs {
  implicit def __tuple2[A, B](implicit a: Codec[A], b: Codec[B]): Codec[(A, B)]
  implicit def __tuple3[A, B, C](implicit a: Codec[A], b: Codec[B], c: Codec[C]): Codec[(A, B, C)]
  implicit def __tuple4[A, B, C, D](implicit a: Codec[A], b: Codec[B], c: Codec[C], d: Codec[D]): Codec[(A, B, C, D)]
  implicit def __tuple5[A, B, C, D, E](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E]
  ): Codec[(A, B, C, D, E)]
  implicit def __tuple6[A, B, C, D, E, F](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F]
  ): Codec[(A, B, C, D, E, F)]
  implicit def __tuple7[A, B, C, D, E, F, G](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G]
  ): Codec[(A, B, C, D, E, F, G)]
  implicit def __tuple8[A, B, C, D, E, F, G, H](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H]
  ): Codec[(A, B, C, D, E, F, G, H)]
  implicit def __tuple9[A, B, C, D, E, F, G, H, I](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I]
  ): Codec[(A, B, C, D, E, F, G, H, I)]
  implicit def __tuple10[A, B, C, D, E, F, G, H, I, J](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J]
  ): Codec[(A, B, C, D, E, F, G, H, I, J)]
  implicit def __tuple11[A, B, C, D, E, F, G, H, I, J, K](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J],
      k: Codec[K]
  ): Codec[(A, B, C, D, E, F, G, H, I, J, K)]
  implicit def __tuple12[A, B, C, D, E, F, G, H, I, J, K, L](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J],
      k: Codec[K],
      l: Codec[L]
  ): Codec[(A, B, C, D, E, F, G, H, I, J, K, L)]
  implicit def __tuple13[A, B, C, D, E, F, G, H, I, J, K, L, M](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J],
      k: Codec[K],
      l: Codec[L],
      m: Codec[M]
  ): Codec[(A, B, C, D, E, F, G, H, I, J, K, L, M)]
  implicit def __tuple14[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J],
      k: Codec[K],
      l: Codec[L],
      m: Codec[M],
      n: Codec[N]
  ): Codec[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)]
  implicit def __tuple15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J],
      k: Codec[K],
      l: Codec[L],
      m: Codec[M],
      n: Codec[N],
      o: Codec[O]
  ): Codec[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)]
  implicit def __tuple16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J],
      k: Codec[K],
      l: Codec[L],
      m: Codec[M],
      n: Codec[N],
      o: Codec[O],
      p: Codec[P]
  ): Codec[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)]
  implicit def __tuple17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J],
      k: Codec[K],
      l: Codec[L],
      m: Codec[M],
      n: Codec[N],
      o: Codec[O],
      p: Codec[P],
      q: Codec[Q]
  ): Codec[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)]
  implicit def __tuple18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J],
      k: Codec[K],
      l: Codec[L],
      m: Codec[M],
      n: Codec[N],
      o: Codec[O],
      p: Codec[P],
      q: Codec[Q],
      r: Codec[R]
  ): Codec[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)]
  implicit def __tuple19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J],
      k: Codec[K],
      l: Codec[L],
      m: Codec[M],
      n: Codec[N],
      o: Codec[O],
      p: Codec[P],
      q: Codec[Q],
      r: Codec[R],
      s: Codec[S]
  ): Codec[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)]
  implicit def __tuple20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J],
      k: Codec[K],
      l: Codec[L],
      m: Codec[M],
      n: Codec[N],
      o: Codec[O],
      p: Codec[P],
      q: Codec[Q],
      r: Codec[R],
      s: Codec[S],
      t: Codec[T]
  ): Codec[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)]
  implicit def __tuple21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J],
      k: Codec[K],
      l: Codec[L],
      m: Codec[M],
      n: Codec[N],
      o: Codec[O],
      p: Codec[P],
      q: Codec[Q],
      r: Codec[R],
      s: Codec[S],
      t: Codec[T],
      u: Codec[U]
  ): Codec[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)]
  implicit def __tuple22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V](
      implicit a: Codec[A],
      b: Codec[B],
      c: Codec[C],
      d: Codec[D],
      e: Codec[E],
      f: Codec[F],
      g: Codec[G],
      h: Codec[H],
      i: Codec[I],
      j: Codec[J],
      k: Codec[K],
      l: Codec[L],
      m: Codec[M],
      n: Codec[N],
      o: Codec[O],
      p: Codec[P],
      q: Codec[Q],
      r: Codec[R],
      s: Codec[S],
      t: Codec[T],
      u: Codec[U],
      v: Codec[V]
  ): Codec[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)]
}
