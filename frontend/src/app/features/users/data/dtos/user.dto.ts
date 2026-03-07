import { PersonCreateDto, PersonDto } from "../../../../shared/dtos/person.dto";
import {
  DentistCreateDto,
  DentistDataDto,
} from "../../../calendar/data/dtos/dentist.dto";
import { RoleDto } from "../../../roles/data/dtos/role.dto";

export interface UserDto {
  id: number;
  username: string;
  rolesList: RoleDto[];
  enabled: boolean;
  person: PersonDto;
  dentist: DentistDataDto;
  avatarUrl?: string;
}

export interface UserCreateDto {
  id: number;
  username: string;
  password1: string;
  password2: string;
  rolesList: number[];
  enabled: boolean;
  person: PersonCreateDto;
  dentist?: DentistCreateDto;
}
