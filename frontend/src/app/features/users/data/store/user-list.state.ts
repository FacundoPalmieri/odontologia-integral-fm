import { UserDto } from "../dtos/user.dto";
import { PaginationParamsInterface } from "../../../../shared/interfaces/pagination-params.interface";

export interface UserListState {
  users: UserDto[];
  totalElements: number;
  pageIndex: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
  isLoading: boolean;
  error: string | null;
}

export const initialUserListState: UserListState = {
  users: [],
  totalElements: 0,
  pageIndex: 0,
  pageSize: 1000, // TODO: change to 5 when backend accepts server-side search
  sortBy: "username",
  sortDirection: "asc",
  isLoading: false,
  error: null,
};

export type { PaginationParamsInterface as LoadUsersParams };
