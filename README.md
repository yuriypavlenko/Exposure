# EXPOSURE
## Exposure is an interactive detective game focused on logical investigation and AI-driven interrogation.
The core mechanic of the project is detecting lies by interrogating characters powered by Artificial Intelligence.
The player does not choose options from a list. Instead, they ask any questions in free-form text and independently analyze the answers, cross-referencing facts, motives, and timelines.

## Key Features
### Generative Detective.
Every story is generated from scratch.

### The AI creates a logical internal model of the case:

- Real events (the "ground truth");
- Distorted testimonies;
- Hidden motives of characters;
- Contradictions in the timeline.

### Interrogation Mechanics

- Free-text questioning;
- Characters can lie, dodge questions, or tell the truth;
- Responses depend on their role in the crime and their knowledge of what occurred.

### Logic Verification
To solve the case, the player must:

- Examine case files and documents;
- Compare timelines;
- Analyze motives;
- Identify logical inconsistencies;
- Link evidence with testimonies.

The AI does not provide the correct answer directly - the final decision rests solely with the player.

### Local AI
All generation is performed locally, without relying on cloud services or paid APIs:

- Powered by Ollama;
- Integrated via Spring AI;
- Full control over models and generation logic.

### Tech Stack

- Backend: Java 21
- Framework: Spring Boot 3.4+ & Spring AI
- Frontend: React (Vite)
- AI Engine: Ollama
- Database: PostgreSQL

#### Project Goal
Exposure explores the potential of creating interactive narratives where AI acts as a participant in a logical system, capable of making mistakes, lying, and concealing information.

## How to run
### Prerequisites
- Java 21
- Node.js (v18 or higher) & npm
- Docker & Docker Compose
- Ollama

### Step 1: Set up AI (Ollama)
1) Install Ollama [here](https://ollama.com/).
2) Pull the model: `ollama pull llama3.1`
3) Ensure Ollama is running locally (usually on port 11434).

### Step 2: Database (Docker)
Navigate to the backend directory *backend*. Find `docker-compose.yml` and run `docker-compose up -d`

### Step 3: Backend (Spring Boot)
1) Navigate to the backend directory *backend*.
2) Check configurations in `application.properties`. \
If you need custom local configuration, copy `application.properties` file and set new file name to `application-local.properties` and make changes in it.
3) Build and run the application:\
For windows: `./mvnw.cmd spring-boot:run`\
For Linux/MacOS: `./mvnw spring-boot:run`\
\
If you have custom configuration file build and run the application with local configuration:\
For windows: `./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"`\
For Linux/MacOS: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`\
\
The server will start on http:`http://localhost:8080`

### Step 4: Frontend (React)
1) Navigate to the frontend directory *frontend*.
2) Check configurations in `.env`. \
If you need custom local configuration, copy `.env` file and set new file name to `.env.local` and make changes in it.
2) Install dependencies: `npm install`
3) Start the development server: `npm run dev`\
   The UI will be available at `http://localhost:5173`
