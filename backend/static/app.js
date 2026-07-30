// ==========================================
// ROOYIFY CLIENT-SIDE WEB CONTROLLER
// ==========================================

const API_BASE = ""; 

let currentUser = null;

// Initialize Session on Load
document.addEventListener("DOMContentLoaded", () => {
    checkSession();
    initGlobalEvents();
});

function checkSession() {
    const sessionData = localStorage.getItem("user");
    if (sessionData) {
        try {
            currentUser = JSON.parse(sessionData);
            renderAuthenticatedUI();
        } catch (e) {
            localStorage.removeItem("user");
            renderGuestUI();
        }
    } else {
        renderGuestUI();
    }
}

function renderGuestUI() {
    currentUser = null;
    document.getElementById("user-display-name").classList.add("hidden");
    document.getElementById("btn-logout").classList.add("hidden");
    document.getElementById("btn-login-trigger").classList.remove("hidden");
    
    document.getElementById("section-landing").classList.remove("hidden");
    document.getElementById("section-patient").classList.add("hidden");
    document.getElementById("section-doctor").classList.add("hidden");
    
    document.querySelectorAll(".home-only").forEach(el => el.classList.remove("hidden"));
}

function renderAuthenticatedUI() {
    document.getElementById("user-display-name").textContent = currentUser.name;
    document.getElementById("user-display-name").classList.remove("hidden");
    document.getElementById("btn-logout").classList.remove("hidden");
    document.getElementById("btn-login-trigger").classList.add("hidden");
    
    document.getElementById("section-landing").classList.add("hidden");
    document.querySelectorAll(".home-only").forEach(el => el.classList.add("hidden"));

    if (currentUser.role === "doctor") {
        document.getElementById("section-patient").classList.add("hidden");
        document.getElementById("section-doctor").classList.remove("hidden");
        initDoctorDashboard();
    } else {
        document.getElementById("section-patient").classList.remove("hidden");
        document.getElementById("section-doctor").classList.add("hidden");
        
        // Populate profile header
        document.getElementById("patient-header-name").textContent = currentUser.name;
        document.getElementById("patient-header-email").textContent = currentUser.email;
        document.getElementById("patient-header-phone").textContent = currentUser.phone || "N/A";
        document.getElementById("patient-header-badge").textContent = currentUser.name.charAt(0).toUpperCase();
        
        // Populate profile table
        document.getElementById("tbl-profile-email").textContent = currentUser.email;
        document.getElementById("tbl-profile-phone").textContent = currentUser.phone || "N/A";
        document.getElementById("tbl-profile-place").textContent = currentUser.place || "N/A";
        document.getElementById("tbl-profile-dob").textContent = currentUser.dob || "N/A";

        initPatientDashboard();
    }
}

// ==========================================
// AUTHENTICATION EVENTS & GENERAL TABS
// ==========================================

