import os
import json
from flask import Flask, request, jsonify, send_from_directory
from werkzeug.utils import secure_filename
import database
from dotenv import load_dotenv

# Load local environment variables from .env file if it exists
load_dotenv()

app = Flask(__name__)

# Configure upload directory for local fallback
UPLOAD_FOLDER = os.path.join(os.path.dirname(__file__), 'uploads')
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Configure Cloudinary if environment variable is set
CLOUDINARY_URL = os.getenv("CLOUDINARY_URL")
if CLOUDINARY_URL:
    try:
        import cloudinary
        import cloudinary.uploader
        # Cloudinary automatically configures itself using the CLOUDINARY_URL env var
    except ImportError:
        print("[WARNING] Cloudinary package is not installed. Will fall back to local file storage.")
        CLOUDINARY_URL = None

# Ensure database is initialized
database.init_db()

def make_basic_response(status, message=None):
    resp = {"status": status}
    if message:
        resp["message"] = message
    return jsonify(resp)

# Static files route for uploaded images (local fallback)
@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)

# Root route to serve the web portal
@app.route('/')
def home_portal():
    return send_from_directory(os.path.join(app.root_path, 'static'), 'index.html')

# --- Authentication routes ---

@app.route('/register.php', methods=['POST'])
@app.route('/oct/spic_726/hairjourney/register.php', methods=['POST'])
def register():
    data = request.get_json(silent=True)
    if not data:
        return make_basic_response("error", "Invalid JSON body")
        
    name = data.get("name")
    email = data.get("email")
    phone = data.get("phone")
    place = data.get("place")
    dob = data.get("dob")
    role = data.get("role", "user")
    password = data.get("password")

    if not all([name, email, phone, place, dob, password]):
        return make_basic_response("error", "Missing required fields")

    success, message = database.register_user(name, email, phone, place, dob, role, password)
    if success:
        return make_basic_response("success", message)
    else:
        return make_basic_response("error", message)

@app.route('/send_otp.php', methods=['POST'])
@app.route('/oct/spic_726/hairjourney/send_otp.php', methods=['POST'])
def send_otp():
    import random
    data = request.get_json(silent=True)
    if not data:
        return jsonify({"status": "error", "message": "Invalid JSON body"})
        
    phone = data.get("phone")
    if not phone:
        return jsonify({"status": "error", "message": "Missing phone parameter"})

    otp = str(random.randint(100000, 999999))
    print(f"[OTP LOG] Generated OTP {otp} for phone {phone}")
    
    return jsonify({
        "status": "success",
        "otp": otp,
        "message": f"OTP successfully generated and sent to {phone}"
    })

@app.route('/login.php', methods=['POST'])
@app.route('/oct/spic_726/hairjourney/login.php', methods=['POST'])
def login():
    data = request.get_json(silent=True)
    if not data:
        return jsonify({"status": "error", "message": "Invalid JSON body"})

    email = data.get("email")
    password = data.get("password")

    if not email or not password:
        return jsonify({"status": "error", "message": "Missing email or password"})

    user = database.login_user(email, password)
    if user:
        # Convert user ID to string as expected by Android User model
        user['id'] = str(user['id'])
        # Exclude password hash from response
        user.pop('password', None)
        return jsonify({
            "status": "success",
            "message": "Login successful",
            "user": user
        })
    else:
        return jsonify({
            "status": "error",
            "message": "Invalid email or password"
        })

@app.route('/get_profile.php', methods=['GET'])
@app.route('/oct/spic_726/hairjourney/get_profile.php', methods=['GET'])
def get_profile():
    user_id = request.args.get("user_id")
    if not user_id:
        return make_basic_response("error", "user_id parameter is required")
        
    user = database.get_user_profile(user_id)
    if user:
        user['id'] = str(user['id'])
        return jsonify({
            "status": "success",
            "message": "Profile retrieved successfully",
            "user": user
        })
    else:
        return make_basic_response("error", "User not found")


# --- Slots management ---

@app.route('/create_slot.php', methods=['POST'])
@app.route('/oct/spic_726/hairjourney/create_slot.php', methods=['POST'])
def create_slot():
    data = request.get_json(silent=True)
    if not data:
        return make_basic_response("error", "Invalid JSON body")
        
    doctor_id = data.get("doctor_id")
    date = data.get("date")
    time = data.get("time")

    if not all([doctor_id, date, time]):
        return make_basic_response("error", "Missing required fields")

    success = database.add_slot(int(doctor_id), date, time)
    if success:
        return make_basic_response("success", "Slot created successfully")
    else:
        return make_basic_response("error", "Failed to create slot")

