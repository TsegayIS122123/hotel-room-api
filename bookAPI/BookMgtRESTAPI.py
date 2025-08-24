from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import Optional, List
from motor.motor_asyncio import AsyncIOMotorClient
from bson import ObjectId

app = FastAPI()

MONGO_DETAILS = "mongodb://localhost:27017"  # Local MongoDB URI
client = AsyncIOMotorClient(MONGO_DETAILS)
database = client.hotel_db      # You can name it anything you want
collection = database.rooms     # Collection for hotel rooms

# Convert MongoDB document _id to string for API responses
def room_helper(room) -> dict:
    return {
        "id": str(room["_id"]),
        "number": room["number"],
        "type": room["type"],
        "price": room["price"],
        "available": room["available"],
    }

# Pydantic models for validation
class RoomModel(BaseModel):
    number: int = Field(...)
    type: str = Field(...)
    price: float = Field(...)
    available: Optional[bool] = True

class UpdateRoomModel(BaseModel):
    number: Optional[int]
    type: Optional[str]
    price: Optional[float]
    available: Optional[bool]

# Create a new room
@app.post("/rooms/", response_model=dict)
async def create_room(room: RoomModel):
    room_dict = room.dict()
    new_room = await collection.insert_one(room_dict)
    created_room = await collection.find_one({"_id": new_room.inserted_id})
    return room_helper(created_room)

# Get all rooms
@app.get("/rooms/", response_model=List[dict])
async def get_rooms():
    rooms = []
    async for room in collection.find():
        rooms.append(room_helper(room))
    return rooms

# Get a room by ID
@app.get("/rooms/{id}", response_model=dict)
async def get_room(id: str):
    room = await collection.find_one({"_id": ObjectId(id)})
    if room:
        return room_helper(room)
    raise HTTPException(status_code=404, detail=f"Room {id} not found")

# Update a room by ID
@app.put("/rooms/{id}", response_model=dict)
async def update_room(id: str, room: UpdateRoomModel):
    room_dict = {k: v for k, v in room.dict().items() if v is not None}
    if room_dict:
        update_result = await collection.update_one({"_id": ObjectId(id)}, {"$set": room_dict})
        if update_result.modified_count == 1:
            updated_room = await collection.find_one({"_id": ObjectId(id)})
            if updated_room:
                return room_helper(updated_room)
    existing_room = await collection.find_one({"_id": ObjectId(id)})
    if existing_room:
        return room_helper(existing_room)
    raise HTTPException(status_code=404, detail=f"Room {id} not found")

# Delete a room by ID
@app.delete("/rooms/{id}")
async def delete_room(id: str):
    delete_result = await collection.delete_one({"_id": ObjectId(id)})
    if delete_result.deleted_count == 1:
        return {"detail": f"Room {id} deleted successfully"}
    raise HTTPException(status_code=404, detail=f"Room {id} not found")
