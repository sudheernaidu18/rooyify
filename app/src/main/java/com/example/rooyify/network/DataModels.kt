package com.example.rooyify.network

import com.google.gson.annotations.SerializedName

// Common Response expected from all endpoints: {"status": "success"} or {"status": "error", "message": "..."}
data class BasicResponse(
    val status: String,
    val message: String? = null
)

// Login
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val message: String? = null,
    val user: User? = null
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val place: String,
    val dob: String,
    val role: String
)

// Registration
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val place: String,
    val dob: String,
    val role: String,
    val password: String
)

// Appointment
data class BookAppointmentRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("doctor_id") val doctorId: String,
    @SerializedName("slot_id") val slotId: String
)

// Slots
data class CreateSlotRequest(
    @SerializedName("doctor_id") val doctorId: String,
    val date: String,
    val time: String
)

data class Slot(
    val id: String,
    @SerializedName("doctor_id") val doctorId: String,
    val date: String,
    val time: String,
    val status: String,
    @SerializedName("doctor_name") val doctorName: String? = null
)

data class SlotsResponse(
    val status: String,
    val slots: List<Slot>? = null
)

// Appointments
data class Appointment(
    val id: String,
    val status: String,
    val date: String,
    val time: String,
    @SerializedName("doctor_name") val doctorName: String? = null,
    @SerializedName("patient_name") val patientName: String? = null
)

data class AppointmentsResponse(
    val status: String,
    val appointments: List<Appointment>? = null
)

data class UpdateAppointmentStatusRequest(
    @SerializedName("appointment_id") val appointmentId: String,
    val status: String
)

// Quiz
data class SubmitQuizRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("risk_level") val riskLevel: String,
    val answers: String
)

data class QuizReport(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("risk_level") val riskLevel: String,
    val answers: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("patient_name") val patientName: String? = null
)

data class QuizReportsResponse(
    val status: String,
    val reports: List<QuizReport>? = null
)

// Hair Images
data class HairImage(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("progress_tag") val progressTag: String,
    @SerializedName("image_path") val imagePath: String,
    @SerializedName("image_url") val imageUrl: String? = null,
    val remark: String? = null,
    @SerializedName("uploaded_at") val uploadedAt: String,
    @SerializedName("patient_name") val patientName: String? = null
)

data class HairImagesResponse(
    val status: String,
    val images: List<HairImage>? = null
)

data class SaveHairRemarkRequest(
    @SerializedName("image_id") val imageId: String,
    val remark: String
)

// Doctors list
data class DoctorsResponse(
    val status: String,
    val doctors: List<User>? = null
)

// Routines Models
data class Routine(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    val time: String,
    @SerializedName("created_at") val createdAt: String
)

data class RoutinesResponse(
    val status: String,
    val routines: List<Routine>? = null
)

data class AddRoutineRequest(
    @SerializedName("user_id") val userId: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    val time: String
)

data class DeleteRoutineRequest(
    @SerializedName("routine_id") val routineId: String
)

data class PatientsResponse(
    val status: String,
    val patients: List<User>? = null
)



