import os
import sqlite3
from werkzeug.security import generate_password_hash, check_password_hash

# Try importing psycopg2 for production PostgreSQL support
try:
    import psycopg2
    from psycopg2.extras import RealDictCursor
except ImportError:
    psycopg2 = None
    RealDictCursor = None

DATABASE_URL = os.getenv("DATABASE_URL")

def get_db_connection():
    if DATABASE_URL:
        if not psycopg2:
            raise ImportError("psycopg2 is required when DATABASE_URL is set but is not installed.")
        conn = psycopg2.connect(DATABASE_URL)
        return conn
    else:
        DB_PATH = os.path.join(os.path.dirname(__file__), 'rooyify.db')
        conn = sqlite3.connect(DB_PATH)
        conn.row_factory = sqlite3.Row
        return conn

def get_cursor(conn):
    if DATABASE_URL:
        return conn.cursor(cursor_factory=RealDictCursor)
    else:
        return conn.cursor()

def db_execute(cursor, query, params=None):
    if DATABASE_URL:
        # Convert SQLite placeholders (?) to PostgreSQL placeholders (%s)
        query = query.replace('?', '%s')
        # Convert AUTOINCREMENT to PostgreSQL SERIAL
        query = query.replace('INTEGER PRIMARY KEY AUTOINCREMENT', 'SERIAL PRIMARY KEY')
    if params is not None:
        cursor.execute(query, params)
    else:
        cursor.execute(query)

def init_db():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    
    # Enable foreign keys (SQLite specific)
    if not DATABASE_URL:
        cursor.execute("PRAGMA foreign_keys = ON;")

    # Users table
    db_execute(cursor, '''
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            email TEXT NOT NULL UNIQUE,
            phone TEXT NOT NULL,
            place TEXT NOT NULL,
            dob TEXT NOT NULL,
            role TEXT NOT NULL,
            password TEXT NOT NULL
        )
    ''')

    # Slots table
    db_execute(cursor, '''
        CREATE TABLE IF NOT EXISTS slots (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            doctor_id INTEGER NOT NULL,
            date TEXT NOT NULL,
            time TEXT NOT NULL,
            status TEXT NOT NULL DEFAULT 'available',
            FOREIGN KEY (doctor_id) REFERENCES users (id) ON DELETE CASCADE
        )
    ''')

    # Appointments table
    db_execute(cursor, '''
        CREATE TABLE IF NOT EXISTS appointments (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            doctor_id INTEGER NOT NULL,
            slot_id INTEGER NOT NULL,
            status TEXT NOT NULL DEFAULT 'pending',
            FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
            FOREIGN KEY (doctor_id) REFERENCES users (id) ON DELETE CASCADE,
            FOREIGN KEY (slot_id) REFERENCES slots (id) ON DELETE CASCADE
        )
    ''')

    # Hair images table
    db_execute(cursor, '''
        CREATE TABLE IF NOT EXISTS hair_images (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            progress_tag TEXT NOT NULL,
            image_path TEXT NOT NULL,
            remark TEXT DEFAULT NULL,
            uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
        )
    ''')

    # Quiz reports table
    db_execute(cursor, '''
        CREATE TABLE IF NOT EXISTS quiz_reports (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            risk_level TEXT NOT NULL,
            answers TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
        )
    ''')

    # Routines table
    db_execute(cursor, '''
        CREATE TABLE IF NOT EXISTS routines (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            name TEXT NOT NULL,
            dosage TEXT NOT NULL,
            frequency TEXT NOT NULL,
            time TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
        )
    ''')

    conn.commit()
    conn.close()
    print("Database initialized successfully.")

# --- Users Helper ---

def register_user(name, email, phone, place, dob, role, password):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    hashed_password = generate_password_hash(password)
    try:
        db_execute(cursor, '''
            INSERT INTO users (name, email, phone, place, dob, role, password)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        ''', (name, email, phone, place, dob, role, hashed_password))
        conn.commit()
        return True, "User registered successfully"
    except Exception as e:
        err_msg = str(e).lower()
        if "unique" in err_msg or "duplicate key" in err_msg or "already exists" in err_msg:
            return False, "Email already exists"
        return False, f"Database error: {e}"
    finally:
        conn.close()

def login_user(email, password):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, 'SELECT * FROM users WHERE email = ?', (email,))
    user = cursor.fetchone()
    conn.close()
    
    if user and check_password_hash(user['password'], password):
        return dict(user)
    return None

def get_user_profile(user_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, 'SELECT id, name, email, phone, place, dob, role FROM users WHERE id = ?', (int(user_id),))
    user = cursor.fetchone()
    conn.close()
    return dict(user) if user else None

def get_doctors():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, "SELECT id, name, email, phone, place, dob, role FROM users WHERE role = 'doctor'")
    doctors = cursor.fetchall()
    conn.close()
    return [dict(doc) for doc in doctors]

def get_patients():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, "SELECT id, name, email, phone, place, dob, role FROM users WHERE role = 'user'")
    patients = cursor.fetchall()
    conn.close()
    return [dict(pat) for pat in patients]

# --- Slots Helper ---

def add_slot(doctor_id, date, time):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, '''
        INSERT INTO slots (doctor_id, date, time, status)
        VALUES (?, ?, ?, 'available')
    ''', (doctor_id, date, time))
    conn.commit()
    conn.close()
    return True

def get_doctor_slots(doctor_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, 'SELECT * FROM slots WHERE doctor_id = ?', (doctor_id,))
    slots = cursor.fetchall()
    conn.close()
    return [dict(slot) for slot in slots]

