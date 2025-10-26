from fastapi import APIRouter, Query, HTTPException, status
from typing import Optional, List
from schemas.room import RoomCreate, RoomUpdate, RoomResponse
from models.response import StandardResponse, PaginatedResponse
from crud.room import RoomCRUD

router = APIRouter(prefix="/rooms", tags=["Rooms"])

@router.post("/", response_model=StandardResponse[RoomResponse], status_code=status.HTTP_201_CREATED)
async def create_room(room: RoomCreate):
    try:
        created_room = await RoomCRUD.create_room(room)
        return StandardResponse(
            success=True,
            message="Room created successfully",
            data=created_room
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Internal server error"
        )

@router.get("/", response_model=PaginatedResponse[RoomResponse])
async def get_rooms(
    skip: int = Query(0, ge=0, description="Number of records to skip"),
    limit: int = Query(100, ge=1, le=1000, description="Number of records to return"),
    available: Optional[bool] = Query(None, description="Filter by availability")
):
    try:
        rooms = await RoomCRUD.get_rooms(skip=skip, limit=limit, available=available)
        total = await RoomCRUD.get_rooms_count(available=available)
        
        return PaginatedResponse(
            success=True,
            message=f"Retrieved {len(rooms)} rooms",
            data=rooms,
            total=total,
            skip=skip,
            limit=limit
        )


    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Internal server error"
        )

@router.get("/{room_id}", response_model=StandardResponse[RoomResponse])
async def get_room(room_id: str):
    try:
        room = await RoomCRUD.get_room_by_id(room_id)
        return StandardResponse(
            success=True,
            message="Room retrieved successfully",
            data=room
        )

        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Internal server error"
        )



@router.put("/{room_id}", response_model=StandardResponse[RoomResponse])
async def update_room(room_id: str, room: RoomUpdate):
    try:
        updated_room = await RoomCRUD.update_room(room_id, room)
        return StandardResponse(
            success=True,
            message="Room updated successfully",
            data=updated_room
        )


    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Internal server error"
        )

@router.delete("/{room_id}")
async def delete_room(room_id: str):
    try:
        await RoomCRUD.delete_room(room_id)
        return StandardResponse(
            success=True,
            message="Room deleted successfully",
            data=None
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Internal server error"
        )