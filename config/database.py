import os
from motor.motor_asyncio import AsyncIOMotorClient
from dotenv import load_dotenv

load_dotenv()

MONGO_DETAILS = os.getenv("MONGODB_URL", "mongodb://localhost:27017")
DATABASE_NAME = os.getenv("DATABASE_NAME", "hotel_db")

client = None
database = None

async def connect_to_mongo():
    global client, database
    client = AsyncIOMotorClient(MONGO_DETAILS)
    database = client[DATABASE_NAME]
    print("Connected to MongoDB")

async def close_mongo_connection():
    if client:
        client.close()
    print("Disconnected from MongoDB")

def get_database():
    return database

def get_collection(collection_name: str):
    return database[collection_name]