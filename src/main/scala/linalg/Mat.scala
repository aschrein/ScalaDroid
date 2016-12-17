package linalg
/**
  * Created by anuntiln on 12/8/2016.
  */

object Mat {
	def apply : Mat = new Mat ( 4, 4, new Array[ Float ]( 16 ) )
	def apply ( a : vec4, b : vec4, c : vec4, d : vec4 ) : Mat = new Mat ( 4, 4, Array (
		a.x, a.y, a.z, a.w,
		b.x, b.y, b.z, b.w,
		c.x, c.y, c.z, c.w,
		d.x, d.y, d.z, d.w
	) )
}
case class Mat ( N : Int, M : Int, m : Array[ Float ] ) {
	def EPS = 1.0e-4f
	def apply ( i : Int, j : Int ) : Float = m ( i * M + j )
	def row ( i : Int ) : Vec = {
		val arr = new Array[ Float ]( N )
		for ( j <- 0 until M ) {
			arr ( j ) = m ( i * M + j )
		}
		new Vec ( arr )
	}
	def col ( j : Int ) : Vec = {
		val arr = new Array[ Float ]( N )
		for ( i <- 0 until N ) {
			arr ( i ) = m ( i * M + j )
		}
		new Vec ( arr )
	}
	def array : Array[ Float ] = m
	def chechSize[ T ] ( a : Vec )( f : => T ) : T = if ( M == a.N ) f
	else {
		throw new Exception ( "different vecuntilr and matrix sizes" )
	}
	def chechSize[ T ] ( a : Mat )( f : => T ) : T = if ( M == a.N ) f
	else {
		throw new Exception ( "different vecuntilr and matrix sizes" )
	}
	def * ( a : Vec ) = chechSize ( a ) {
		val arr = new Array[ Float ]( N )
		for ( i <- 0 until N ) {
			arr ( i ) = row ( i ) * a
		}
		new Vec ( arr )
	}
	def T = {
		val arr = new Array[ Float ]( N * M )
		for ( i <- 0 until N ) {
			for ( j <- 0 until M ) {
				arr ( j * N + i ) = this ( i, j )
			}
		}
		new Mat ( M, N, arr )
	}
	def * ( a : Mat ) = chechSize ( a ) {
		val arr = new Array[ Float ]( N * a.M )
		val c = ( a T )
		for ( i <- 0 until N ) {
			for ( j <- 0 until a.M ) {
				var acc = 0.0f
				for ( k <- 0 until M ) {
					acc += this ( i, k ) * c ( j, k )
				}
				arr ( i * N + j ) = acc
			}
		}
		new Mat ( N, a.M, arr )
	}
	def print ( ) = {
		for ( i <- 0 until N ) {
			for ( j <- 0 until M ) {
				printf ( "%f ", this ( i, j ) )
			}
			println ( )
		}
		println ( "___________________" )
	}
	def identity = N == M && m.zipWithIndex.forall ( pair =>
		(( pair._2 % N ) == pair._2 / N && (( pair._1 - 1.0f ).abs < EPS)) || ( ( pair._2 % N ) != pair._2 / N && pair._1.abs < EPS )
	)
	def inv =
		if ( N == M ) {
			val out = new Array[ Float ]( 2 * N * N )
			def fr ( i : Int, j : Int )( x : Float ) = out ( i * 2 * N + j ) = x
			def inv ( i : Int, j : Int )( x : Float ) = out ( i * 2 * N + j + N ) = x
			def getfr ( i : Int, j : Int ) = out ( i * 2 * N + j )
			def getinv ( i : Int, j : Int ) = out ( i * 2 * N + j + N )
			def swaprow ( i1 : Int, i2 : Int ) = {
				for ( i <- 0 until 2 * N ) {
					val tmp = out ( i1 * N * 2 + i )
					out ( i1 * N * 2 + i ) = out ( i2 * N * 2 + i )
					out ( i2 * N * 2 + i ) = tmp
				}
			}
			for ( i <- 0 until N ) {
				inv ( i, i )( 1 )
			}
			for ( i <- 0 until N ) {
				for ( j <- 0 until N ) {
					fr ( i, j )( m ( i * N + j ) )
				}
			}
			for ( n <- 0 until N ) {
				//new Mat( N , N * 2 , out).print()
				var ann = getfr ( n, n )
				var annmod = ann.abs
				if ( annmod < EPS ) {
					var new_index = n
					for ( k <- n + 1 until N ) {
						val akk = getfr ( k, n )
						val akkmod = akk.abs
						if ( akkmod > annmod ) {
							ann = akk
							annmod = akkmod
							new_index = k
						}
					}
					if ( new_index != n ) {
						swaprow ( n, new_index )
					} else {
						throw new Exception ( "matrix is not invertible" )
					}
				}
				val anninv = 1.0f / ann
				for ( j <- n until 2 * N ) {
					out ( n * N * 2 + j ) *= anninv
				}
				for ( i <- n + 1 until N ) {
					val ain = out ( i * N * 2 + n )
					for ( j <- n until 2 * N ) {
						out ( i * N * 2 + j ) -= out ( n * N * 2 + j ) * ain
					}
				}
			}
			for ( n <- 0 until N reverse ) {
				for ( i <- 0 until n ) {
					val ain = out ( i * N * 2 + n )
					for ( j <- N until N * 2 ) {
						out ( i * N * 2 + j ) -= out ( n * N * 2 + j ) * ain
					}
				}
			}
			val swap = new Array[ Float ]( N * N )
			for ( i <- 0 until N ) {
				for ( j <- 0 until N ) {
					swap ( i * N + j ) = out ( i * N * 2 + j + N )
				}
			}
			new Mat ( N, N, swap )
		} else throw new Exception ( "matrix is not square" )
}
