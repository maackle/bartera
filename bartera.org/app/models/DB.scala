package models

import org.squeryl.{PrimitiveTypeMode, KeyedEntity, Table, Schema}
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._

object DB extends Schema {

	val users = table[models.User]
	val items = table[models.Item]

	val userItems = oneToManyRelation(users, items).via((u, i) => u.id === i.user_id)

//	def q(query: String, args: Any*) = new RawTupleQuery(query, args)
}


trait IdPK extends KeyedEntity[Long] {
	var id:Long = 0
}


trait MetaModel[T] extends PrimitiveTypeMode {
	val table:Table[T]

}


//class RawTupleQuery(query: String, args: Seq[Any]) {
//
//	private def prep = {
//		// We'll pretend we don't care about connection, statement, resultSet leaks for now ...
//		val s = Session.currentSession
//
//		val st = s.connection.prepareStatement(query)
//
//		for(z <- args.zipWithIndex)
//			st.setObject(z._2 + 1, z._1.asInstanceOf[AnyRef])
//		st
//	}
//
//	def toSeq[A1]()(implicit f1 : TypedExpressionFactory[A1,_]) = {
//
//		val st = prep
//		val rs = st.executeQuery
//
//		try {
//
//			val ab = new ArrayBuffer[A1]
//
//			val m1 = f1.thisMapper.asInstanceOf[PrimitiveJdbcMapper[A1]]
//
//			while(rs.next)
//				ab.append(m1.convertFromJdbc(m1.extractNativeJdbcValue(rs, 1)))
//
//			ab
//		}
//		finally {
//			rs.close
//			st.close
//		}
//	}
//
//	def toTupleSeq[A1,A2]()(implicit f1 : TypedExpressionFactory[A1,_], f2 : TypedExpressionFactory[A2,_]) = {
//
//		val st = prep
//		val rs = st.executeQuery
//
//		try {
//
//			val ab = new ArrayBuffer[(A1,A2)]
//
//			val m1 = f1.thisMapper.asInstanceOf[PrimitiveJdbcMapper[A1]]
//			val m2 = f2.thisMapper.asInstanceOf[PrimitiveJdbcMapper[A2]]
//
//			while(rs.next)
//				ab.append(
//					(m1.convertFromJdbc(m1.extractNativeJdbcValue(rs, 1)),
//						m2.convertFromJdbc(m2.extractNativeJdbcValue(rs, 2))))
//			ab
//		}
//		finally {
//			rs.close
//			st.close
//		}
//	}
//}
