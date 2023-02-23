package ru.netology.linkedin_network.api

import ru.netology.linkedin_network.auth.AuthState
import ru.netology.linkedin_network.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface   ApiService {
    //МРегистрация
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun signIn(
        @Field("login") login: String,
        @Field("password") pass: String
    ): Response<AuthState>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("password") pass: String,
        @Field("name") name: String
    ): Response<AuthState>

    @Multipart
    @POST("users/registration")
    suspend fun registerUserWithAvatar(
        @Part("login") login: RequestBody,
        @Part("password") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Field("file") file: MultipartBody.Part?
    ): Response<AuthState>

    @POST("users/push-tokens")
    suspend fun saveToken(@Body pushToken: Token): Response<Unit>

    //Медиа
    @Multipart
    @POST("media")
    suspend fun uploadMedia(@Part file: MultipartBody.Part?): Response<Media>
    //User
    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): Response<User>

    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>
    //Posts
    @POST("posts")
    suspend fun savePost(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removePostById(@Path("id") id: Int): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likePost(@Path("id") id: Int): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikePost(@Path("id") id: Int): Response<Post>

    @GET("posts/{post_id}")
    suspend fun getPostById(@Path("post_id") id: Int): Response<Post>

    @GET("posts/latest")
    suspend fun getPostLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getPostBefore(
        @Path("id") id: Int,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getPostAfter(
        @Path("id") id: Int,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts")
    suspend fun getAllPost(): Response<List<Post>>
    @GET("posts/{post_id}/newer")
    suspend fun getNewerPost(@Path("post_id") id: Int): Response<List<Post>>

    //Jobs
    @GET("{user_id}/jobs")
    suspend fun getUserJobs(
        @Path("user_id") id: Int
    ): Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{job_id}")
    suspend fun removeJobById(@Path("job_id") id: Int): Response<Unit>

    @GET("my/jobs")
    suspend fun getMyJobs(): Response<List<Job>>
    //Events
    @GET("events/latest")
    suspend fun getLatestEvents(@Query("count") count: Int): Response<List<Event>>

    @GET("events/{id}/before")
    suspend fun getBeforeEvents(
        @Path("id") id: Int,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{id}/after")
    suspend fun getAfterEvents(
        @Path("id") id: Int,
        @Query("count") count: Int
    ): Response<List<Event>>

    @DELETE("events/{id}")
    suspend fun removeById(@Path("id") id: Int): Response<Unit>

    @POST("events/{id}/likes")
    suspend fun likeEvent(@Path("id") id: Int): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun dislikeEvent(@Path("id") id: Int): Response<Event>

    @POST("events/{id}/participants")
    suspend fun join(@Path("id") id: Int): Response<Event>

    @DELETE("events/{id}/participants")
    suspend fun quitjoin(@Path("id") id: Int): Response<Event>

    @POST("events")
    suspend fun saveEvent(@Body post: Event): Response<Event>

    @GET("events/{event_id}")
    suspend fun getEventById(@Path("event_id") id: Int): Response<Event>
}