def get_available_slots():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, '''
        SELECT slots.*, users.name as doctor_name 
        FROM slots 
        JOIN users ON slots.doctor_id = users.id 
        WHERE slots.status = 'available'
    ''')
    slots = cursor.fetchall()
    conn.close()
    return [dict(slot) for slot in slots]

# --- Appointments Helper ---

def book_appointment(user_id, doctor_id, slot_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    try:
        # Check if slot is available
        db_execute(cursor, 'SELECT status FROM slots WHERE id = ?', (slot_id,))
        slot = cursor.fetchone()
        if not slot:
            return False, "Slot not found"
        if slot['status'] != 'available':
            return False, "Slot already booked"

        # Update slot status
        db_execute(cursor, "UPDATE slots SET status = 'booked' WHERE id = ?", (slot_id,))
        
        # Create appointment
        db_execute(cursor, '''
            INSERT INTO appointments (user_id, doctor_id, slot_id, status)
            VALUES (?, ?, ?, 'pending')
        ''', (user_id, doctor_id, slot_id))
        
        conn.commit()
        return True, "Appointment booked successfully"
    except Exception as e:
        conn.rollback()
        return False, str(e)
    finally:
        conn.close()

def get_user_appointments(user_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, '''
        SELECT appointments.id, appointments.status, slots.date, slots.time, users.name as doctor_name
        FROM appointments
        JOIN slots ON appointments.slot_id = slots.id
        JOIN users ON appointments.doctor_id = users.id
        WHERE appointments.user_id = ?
    ''', (user_id,))
    appointments = cursor.fetchall()
    conn.close()
    return [dict(app) for app in appointments]

def get_doctor_appointments(doctor_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, '''
        SELECT appointments.id, appointments.status, slots.date, slots.time, users.name as patient_name
        FROM appointments
        JOIN slots ON appointments.slot_id = slots.id
        JOIN users ON appointments.user_id = users.id
        WHERE appointments.doctor_id = ?
    ''', (doctor_id,))
    appointments = cursor.fetchall()
    conn.close()
    return [dict(app) for app in appointments]

def update_appointment_status(appointment_id, status):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    try:
        db_execute(cursor, "UPDATE appointments SET status = ? WHERE id = ?", (status, appointment_id))
        
        # If rejected, make the slot available again
        if status == 'rejected':
            db_execute(cursor, 'SELECT slot_id FROM appointments WHERE id = ?', (appointment_id,))
            row = cursor.fetchone()
            if row:
                db_execute(cursor, "UPDATE slots SET status = 'available' WHERE id = ?", (row['slot_id'],))
                
        conn.commit()
        return True
    except Exception as e:
        conn.rollback()
        return False
    finally:
        conn.close()

# --- Hair Images Helper ---

def save_hair_image(user_id, progress_tag, image_path):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, '''
        INSERT INTO hair_images (user_id, progress_tag, image_path)
        VALUES (?, ?, ?)
    ''', (user_id, progress_tag, image_path))
    conn.commit()
    conn.close()
    return True

def get_user_hair_images(user_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, 'SELECT * FROM hair_images WHERE user_id = ? ORDER BY uploaded_at DESC', (user_id,))
    images = cursor.fetchall()
    conn.close()
    return [dict(img) for img in images]

def get_all_patient_hair_images():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, '''
        SELECT hair_images.*, users.name as patient_name 
        FROM hair_images 
        JOIN users ON hair_images.user_id = users.id 
        ORDER BY hair_images.uploaded_at DESC
    ''')
    images = cursor.fetchall()
    conn.close()
    return [dict(img) for img in images]

def update_hair_remark(image_id, remark):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, "UPDATE hair_images SET remark = ? WHERE id = ?", (remark, image_id))
    conn.commit()
    conn.close()
    return True

# --- Quiz Helper ---

def save_quiz_report(user_id, risk_level, answers_json):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, '''
        INSERT INTO quiz_reports (user_id, risk_level, answers)
        VALUES (?, ?, ?)
    ''', (user_id, risk_level, answers_json))
    conn.commit()
    conn.close()
    return True

def get_user_quiz_reports(user_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, 'SELECT * FROM quiz_reports WHERE user_id = ? ORDER BY created_at DESC', (user_id,))
    reports = cursor.fetchall()
    conn.close()
    return [dict(rep) for rep in reports]

def get_all_patient_quiz_reports():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, '''
        SELECT quiz_reports.*, users.name as patient_name 
        FROM quiz_reports 
        JOIN users ON quiz_reports.user_id = users.id 
        ORDER BY quiz_reports.created_at DESC
    ''')
    reports = cursor.fetchall()
    conn.close()
    return [dict(rep) for rep in reports]

# --- Routines Helper ---

def add_routine(user_id, name, dosage, frequency, time):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, '''
        INSERT INTO routines (user_id, name, dosage, frequency, time)
        VALUES (?, ?, ?, ?, ?)
    ''', (user_id, name, dosage, frequency, time))
    conn.commit()
    conn.close()
    return True

def get_user_routines(user_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, 'SELECT * FROM routines WHERE user_id = ? ORDER BY created_at DESC', (user_id,))
    routines = cursor.fetchall()
    conn.close()
    return [dict(r) for r in routines]

def delete_routine(routine_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    db_execute(cursor, 'DELETE FROM routines WHERE id = ?', (routine_id,))
    conn.commit()
    conn.close()
    return True

if __name__ == '__main__':
    init_db()
