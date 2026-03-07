export interface SystemScheduleDto {
  id: number;
  label: string;
  cron: string;
}

export interface SystemScheduleUpdateDto {
  id: number;
  cronExpression: string;
}
