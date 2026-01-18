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
