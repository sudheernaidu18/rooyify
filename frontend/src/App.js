import React, { useState, useEffect } from 'react';
import { Routes, Route, useNavigate, Navigate } from 'react-router-dom';

const API_BASE = "https://ae2ae0342a9fa6cf-157-51-145-78.serveousercontent.com";

function App() {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const cached = localStorage.getItem("user");
    if (cached) {
      setUser(JSON.parse(cached));
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("user");
    setUser(null);
    navigate("/login");
  };

  return (
    <div className="app-container">
      <Routes>
        <Route path="/login" element={<Login setUser={setUser} user={user} />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={user ? <Dashboard user={user} handleLogout={handleLogout} setUser={setUser} /> : <Navigate to="/login" />} />
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </div>
  );
}

// ----------------------------------------------------
// LOGIN COMPONENT
// ----------------------------------------------------
function Login({ setUser, user }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    if (user) {
      navigate("/dashboard");
    }
  }, [user, navigate]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const resp = await fetch(`${API_BASE}/login.php`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
      });
      const data = await resp.json();
      if (data.status === "success") {
        localStorage.setItem("user", JSON.stringify(data.user));
        setUser(data.user);
        navigate("/dashboard");
      } else {
        setError(data.message || "Invalid credentials");
      }
    } catch (err) {
      setError("Server connection failed.");
    }
  };

  return (
    <div className="auth-card">
      <h2>Welcome Back</h2>
      <p>Log in to continue your hair journey</p>
      {error && <div className="error-badge">{error}</div>}
      <form onSubmit={onSubmit}>
        <div className="form-group">
          <label>Email Address</label>
          <input
            type="email"
            id="email"
            className="form-input"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input
            type="password"
            id="password"
            className="form-input"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" id="login-button" className="btn btn-primary">Login</button>
      </form>
      <p className="auth-link-text">
        Don't have an account? <span onClick={() => navigate("/register")} className="link-action">Register</span>
      </p>
    </div>
  );
}

