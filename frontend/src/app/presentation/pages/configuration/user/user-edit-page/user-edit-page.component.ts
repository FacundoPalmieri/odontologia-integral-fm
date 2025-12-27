import {
  Component,
  ElementRef,
  inject,
  OnDestroy,
  OnInit,
  signal,
  ViewChild,
} from "@angular/core";
import { PageToolbarComponent } from "../../../../components/page-toolbar/page-toolbar.component";
import { Router, ActivatedRoute } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { IconsModule } from "../../../../../utils/tabler-icons.module";
import { SnackbarService } from "../../../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../../utils/enums/snackbar-type.enum";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatSelectModule } from "@angular/material/select";
import { Subject, takeUntil } from "rxjs";
import { PersonDataService } from "../../../../../services/person-data.service";
import { ApiResponseInterface } from "../../../../../domain/interfaces/api-response.interface";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { RoleInterface } from "../../../../../domain/interfaces/role.interface";
import { MatIconModule } from "@angular/material/icon";
import { RoleService } from "../../../../../services/role.service";
import { UserInterface } from "../../../../../domain/interfaces/user.interface";
import { UserService } from "../../../../../services/user.service";
import {
  CountryInterface,
  DniTypeInterface,
  GenderInterface,
  LocalityInterface,
  NationalityInterface,
  PhoneTypeInterface,
  ProvinceInterface,
} from "../../../../../domain/interfaces/person-data.interface";
import { DentistSpecialtyInterface } from "../../../../../domain/interfaces/dentist.interface";
import { UserDtoInterface } from "../../../../../domain/dto/user.dto";
import { FileMetadataInterface } from "../../../../../domain/interfaces/patient.interface";
import { FileService } from "../../../../../services/file.service";
import { MatTableModule } from "@angular/material/table";
import { AttachedFileComponent } from "../../../../components/attached-file/attached-file.component";
import { EntityTypeEnum } from "../../../../../utils/enums/entity-type.enum";
import { RoleEnum } from "../../../../../utils/enums/role.enum";

