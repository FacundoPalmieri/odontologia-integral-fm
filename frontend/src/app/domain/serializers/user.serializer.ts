import { UserCreateDtoInterface, UserDtoInterface } from "../dto/user.dto";
import { UserInterface } from "../interfaces/user.interface";

export class UserSerializer {
  // static toView(user: UserDtoInterface): UserInterface {
  //   const userView: UserInterface = {
  //     id: user.id,
  //     username: user.username,
  //     enabled: user.enabled,
  //     rolesList: user.rolesList,
  //     person: PersonSerializer.toView(user.person),
  //   };
  //   return userView;
  // }
  // static toCreateDto(user: UserInterface): UserCreateDtoInterface | {} {
  //   return {};
  // }
}
