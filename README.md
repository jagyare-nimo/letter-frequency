# letter-frequency

This project is a Kotlin-based Spring Boot application that analyzes the frequency of 
letters in JavaScript/TypeScript files within a specified GitHub repository. 
The application uses Kotlin coroutines and Spring WebFlux to create a reactive web service.

## Features

- Fetches JavaScript/TypeScript files from a specified GitHub repository.
- Analyzes the frequency of each letter in the files.
- Exposes a REST API to return the frequency of letters as a JSON object.

## Technologies Used

- Kotlin
- Spring Boot
- Spring WebFlux
- Jackson
- Gradle

## Requirements

- Java 17
- Kotlin 1.9.24
- Gradle
- GitHub Personal Access Token

## Setup
### Clone the Repository

```bash
    git clone https://github.com/your-username/letter-frequency.git
    cd letter-frequency
```

## Build
```bash
 ./gradlew build
```

## Run
```bash
./gradlew bootRun
```

## Run Test
```bash
 ./gradlew test
```

## API EndPoint
URL: /api/letters/frequency
Method: GET
Response:
Status: 200 OK
Body: JSON object containing the frequency of each letter

## Sample Response

{
"a": 47255,
"b": 13838,
"c": 29028,
"d": 17151,
"e": 71155,
"f": 13295
}


