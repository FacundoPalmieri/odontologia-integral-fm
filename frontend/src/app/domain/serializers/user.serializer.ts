import { UserCreateDtoInterface, UserDtoInterface } from "../dto/user.dto";
import { UserInterface } from "../interfaces/user.interface";

export class UserSerializer {
  static toCreateDto(user: UserInterface): UserCreateDtoInterface | {} {
    return {};
  }

  static toView(user: UserDtoInterface): UserInterface | {} {
    return {};
  }
}
