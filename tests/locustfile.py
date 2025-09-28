from locust import HttpUser, task, between
import random

class StudentUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        # Attempt login
        self.client.post("/api/auth/login", json={"username": "student", "password": "secret"})

    @task(3)
    def submit_assignment(self):
        filename = f"assignment_{random.randint(1,1000)}.txt"
        content = "Hello Madrasati" * 100
        self.client.post("/api/assignments/submit", json={
            "studentId": "s-" + str(random.randint(1, 100000)),
            "courseId": "c-" + str(random.randint(1, 200)),
            "filename": filename,
            "content": content
        })

    @task(1)
    def submit_exam(self):
        self.client.post("/api/exam/submit")
