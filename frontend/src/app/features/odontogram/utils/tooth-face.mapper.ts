import { ToothFaceEnum as OdontogramFace } from "./enums/tooth-face.enum";
import { ToothFaceEnum as ApiFace } from "../../patients/utils/enums/consultation-instance.enum";

export class ToothFaceMapper {
  
  /**
   * Mapea una cara anatómica (del odontograma) a su respectiva posición física requerida por la API.
   */
  static toApiFace(toothNumber: number, face: OdontogramFace): ApiFace {
    const quadrant = Math.floor(toothNumber / 10);
    const isUpperRight = quadrant === 1 || quadrant === 5;
    const isUpperLeft = quadrant === 2 || quadrant === 6;
    const isLowerRight = quadrant === 4 || quadrant === 8;

    if (face === OdontogramFace.OCLUSAL) {
      return ApiFace.CENTER;
    }

    if (isUpperRight) {
      switch (face) {
        case OdontogramFace.VESTIBULAR: return ApiFace.TOP;
        case OdontogramFace.PALATINO: return ApiFace.BOTTOM;
        case OdontogramFace.DISTAL: return ApiFace.LEFT;
        case OdontogramFace.MESIAL: return ApiFace.RIGHT;
      }
    } else if (isUpperLeft) {
      switch (face) {
        case OdontogramFace.VESTIBULAR: return ApiFace.TOP;
        case OdontogramFace.PALATINO: return ApiFace.BOTTOM;
        case OdontogramFace.MESIAL: return ApiFace.LEFT;
        case OdontogramFace.DISTAL: return ApiFace.RIGHT;
      }
    } else if (isLowerRight) {
      switch (face) {
        case OdontogramFace.LINGUAL: return ApiFace.TOP;
        case OdontogramFace.VESTIBULAR: return ApiFace.BOTTOM;
        case OdontogramFace.DISTAL: return ApiFace.LEFT;
        case OdontogramFace.MESIAL: return ApiFace.RIGHT;
      }
    } else {
      // Lower Left (Cuadrantes 3 y 7)
      switch (face) {
        case OdontogramFace.LINGUAL: return ApiFace.TOP;
        case OdontogramFace.VESTIBULAR: return ApiFace.BOTTOM;
        case OdontogramFace.MESIAL: return ApiFace.LEFT;
        case OdontogramFace.DISTAL: return ApiFace.RIGHT;
      }
    }
    
    return ApiFace.CENTER; // Fallback
  }

  /**
   * Mapea la posición de la API (visual) a la cara anatómica correcta según el diente.
   */
  static toOdontogramFace(toothNumber: number, apiFace: ApiFace): OdontogramFace {
    const quadrant = Math.floor(toothNumber / 10);
    const isUpperRight = quadrant === 1 || quadrant === 5;
    const isUpperLeft = quadrant === 2 || quadrant === 6;
    const isLowerRight = quadrant === 4 || quadrant === 8;

    if (apiFace === ApiFace.CENTER) {
      return OdontogramFace.OCLUSAL;
    }

    if (isUpperRight) {
      switch (apiFace) {
        case ApiFace.TOP: return OdontogramFace.VESTIBULAR;
        case ApiFace.BOTTOM: return OdontogramFace.PALATINO;
        case ApiFace.LEFT: return OdontogramFace.DISTAL;
        case ApiFace.RIGHT: return OdontogramFace.MESIAL;
      }
    } else if (isUpperLeft) {
      switch (apiFace) {
        case ApiFace.TOP: return OdontogramFace.VESTIBULAR;
        case ApiFace.BOTTOM: return OdontogramFace.PALATINO;
        case ApiFace.LEFT: return OdontogramFace.MESIAL;
        case ApiFace.RIGHT: return OdontogramFace.DISTAL;
      }
    } else if (isLowerRight) {
      switch (apiFace) {
        case ApiFace.TOP: return OdontogramFace.LINGUAL;
        case ApiFace.BOTTOM: return OdontogramFace.VESTIBULAR;
        case ApiFace.LEFT: return OdontogramFace.DISTAL;
        case ApiFace.RIGHT: return OdontogramFace.MESIAL;
      }
    } else {
      // Lower Left (Cuadrantes 3 y 7)
      switch (apiFace) {
        case ApiFace.TOP: return OdontogramFace.LINGUAL;
        case ApiFace.BOTTOM: return OdontogramFace.VESTIBULAR;
        case ApiFace.LEFT: return OdontogramFace.MESIAL;
        case ApiFace.RIGHT: return OdontogramFace.DISTAL;
      }
    }

    return OdontogramFace.OCLUSAL; // Fallback
  }
}
