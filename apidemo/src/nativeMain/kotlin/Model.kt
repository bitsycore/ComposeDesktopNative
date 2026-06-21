package apidemo

import kotlinx.serialization.Serializable

// ==================
// MARK: Pack model (serialized to / from .json)
// ==================

/* A "pack" is a savable collection of requests — the export/import unit. */
@Serializable
data class Pack(
    val name: String = "My Pack",
    val requests: List<ApiRequest> = listOf(
        ApiRequest(name = "Get IP", url = "https://httpbin.org/get"),
    ),
)

@Serializable
data class ApiRequest(
    val name: String = "New request",
    val method: ReqMethod = ReqMethod.GET,
    val url: String = "https://",
    val params: List<KeyVal> = emptyList(),
    val headers: List<KeyVal> = emptyList(),
    val bodyType: BodyType = BodyType.NONE,
    val body: String = "",
)

/* A toggleable key/value row, used for both query params and headers. */
@Serializable
data class KeyVal(val key: String = "", val value: String = "", val enabled: Boolean = true)

@Serializable
enum class ReqMethod { GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS }

/* Whether a method conventionally carries a request body. */
val ReqMethod.allowsBody: Boolean
    get() = this == ReqMethod.POST || this == ReqMethod.PUT ||
        this == ReqMethod.PATCH || this == ReqMethod.DELETE

@Serializable
enum class BodyType { NONE, JSON, TEXT, FORM }

// ==================
// MARK: Response (runtime only — not part of a pack)
// ==================

class ApiResponse(
    val ok: Boolean,
    val status: Int,
    val statusText: String,
    val timeMs: Long,
    val sizeBytes: Long,
    val headers: List<Pair<String, String>>,
    val body: String,
    val error: String? = null,
)
