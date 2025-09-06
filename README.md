# 🏨 Hotel Room API

A powerful **FastAPI-based REST API** for managing hotel rooms, offering full CRUD (Create, Read, Update, Delete) operations with **MongoDB integration**. 🌟  
This project is perfect for hotel management systems and provides a **scalable, high-performance backend solution**. 🚀  

## 📑 Table of Contents
- [✨ Features](#-features)
- [📋 Prerequisites](#-prerequisites)
- [⚙️ Installation](#️-installation)
- [🚀 Usage](#-usage)
- [📑 API Documentation](#-api-documentation)
- [📜 License](#-license)
- [🤝 Contributing](#-contributing)
- [📬 Contact](#-contact)

## ✨ Features
- **CRUD Operations**: Seamlessly create, retrieve, update, and delete hotel room data.  
- **MongoDB Integration**: Robust persistent storage with MongoDB.  
- **FastAPI Framework**: Lightning-fast performance with auto-generated OpenAPI documentation.  
- **Asynchronous**: Utilizes async/await for efficient and responsive request handling.  
- **Interactive Docs**: Built-in Swagger UI for easy API exploration.  

## 📋 Prerequisites
Before running this project, ensure you have:  
- Python 3.8+ installed  
- MongoDB (local or remote instance) running  
- pip installed  

## ⚙️ Installation
Clone the repository and install dependencies:

```bash
git clone https://github.com/TsegayIS122123/hotel-room-api.git
cd hotel-room-api
pip install fastapi motor pymongo pydantic uvicorn
Make sure MongoDB is running locally at:
mongodb://localhost:27017
(Or update the URI in the code for a remote instance).

🚀 Usage
Run the API locally:

bash
Copy code
uvicorn main:app --reload
Available Endpoints
Create a room → POST /rooms/

Get all rooms → GET /rooms/

Get a room by ID → GET /rooms/{id}

Update a room → PUT /rooms/{id}

Delete a room → DELETE /rooms/{id}

📑 API Documentation
Once the API is running, open your browser and visit:
👉 http://127.0.0.1:8000/docs

You can test endpoints, view request/response schemas, and explore the API interactively using Swagger UI.

📜 License
This project is licensed under the MIT License – see the LICENSE file for details.
You are free to use, modify, and distribute this project.

🤝 Contributing
Contributions are welcome! 🎉

Fork the repository

Create a new branch for your feature or bugfix

Submit a pull request

Let’s build something amazing together! 🚀

📬 Contact
GitHub: TsegayIS122123

Email: tsegayassefa27@gmail.com

LinkedIn: Tsegay Assefa

Portfolio: tsegayassefa.github.io