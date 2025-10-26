from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import logging
from config.database import connect_to_mongo, close_mongo_connection
from api.routes import rooms, health

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Hotel Booking API",
    description="A professional hotel room management API",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Adjust in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Database event handlers
app.add_event_handler("startup", connect_to_mongo)
app.add_event_handler("shutdown", close_mongo_connection)

# Include routers
app.include_router(health.router, tags=["Health"])
app.include_router(rooms.router, prefix="/api/v1", tags=["Rooms"])

@app.get("/")
async def root():
    return {"message": "Hotel Booking API", "version": "1.0.0"}