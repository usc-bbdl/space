package bbdl.space
import breeze.linalg.{DenseVector, DenseMatrix}
/*
This is a set of functions which help you set up the matrices for multiple A and b matrices, and comparisons between them.
In this implementation, we look specifically at how these spaces change over time, and thus the order of the System
Arrays is significant.
 */
object KSystemConstraints {
  /*
  A DenseMatrix[Double] is a matrix of any size
  j int is the index of the matrix among other matricies of the same size (0 indexed)
  K int the length of the array of systems
  @return A' A wider DenseMatrix[Double], with zeros where the other matrices would fit on both sides.
  When j=0 or j=k, A is padded on the right/left side, respectively
  Left side Zeros size = (m, nj)
  Right side Zeros size = (m, (K-1-j)*n
   */
  def PadWithZeroMatrices(A: DenseMatrix[Double], j: Int, K: Int): DenseMatrix[Double] = {
    if (j == 0) {
      //if it's the first matrix on the left
      DenseMatrix.horzcat(A, DenseMatrix.zeros[Double](A.rows, A.cols * (K - 1)))
    } else if (j == (K - 1)) {
      //if it's the last one on the right
      DenseMatrix.horzcat(DenseMatrix.zeros[Double](A.rows, A.cols * (K - 1)), A)
    } else {
      //if its a matrix in the middle somewhere, with zeros padded on both sides
      val FirstPartZeros = DenseMatrix.zeros[Double](A.rows, (K - 1 - j) * A.cols)
      val SecondPartZeros = DenseMatrix.zeros[Double](A.rows, A.cols * (K - j - 1))
      DenseMatrix.horzcat(Array(FirstPartZeros, A, SecondPartZeros): _*)
    }
  }

  def NegStackbVector(b: DenseVector[Double]): DenseVector[Double] = {
    DenseVector.vertcat(b, -b)
  }

  def NegStackAMatrices(A: DenseMatrix[Double]): DenseMatrix[Double] = {
    DenseMatrix.vertcat(A, -A)
  }

  /*
  @param kGeneratorSystems the set containing all of the consecutive A's b's and deltavecs
  @return equalityconstraints It adds all of them together in one big matrix
   */
  def ConcatConstraints_A(kGeneratorSystems: KGeneratorSystems): DenseMatrix[Double] = {
    val Ks = kGeneratorSystems.KSystemArray
    val PaddedAs = (0 to Ks.length - 1)
      .map(j => PadWithZeroMatrices(Ks(j).A, j, Ks.length)) //pad each of the matrices on both sides
      .map(Aj => NegStackAMatrices(Aj)) //do this to get an equality constraint instead of inequality
    DenseMatrix.vertcat(PaddedAs: _*)
  }
  /*
  @param Vector the constraints upon each of the muscles as you move from one system to the next
  @param j The index of the current location of the imposed vector
  @param Kn the total width of the matrix
  @return M a densevector that has padded zeros on both sides, according to how far it is from the left, as described
   by the j and Kn.
   */
  def PadWithZeroVector(Vector: DenseVector[Double], j: Int, Kn: Int): DenseVector[Double]={
    if (j==0){
      DenseVector.vertcat(Vector, DenseVector.zeros[Double](Kn-Vector.length))
    } else if (j==Kn) {
      DenseVector.vertcat(DenseVector.zeros[Double](Kn-Vector.length), Vector)
    } else {
      val VecList = List(DenseVector.zeros[Double](j), Vector, DenseVector.zeros[Double](Kn - j - Vector.length))
      DenseVector.vertcat(VecList: _*)
    }
  }
  /*
  @param len the length of the desired vector
  @return V a vector of length len, with 1 as the first element, and -1 as the last element.
   */
  def PaddingPosNegOne(len: Int): DenseVector[Double] ={
    DenseVector.zeros[Double](len).update(i=0, 1). update(i=len-1, -1)
  }
}