package ujson.argonaut

import argonaut.{Json, JsonNumber, JsonObject}
import ujson.{ArrVisitor, ObjVisitor, Visitor}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object ArgonautJson extends ujson.AstTransformer[Json]{
  override def transform[T](j: Json, f: Visitor[_, T]) = j.fold(
    f.visitNull(),
    if (_) f.visitTrue() else f.visitFalse(),
    n => n.toDouble match{
      case Some(d) => f.visitNumRaw(d, -1)
      case None => f.visitNumRawString(n.asJson.toString(), -1)
    },
    f.visitString(_),
    arr => transformArray(f, arr),
    obj => transformObject(f, obj.toList)
  )

  def visitArray(index: Int) = new AstArrVisitor[List](xs => Json.jArray(xs))
  def visitObject(index: Int) = new AstObjVisitor[ArrayBuffer[(String, Json)]](
    vs => Json.jObject(JsonObject.fromTraversableOnce(vs))
  )

  def visitNull(index: Int) = Json.jNull

  def visitFalse(index: Int) = Json.jFalse

  def visitTrue(index: Int) = Json.jTrue

  def visitNum(s: CharSequence, decIndex: Int, expIndex: Int, index: Int) = {
    Json.jNumber(JsonNumber.unsafeDecimal(s.toString))
  }

  def visitString(s: CharSequence, index: Int) = Json.jString(s.toString)
}