function initGlobalEvents() {
    const authModal = document.getElementById("modal-auth");
    const closeAuthBtn = document.getElementById("btn-close-auth-modal");
    
    document.getElementById("btn-login-trigger").onclick = () => showAuthModal(true);
    document.querySelector(".btn-cta-login").onclick = () => showAuthModal(true);
    closeAuthBtn.onclick = () => authModal.style.display = "none";
    
    const tabLogin = document.getElementById("tab-login");
    const tabRegister = document.getElementById("tab-register");
    const formLogin = document.getElementById("form-login");
    const formRegister = document.getElementById("form-register");

    tabLogin.onclick = () => {
        tabLogin.classList.add("active");
        tabRegister.classList.remove("active");
        formLogin.classList.remove("hidden");
        formRegister.classList.add("hidden");
    };

    tabRegister.onclick = () => {
        tabRegister.classList.add("active");
        tabLogin.classList.remove("active");
        formRegister.classList.remove("hidden");
        formLogin.classList.add("hidden");
    };

    // Close modals when clicking outside
    window.onclick = (e) => {
        if (e.target.classList.contains("modal")) {
            e.target.style.display = "none";
        }
    };

    // Login submit
    formLogin.onsubmit = async (e) => {
        e.preventDefault();
        const email = document.getElementById("login-email").value;
        const password = document.getElementById("login-password").value;

        try {
            const resp = await fetch(`${API_BASE}/login.php`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });
            const data = await resp.json();
            if (data.status === "success") {
                localStorage.setItem("user", JSON.stringify(data.user));
                authModal.style.display = "none";
                formLogin.reset();
                checkSession();
            } else {
                alert(data.message || "Invalid credentials");
            }
        } catch (err) {
            alert("Connection error. Server may be down.");
        }
    };

    // Register submit
    formRegister.onsubmit = async (e) => {
        e.preventDefault();
        const payload = {
            name: document.getElementById("reg-name").value,
            email: document.getElementById("reg-email").value,
            phone: document.getElementById("reg-phone").value,
            dob: document.getElementById("reg-dob").value,
            place: document.getElementById("reg-place").value,
            role: document.getElementById("reg-role").value,
            password: document.getElementById("reg-password").value
        };

        try {
            const resp = await fetch(`${API_BASE}/register.php`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            const data = await resp.json();
            if (data.status === "success") {
                alert("Account created! Please sign in.");
                tabLogin.click();
            } else {
                alert(data.message || "Registration failed");
            }
        } catch (err) {
            alert("Connection error: " + err);
        }
    };

    // Logout Button
    document.getElementById("btn-logout").onclick = () => {
        localStorage.removeItem("user");
        renderGuestUI();
    };

    // Patient Dashboard Horizontal Tabs switching
    document.querySelectorAll(".app-tab-btn").forEach(btn => {
        btn.onclick = () => {
            const tabId = btn.dataset.tab;
            if (!tabId) return;

            // Remove active classes
            document.querySelectorAll(".app-tab-btn").forEach(b => b.classList.remove("active"));
            document.querySelectorAll(".tab-panel-item").forEach(p => p.classList.remove("active"));

            // Add active classes
            btn.classList.add("active");
            document.getElementById(tabId).classList.add("active");
        };
    });
}

function showAuthModal(showLogin = true) {
    document.getElementById("modal-auth").style.display = "flex";
    if (showLogin) {
        document.getElementById("tab-login").click();
    } else {
        document.getElementById("tab-register").click();
    }
}

// ==========================================
// PATIENT DASHBOARD LOGIC
// ==========================================

