# 🏨 Hotel Room API

A FastAPI-based REST API for managing hotel rooms, featuring full **CRUD (Create, Read, Update, Delete)** operations with MongoDB.  
This project is ideal for hotel management systems and provides a scalable backend solution.

---

## ✨ Features
- **CRUD Operations**: Create, retrieve, update, and delete hotel room data.  
- **MongoDB Integration**: Uses MongoDB for persistent storage.  
- **FastAPI Framework**: High performance with automatic OpenAPI documentation.  
- **Asynchronous**: Leverages `async/await` for efficient request handling.  

---

## 📋 Prerequisites
- Python **3.8+**  
- MongoDB (local or remote instance)  
- `pip` (for installing dependencies)  

---

## ⚙️ Installation

1. Clone the repository:

     git clone https://github.com/TsegayIS122123/hotel-room-api.git

2. Navigate to the project directory:
     cd hotel-room-api

3. Install the required packages:
     pip install fastapi motor pymongo pydantic uvicorn

4. Ensure MongoDB is running locally on:
   mongodb://localhost:27017
   (Or update the URI in the code for a remote instance)

🚀 Usage
Run the API locally:
    uvicorn main:app --reload

Available endpoints:

   Create a room → POST /rooms/
   Get all rooms → GET /rooms/
   Get a room by ID → GET /rooms/{id}
   Update a room → PUT /rooms/{id}
   Delete a room → DELETE /rooms/{id}

📑 API Documentation
Once running, visit:
👉 http://127.0.0.1:8000/docs for the interactive Swagger UI.

📜 License
This project is licensed under the MIT License – see the LICENSE file for details.

🤝 Contributing
Feel free to fork this repository, submit issues, or create pull requests to improve the project.

📬 Contact
For questions or support, reach out via GitHub: TsegayIS122123

