package com.example.rooyify.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @POST("login.php")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @POST("register.php")
    fun registerUser(@Body request: RegisterRequest): Call<BasicResponse>

    @POST("send_otp.php")
    fun sendOtp(@Body request: SendOtpRequest): Call<OtpResponse>

    @POST("book_appointment.php")
    fun bookAppointment(@Body request: BookAppointmentRequest): Call<BasicResponse>

    @Multipart
    @POST("upload_hair_image.php")
    fun uploadHairImage(
        @Part("user_id") userId: RequestBody,
        @Part("progress_tag") progressTag: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<BasicResponse>

    // Profile Retrieval
    @GET("get_profile.php")
    fun getProfile(@Query("user_id") userId: String): Call<LoginResponse>

    // Slots
    @POST("create_slot.php")
    fun createSlot(@Body request: CreateSlotRequest): Call<BasicResponse>

    @GET("get_slots.php")
    fun getSlots(@Query("doctor_id") doctorId: String? = null): Call<SlotsResponse>

    // Appointments
    @GET("get_appointments.php")
    fun getAppointments(
        @Query("user_id") userId: String? = null,
        @Query("doctor_id") doctorId: String? = null
    ): Call<AppointmentsResponse>

    @POST("update_appointment_status.php")
    fun updateAppointmentStatus(@Body request: UpdateAppointmentStatusRequest): Call<BasicResponse>

    // Quiz
    @POST("submit_quiz.php")
    fun submitQuiz(@Body request: SubmitQuizRequest): Call<BasicResponse>

    @GET("get_quiz_reports.php")
    fun getQuizReports(@Query("user_id") userId: String? = null): Call<QuizReportsResponse>

    // Hair Images
    @GET("get_hair_images.php")
    fun getHairImages(@Query("user_id") userId: String? = null): Call<HairImagesResponse>

    @POST("save_hair_remark.php")
    fun saveHairRemark(@Body request: SaveHairRemarkRequest): Call<BasicResponse>

    // Fetch doctors
    @GET("get_doctors.php")
    fun getDoctors(): Call<DoctorsResponse>

    // Routines
    @GET("get_routines.php")
    fun getRoutines(@Query("user_id") userId: String): Call<RoutinesResponse>

    @POST("add_routine.php")
    fun addRoutine(@Body request: AddRoutineRequest): Call<BasicResponse>

    @POST("delete_routine.php")
    fun deleteRoutine(@Body request: DeleteRoutineRequest): Call<BasicResponse>

    @GET("get_patients.php")
    fun getPatients(): Call<PatientsResponse>
}
