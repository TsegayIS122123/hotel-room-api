# 🏨 Hotel Room API

A powerful **FastAPI-based REST API** for managing hotel rooms, offering full CRUD (Create, Read, Update, Delete) operations with **MongoDB integration**. 🌟  
This project is perfect for hotel management systems and provides a **scalable, high-performance backend solution**. 🚀  

---

## 📑 Table of Contents
- [✨ Features](#-features)
- [📋 Prerequisites](#-prerequisites)
- [⚙️ Installation](#️-installation)
- [🚀 Usage](#-usage)
- [📑 API Documentation](#-api-documentation)
- [📜 License](#-license)
- [🤝 Contributing](#-contributing)
- [📬 Contact](#-contact)

---

## ✨ Features

- **CRUD Operations**: Seamlessly create, retrieve, update, and delete hotel room data. 🛠️  
- **MongoDB Integration**: Robust persistent storage with MongoDB. 🗃️  
- **FastAPI Framework**: Lightning-fast performance with auto-generated OpenAPI documentation. ⚡  
- **Asynchronous**: Utilizes async/await for efficient and responsive request handling. ⏱️  
- **Interactive Docs**: Built-in Swagger UI for easy API exploration. 📚  

---

## 📋 Prerequisites

- Python 3.8+ 🐍  
- MongoDB (local or remote instance) 🖥️  
- pip (for installing dependencies) 📦  

---

## ⚙️ Installation

```bash
# Clone the repository
git clone https://github.com/TsegayIS122123/hotel-room-api.git

# Navigate to the project directory
cd hotel-room-api

# Install the required packages
pip install fastapi motor pymongo pydantic uvicorn
Ensure MongoDB is running locally at:
mongodb://localhost:27017
(Or update the URI in the code for a remote instance) 🌐

🚀 Usage
Run the API locally:

bash
Copy code
uvicorn main:app --reload
Available Endpoints:
Create a room → POST /rooms/ ➕

Get all rooms → GET /rooms/ 🔍

Get a room by ID → GET /rooms/{id} 📌

Update a room → PUT /rooms/{id} ✏️

Delete a room → DELETE /rooms/{id} 🗑️

📑 API Documentation
Once the API is running, explore the interactive Swagger UI by visiting:
👉 http://127.0.0.1:8000/docs 📖

Test endpoints, view schemas, and dive into the API details effortlessly! 🔧

📜 License
This project is licensed under the MIT License – see the LICENSE file for full details. 📝
Your freedom to use, modify, and distribute is guaranteed! 🎉

🤝 Contributing
We welcome contributions! 🌍
Fork this repository, submit issues, or create pull requests to enhance the project. 💡
Let’s build something amazing together! 🤝

📬 Contact
GitHub: TsegayIS122123 👨‍💻

Email: tsegayassefa27@gmail.com 📧

LinkedIn: Tsegay Assefa 💼

Portfolio: tsegayassefa.github.io 🌐