@app.route('/get_slots.php', methods=['GET'])
@app.route('/oct/spic_726/hairjourney/get_slots.php', methods=['GET'])
def get_slots():
    doctor_id = request.args.get("doctor_id")
    if doctor_id:
        slots = database.get_doctor_slots(int(doctor_id))
    else:
        slots = database.get_available_slots()
        
    # Convert integer IDs to strings for Retrofit consistency if needed
    for s in slots:
        s['id'] = str(s['id'])
        s['doctor_id'] = str(s['doctor_id'])
        
    return jsonify({
        "status": "success",
        "slots": slots
    })


# --- Appointment routes ---

@app.route('/book_appointment.php', methods=['POST'])
@app.route('/oct/spic_726/hairjourney/book_appointment.php', methods=['POST'])
def book_appointment():
    data = request.get_json(silent=True)
    if not data:
        return make_basic_response("error", "Invalid JSON")
        
    user_id = data.get("user_id")
    doctor_id = data.get("doctor_id")
    slot_id = data.get("slot_id")

    if not all([user_id, doctor_id, slot_id]):
        return make_basic_response("error", "Missing required fields")

    success, message = database.book_appointment(int(user_id), int(doctor_id), int(slot_id))
    if success:
        return make_basic_response("success", message)
    else:
        return make_basic_response("error", message)

@app.route('/get_appointments.php', methods=['GET'])
@app.route('/oct/spic_726/hairjourney/get_appointments.php', methods=['GET'])
def get_appointments():
    user_id = request.args.get("user_id")
    doctor_id = request.args.get("doctor_id")
    
    if user_id:
        appointments = database.get_user_appointments(int(user_id))
    elif doctor_id:
        appointments = database.get_doctor_appointments(int(doctor_id))
    else:
        return make_basic_response("error", "Either user_id or doctor_id is required")
        
    for a in appointments:
        a['id'] = str(a['id'])
        
    return jsonify({
        "status": "success",
        "appointments": appointments
    })

@app.route('/update_appointment_status.php', methods=['POST'])
@app.route('/oct/spic_726/hairjourney/update_appointment_status.php', methods=['POST'])
def update_appointment_status():
    data = request.get_json(silent=True)
    if not data:
        return make_basic_response("error", "Invalid JSON")
        
    appointment_id = data.get("appointment_id")
    status = data.get("status") # 'approved' or 'rejected'

    if not appointment_id or not status:
        return make_basic_response("error", "Missing fields")

    success = database.update_appointment_status(int(appointment_id), status)
    if success:
        return make_basic_response("success", "Appointment status updated")
    else:
        return make_basic_response("error", "Failed to update status")


# --- Hair images (clinical photos) ---

@app.route('/upload_hair_image.php', methods=['POST'])
@app.route('/oct/spic_726/hairjourney/upload_hair_image.php', methods=['POST'])
def upload_hair_image():
    # In multipart requests, form parameters are inside request.form
    user_id = request.form.get("user_id")
    progress_tag = request.form.get("progress_tag")
    
    if 'image' not in request.files:
        return make_basic_response("error", "No image file provided")
        
    file = request.files['image']
    if file.filename == '':
        return make_basic_response("error", "Empty filename")

    if not user_id or not progress_tag:
        return make_basic_response("error", "Missing user_id or progress_tag")

    if CLOUDINARY_URL:
        try:
            # Upload directly to Cloudinary
            clean_filename = secure_filename(os.path.splitext(file.filename)[0])
            upload_result = cloudinary.uploader.upload(
                file,
                folder="rooyify_hair_journey",
                public_id=f"user_{user_id}_{progress_tag}_{clean_filename}"
            )
            # Use the secure URL from Cloudinary
            image_path = upload_result.get("secure_url")
            
            success = database.save_hair_image(int(user_id), progress_tag, image_path)
            if success:
                return make_basic_response("success", "Hair image uploaded to cloud successfully")
            else:
                return make_basic_response("error", "Failed to save cloud image path to database")
        except Exception as e:
            return make_basic_response("error", f"Cloudinary upload failed: {e}")
    else:
        # Fallback to local storage
        filename = secure_filename(f"user_{user_id}_{progress_tag}_{file.filename}")
        filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(filepath)
        
        # Store relative path (url path) in database
        relative_url = f"/uploads/{filename}"
        
        success = database.save_hair_image(int(user_id), progress_tag, relative_url)
        if success:
            return make_basic_response("success", "Hair image uploaded locally successfully")
        else:
            return make_basic_response("error", "Failed to save local path to database")

@app.route('/get_hair_images.php', methods=['GET'])
@app.route('/oct/spic_726/hairjourney/get_hair_images.php', methods=['GET'])
def get_hair_images():
    user_id = request.args.get("user_id")
    if user_id:
        images = database.get_user_hair_images(int(user_id))
    else:
        images = database.get_all_patient_hair_images()
        
    for img in images:
        img['id'] = str(img['id'])
        img['user_id'] = str(img['user_id'])
        
        # Build full URL path depending on storage type
        if img['image_path'].startswith("http://") or img['image_path'].startswith("https://"):
            img['image_url'] = img['image_path']
        else:
            img['image_url'] = f"http://{request.host}{img['image_path']}"
        
    return jsonify({
        "status": "success",
        "images": images
    })

