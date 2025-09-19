export interface HolidayUpdateDtoInterface {
  id: number;
  name: string;
  date: Date;
  type: string;
}

export interface HolidayCreateDtoInterface {
  name: string;
  date: Date;
  type: string;
}
