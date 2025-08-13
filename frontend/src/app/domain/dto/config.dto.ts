export interface ScheduleDtoInterface {
  id: number;
  label: string;
  cron: string;
}

export interface ScheduleUpdateDtoInterface {
  id: number;
  cronExpression: string;
}

export interface SystemParameterDtoInterface {
  id: number;
  value: string;
  description: string;
}

export interface SystemParameterUpdateDtoInterface {
  id: number;
  value: string;
}

export interface MessageDtoInterface {
  id: number;
  key: string;
  value: string;
  locale: string;
}

export interface MessageUpdateDtoInterface {
  id: number;
  key: string;
  value: string;
  locale: string;
}
