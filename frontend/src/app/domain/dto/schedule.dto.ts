export interface ScheduleDtoInterface {
  id: number;
  name: string;
  label: string;
  cron: string;
}

export interface ScheduleUpdateDtoInterface {
  id: number;
  cronExpression: string;
}
