export interface MessageInterface {
  id: number;
  key: string;
  value: string;
  locale: string;
}

export interface SystemParameterInterface {
  id: number;
  value: string;
  description: string;
}

export interface ScheduleInterface {
  id: number;
  label: string;
  cron: string;
}