@app.route('/save_hair_remark.php', methods=['POST'])
@app.route('/oct/spic_726/hairjourney/save_hair_remark.php', methods=['POST'])
def save_hair_remark():
    data = request.get_json(silent=True)
    if not data:
        return make_basic_response("error", "Invalid JSON")
        
    image_id = data.get("image_id")
    remark = data.get("remark")

    if not image_id or remark is None:
        return make_basic_response("error", "Missing fields")

    success = database.update_hair_remark(int(image_id), remark)
    if success:
        return make_basic_response("success", "Remark saved successfully")
    else:
        return make_basic_response("error", "Failed to save remark")


# --- Quiz reports ---

@app.route('/submit_quiz.php', methods=['POST'])
@app.route('/oct/spic_726/hairjourney/submit_quiz.php', methods=['POST'])
def submit_quiz():
    data = request.get_json(silent=True)
    if not data:
        return make_basic_response("error", "Invalid JSON")
        
    user_id = data.get("user_id")
    risk_level = data.get("risk_level")
    answers = data.get("answers") # Expecting a JSON dict or string

    if not user_id or not risk_level or not answers:
        return make_basic_response("error", "Missing required fields")

    answers_str = json.dumps(answers) if isinstance(answers, dict) else str(answers)

    success = database.save_quiz_report(int(user_id), risk_level, answers_str)
    if success:
        return make_basic_response("success", "Quiz submitted successfully")
    else:
        return make_basic_response("error", "Failed to save quiz report")

@app.route('/get_quiz_reports.php', methods=['GET'])
@app.route('/oct/spic_726/hairjourney/get_quiz_reports.php', methods=['GET'])
def get_quiz_reports():
    user_id = request.args.get("user_id")
    if user_id:
        reports = database.get_user_quiz_reports(int(user_id))
    else:
        reports = database.get_all_patient_quiz_reports()
        
    for r in reports:
        r['id'] = str(r['id'])
        r['user_id'] = str(r['user_id'])
            
    return jsonify({
        "status": "success",
        "reports": reports
    })

# --- Helper to list doctors ---
@app.route('/get_doctors.php', methods=['GET'])
@app.route('/oct/spic_726/hairjourney/get_doctors.php', methods=['GET'])
def get_doctors():
    docs = database.get_doctors()
    for d in docs:
        d['id'] = str(d['id'])
    return jsonify({
        "status": "success",
        "doctors": docs
    })


# --- Routines Endpoints ---

@app.route('/get_routines.php', methods=['GET'])
@app.route('/oct/spic_726/hairjourney/get_routines.php', methods=['GET'])
def get_routines():
    user_id = request.args.get("user_id")
    if not user_id:
        return make_basic_response("error", "Missing user_id")
    
    routines = database.get_user_routines(int(user_id))
    for r in routines:
        r['id'] = str(r['id'])
        r['user_id'] = str(r['user_id'])
        
    return jsonify({
        "status": "success",
        "routines": routines
    })

@app.route('/add_routine.php', methods=['POST'])
@app.route('/oct/spic_726/hairjourney/add_routine.php', methods=['POST'])
def add_routine():
    data = request.get_json(silent=True)
    if not data:
        return make_basic_response("error", "Invalid JSON")
        
    user_id = data.get("user_id")
    name = data.get("name")
    dosage = data.get("dosage", "")
    frequency = data.get("frequency", "")
    time = data.get("time", "")
    
    if not user_id or not name:
        return make_basic_response("error", "Missing required fields (user_id, name)")
        
    success = database.add_routine(int(user_id), name, dosage, frequency, time)
    if success:
        return make_basic_response("success", "Routine added successfully")
    else:
        return make_basic_response("error", "Failed to add routine")

@app.route('/delete_routine.php', methods=['POST'])
@app.route('/oct/spic_726/hairjourney/delete_routine.php', methods=['POST'])
def delete_routine():
    data = request.get_json(silent=True)
    if not data:
        return make_basic_response("error", "Invalid JSON")
        
    routine_id = data.get("routine_id")
    if not routine_id:
        return make_basic_response("error", "Missing routine_id")
        
    success = database.delete_routine(int(routine_id))
    if success:
        return make_basic_response("success", "Routine deleted successfully")
    else:
        return make_basic_response("error", "Failed to delete routine")


@app.route('/get_patients.php', methods=['GET'])
@app.route('/oct/spic_726/hairjourney/get_patients.php', methods=['GET'])
def get_patients():
    pats = database.get_patients()
    for p in pats:
        p['id'] = str(p['id'])
    return jsonify({
        "status": "success",
        "patients": pats
    })


if __name__ == '__main__':
    # Listen on all interfaces so emulator and network devices can reach it
    app.run(host='0.0.0.0', port=5000, debug=True)
