from pydantic import BaseModel, Field, validator
from typing import Optional, List
from bson import ObjectId

class PyObjectId(ObjectId):
    @classmethod
    def __get_validators__(cls):
        yield cls.validate

    @classmethod
    def validate(cls, v):
        if not ObjectId.is_valid(v):
            raise ValueError("Invalid objectid")
        return ObjectId(v)

    @classmethod
    def __modify_schema__(cls, field_schema):
        field_schema.update(type="string")

class RoomCreate(BaseModel):
    number: int = Field(..., gt=0, description="Room number must be positive")
    type: str = Field(..., min_length=2, max_length=50)
    price: float = Field(..., gt=0, description="Price must be positive")
    available: Optional[bool] = True

    @validator('type')
    def validate_room_type(cls, v):
        allowed_types = ['single', 'double', 'suite', 'deluxe']
        if v.lower() not in allowed_types:
            raise ValueError(f'Room type must be one of: {", ".join(allowed_types)}')
        return v.title()

class RoomUpdate(BaseModel):
    number: Optional[int] = Field(None, gt=0)
    type: Optional[str] = Field(None, min_length=2, max_length=50)
    price: Optional[float] = Field(None, gt=0)
    available: Optional[bool] = None

class RoomResponse(BaseModel):
    id: str
    number: int
    type: str
    price: float
    available: bool

    class Config:
        json_encoders = {ObjectId: str}