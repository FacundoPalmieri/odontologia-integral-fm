
export enum ToothEnum {
  /* Cuadrante 1 – Superior derecho */
  T11 = "T11",
  T12 = "T12",
  T13 = "T13",
  T14 = "T14",
  T15 = "T15",
  T16 = "T16",
  T17 = "T17",
  T18 = "T18",

  /* Cuadrante 2 – Superior izquierdo */
  T21 = "T21",
  T22 = "T22",
  T23 = "T23",
  T24 = "T24",
  T25 = "T25",
  T26 = "T26",
  T27 = "T27",
  T28 = "T28",

  /* Cuadrante 3 – Inferior izquierdo */
  T31 = "T31",
  T32 = "T32",
  T33 = "T33",
  T34 = "T34",
  T35 = "T35",
  T36 = "T36",
  T37 = "T37",
  T38 = "T38",

  /* Cuadrante 4 – Inferior derecho */
  T41 = "T41",
  T42 = "T42",
  T43 = "T43",
  T44 = "T44",
  T45 = "T45",
  T46 = "T46",
  T47 = "T47",
  T48 = "T48",
}

export enum ToothFaceEnum {
    TOP = "TOP",
    BOTTOM = "BOTTOM",
    RIGHT = "RIGHT",
    LEFT = "LEFT",
    CENTER = "CENTER"
}

export enum QuadrantEnum {
    UPPER_RIGHT = 'UPPER_RIGHT',
    UPPER_LEFT = 'UPPER_LEFT',
    LOWER_LEFT = 'LOWER_LEFT',
    LOWER_RIGHT = 'LOWER_RIGHT'
}


export enum PrestationScopeEnum {
    TOOTH = "TOOTH",
    TOOTH_FACE = "TOOTH_FACE",
    QUADRANT = "QUADRANT",
    MAXILLARY = "MAXILLARY",
    FULL_MOUTH = "FULL_MOUTH"
}

export enum PrestationInstanceStatusEnum {
    IN_PROGRESS = 'IN_PROGRESS',
    COMPLETED = 'COMPLETED',
    CANCELED = 'CANCELED'
}

export enum DiscountTypeEnum {
    PERCENTAGE = 'PERCENTAGE',
    FIXED = 'FIXED'
}