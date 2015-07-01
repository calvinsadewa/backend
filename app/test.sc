def mapFun[T, U](xs: List[T], f: T => U): List[U] =
  (xs foldRight List[U]())( f(_) :: _ )

val l:List[Int] = List(1,2,3,4)
mapFun[Int,String](l, Function"Ini" + (_ * 2))