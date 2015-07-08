val mnem = Map(
  '2' -> "ABC", '3' -> "DEF", '4' -> "GHI", '5' -> "JKL",
  '6' -> "MNO", '7' -> "PQRS", '8' -> "TUV", '9' -> "WXYZ"
)

val charCode = mnem.flatMap{
  case (code,string) => string.map((_,code)).toMap
}

def wordCode(s:String) = s.toUpperCase map charCode

wordCode("HALOBANDUNG")