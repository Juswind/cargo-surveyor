# CargoSurveyor: Visual Defect & Container Inspection Assistant

A multimodal mobile inspection assistant designed for freight forwarding, warehouse, and yard operations teams to cross-examine multiple container and cargo angles into a single, structured quality-assurance survey report.

---

## Business Problem & Functional Context

In global ocean and air freight operations, container and cargo inspections rely heavily on scattered phone photos, inconsistent checklists, and manual email reporting. Critical compliance issues—such as structural dents, water ingress risks, tampered High-Security Seals (HSS), or missing hazard placards—often go unnoticed until a shipment is rejected at origin or destination.

As a **Senior Business Analyst**, I conceptualized and built **CargoSurveyor** to standardize pre-loading and receiving inspections: turning multi-angle field photography into deterministic, audit-ready exception reports with clear severity ratings and corrective actions.

---

## Key Capabilities

* **Multi-Angle Photo Correlation:** Evaluates multiple container and cargo perspectives simultaneously (front exterior, rear doors, interior side walls, floor integrity, cargo lashing/packaging, and bolt seal close-ups) to produce one unified survey report.
* **Deterministic Structured Reporting:** Outputs consistent inspection findings categorized by **Pass / Warn / Fail** status, defect severity levels, and recommended corrective action steps.
* **Container & Cargo Integrity Checks:**
  * Structural condition (dents, holes, light leaks, rust, floor damage)
  * Security & compliance (High-Security Bolt Seal verification, container number legibility, IMO/hazard label visibility)
  * Cargo condition (crushed cartons, moisture damage, improper palletization or load distribution)
* **Mobile Performance Optimization:** Incorporates on-device image downscaling prior to multimodal analysis to ensure fast responsiveness and prevent memory (OOM) crashes during multi-photo inspections.

---

## Inspection Workflow (As-Is vs. To-Be)

| Stage | Traditional Field Process (As-Is) | With CargoSurveyor (To-Be) |
| :--- | :--- | :--- |
| **Capture** | Ad-hoc photos saved in personal phone galleries | Guided multi-angle capture across standardized container checkpoints |
| **Analysis** | Subjective manual review; subtle seal or wall defects easily missed | Automated cross-angle multimodal defect and compliance verification |
| **Reporting** | Delayed manual email summaries with unorganized attachments | Instant, structured Pass/Warn/Fail survey report ready for operational sign-off |

---

## Tech Stack

* **Language & UI:** Kotlin, Jetpack Compose (Android)
* **Multimodal AI Engine:** Google AI Studio / Gemini Multimodal API (Structured JSON Outputs)
* **Domain Focus:** Container Yard QA, Freight Forwarding Operations, Cargo Loss Prevention

---

## Local Setup & Configuration

1. Clone this repository:
   ```bash
   git clone [https://github.com/Juswind/cargo-surveyor.git](https://github.com/Juswind/cargo-surveyor.git)

  (Update the repository URL above if you name your GitHub repo differently.)
2. Open the project in Android Studio.
3. Copy .env.example to .env (or add your key to local.properties) and configure your own Gemini API key:GEMINI_API_KEY=your_api_key_here
4. Build and run on an Android device or emulator.

## About the Author
Andrews Rajkumar — Senior Business Analyst | Supply Chain, ERP & TMS Workflows | CargoWise Certified Professional (CCP)

Portfolio: (https://juswind.github.io/)

LinkedIn: (https://www.linkedin.com/in/andrews-rajkumar/)

YouTube: (https://www.youtube.com/@AndrewsRajkumarEDI)