function initPatientDashboard() {
    loadPatientRoutines();
    loadPatientPhotos();
    loadDoctorsAndSlots();
    loadBookedAppointments();
    loadQuizStatus();

    // Modals
    const routineModal = document.getElementById("modal-routine");
    const photoModal = document.getElementById("modal-photo");
    const quizModal = document.getElementById("modal-quiz");

    // Close buttons
    document.getElementById("btn-close-routine-modal").onclick = () => routineModal.style.display = "none";
    document.getElementById("btn-close-photo-modal").onclick = () => photoModal.style.display = "none";
    document.getElementById("btn-close-quiz-modal").onclick = () => quizModal.style.display = "none";

    // Tab buttons to trigger modals
    document.getElementById("btn-patient-add-routine").onclick = () => routineModal.style.display = "flex";
    document.getElementById("btn-patient-upload-photo").onclick = () => photoModal.style.display = "flex";
    document.getElementById("btn-patient-quiz-trigger").onclick = () => startHairQuiz();

    // Submit New Routine
    document.getElementById("form-add-routine").onsubmit = async (e) => {
        e.preventDefault();
        const payload = {
            user_id: currentUser.id,
            name: document.getElementById("routine-name").value,
            dosage: document.getElementById("routine-dosage").value,
            frequency: document.getElementById("routine-frequency").value,
            time: document.getElementById("routine-time").value
        };

        const resp = await fetch(`${API_BASE}/add_routine.php`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const data = await resp.json();
        if (data.status === "success") {
            routineModal.style.display = "none";
            document.getElementById("form-add-routine").reset();
            loadPatientRoutines();
        } else {
            alert(data.message || "Failed to add routine");
        }
    };

    // Submit Photo Upload
    document.getElementById("form-upload-photo").onsubmit = async (e) => {
        e.preventDefault();
        const fileInput = document.getElementById("photo-file");
        const progressTag = document.getElementById("photo-tag").value;
        
        if (fileInput.files.length === 0) return;

        const formData = new FormData();
        formData.append("user_id", currentUser.id);
        formData.append("progress_tag", progressTag);
        formData.append("image", fileInput.files[0]);

        try {
            const resp = await fetch(`${API_BASE}/upload_hair_image.php`, {
                method: "POST",
                body: formData
            });
            const data = await resp.json();
            if (data.status === "success") {
                photoModal.style.display = "none";
                document.getElementById("form-upload-photo").reset();
                loadPatientPhotos();
            } else {
                alert(data.message || "Failed to upload photo");
            }
        } catch (err) {
            alert("Error uploading image: " + err);
        }
    };

    // Book Appointment
    document.getElementById("btn-patient-book-appt").onclick = async () => {
        const slotSelect = document.getElementById("patient-select-slot");
        const slotId = slotSelect.value;
        if (!slotId) {
            alert("Please select an available appointment slot");
            return;
        }

        const selectedOption = slotSelect.options[slotSelect.selectedIndex];
        const doctorId = selectedOption.dataset.doctorId;

        const payload = {
            user_id: currentUser.id,
            doctor_id: doctorId,
            slot_id: slotId
        };

        const resp = await fetch(`${API_BASE}/book_appointment.php`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const data = await resp.json();
        if (data.status === "success") {
            alert("Booking request submitted successfully! Pending doctor approval.");
            loadDoctorsAndSlots();
            loadBookedAppointments();
        } else {
            alert(data.message || "Failed to book slot");
        }
    };
}

function updateComplianceRing(percent) {
    document.getElementById("patient-header-compliance").textContent = `${percent}%`;
    const circle = document.querySelector('.progress-ring-circle');
    const radius = circle.r.baseVal.value;
    const circumference = radius * 2 * Math.PI;
    
    circle.style.strokeDasharray = `${circumference} ${circumference}`;
    const offset = circumference - (percent / 100 * circumference);
    circle.style.strokeDashoffset = offset;
}

async function loadPatientRoutines() {
    const listEl = document.getElementById("list-patient-routines");
    listEl.innerHTML = '<li class="empty-list-msg">Loading routines...</li>';
    
    try {
        const resp = await fetch(`${API_BASE}/get_routines.php?user_id=${currentUser.id}`);
        const data = await resp.json();
        
        if (data.status === "success" && data.routines && data.routines.length > 0) {
            listEl.innerHTML = "";
            data.routines.forEach(r => {
                const li = document.createElement("li");
                li.className = "routine-item";
                li.innerHTML = `
                    <div class="routine-info">
                        <h4>${r.name}</h4>
                        <p>${r.dosage} • ${r.frequency} • ${r.time}</p>
                    </div>
                    <button class="btn-delete" onclick="deleteRoutine('${r.id}')"><i class="fa-solid fa-trash"></i></button>
                `;
                listEl.appendChild(li);
            });
            // Update compliance based on routines
            updateComplianceRing(85);
        } else {
            listEl.innerHTML = '<li class="empty-list-msg">No active routines. Add one to start tracking.</li>';
            updateComplianceRing(0);
        }
    } catch (e) {
        listEl.innerHTML = '<li class="empty-list-msg">Failed to load routines.</li>';
        updateComplianceRing(0);
    }
}

async function deleteRoutine(id) {
    if (!confirm("Are you sure you want to delete this routine?")) return;
    const resp = await fetch(`${API_BASE}/delete_routine.php`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ routine_id: id })
    });
    const data = await resp.json();
    if (data.status === "success") {
        loadPatientRoutines();
    }
}

