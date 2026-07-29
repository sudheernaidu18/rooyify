import os
import json
import sqlite3
import datetime

try:
    import pymysql
except ImportError:
    print("Installing pymysql...")
    import subprocess
    subprocess.run(['py', '-m', 'pip', 'install', 'pymysql'])
    import pymysql

# Path to SQLite database
SQLITE_DB_PATH = os.path.join(os.path.dirname(__file__), 'rooyify.db')

def migrate():
    # 1. Connect to local XAMPP MySQL
    try:
        mysql_conn = pymysql.connect(
            host='localhost',
            user='root',
            password='',
            database='hairjourney',
            cursorclass=pymysql.cursors.DictCursor
        )
        print("Connected to XAMPP MySQL successfully!")
    except Exception as e:
        print("Could not connect to local XAMPP MySQL. Make sure MySQL is running in XAMPP.")
        print(f"Error: {e}")
        return

    # 2. Connect to local SQLite
    sqlite_conn = sqlite3.connect(SQLITE_DB_PATH)
    sqlite_cursor = sqlite_conn.cursor()
    
    # Initialize SQLite database schema
    import database
    database.init_db()

    # 3. Migrate USERS
    print("Migrating users...")
    with mysql_conn.cursor() as mysql_cursor:
        mysql_cursor.execute("SELECT * FROM users")
        mysql_users = mysql_cursor.fetchall()
        
        for u in mysql_users:
            try:
                sqlite_cursor.execute('''
                    INSERT OR IGNORE INTO users (id, name, email, phone, place, dob, role, password)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ''', (u['id'], u['name'], u['email'], u['phone'] or '', u['place'], str(u['dob']), u['role'], u['password']))
            except Exception as e:
                print(f"Failed to migrate user {u['email']}: {e}")

    # 4. Migrate DOCTOR SLOTS
    print("Migrating doctor slots...")
    with mysql_conn.cursor() as mysql_cursor:
        mysql_cursor.execute("SELECT * FROM doctor_slots")
        mysql_slots = mysql_cursor.fetchall()
        
        for s in mysql_slots:
            slot_time = s['slot_time'] # datetime object or string
            if isinstance(slot_time, str):
                dt = datetime.datetime.strptime(slot_time, "%Y-%m-%d %H:%M:%S")
            else:
                dt = slot_time
                
            date_str = dt.strftime("%Y-%m-%d")
            time_str = dt.strftime("%H:%M:%S")
            status = 'booked' if s['is_booked'] else 'available'
            
            sqlite_cursor.execute('''
                INSERT OR IGNORE INTO slots (id, doctor_id, date, time, status)
                VALUES (?, ?, ?, ?, ?)
            ''', (s['id'], s['doctor_id'], date_str, time_str, status))

    # 5. Migrate APPOINTMENTS
    print("Migrating appointments...")
    with mysql_conn.cursor() as mysql_cursor:
        mysql_cursor.execute("SELECT * FROM appointments")
        mysql_apps = mysql_cursor.fetchall()
        
        for a in mysql_apps:
            # Match status formatting
            status = (a['status'] or 'pending').lower()
            sqlite_cursor.execute('''
                INSERT OR IGNORE INTO appointments (id, user_id, doctor_id, slot_id, status)
                VALUES (?, ?, ?, ?, ?)
            ''', (a['id'], a['user_id'], a['doctor_id'], a['slot_id'], status))

    # 6. Migrate HAIR IMAGES
    print("Migrating hair images...")
    with mysql_conn.cursor() as mysql_cursor:
        mysql_cursor.execute("SELECT * FROM hair_images")
        mysql_images = mysql_cursor.fetchall()
        
        for img in mysql_images:
            sqlite_cursor.execute('''
                INSERT OR IGNORE INTO hair_images (id, user_id, progress_tag, image_path, remark, uploaded_at)
                VALUES (?, ?, ?, ?, ?, ?)
            ''', (img['id'], img['user_id'], img['progress_tag'] or 'before', img['image_path'] or '', img['doctor_remark'], str(img['created_at'])))

    # 7. Migrate QUIZ REPORTS
    print("Migrating quiz reports...")
    with mysql_conn.cursor() as mysql_cursor:
        mysql_cursor.execute("SELECT * FROM hair_quiz_reports")
        mysql_reports = mysql_cursor.fetchall()
        
        for r in mysql_reports:
            # Serialize the quiz answers as JSON string
            answers = {
                "scalp_type": r['scalp_type'],
                "sleep_hours": r['sleep_hours'],
                "hair_fall_frequency": r['hair_fall_frequency'],
                "family_history": r['family_history'],
                "stress_level": r['stress_level'],
                "diet": r['diet'],
                "deficiencies": r['deficiencies'],
                "hair_texture": r['hair_texture'],
                "hair_loss_stage": r['hair_loss_stage'],
                "sudden_thinning": r['sudden_thinning'],
                "dandruff": r['dandruff'],
                "styling_tools": r['styling_tools'],
                "scalp_pain": r['scalp_pain'],
                "doctor_remark": r['doctor_remark']
            }
            answers_json = json.dumps(answers)
            risk_level = r['result_summary'] or 'unknown'
            
            sqlite_cursor.execute('''
                INSERT OR IGNORE INTO quiz_reports (id, user_id, risk_level, answers, created_at)
                VALUES (?, ?, ?, ?, ?)
            ''', (r['id'], r['user_id'], risk_level, answers_json, str(r['created_at'])))

    # 8. Migrate PRESCRIPTIONS (Routines)
    print("Migrating prescriptions to routines...")
    with mysql_conn.cursor() as mysql_cursor:
        mysql_cursor.execute("SELECT * FROM prescriptions")
        mysql_prescs = mysql_cursor.fetchall()
        
        for p in mysql_prescs:
            sqlite_cursor.execute('''
                INSERT OR IGNORE INTO routines (id, user_id, name, dosage, frequency, time, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            ''', (p['id'], p['user_id'], p['medicines'] or 'Prescription', 'As directed', 'Daily', 'Morning', str(p['created_at'])))

    sqlite_conn.commit()
    sqlite_conn.close()
    mysql_conn.close()
    print("Migration completed successfully!")

if __name__ == '__main__':
    migrate()