// ----------------------------------------------------
// REGISTER COMPONENT
// ----------------------------------------------------
function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [place, setPlace] = useState("");
  const [dob, setDob] = useState("");
  const [role, setRole] = useState("user");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const navigate = useNavigate();

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    const payload = { name, email, phone, place, dob, role, password };

    try {
      const resp = await fetch(`${API_BASE}/register.php`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      const data = await resp.json();
      if (data.status === "success") {
        setSuccess("Account created! Redirecting to login...");
        setTimeout(() => navigate("/login"), 2000);
      } else {
        setError(data.message || "Registration failed");
      }
    } catch (err) {
      setError("Server connection failed.");
    }
  };

  return (
    <div className="auth-card">
      <h2>Create Account</h2>
      <p>Start tracking hair health logs</p>
      {error && <div className="error-badge">{error}</div>}
      {success && <div className="success-badge">{success}</div>}
      <form onSubmit={onSubmit}>
        <div className="form-group">
          <label>Full Name</label>
          <input
            type="text"
            id="reg-name"
            className="form-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Email</label>
          <input
            type="email"
            id="reg-email"
            className="form-input"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Phone Number</label>
          <input
            type="text"
            id="reg-phone"
            className="form-input"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Place</label>
          <input
            type="text"
            id="reg-place"
            className="form-input"
            value={place}
            onChange={(e) => setPlace(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Date of Birth</label>
          <input
            type="date"
            id="reg-dob"
            className="form-input"
            value={dob}
            onChange={(e) => setDob(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>User Role</label>
          <select
            id="reg-role"
            className="form-input"
            value={role}
            onChange={(e) => setRole(e.target.value)}
          >
            <option value="user">Patient</option>
            <option value="doctor">Certified Doctor</option>
          </select>
        </div>
        <div className="form-group">
          <label>Password</label>
          <input
            type="password"
            id="reg-password"
            className="form-input"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" id="signup-button" className="btn btn-primary">Sign Up</button>
      </form>
      <p className="auth-link-text">
        Back to <span onClick={() => navigate("/login")} className="link-action">Login</span>
      </p>
    </div>
  );
}

// ----------------------------------------------------
// DASHBOARD COMPONENT
// ----------------------------------------------------
function Dashboard({ user, handleLogout, setUser }) {
  const [activeTab, setActiveTab] = useState("home");
  
  // Profile Form States
  const [name, setName] = useState(user.name);
  const [phone, setPhone] = useState(user.phone || "");
  const [place, setPlace] = useState(user.place || "");
  const [dob, setDob] = useState(user.dob || "");
  const [profError, setProfError] = useState("");
  const [profSuccess, setProfSuccess] = useState("");

  const updateProfile = async (e) => {
    e.preventDefault();
    setProfError("");
    setProfSuccess("");
    const payload = { user_id: user.id, name, phone, place, dob };

    try {
      const resp = await fetch(`${API_BASE}/update_profile.php`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      const data = await resp.json();
      if (data.status === "success") {
        setProfSuccess("Profile updated successfully!");
        const updatedUser = { ...user, name, phone, place, dob };
        localStorage.setItem("user", JSON.stringify(updatedUser));
        setUser(updatedUser);
      } else {
        setProfError(data.message || "Failed to update profile");
      }
    } catch (err) {
      setProfError("Error contacting server.");
    }
  };

  const deleteAccount = async () => {
    const password = prompt("Are you sure? Enter your password to delete your account:");
    if (!password) return;

    try {
      const resp = await fetch(`${API_BASE}/delete_account.php`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ user_id: user.id, password })
      });
      const data = await resp.json();
      if (data.status === "success") {
        alert("Account deleted.");
        handleLogout();
      } else {
        alert(data.message || "Incorrect password");
      }
    } catch (err) {
      alert("Error contacting server.");
    }
  };

  return (
    <div className="dashboard-layout">
      <div className="sidebar">
        <h2 className="brand-title">Rooyify</h2>
        <div className={`nav-item ${activeTab === 'home' ? 'active' : ''}`} onClick={() => setActiveTab("home")}>
          <i className="fa-solid fa-house"></i> Home
        </div>
        <div className={`nav-item ${activeTab === 'profile' ? 'active' : ''}`} onClick={() => setActiveTab("profile")}>
          <i className="fa-solid fa-user"></i> Profile
        </div>
        <div className="nav-item logout-nav" onClick={handleLogout}>
          <i className="fa-solid fa-right-from-bracket"></i> Logout
        </div>
      </div>
      
      <div className="main-content">
        {activeTab === "home" ? (
          <div>
            <h1>Dashboard</h1>
            <p>Welcome back, <strong>{user.name}</strong> ({user.role === 'doctor' ? 'Certified Expert' : 'Patient'})</p>
            <div className="metrics-row">
              <div className="metric-box">
                <h3>5 Days</h3>
                <p>Consecutive Streak</p>
              </div>
              <div className="metric-box">
                <h3>85%</h3>
                <p>Treatment Adherence</p>
              </div>
            </div>
          </div>
        ) : (
          <div>
            <h1>Edit Profile</h1>
            {profError && <div className="error-badge">{profError}</div>}
            {profSuccess && <div className="success-badge">{profSuccess}</div>}
            <form onSubmit={updateProfile} className="profile-form">
              <div className="form-group">
                <label>Name</label>
                <input type="text" className="form-input" value={name} onChange={(e) => setName(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Phone</label>
                <input type="text" className="form-input" value={phone} onChange={(e) => setPhone(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Place</label>
                <input type="text" className="form-input" value={place} onChange={(e) => setPlace(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Date of Birth</label>
                <input type="date" className="form-input" value={dob} onChange={(e) => setDob(e.target.value)} required />
              </div>
              <button type="submit" id="update-profile-button" className="btn btn-primary">Update Profile</button>
            </form>
            <hr style={{ margin: "25px 0", borderColor: "rgba(0,0,0,0.1)" }} />
            <button onClick={deleteAccount} id="delete-account-button" className="btn btn-danger">Delete Account</button>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