async function loadPatientPhotos() {
    const gridEl = document.getElementById("grid-patient-images");
    gridEl.innerHTML = '<p class="empty-list-msg">Loading photos...</p>';

    try {
        const resp = await fetch(`${API_BASE}/get_hair_images.php?user_id=${currentUser.id}`);
        const data = await resp.json();
        
        if (data.status === "success" && data.images && data.images.length > 0) {
            gridEl.innerHTML = "";
            data.images.forEach(img => {
                const card = document.createElement("div");
                card.className = "hair-image-card";
                card.innerHTML = `
                    <img src="${img.image_url}" alt="Hair Log">
                    <span class="image-tag-badge">${img.progress_tag}</span>
                `;
                card.onclick = () => {
                    alert(`Tag: ${img.progress_tag.toUpperCase()}\nUploaded: ${img.uploaded_at}\nRemark: ${img.remark || "No remarks from doctor yet."}`);
                };
                gridEl.appendChild(card);
            });
            document.getElementById("stat-photos").textContent = data.images.length;
        } else {
            gridEl.innerHTML = '<p class="empty-list-msg">No progress photos logged yet.</p>';
            document.getElementById("stat-photos").textContent = "0";
        }
    } catch (e) {
        gridEl.innerHTML = '<p class="empty-list-msg">Failed to load photos.</p>';
    }
}

async function loadDoctorsAndSlots() {
    const selectEl = document.getElementById("patient-select-slot");
    selectEl.innerHTML = '<option value="">Loading slots...</option>';

    try {
        const resp = await fetch(`${API_BASE}/get_slots.php`);
        const data = await resp.json();
        
        if (data.status === "success" && data.slots && data.slots.length > 0) {
            selectEl.innerHTML = '<option value="">-- Choose a slot --</option>';
            data.slots.forEach(s => {
                const opt = document.createElement("option");
                opt.value = s.id;
                opt.dataset.doctorId = s.doctor_id;
                opt.textContent = `${s.doctor_name || "Doctor"} - ${s.date} @ ${s.time}`;
                selectEl.appendChild(opt);
            });
        } else {
            selectEl.innerHTML = '<option value="">No available doctor slots found</option>';
        }
    } catch (e) {
        selectEl.innerHTML = '<option value="">Failed to connect to appointments API</option>';
    }
}

async function loadBookedAppointments() {
    const listEl = document.getElementById("list-patient-appts");
    listEl.innerHTML = '<li class="empty-list-msg">Loading...</li>';

    try {
        const resp = await fetch(`${API_BASE}/get_appointments.php?user_id=${currentUser.id}`);
        const data = await resp.json();
        
        if (data.status === "success" && data.appointments && data.appointments.length > 0) {
            listEl.innerHTML = "";
            data.appointments.forEach(a => {
                const li = document.createElement("li");
                li.className = "appointment-item";
                li.innerHTML = `
                    <div class="app-meta">
                        <h5>${a.doctor_name}</h5>
                        <p>${a.date} at ${a.time}</p>
                    </div>
                    <span class="status-badge status-${a.status.toLowerCase()}">${a.status}</span>
                `;
                listEl.appendChild(li);
            });
            document.getElementById("stat-appts").textContent = data.appointments.length;
        } else {
            listEl.innerHTML = '<li class="empty-list-msg">No booked consultations.</li>';
            document.getElementById("stat-appts").textContent = "0";
        }
    } catch (e) {
        listEl.innerHTML = '<li class="empty-list-msg">Failed to load appointments.</li>';
    }
}

