import { SnackbarTypeEnum } from "../../utils/enums/snackbar-type.enum";

export interface SnackbarDataInterface {
  message: string;
  type: SnackbarTypeEnum;
  horizontalPosition: string;
  verticalPosition: string;
  duration: number;
}
