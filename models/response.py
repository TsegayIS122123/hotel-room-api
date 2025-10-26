from typing import Generic, TypeVar, Optional, List
from pydantic import BaseModel

T = TypeVar('T')

class StandardResponse(BaseModel):
    success: bool
    message: str
    data: Optional[T] = None

class PaginatedResponse(BaseModel):
    success: bool
    message: str
    data: List[T]
    total: int
    skip: int
    limit: int