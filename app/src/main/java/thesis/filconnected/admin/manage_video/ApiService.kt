package thesis.filconnected.FastApi

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("upload/")
    @Multipart
    fun uploadVideo(@Part file: MultipartBody.Part): Call<UploadResponse>

    @GET("list-videos/")
    fun listVideos(): Call<ListVideosResponse>

    @PUT("edit-videos/{old_name}")
    fun renameVideo(
        @Path("old_name") oldName: String,
        @Body renameRequest: RenameRequest
    ): Call<RenameResponse>

    @DELETE("delete-videos/{filename}")
    fun deleteVideo(@Path("filename") filename: String): Call<DeleteResponse>

    @GET("videos/{filename}")
    fun getVideo(@Path("filename") filename: String): Call<GetVideoResponse>
}

data class UploadResponse(
    val filename: String,
    val message: String,
    val url: String
)

data class ListVideosResponse(
    val videos: List<String>
)

data class RenameRequest(
    val new_name: String
)

data class RenameResponse(
    val message: String,
    val new_filename: String
)

data class DeleteResponse(
    val message: String
)

data class GetVideoResponse(
    val url: String
)