async function loadQuizStatus() {
    try {
        const resp = await fetch(`${API_BASE}/get_quiz_reports.php?user_id=${currentUser.id}`);
        const data = await resp.json();
        
        if (data.status === "success" && data.reports && data.reports.length > 0) {
            const last = data.reports[0];
            let answers = {};
            try {
                answers = typeof last.answers === 'string' ? JSON.parse(last.answers) : last.answers;
            } catch (e) {
                answers = {};
            }

            document.getElementById("quiz-last-date").textContent = last.created_at.split(" ")[0];
            document.getElementById("quiz-last-id").textContent = `#${last.id}`;
            document.getElementById("quiz-last-summary").textContent = `${last.risk_level.toUpperCase()} RISK`;
            document.getElementById("quiz-last-summary").className = `gradient-text`; // Keep standard header styling
            
            document.getElementById("quiz-detail-scalp").textContent = answers.scalp_type || "N/A";
            document.getElementById("quiz-detail-sleep").textContent = answers.sleep_hours || "N/A";
            document.getElementById("quiz-detail-stress").textContent = answers.stress_level || "N/A";
            document.getElementById("quiz-detail-history").textContent = answers.family_history || "N/A";
            
            document.getElementById("card-last-quiz-container").classList.remove("hidden");
            document.getElementById("lbl-empty-quiz").classList.add("hidden");
            
            // Set dynamic quiz counts
            document.getElementById("stat-quizzes").textContent = data.reports.length;
            document.getElementById("stat-streak").textContent = Math.min(data.reports.length * 2, 7); // Mock streak days
        } else {
            document.getElementById("card-last-quiz-container").classList.add("hidden");
            document.getElementById("lbl-empty-quiz").classList.remove("hidden");
            document.getElementById("stat-quizzes").textContent = "0";
            document.getElementById("stat-streak").textContent = "0";
        }
    } catch (e) {}
}

// ==========================================
// PATIENT QUIZ LOGIC
// ==========================================

const quizQuestions = [
    {
        id: "scalp_type",
        q: "How would you describe your scalp type?",
        options: ["Oily", "Dry", "Normal", "Itchy/Flaky"]
    },
    {
        id: "sleep_hours",
        q: "On average, how many hours of sleep do you get per night?",
        options: ["Less than 5 hours", "5 to 7 hours", "More than 7 hours"]
    },
    {
        id: "stress_level",
        q: "What is your typical daily stress level?",
        options: ["High", "Moderate", "Low"]
    },
    {
        id: "family_history",
        q: "Is there a family history of premature hair loss or balding?",
        options: ["Yes, immediate family", "Yes, extended family", "No history"]
    },
    {
        id: "dandruff",
        q: "How frequently do you experience severe dandruff?",
        options: ["Frequently", "Occasionally", "Never"]
    }
];

let quizAnswers = {};
let quizIndex = 0;

function startHairQuiz() {
    quizAnswers = {};
    quizIndex = 0;
    document.getElementById("modal-quiz").style.display = "flex";
    renderQuizSlide();
}

function renderQuizSlide() {
    const container = document.getElementById("quiz-slides-container");
    const q = quizQuestions[quizIndex];
    
    if (quizIndex === 0) {
        document.getElementById("btn-quiz-prev").classList.add("hidden");
    } else {
        document.getElementById("btn-quiz-prev").classList.remove("hidden");
    }
    
    if (quizIndex === quizQuestions.length - 1) {
        document.getElementById("btn-quiz-next").textContent = "Submit Quiz";
    } else {
        document.getElementById("btn-quiz-next").textContent = "Next";
    }

    container.innerHTML = `
        <div class="quiz-slide">
            <p class="text-secondary">Question ${quizIndex + 1} of ${quizQuestions.length}</p>
            <div class="quiz-question">${q.q}</div>
            <div class="quiz-options">
                ${q.options.map((opt, i) => `
                    <div class="quiz-option ${quizAnswers[q.id] === opt ? 'selected' : ''}" onclick="selectQuizOption('${q.id}', '${opt}')">
                        <i class="fa-regular ${quizAnswers[q.id] === opt ? 'fa-circle-dot' : 'fa-circle'}"></i>
                        <span>${opt}</span>
                    </div>
                `).join('')}
            </div>
        </div>
    `;
}

function selectQuizOption(qId, val) {
    quizAnswers[qId] = val;
    renderQuizSlide();
}

document.getElementById("btn-quiz-prev").onclick = () => {
    if (quizIndex > 0) {
        quizIndex--;
        renderQuizSlide();
    }
};

