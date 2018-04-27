package com.chat.server

import java.util.UUID

import com.twitter.finagle.Http
import com.twitter.util.Await
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import io.circe.generic.auto._
import com.twitter.util.Future

object ChatServer {

  case class ChatRequest(correlationID: String, message: String)

  case class ChatResponse(correlationID: String, displayText: String)

  def main(args: Array[String]): Unit = {

    trait InitResponse
    case class ChatInitResponse(correlationID: String, message: String) extends InitResponse
    case class InvalidResponse(error: String) extends InitResponse

    val chatInitHeader: Endpoint[String] =
      header("correlationID").mapOutputAsync(id => {
        if (id == null) Future.value(Ok("Invalid correlationID"))
        else Future.value(Ok(UUID.randomUUID().toString))
      })

    val chatInit: Endpoint[ChatInitResponse] = get("init" :: header("x-correlation-id") :: header("x-version")) { (id: String, version: String) =>
      Ok(ChatInitResponse(id, "Hi, How can I help you?"))
        .withHeader("version", version)
    }

    val chat: Endpoint[ChatResponse] =
      post("chat"
        :: header("x-user")
        :: header("x-client-version")
        :: jsonBody[ChatRequest]) {
        (user: String, version: String, chatRequest: ChatRequest) =>

        Ok(ChatPipeline.pipeline(chatRequest))
      }

    val endpoints = (chatInit :+: chat).toService

    Await.ready(Http.server.serve(":9090", endpoints))

  }
}
