import subprocess
import re
import sys
import time
import os
import threading

def read_flask_output(process):
    for line in iter(process.stdout.readline, b''):
        decoded = line.decode('utf-8', errors='ignore')
        sys.stdout.write("[Flask] " + decoded)
        sys.stdout.flush()

def main():
    backend_dir = os.path.dirname(os.path.abspath(__file__))
    
    # 1. Start Flask Server in background
    print("[INFO] Starting Flask server...")
    flask_process = subprocess.Popen(
        [sys.executable, "app.py"],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        cwd=backend_dir
    )
    
    # Thread to continuously print flask server output
    flask_thread = threading.Thread(target=read_flask_output, args=(flask_process,))
    flask_thread.daemon = True
    flask_thread.start()
    
    # Give Flask a brief moment to initialize
    time.sleep(2)
    
    # 2. Start SSH Serveo Tunnel in background
    print("[INFO] Starting Serveo SSH Tunnel (exposing port 5000)...")
    # Using StrictHostKeyChecking=no to avoid user prompting
    ssh_process = subprocess.Popen(
        ["ssh", "-o", "StrictHostKeyChecking=no", "-R", "80:127.0.0.1:5000", "serveo.net"],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        stdin=subprocess.PIPE,
        cwd=backend_dir
    )
    
    tunnel_url = None
    
    # Read SSH output to find the public Serveo URL
    try:
        for line in iter(ssh_process.stdout.readline, b''):
            decoded = line.decode('utf-8', errors='ignore')
            clean_line = decoded.strip()
            print("[Tunnel] " + clean_line)
            
            # Look for forwarding pattern: e.g. "Forwarding HTTP traffic from https://..."
            match = re.search(r"https://[a-zA-Z0-9.-]+\.serveo[a-zA-Z0-9.-]+", clean_line)
            if match:
                tunnel_url = match.group(0)
                # Ensure trailing slash
                if not tunnel_url.endswith("/"):
                    tunnel_url += "/"
                print(f"\n=========================================")
                print(f" SUCCESS: Public Tunnel URL is live!")
                print(f" URL: {tunnel_url}")
                print(f"=========================================\n")
                break
    except Exception as e:
        print(f"[ERROR] Failed reading tunnel output: {e}")
        
    if tunnel_url:
        # 3. Update RetrofitClient.kt with the new BASE_URL
        client_path = os.path.abspath(os.path.join(
            backend_dir,
            "..",
            "app", "src", "main", "java", "com", "example", "rooyify", "network", "RetrofitClient.kt"
        ))
        
        if os.path.exists(client_path):
            print(f"[INFO] Updating RetrofitClient.kt BASE_URL to: {tunnel_url}")
            try:
                with open(client_path, "r", encoding="utf-8") as f:
                    content = f.read()
                
                # Replace matching BASE_URL declaration
                new_content = re.sub(
                    r'private const val BASE_URL = ".*?"',
                    f'private const val BASE_URL = "{tunnel_url}"',
                    content
                )
                
                with open(client_path, "w", encoding="utf-8") as f:
                    f.write(new_content)
                print("[INFO] RetrofitClient.kt updated successfully!")
            except Exception as e:
                print(f"[ERROR] Failed writing to RetrofitClient.kt: {e}")
        else:
            print(f"[WARNING] RetrofitClient.kt not found at: {client_path}")
            
    print("\n[INFO] Backend services are running. Press Ctrl+C to terminate both...")
    
    try:
        # Print any remaining outputs from SSH tunnel
        for line in iter(ssh_process.stdout.readline, b''):
            decoded = line.decode('utf-8', errors='ignore')
            print("[Tunnel] " + decoded.strip())
    except KeyboardInterrupt:
        print("\n[INFO] Shutting down backend server and tunnel...")
    finally:
        flask_process.terminate()
        ssh_process.terminate()
        print("[INFO] Clean shutdown complete.")

if __name__ == '__main__':
    main()
