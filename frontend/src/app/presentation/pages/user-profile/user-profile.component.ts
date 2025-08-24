import { Component, effect, inject, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";
import { MatCardModule } from "@angular/material/card";
import { UserService } from "../../../services/user.service";
import { AuthService } from "../../../services/auth.service";
import { Subject, takeUntil } from "rxjs";
import { UserInterface } from "../../../domain/interfaces/user.interface";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { MatChipsModule } from "@angular/material/chips";
import { Router } from "@angular/router";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { MatTabsModule } from "@angular/material/tabs";
import { PersonDataService } from "../../../services/person-data.service";

@Component({
  selector: "app-user-profile",
  templateUrl: "./user-profile.component.html",
  styleUrls: ["./user-profile.component.scss"],
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatCardModule,
    MatChipsModule,
    MatTabsModule,
  ],
})
export class UserProfileComponent implements OnInit {
  private readonly _destroy$ = new Subject<void>();
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly router = inject(Router);
  private readonly personDataService = inject(PersonDataService);

  userId = signal<number>(0);
  user = signal<UserInterface | null>(null);
  selectedTabIndex = signal<number>(0);
  avatar = signal<string | null>(null);

  constructor() {
    effect(() => {
      if (this.userId()) {
        this.userService
          .getById(this.userId())
          .pipe(takeUntil(this._destroy$))
          .subscribe((response: ApiResponseInterface<UserInterface>) => {
            if (!response.data || !response.data.person) {
              this.snackbarService.openSnackbar(
                "Ha ocurrido un error.",
                6000,
                "center",
                "bottom",
                SnackbarTypeEnum.Error
              );
              this.router.navigate(["/"]);
            } else {
              this.user.set(response.data);
            }
          });
      }
    });

    effect(() => {
      if (this.user()) {
        this.personDataService
          .getAvatar(this.user()?.person?.id!)
          .subscribe((avatar: string) => {
            this.avatar.set(avatar);
          });
      }
    });
  }

  ngOnInit(): void {
    this.userId.set(this.authService.getUserData()!.idUser);
  }
}
