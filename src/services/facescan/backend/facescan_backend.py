from abc import ABC, abstractmethod
from typing import List

from src.services.facescan.constants import NO_LIMIT
from src.services.facescan.scanned_face import ScannedFace
from src.services.utils.nputils import Array3D


class FacescanBackend(ABC):
    ID = None

    def __init__(self):
        assert self.ID

    @abstractmethod
    def scan(self, img: Array3D,
             return_limit: int = NO_LIMIT,
             facenet_detection_threshold_c: float = None) -> List[ScannedFace]:
        raise NotImplementedError
