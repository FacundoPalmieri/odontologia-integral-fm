import { PatientDto } from "../dtos/patient.dto";
import { PaginationParamsInterface } from "../../../../shared/interfaces/pagination-params.interface";

export interface PatientListState {
  patients: PatientDto[];
  totalElements: number;
  pageIndex: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
  isLoading: boolean;
  error: string | null;
}

export const initialPatientListState: PatientListState = {
  patients: [],
  totalElements: 0,
  pageIndex: 0,
  pageSize: 1000, // TODO: change to a smaller value when backend supports server-side search
  sortBy: "person.lastName",
  sortDirection: "asc",
  isLoading: false,
  error: null,
};

export type { PaginationParamsInterface as LoadPatientsParams };
