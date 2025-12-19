package com.alonalbert.enphase.monitor.emporia

import io.ktor.client.plugins.auth.AuthProvider
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.auth.HttpAuthHeader
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest

private val HOSTS = mapOf("api.emporiaenergy.com" to "AuthToken", "c-api.emporiaenergy.com" to "Authorization")

class EmporiaAuthProvider : AuthProvider {
  private val tokensHolder = TokenHolder {
    getToken()
  }

  override fun isApplicable(auth: HttpAuthHeader): Boolean {
    return true
  }

  override suspend fun addRequestHeaders(request: HttpRequestBuilder, authHeader: HttpAuthHeader?) {
    val token = tokensHolder.loadToken() ?: return
    val header = HOSTS[request.url.host] ?: return
    request.headers.append(header, token)
  }

  override fun sendWithoutRequest(request: HttpRequestBuilder) = HOSTS.contains(request.url.host)

  override suspend fun refreshToken(response: HttpResponse): Boolean {
    val newToken = tokensHolder.setToken(::getToken)
    return newToken != null
  }

  @Deprecated("Please use sendWithoutRequest function instead", level = DeprecationLevel.ERROR)
  override val sendWithoutRequest: Boolean
    get() = TODO("Not yet implemented")
}

private fun getToken(): String {
  println("===================== Calling getToken()")
  val cognitoClient = CognitoIdentityProviderClient.builder()
    .region(Region.US_EAST_2)
    .credentialsProvider(AnonymousCredentialsProvider.create())
    .build()

  val authParameters = mapOf(
    "USERNAME" to "alon.albert+emporia@gmail.com",
    "PASSWORD" to "deerfield23342"
  )
  val authRequest1 = InitiateAuthRequest.builder()
    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
    .authParameters(authParameters)
    .clientId("4qte47jbstod8apnfic0bunmrq")
    .build()

  val response = cognitoClient.initiateAuth(authRequest1)

  return response.authenticationResult().idToken()
}
