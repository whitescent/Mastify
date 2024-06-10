package com.github.whitescent.mastify.core.network

import com.github.whitescent.mastify.core.common.cast
import com.github.whitescent.mastify.core.common.debug
import com.github.whitescent.mastify.core.network.exception.MastodonErrorException
import com.github.whitescent.mastify.core.network.exception.NetworkException
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpResponseContainer
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.serializerForTypeInfo
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.platformType
import io.ktor.util.reflect.typeInfo
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

fun networkInterceptor(json: Json) = createClientPlugin("NetworkResultInterceptor") {
  client.responsePipeline.intercept(HttpResponsePipeline.Transform) {
    val (typeInfo, content) = subject
    if (typeInfo.type == NetworkResult::class) {
      val expectedType = typeInfo.argumentTypeInfo()
      val newContent = transform<Any?>(
        response = context.response,
        content = content,
        expectedType = expectedType,
        json = json
      )
      if (!typeInfo.type.isInstance(newContent)) {
        error("networkResultInterceptor returned $newContent but expected value of type $typeInfo")
      }
      proceedWith(HttpResponseContainer(typeInfo, newContent))
    }
  }
}

internal fun TypeInfo.argumentTypeInfo(): TypeInfo {
  val elementType = kotlinType!!.arguments[0].type!!
  return TypeInfo(
    type = elementType.classifier.cast<KClass<*>>(),
    reifiedType = elementType.platformType,
    kotlinType = elementType
  )
}

internal suspend fun <T> transform(
  response: HttpResponse,
  content: Any,
  expectedType: TypeInfo,
  json: Json,
): NetworkResult<T> {
  fun serializer() = json.serializersModule.serializerForTypeInfo(expectedType)

  debug("NetworkResultInterceptor") {
    "Transform: response=$response \n expectedType=$expectedType \n content=$content"
  }

  val status = response.status
  val statusCode = status.value
  val isSuccess = status.isSuccess()
  val text = content.cast<ByteReadChannel>().readRemaining().readText()

  val resultJson = json.parseToJsonElement(text)

  return when {
    isSuccess -> NetworkResult.success(json.decodeFromJsonElement(serializer(), resultJson))
    else -> {
      val errorSerializer = json.serializersModule.serializerForTypeInfo(typeInfo<MastodonErrorException>())
      val errorData = json.decodeFromJsonElement(errorSerializer, resultJson).cast<MastodonErrorException>()
      NetworkResult.failure<T>(
        NetworkException(
          errorCode = statusCode,
          message = errorData.error,
          response = text
        )
      )
    }
  }
}
