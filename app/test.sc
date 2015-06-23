val tolerance = 0.001

def fixedPoint(f:Double => Double)(initialGuess:Double) ={
  def isCloseEnough(guess: Double) = {
    val interval = (guess - f(guess)) / guess
    Math.abs(interval) < tolerance
  }

  def iterate(guess:Double):Double = {
    if (isCloseEnough(guess)) guess
    else iterate(f(guess))
  }

  iterate(initialGuess)
}

def averageDamp(f:Double => Double)(x:Double) = {
  (x + f(x))/2
}

def square(n:Int) = fixedPoint(averageDamp(x => n/x))(n)

square(80)