import {
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  OnDestroy,
  OnInit,
  signal,
  ViewChild,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatCardModule } from "@angular/material/card";
import { UserService } from "../../../services/user.service";
import { AuthService } from "../../../services/auth.service";
import { DentistService } from "../../../services/dentist.service";
import { Subject, takeUntil } from "rxjs";
import { UserInterface } from "../../../domain/interfaces/user.interface";
import { PersonInterface } from "../../../domain/interfaces/person.interface";
import { DentistAvailabilityResponseInterface } from "../../../domain/interfaces/dentist.interface";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { MatChipsModule } from "@angular/material/chips";
import { Router } from "@angular/router";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { MatTabsModule } from "@angular/material/tabs";
import { PersonDataService } from "../../../services/person-data.service";
import { MatButtonModule } from "@angular/material/button";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatTooltipModule } from "@angular/material/tooltip";
import { AttachedFileComponent } from "../../components/attached-file/attached-file.component";
import { EntityTypeEnum } from "../../../utils/enums/entity-type.enum";
import { AppointmentConflictComponent } from "../../components/appointment-conflict/appointment-conflict.component";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { PersonDataEditDialogComponent } from "./person-data-edit-dialog/person-data-edit-dialog.component";

@Component({
  selector: "app-user-profile",
  templateUrl: "./user-profile.component.html",
  styleUrls: ["./user-profile.component.scss"],
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    MatCardModule,
    MatChipsModule,
    MatTabsModule,
    MatButtonModule,
    MatExpansionModule,
    MatTooltipModule,
    AttachedFileComponent,
    AppointmentConflictComponent,
    MatDialogModule,
  ],
})
export class UserProfileComponent implements OnInit, OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly router = inject(Router);
  private readonly personDataService = inject(PersonDataService);
  private readonly dialog = inject(MatDialog);

  @ViewChild("inputAvatar") inputAvatar!: ElementRef<HTMLInputElement>;

  userId = signal<number>(0);
  user = signal<UserInterface | null>(null);
  selectedTabIndex = signal<number>(0);
  avatar = signal<string | null>(null);
  canDeleteAvatar = signal<boolean>(false);
  isPersonFormValid = signal<boolean>(false);
  updatedPersonData = signal<PersonInterface | null>(null);
  isSaving = signal<boolean>(false);
  isAnyPanelExpanded = signal<boolean>(false);
  selectedPreference = signal<string | null>(null);
  dentistAvailability = signal<DentistAvailabilityResponseInterface | null>(
    null
  );
  isLoadingAvailability = signal<boolean>(false);

  entityTypeEnum = EntityTypeEnum;

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
          .subscribe((avatar: string | null) => {
            if (avatar) {
              this.avatar.set(avatar);
              this.canDeleteAvatar.set(true);
            } else {
              const gender = this.user()?.person?.gender?.name.toLowerCase();
              this.avatar.set(
                gender === "femenino"
                  ? "img/women-avatar.png"
                  : "img/men-avatar.png"
              );
              this.canDeleteAvatar.set(false);
            }
          });
      }
    });
  }

  ngOnInit(): void {
    this.userId.set(this.authService.getUserData()!.idUser);
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  triggerFileInput(): void {
    this.inputAvatar.nativeElement.click();
  }

  removeAvatar(): void {
    this.personDataService
      .removeAvatar(this.user()?.person?.id!)
      .subscribe(() => {
        this.personDataService
          .getAvatar(this.user()?.person?.id!)
          .subscribe((avatar: string | null) => {
            if (avatar) {
              this.avatar.set(avatar);
              this.canDeleteAvatar.set(true);
            } else {
              const gender = this.user()?.person?.gender?.name.toLowerCase();
              this.avatar.set(
                gender === "femenino"
                  ? "img/women-avatar.png"
                  : "img/men-avatar.png"
              );
              this.canDeleteAvatar.set(false);
            }
          });
        this.snackbarService.openSnackbar(
          "Imagen de perfil eliminada.",
          6000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
      });
  }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    if (!file.type.startsWith("image/")) {
      this.snackbarService.openSnackbar(
        "El archivo debe ser una imagen.",
        6000,
        "center",
        "bottom",
        SnackbarTypeEnum.Error
      );
      return;
    }
    const reader = new FileReader();
    const oldAvatar = this.avatar();
    reader.onload = (e) => {
      this.avatar.set(e.target?.result as string);
    };
    reader.readAsDataURL(file);

    if (this.user()?.person?.id) {
      this.personDataService
        .setAvatar(this.user()?.person?.id!, file)
        .pipe(takeUntil(this._destroy$))
        .subscribe({
          next: () => {
            this.snackbarService.openSnackbar(
              "Imagen de perfil actualizada correctamente.",
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Success
            );
            this.canDeleteAvatar.set(true);
          },
          error: () => {
            this.avatar.set(oldAvatar);
          },
        });
    }
  }

  openEditPersonDialog(): void {
    const dialogRef = this.dialog.open(PersonDataEditDialogComponent, {
      data: {
        personData: this.user()?.person,
      },
      width: "800px",
      maxHeight: "90vh",
    });

    dialogRef.afterClosed().subscribe((result: PersonInterface | undefined) => {
      if (result) {
        const updatedUser: UserInterface = {
          ...this.user()!,
          person: result,
        };

        this.userService
          .update(updatedUser)
          .pipe(takeUntil(this._destroy$))
          .subscribe({
            next: () => {
              this.snackbarService.openSnackbar(
                "Datos personales actualizados correctamente.",
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success
              );

              this.userService
                .getById(this.userId())
                .pipe(takeUntil(this._destroy$))
                .subscribe(
                  (userResponse: ApiResponseInterface<UserInterface>) => {
                    this.user.set(userResponse.data);
                  }
                );
            },
          });
      }
    });
  }
}
