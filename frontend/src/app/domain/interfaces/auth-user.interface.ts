export interface AuthUserInterface {
  username: string;
  message: string;
  jwt: string;
  status: boolean;
  roleAndPermission: string[];
}
