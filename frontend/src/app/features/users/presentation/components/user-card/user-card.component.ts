import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from "@angular/core";
import { UserDto } from "../../../data/dtos/user.dto";
import { MatCardModule } from "@angular/material/card";
import { MatDividerModule } from "@angular/material/divider";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { MatButtonModule } from "@angular/material/button";
import { MatMenuModule } from "@angular/material/menu";
import { MatTooltipModule } from "@angular/material/tooltip";

@Component({
  selector: "app-user-card",
  standalone: true,
  imports: [
    MatCardModule,
    MatDividerModule,
    IconsModule,
    MatButtonModule,
    MatMenuModule,
    MatTooltipModule,
  ],
  templateUrl: "./user-card.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserCardComponent {
  readonly user = input.required<UserDto>();
  readonly editUser = output<UserDto>();
}
