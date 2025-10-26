from bson import ObjectId
from typing import Dict, Any

def room_helper(room) -> dict:
    if not room:
        return None
    return {
        "id": str(room["_id"]),
        "number": room["number"],
        "type": room["type"],
        "price": room["price"],
        "available": room["available"],
    }

def validate_objectid(id: str) -> bool:
    return ObjectId.is_valid(id)