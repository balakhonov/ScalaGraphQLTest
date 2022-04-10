package com.balakhonov.controllers.decoders

import com.balakhonov.models.News
import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

object NewsDecoder {

  implicit val singleNewsEncoder: Encoder[News] = deriveEncoder[News]
  implicit val multipleNewsEncoder: Encoder[List[News]] = { (seq: List[News]) =>
    Json.arr(seq.map(singleNewsEncoder.apply): _*)
  }

  implicit def singleNewsEntityEncoder[F[_]]: EntityEncoder[F, News] = jsonEncoderOf

  implicit def multipleNewsEntityEncoder[F[_]]: EntityEncoder[F, List[News]] = jsonEncoderOf

}
