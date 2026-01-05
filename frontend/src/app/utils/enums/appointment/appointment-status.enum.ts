export enum AppointmentStatusEnum {
  RESERVED = "Reservado",
  ATTENDED = "Atendido",
  NO_SHOW = "Ausente",
  RESCHEDULED = "Reprogramado",
  CANCELED = "Cancelado",
}

export enum SlotStatusEnum {
  FREE = "FREE",
  RESERVED = "RESERVED",
  LOCKED = "LOCKED",
  NOT_AVAILABLE = "NOT_AVAILABLE",
  BREAK = "BREAK",
}

export enum CalendarMonthDayStatusEnum {
  FREE = "FREE",
  HOLIDAY = "HOLIDAY",
  LOCKED = "LOCKED",
  FULL = "FULL",
  NOT_AVAILABLE = "NOT_AVAILABLE",
}