document.getElementById("btn-quiz-next").onclick = async () => {
    const q = quizQuestions[quizIndex];
    if (!quizAnswers[q.id]) {
        alert("Please select an option to proceed.");
        return;
    }

    if (quizIndex < quizQuestions.length - 1) {
        quizIndex++;
        renderQuizSlide();
    } else {
        let riskScore = 0;
        if (quizAnswers.scalp_type === "Itchy/Flaky") riskScore += 2;
        if (quizAnswers.sleep_hours === "Less than 5 hours") riskScore += 2;
        if (quizAnswers.stress_level === "High") riskScore += 2;
        if (quizAnswers.family_history === "Yes, immediate family") riskScore += 3;
        if (quizAnswers.dandruff === "Frequently") riskScore += 1;
        
        let riskLevel = "low";
        if (riskScore >= 6) riskLevel = "high";
        else if (riskScore >= 3) riskLevel = "moderate";

        const payload = {
            user_id: currentUser.id,
            risk_level: riskLevel,
            answers: quizAnswers
        };

        const resp = await fetch(`${API_BASE}/submit_quiz.php`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const data = await resp.json();
        
        if (data.status === "success") {
            alert(`Assessment submitted! Risk calculated: ${riskLevel.toUpperCase()}`);
            document.getElementById("modal-quiz").style.display = "none";
            loadQuizStatus();
            // Switch tabs back to Quiz panel automatically to show result
            document.querySelector(".app-tab-btn[data-tab='container-quiz']").click();
        } else {
            alert("Failed to submit assessment.");
        }
    }
};

// ==========================================
// DOCTOR DASHBOARD LOGIC
// ==========================================

function initDoctorDashboard() {
    loadDoctorSlots();
    loadDoctorAppointments();
    loadPatientsList();

    // Create slot form handler
    document.getElementById("form-doctor-create-slot").onsubmit = async (e) => {
        e.preventDefault();
        const payload = {
            doctor_id: currentUser.id,
            date: document.getElementById("doc-slot-date").value,
            time: document.getElementById("doc-slot-time").value
        };

        const resp = await fetch(`${API_BASE}/create_slot.php`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const data = await resp.json();
        if (data.status === "success") {
            alert("Slot created!");
            document.getElementById("form-doctor-create-slot").reset();
            loadDoctorSlots();
        }
    };

    // Patient Search filter
    document.getElementById("search-patients-list").oninput = (e) => {
        const query = e.target.value.toLowerCase();
        document.querySelectorAll(".patient-item").forEach(item => {
            const name = item.dataset.name;
            if (name.includes(query)) {
                item.classList.remove("hidden");
            } else {
                item.classList.add("hidden");
            }
        });
    };

    // Patient detail modal close
    document.getElementById("btn-close-portfolio-modal").onclick = () => {
        document.getElementById("modal-portfolio-detail").style.display = "none";
    };

    // Bind doctor overlay tabs switching
    document.querySelectorAll(".doc-tab").forEach(btn => {
        btn.onclick = () => {
            const panelId = btn.dataset.detailTab;
            if (!panelId) return;

            document.querySelectorAll(".doc-tab").forEach(b => b.classList.remove("active"));
            document.querySelectorAll(".doc-tab-panel").forEach(p => p.classList.remove("active"));

            btn.classList.add("active");
            document.getElementById(panelId).classList.add("active");
        };
    });
}

async function loadDoctorSlots() {
    // Optional display or list slots if desired
}

async function loadDoctorAppointments() {
    const listEl = document.getElementById("list-doctor-pending-appts");
    listEl.innerHTML = '<li class="empty-list-msg">Loading requests...</li>';

    try {
        const resp = await fetch(`${API_BASE}/get_appointments.php?doctor_id=${currentUser.id}`);
        const data = await resp.json();
        
        if (data.status === "success" && data.appointments) {
            const pending = data.appointments.filter(a => a.status.toLowerCase() === "pending");
            if (pending.length > 0) {
                listEl.innerHTML = "";
                pending.forEach(a => {
                    const li = document.createElement("li");
                    li.className = "appointment-item";
                    li.innerHTML = `
                        <div class="app-meta">
                            <h5>${a.patient_name}</h5>
                            <p>${a.date} at ${a.time}</p>
                        </div>
                        <div style="display: flex; gap: 0.5rem;">
                            <button class="btn btn-primary" onclick="setAppointmentStatus('${a.id}', 'approved')" style="padding: 0.4rem 0.8rem; font-size: 0.8rem; border-radius:4px;">Approve</button>
                            <button class="btn btn-secondary" onclick="setAppointmentStatus('${a.id}', 'rejected')" style="padding: 0.4rem 0.8rem; font-size: 0.8rem; border-radius:4px;">Reject</button>
                        </div>
                    `;
                    listEl.appendChild(li);
                });
            } else {
                listEl.innerHTML = '<li class="empty-list-msg">No pending requests.</li>';
            }
        }
    } catch (e) {
        listEl.innerHTML = '<li class="empty-list-msg">Failed to load appointments.</li>';
    }
}

async function setAppointmentStatus(appId, status) {
    const resp = await fetch(`${API_BASE}/update_appointment_status.php`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ appointment_id: appId, status })
    });
    const data = await resp.json();
    if (data.status === "success") {
        loadDoctorAppointments();
    }
}

