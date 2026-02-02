export interface ApiResponseInterface<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface PagedDataInterface<T> {
  content: T;
  size: number;
  totalPages: number;
  totalElements: number;
  number: number;
}