@Component({
  selector: "app-user-edit-page",
  templateUrl: "./user-edit-page.component.html",
  standalone: true,
  imports: [
    PageToolbarComponent,
    MatCardModule,
    ReactiveFormsModule,
    IconsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDatepickerModule,
    MatIconModule,
    MatTableModule,
    AttachedFileComponent,
  ],
})
export class UserEditPageComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly snackbarService = inject(SnackbarService);
  private readonly userService = inject(UserService);
  private readonly _destroy$ = new Subject<void>();
  private readonly roleService = inject(RoleService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly fileService = inject(FileService);
  personDataService = inject(PersonDataService);

  @ViewChild("fileInput") fileInput!: ElementRef<HTMLInputElement>;
  @ViewChild("resourceFileInput")
  resourceFileInput!: ElementRef<HTMLInputElement>;

  userForm: FormGroup = new FormGroup({
    id: new FormControl<number>(0, [Validators.required]),
    username: new FormControl<string>("", [
      Validators.required,
      Validators.email,
    ]),
    rolesList: new FormControl<RoleInterface | null>(null, [
      Validators.required,
    ]),
    enabled: new FormControl<boolean>(false, [Validators.required]),
    person: new FormGroup({
      id: new FormControl<number>(0, [Validators.required]),
      firstName: new FormControl<string>("", [
        Validators.required,
        Validators.maxLength(30),
      ]),
      lastName: new FormControl<string>("", [
        Validators.required,
        Validators.maxLength(30),
      ]),
      dniType: new FormControl<DniTypeInterface | null>(null, [
        Validators.required,
      ]),
      dni: new FormControl<string | null>("", [
        Validators.required,
        Validators.maxLength(30),
      ]),
      birthDate: new FormControl<Date | null>(null, [Validators.required]),
      gender: new FormControl<GenderInterface | null>(null, [
        Validators.required,
      ]),
      nationality: new FormControl<NationalityInterface | null>(null, [
        Validators.required,
      ]),
      country: new FormControl<CountryInterface | null>(null, [
        Validators.required,
      ]),
      province: new FormControl<ProvinceInterface | null>(null, [
        Validators.required,
      ]),
      locality: new FormControl<LocalityInterface | null>(null, [
        Validators.required,
      ]),
      street: new FormControl<string | null>("", [
        Validators.required,
        Validators.maxLength(30),
      ]),
      number: new FormControl<number | null>(null, [Validators.required]),
      floor: new FormControl<number | null>(null, [
        Validators.min(0),
        Validators.max(99),
      ]),
      apartment: new FormControl<string | null>(null, [
        Validators.maxLength(2),
      ]),
      contactEmails: new FormControl<string>("", [
        Validators.email,
        Validators.required,
        Validators.maxLength(50),
      ]),
      phoneType: new FormControl<PhoneTypeInterface | null>(null, [
        Validators.required,
      ]),
      phone: new FormControl<number | null>(null, [
        Validators.required,
        Validators.maxLength(20),
      ]),
    }),
  });

  userId: number | null = null;
  maxDate = new Date();
  entityTypeEnum = EntityTypeEnum;

  avatarUrl = signal<string | null>(null);
  canDeleteAvatar = signal<boolean>(false);
  showProfessionalData = signal(false);
  countries = signal<CountryInterface[]>([]);
  localities = signal<LocalityInterface[]>([]);
  provinces = signal<ProvinceInterface[]>([]);
  roles = signal<RoleInterface[]>([]);
  filesMetadata = signal<FileMetadataInterface[]>([]);

  constructor() {
    this.userForm
      .get("rolesList")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((role: RoleInterface | null) => {
        // Convert single role to array for compatibility
        const roles = role ? [role] : [];
        this.updateLicenseNumberValidation(roles);
      });
  }

  ngOnInit() {
    this.roleService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<RoleInterface[]>) => {
        this.roles.set(response.data);
      });

    this.userForm
      .get("rolesList")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((selectedRole: RoleInterface | null) => {
        // Check if role exists

        const hasDentistOrAdministratorRole =
          selectedRole &&
          (selectedRole.name === RoleEnum.DENTIST ||
            selectedRole.name === RoleEnum.ADMINISTRATOR);
        const hasDentistRole =
          selectedRole && selectedRole.name === RoleEnum.DENTIST;

        this.showProfessionalData.set(!!hasDentistOrAdministratorRole);

        if (hasDentistOrAdministratorRole && !this.userForm.get("dentist")) {
          // Create dentist form group with conditional validators
          const validators = hasDentistRole ? [Validators.required] : [];
          this.userForm.addControl(
            "dentist",
            new FormGroup({
              licenseNumber: new FormControl<string | null>("", [
                ...validators,
                Validators.maxLength(30),
              ]),
              dentistSpecialty:
                new FormControl<DentistSpecialtyInterface | null>(
                  null,
                  validators
                ),
            })
          );
        } else if (
          !hasDentistOrAdministratorRole &&
          this.userForm.get("dentist")
        ) {
          this.userForm.removeControl("dentist");
        } else if (
          hasDentistOrAdministratorRole &&
          this.userForm.get("dentist")
        ) {
          // Update validators if dentist form group already exists
          const validators = hasDentistRole ? [Validators.required] : [];
          this.userForm
            .get("dentist.licenseNumber")
            ?.setValidators([...validators, Validators.maxLength(30)]);
          this.userForm
            .get("dentist.dentistSpecialty")
            ?.setValidators(validators);
          this.userForm.get("dentist.licenseNumber")?.updateValueAndValidity();
          this.userForm
            .get("dentist.dentistSpecialty")
            ?.updateValueAndValidity();
        }
      });

    this.userForm
      .get("person.country")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((country: CountryInterface) => {
        if (country) {
          this._getProvincesByCountryId(country.id);
        } else {
          this.provinces.set([]);
        }
      });

    this.userForm
      .get("person.province")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((province: ProvinceInterface) => {
        if (province) {
          this._getLocalitiesByProvinceId(province.id);
        } else {
          this.localities.set([]);
        }
      });

    this._getUserIdFromRoute();
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  goBack(): void {
    this.router.navigate(["/configuration/users"]);
  }

  triggerFileInput(): void {
    this.fileInput.nativeElement.click();
  }

  compare = (
    item1:
      | GenderInterface
      | LocalityInterface
      | NationalityInterface
      | ProvinceInterface
      | CountryInterface
      | DniTypeInterface
      | PhoneTypeInterface
      | RoleInterface
      | DentistSpecialtyInterface
      | null,
    item2:
      | GenderInterface
      | LocalityInterface
      | NationalityInterface
      | ProvinceInterface
      | CountryInterface
      | DniTypeInterface
      | PhoneTypeInterface
      | RoleInterface
      | DentistSpecialtyInterface
      | null
  ): boolean => {
    return item1 && item2 ? item1.id === item2.id : item1 === item2;
  };

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
    const oldAvatar = this.avatarUrl();
    reader.onload = (e) => {
      this.avatarUrl.set(e.target?.result as string);
      this.userForm.markAsDirty();
    };
    reader.readAsDataURL(file);

    const personId = this.userForm.get("person.id")?.value;
    if (personId) {
      this.personDataService
        .setAvatar(personId, file)
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
            this.avatarUrl.set(oldAvatar);
          },
        });
    }
  }

  viewFile(fileId: number): void {
    this.fileService.downloadUserFile(fileId).subscribe((blob: Blob) => {
      const url = window.URL.createObjectURL(blob);
      window.open(url, "_blank");
    });
  }

  downloadFile(fileId: number, fileName: string): void {
    this.fileService.downloadUserFile(fileId).subscribe((blob: Blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = fileName;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  triggerResourceFileInput(): void {
    this.resourceFileInput.nativeElement.click();
  }

  onStudyFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const allowedTypes = [
      "application/pdf",
      "application/msword",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    ];
    if (!allowedTypes.includes(file.type)) {
      this.snackbarService.openSnackbar(
        "El archivo debe ser PDF o Word (doc, docx)",
        6000,
        "center",
        "bottom",
        SnackbarTypeEnum.Error
      );
      return;
    }

    this.fileService
      .uploadUserFile(this.userId!, file)
      .pipe(takeUntil(this._destroy$))
      .subscribe({
        next: () => {
          this.fileService
            .getUserFilesMetadata(this.userId!)
            .subscribe(
              (response: ApiResponseInterface<FileMetadataInterface[]>) => {
                this.filesMetadata.set(response.data);
              }
            );
          this.snackbarService.openSnackbar(
            "Archivo subido correctamente.",
            6000,
            "center",
            "top",
            SnackbarTypeEnum.Success
          );
        },
        error: () => {},
      });
  }

  private _getUserFiles(id: number) {
    this.fileService
      .getUserFilesMetadata(id)
      .subscribe((response: ApiResponseInterface<FileMetadataInterface[]>) => {
        this.filesMetadata.set(response.data);
      });
  }

  removeAvatar(): void {
    this.personDataService.removeAvatar(this.personId).subscribe(() => {
      const gender = this.userForm.get("person.gender")?.value;
      this.personDataService
        .getAvatar(this.personId)
        .subscribe((avatar: string | null) => {
          if (avatar) {
            this.avatarUrl.set(avatar);
            this.canDeleteAvatar.set(true);
          } else {
            const genderName = gender?.name?.toLowerCase();
            this.avatarUrl.set(
              genderName === "femenino"
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

  save() {
    const formValue = this.userForm.getRawValue();
    // Convert single role back to array for backend compatibility
    const user: UserInterface = {
      ...formValue,
      rolesList: formValue.rolesList ? [formValue.rolesList] : [],
    };
    this.userService
      .update(user)
      .subscribe((response: ApiResponseInterface<UserDtoInterface>) => {
        this.snackbarService.openSnackbar(
          "El usuario ha sido modificado.",
          6000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
        this._loadUserData();
      });
  }

  private _getProvincesByCountryId(id: number) {
    this.personDataService
      .getProvinceByCountryId(id)
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<ProvinceInterface[]>) => {
        this.provinces.set(response.data);
      });
  }

  private _getLocalitiesByProvinceId(id: number) {
    this.personDataService
      .getLocalityByProvinceId(id)
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<LocalityInterface[]>) => {
        this.localities.set(response.data);
      });
  }

  private updateLicenseNumberValidation(roles: RoleInterface[] | null): void {
    if (!roles || roles.length === 0) {
      this.userForm.get("dentist.licenseNumber")?.clearValidators();
      this.userForm.get("dentist.dentistSpecialty")?.clearValidators();
      this.showProfessionalData.set(false);
      return;
    }

    // Get the single selected role
    const selectedRole = roles[0];
    const hasDentistAdministratorRole =
      selectedRole.name === RoleEnum.DENTIST ||
      selectedRole.name === RoleEnum.ADMINISTRATOR;
    const hasDentistRole = selectedRole.name === RoleEnum.DENTIST;

    this.showProfessionalData.set(hasDentistAdministratorRole);

    // Only require license number and specialty for DENTIST role
    if (hasDentistRole) {
      this.userForm
        .get("dentist.licenseNumber")
        ?.setValidators([Validators.required, Validators.maxLength(30)]);
      this.userForm
        .get("dentist.dentistSpecialty")
        ?.setValidators([Validators.required]);
    } else {
      // For ADMINISTRATOR, make them optional
      this.userForm
        .get("dentist.licenseNumber")
        ?.setValidators([Validators.maxLength(30)]);
      this.userForm.get("dentist.dentistSpecialty")?.clearValidators();
    }
    this.userForm.get("dentist.licenseNumber")?.updateValueAndValidity();
    this.userForm.get("dentist.dentistSpecialty")?.updateValueAndValidity();
  }

  private _getUserIdFromRoute() {
    this.activatedRoute.params.subscribe((params) => {
      this.userId = params["id"];
      if (this.userId) {
        this._loadUserData();
      } else {
        this.snackbarService.openSnackbar(
          "El usuario no se pudo cargar correctamente.",
          6000,
          "center",
          "bottom",
          SnackbarTypeEnum.Error
        );
        this.goBack();
      }
    });
  }

  private _loadUserData() {
    if (!this.userId) return;

    this.userService
      .getById(this.userId)
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<UserInterface>) => {
        const user = response.data;

        if (user.person?.id) {
          this.personDataService
            .getAvatar(user.person?.id)
            .pipe(takeUntil(this._destroy$))
            .subscribe((avatar: string | null) => {
              if (avatar) {
                this.avatarUrl.set(avatar);
                this.canDeleteAvatar.set(true);
              } else {
                const gender = user.person?.gender?.name?.toLowerCase();
                this.avatarUrl.set(
                  gender === "femenino"
                    ? "img/women-avatar.png"
                    : "img/men-avatar.png"
                );
                this.canDeleteAvatar.set(false);
              }
            });
        }
        this._getUserFiles(this.userId!);
        this._populateForm(user);
      });
  }

  private _populateForm(user: UserInterface) {
    // Since rolesList is now single-selection, we need to pass only the first role
    const selectedRole =
      user.rolesList && user.rolesList.length > 0 ? user.rolesList[0] : null;

    this.userForm.patchValue({
      id: user.id,
      username: user.username,
      rolesList: selectedRole,
      enabled: user.enabled,
    });

    if (user.person && this.userForm.get("person")) {
      this.userForm.get("person")?.patchValue({
        id: user.person.id,
        firstName: user.person.firstName,
        lastName: user.person.lastName,
        dniType: user.person.dniType,
        dni: user.person.dni,
        birthDate: user.person.birthDate
          ? new Date(user.person.birthDate)
          : null,
        gender: user.person.gender,
        nationality: user.person.nationality,
        country: user.person.country,
        province: user.person.province,
        locality: user.person.locality,
        street: user.person.street,
        number: user.person.number,
        floor: user.person.floor,
        apartment: user.person.apartment,
        contactEmails: user.person.contactEmails,
        phoneType: user.person.phoneType,
        phone: user.person.phone,
      });
    }

    const roles = user.rolesList || [];
    // Get the single selected role
    const userRole = roles.length > 0 ? roles[0] : null;
    const hasDentistOrAdministratorRole =
      userRole &&
      (userRole.name === RoleEnum.DENTIST ||
        userRole.name === RoleEnum.ADMINISTRATOR);
    const hasDentistRole = userRole && userRole.name === RoleEnum.DENTIST;

    if (hasDentistOrAdministratorRole && !this.userForm.get("dentist")) {
      // Create dentist form group with conditional validators
      const validators = hasDentistRole ? [Validators.required] : [];
      this.userForm.addControl(
        "dentist",
        new FormGroup({
          licenseNumber: new FormControl<string | null>("", [
            ...validators,
            Validators.maxLength(30),
          ]),
          dentistSpecialty: new FormControl<DentistSpecialtyInterface | null>(
            null,
            validators
          ),
        })
      );
    }

    if (
      (user.dentist?.licenseNumber || user.dentist?.dentistSpecialty) &&
      this.userForm.get("dentist")
    ) {
      this.userForm.get("dentist")?.patchValue({
        licenseNumber: user.dentist.licenseNumber,
        dentistSpecialty: user.dentist.dentistSpecialty,
      });
    }
    this.userForm.markAsPristine();
  }

  get personId(): number {
    return this.userForm.get("person.id")?.value;
  }
}
