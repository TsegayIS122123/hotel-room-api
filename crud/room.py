from bson import ObjectId
from fastapi import HTTPException, status
from typing import List, Optional
from config.database import get_collection
from schemas.room import RoomCreate, RoomUpdate
from utils.helpers import room_helper, validate_objectid

collection = get_collection("rooms")

class RoomCRUD:
    @staticmethod
    async def create_room(room_data: RoomCreate) -> dict:
        # Check if room number exists
        existing_room = await collection.find_one({"number": room_data.number})
        if existing_room:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Room number {room_data.number} already exists"
            )
        
        room_dict = room_data.dict()
        result = await collection.insert_one(room_dict)
        new_room = await collection.find_one({"_id": result.inserted_id})
        return room_helper(new_room)

    @staticmethod
    async def get_rooms(skip: int = 0, limit: int = 100, available: Optional[bool] = None) -> List[dict]:
        query = {}
        if available is not None:
            query["available"] = available
        
        rooms = []
        cursor = collection.find(query).skip(skip).limit(limit)
        async for room in cursor:
            rooms.append(room_helper(room))
        return rooms

    @staticmethod
    async def get_room_by_id(room_id: str) -> dict:
        if not validate_objectid(room_id):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid room ID format"
            )
        
        room = await collection.find_one({"_id": ObjectId(room_id)})
        if not room:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Room {room_id} not found"
            )
        return room_helper(room)

    @staticmethod
    async def update_room(room_id: str, room_data: RoomUpdate) -> dict:
        if not validate_objectid(room_id):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid room ID format"
            )
        
        # Check if room exists
        existing_room = await collection.find_one({"_id": ObjectId(room_id)})
        if not existing_room:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Room {room_id} not found"
            )

        update_data = {k: v for k, v in room_data.dict().items() if v is not None}
        
        if update_data:
            await collection.update_one(
                {"_id": ObjectId(room_id)},
                {"$set": update_data}
            )
        
        updated_room = await collection.find_one({"_id": ObjectId(room_id)})
        return room_helper(updated_room)

    @staticmethod
    async def delete_room(room_id: str) -> bool:
        if not validate_objectid(room_id):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid room ID format"
            )
        
        result = await collection.delete_one({"_id": ObjectId(room_id)})
        if result.deleted_count == 0:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Room {room_id} not found"
            )
        return True

    @staticmethod
    async def get_rooms_count(available: Optional[bool] = None) -> int:
        query = {}
        if available is not None:
            query["available"] = available
        return await collection.count_documents(query)