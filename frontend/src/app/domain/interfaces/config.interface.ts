export interface MessageInterface {
  id: number;
  key: string;
  value: string;
  locale: string;
}

export interface SystemParameterInterface {
  id: number;
  value: number;
  description: string;
}

export interface ScheduleInterface {
  id: number;
  label: string;
  cron: string;
}
