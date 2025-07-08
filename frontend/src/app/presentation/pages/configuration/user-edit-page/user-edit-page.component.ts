import {
  Component,
  ElementRef,
  inject,
  OnDestroy,
  OnInit,
  signal,
  ViewChild,
} from "@angular/core";
import { PageToolbarComponent } from "../../../components/page-toolbar/page-toolbar.component";
import { Router, ActivatedRoute } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { SnackbarService } from "../../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../utils/enums/snackbar-type.enum";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatSelectModule } from "@angular/material/select";
import { Subject, takeUntil } from "rxjs";
import { PersonDataService } from "../../../../services/person-data.service";
import { ApiResponseInterface } from "../../../../domain/interfaces/api-response.interface";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { RoleInterface } from "../../../../domain/interfaces/role.interface";
import { MatIconModule } from "@angular/material/icon";
import { RoleService } from "../../../../services/role.service";
import { UserInterface } from "../../../../domain/interfaces/user.interface";
import { UserService } from "../../../../services/user.service";
import {
  CountryInterface,
  DniTypeInterface,
  GenderInterface,
  LocalityInterface,
  NationalityInterface,
  PhoneTypeInterface,
  ProvinceInterface,
} from "../../../../domain/interfaces/person-data.interface";
import { DentistSpecialtyInterface } from "../../../../domain/interfaces/dentist.interface";
import { UserDtoInterface } from "../../../../domain/dto/user.dto";

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
  ],
})
export class UserEditPageComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly snackbarService = inject(SnackbarService);
  private readonly personDataService = inject(PersonDataService);
  private readonly userService = inject(UserService);
  private readonly _destroy$ = new Subject<void>();
  private readonly roleService = inject(RoleService);
  private readonly activatedRoute = inject(ActivatedRoute);
  personDataSerializer = inject(PersonDataService);

  @ViewChild("fileInput") fileInput!: ElementRef<HTMLInputElement>;

  userForm: FormGroup = new FormGroup({
    id: new FormControl<number>(0, [Validators.required]),
    username: new FormControl<string>("", [
      Validators.required,
      Validators.email,
    ]),
    rolesList: new FormControl<RoleInterface[] | null>(null, [
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

  avatarUrl = signal<string | null>(null);
  showProfessionalData = signal(false);
  countries = signal<CountryInterface[]>([]);
  localities = signal<LocalityInterface[]>([]);
  provinces = signal<ProvinceInterface[]>([]);
  roles = signal<RoleInterface[]>([]);

  constructor() {
    this.userForm
      .get("rolesList")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((roles: RoleInterface[]) => {
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
      .subscribe((roles: RoleInterface[]) => {
        const hasDentistRole = roles?.some(
          (role) => role.role === "Odontólogo"
        );
        this.showProfessionalData.set(hasDentistRole);

        if (hasDentistRole && !this.userForm.get("dentist")) {
          this.userForm.addControl(
            "dentist",
            new FormGroup({
              licenseNumber: new FormControl<string | null>("", [
                Validators.required,
                Validators.maxLength(30),
              ]),
              dentistSpecialty:
                new FormControl<DentistSpecialtyInterface | null>(null, [
                  Validators.required,
                ]),
            })
          );
        } else if (!hasDentistRole && this.userForm.get("dentist")) {
          this.userForm.removeControl("dentist");
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
    this.router.navigate(["/configuration"]);
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
          },
          error: () => {
            this.snackbarService.openSnackbar(
              "Error al actualizar la imagen de perfil.",
              6000,
              "center",
              "bottom",
              SnackbarTypeEnum.Error
            );
          },
        });
    }
  }

  removeAvatar(): void {
    this.avatarUrl.set(null);
    this.userForm.markAsDirty();
  }

  save() {
    const user: UserInterface = this.userForm.getRawValue();
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
        this.router.navigate(["/configuration/users/edit", response.data.id]);
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
    if (!roles) {
      this.userForm.get("licenseNumber")?.clearValidators();
      this.showProfessionalData.set(false);
      return;
    }

    const hasDentistRole = roles.some((role) => role.role === "Odontólogo");
    this.showProfessionalData.set(hasDentistRole);

    if (hasDentistRole) {
      this.userForm.get("licenseNumber")?.setValidators([Validators.required]);
      this.userForm
        .get("dentistSpecialty")
        ?.setValidators([Validators.required]);
    } else {
      this.userForm.get("licenseNumber")?.clearValidators();
      this.userForm.get("dentistSpecialty")?.clearValidators();
    }
    this.userForm.get("licenseNumber")?.updateValueAndValidity();
    this.userForm.get("dentistSpecialty")?.updateValueAndValidity();
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
            .subscribe((response: string) => {
              this.avatarUrl.set(response);
            });
        }
        this._populateForm(user);
      });
  }

  private _populateForm(user: UserInterface) {
    this.userForm.patchValue({
      id: user.id,
      username: user.username,
      rolesList: user.rolesList,
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
    const hasDentistRole = roles.some((role) => role.role === "Odontólogo");

    if (hasDentistRole && !this.userForm.get("dentist")) {
      this.userForm.addControl(
        "dentist",
        new FormGroup({
          licenseNumber: new FormControl<string | null>("", [
            Validators.required,
          ]),
          dentistSpecialty: new FormControl<DentistSpecialtyInterface | null>(
            null,
            [Validators.required]
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
}