async function loadPatientsList() {
    const listEl = document.getElementById("list-doctor-patients");
    listEl.innerHTML = '<li class="empty-list-msg">Loading patients...</li>';

    try {
        const resp = await fetch(`${API_BASE}/get_patients.php`);
        const data = await resp.json();
        
        if (data.status === "success" && data.patients && data.patients.length > 0) {
            listEl.innerHTML = "";
            data.patients.forEach(p => {
                const li = document.createElement("li");
                li.className = "patient-item";
                li.dataset.name = p.name.toLowerCase();
                li.innerHTML = `
                    <div>
                        <h4>${p.name}</h4>
                        <span class="patient-meta-desc">${p.place} • DOB: ${p.dob}</span>
                    </div>
                    <i class="fa-solid fa-chevron-right text-secondary"></i>
                `;
                li.onclick = () => openPatientPortfolio(p);
                listEl.appendChild(li);
            });
        } else {
            listEl.innerHTML = '<li class="empty-list-msg">No registered patients found.</li>';
        }
    } catch (e) {
        listEl.innerHTML = '<li class="empty-list-msg">Error loading patients.</li>';
    }
}

// ==========================================
// DOCTOR PORTFOLIO DETAILS OVERLAY
// ==========================================

async function openPatientPortfolio(patient) {
    const modal = document.getElementById("modal-portfolio-detail");
    document.getElementById("detail-patient-name").textContent = patient.name;
    document.getElementById("detail-patient-email").textContent = patient.email;
    document.getElementById("detail-patient-phone").textContent = patient.phone || "N/A";
    document.getElementById("detail-patient-badge").textContent = patient.name.charAt(0).toUpperCase();
    modal.style.display = "flex";

    // Load data
    loadPatientPortfolioPhotos(patient.id);
    loadPatientPortfolioQuiz(patient.id);
    loadPatientPortfolioRoutines(patient.id);
    
    // Set photos active by default
    document.querySelector(".doc-tab[data-detail-tab='doc-detail-photos']").click();
}

async function loadPatientPortfolioPhotos(patientId) {
    const gridEl = document.getElementById("detail-grid-photos");
    gridEl.innerHTML = '<p class="empty-list-msg">Loading photos...</p>';

    const resp = await fetch(`${API_BASE}/get_hair_images.php?user_id=${patientId}`);
    const data = await resp.json();
    
    if (data.status === "success" && data.images && data.images.length > 0) {
        gridEl.innerHTML = "";
        data.images.forEach(img => {
            const card = document.createElement("div");
            card.className = "hair-image-card";
            card.style.height = "auto";
            card.style.aspectRatio = "unset";
            card.innerHTML = `
                <img src="${img.image_url}" alt="Hair Log" style="height: 180px; width:100%; object-fit:cover;">
                <span class="image-tag-badge">${img.progress_tag}</span>
                <div style="padding: 0.75rem;">
                    <p style="font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 0.25rem;">Uploaded: ${img.uploaded_at.split(' ')[0]}</p>
                    <p style="font-size:0.85rem; margin-bottom: 0.5rem;" id="remark-text-${img.id}"><strong>Remark:</strong> ${img.remark || "None"}</p>
                    <div style="display:flex; gap:0.25rem;">
                        <input type="text" placeholder="Add remark..." class="form-input" id="remark-input-${img.id}" style="padding: 0.35rem 0.6rem; font-size: 0.8rem;">
                        <button class="btn btn-primary" onclick="saveHairImageRemark('${img.id}', '${patientId}')" style="padding:0 0.5rem; font-size:0.8rem; border-radius:4px;"><i class="fa-solid fa-check"></i></button>
                    </div>
                </div>
            `;
            gridEl.appendChild(card);
        });
    } else {
        gridEl.innerHTML = '<p class="empty-list-msg">No logs uploaded by this patient.</p>';
    }
}

async function saveHairImageRemark(imageId, patientId) {
    const inputEl = document.getElementById(`remark-input-${imageId}`);
    const remark = inputEl.value;
    if (!remark) return;

    const resp = await fetch(`${API_BASE}/save_hair_remark.php`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ image_id: imageId, remark })
    });
    const data = await resp.json();
    if (data.status === "success") {
        alert("Remark saved!");
        loadPatientPortfolioPhotos(patientId);
    }
}

async function loadPatientPortfolioQuiz(patientId) {
    const listEl = document.getElementById("detail-list-quiz");
    listEl.innerHTML = '<p class="empty-list-msg">Loading assessments...</p>';

    const resp = await fetch(`${API_BASE}/get_quiz_reports.php?user_id=${patientId}`);
    const data = await resp.json();
    
    if (data.status === "success" && data.reports && data.reports.length > 0) {
        listEl.innerHTML = "";
        data.reports.forEach(r => {
            let answers = {};
            try {
                answers = typeof r.answers === 'string' ? JSON.parse(r.answers) : r.answers;
            } catch (e) {
                answers = {};
            }

            const item = document.createElement("div");
            item.className = "appointment-item";
            item.style.flexDirection = "column";
            item.style.alignItems = "flex-start";
            item.style.marginBottom = "1rem";
            
            const optionsHtml = Object.entries(answers).map(([key, val]) => `
                <li><strong>${key.replace('_', ' ').toUpperCase()}:</strong> ${val}</li>
            `).join('');

            item.innerHTML = `
                <div class="card-header-flex w-full">
                    <h5>Assessment Date: ${r.created_at}</h5>
                    <span class="status-badge status-${r.risk_level === 'high' ? 'rejected' : r.risk_level === 'moderate' ? 'pending' : 'approved'}">${r.risk_level.toUpperCase()} RISK</span>
                </div>
                <ul style="margin: 0.5rem 0 0 1.25rem; font-size: 0.85rem; color: var(--text-secondary);">
                    ${optionsHtml}
                </ul>
            `;
            listEl.appendChild(item);
        });
    } else {
        listEl.innerHTML = '<p class="empty-list-msg">No quiz reports submitted yet.</p>';
    }
}

async function loadPatientPortfolioRoutines(patientId) {
    const listEl = document.getElementById("detail-list-routines");
    listEl.innerHTML = '<li class="empty-list-msg">Loading routines...</li>';

    const resp = await fetch(`${API_BASE}/get_routines.php?user_id=${patientId}`);
    const data = await resp.json();
    
    if (data.status === "success" && data.routines && data.routines.length > 0) {
        listEl.innerHTML = "";
        data.routines.forEach(r => {
            const li = document.createElement("li");
            li.className = "routine-item";
            li.innerHTML = `
                <div class="routine-info">
                    <h4>${r.name}</h4>
                    <p>${r.dosage} • ${r.frequency} • ${r.time}</p>
                </div>
            `;
            listEl.appendChild(li);
        });
    } else {
        listEl.innerHTML = '<li class="empty-list-msg">No active routines found.</li>';
    }
}
