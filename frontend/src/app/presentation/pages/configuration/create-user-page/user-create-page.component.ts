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
import { Router } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
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
import { UserCreateDtoInterface } from "../../../../domain/dto/user.dto";
import { UserInterface } from "../../../../domain/interfaces/user.interface";
import { UserService } from "../../../../services/user.service";
import {
  CountryInterface,
  DniTypeInterface,
  GenderInterface,
  HealthPlanInterface,
  LocalityInterface,
  NationalityInterface,
  PhoneTypeInterface,
  ProvinceInterface,
} from "../../../../domain/interfaces/person-data.interface";
import { DentistSpecialtyInterface } from "../../../../domain/interfaces/dentist.interface";

@Component({
  selector: "app-create-user-page",
  templateUrl: "./user-create-page.component.html",
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
export class UserCreatePageComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly snackbarService = inject(SnackbarService);
  private readonly personDataService = inject(PersonDataService);
  private readonly userService = inject(UserService);
  private readonly _destroy$ = new Subject<void>();
  private readonly roleService = inject(RoleService);

  userForm: FormGroup = new FormGroup({});

  @ViewChild("fileInput") fileInput!: ElementRef<HTMLInputElement>;

  avatarUrl = signal<string | null>(null);
  showAdditionalInfo = signal(false);
  showProfessionalData = signal(false);

  hidePassword = signal(true);

  countries = signal<CountryInterface[]>([]);
  localities = signal<LocalityInterface[]>([]);
  provinces = signal<ProvinceInterface[]>([]);
  dniTypes = signal<DniTypeInterface[]>([]);
  genders = signal<GenderInterface[]>([]);
  nationalities = signal<NationalityInterface[]>([]);
  phoneTypes = signal<PhoneTypeInterface[]>([]);
  healthPlans = signal<HealthPlanInterface[]>([]);
  roles = signal<RoleInterface[]>([]);

  constructor() {
    this._loadForm();
    this._loadData();
  }

  ngOnInit() {
    this.userForm
      .get("country")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((country: CountryInterface) => {
        if (country) {
          this._getProvincesByCountryId(country.id);
        } else {
          this.provinces.set([]);
        }
      });

    this.userForm
      .get("province")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((province: ProvinceInterface) => {
        if (province) {
          this._getLocalitiesByProvinceId(province.id);
        } else {
          this.localities.set([]);
        }
      });

    this.userForm
      .get("rolesList")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((roles: RoleInterface[]) => {
        if (roles) {
          const hasDentistRole = roles.some(
            (role) => role.role === "Odontólogo"
          );
          this.showProfessionalData.set(hasDentistRole);
        } else {
          this.showProfessionalData.set(false);
        }
      });

    this.userForm
      .get("password1")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe(() => {
        this.userForm
          .get("password2")
          ?.updateValueAndValidity({ emitEvent: false });
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  goBack(): void {
    this.router.navigate(["/configuration"]);
  }

  toggleAdditionalInfo(): void {
    this.showAdditionalInfo.update((value) => !value);
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
      | HealthPlanInterface
      | RoleInterface
      | null,
    item2:
      | GenderInterface
      | LocalityInterface
      | NationalityInterface
      | ProvinceInterface
      | CountryInterface
      | DniTypeInterface
      | PhoneTypeInterface
      | HealthPlanInterface
      | RoleInterface
      | null
  ): boolean => {
    return item1 && item2 ? item1.id === item2.id : item1 === item2;
  };

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      if (!file.type.startsWith("image/")) {
        this.snackbarService.openSnackbar(
          "El archivo debe ser una imagen",
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
    }
  }

  removeAvatar(): void {
    this.avatarUrl.set(null);
    this.userForm.markAsDirty();
  }

  create() {
    const user: UserInterface = this.userForm.getRawValue();
    console.log(user);

    this.userService
      .create(user)
      .subscribe((response: ApiResponseInterface<UserInterface>) => {
        this.snackbarService.openSnackbar(
          "Usuario creado correctamente",
          6000,
          "center",
          "bottom",
          SnackbarTypeEnum.Success
        );
        this.router.navigate(["/configuration"]);
      });
  }

  private _loadData() {
    this._getCountries();
    this._getDniTypes();
    this._getGenders();
    this._getNationalities();
    this._getPhoneTypes();
    this._getRoles();
  }

  private _getCountries() {
    this.personDataService
      .getAllCountries()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<CountryInterface[]>) => {
        this.countries.set(response.data);
      });
  }

  private _getDniTypes() {
    this.personDataService
      .getAllDNITypes()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<DniTypeInterface[]>) => {
        this.dniTypes.set(response.data);
      });
  }

  private _getGenders() {
    this.personDataService
      .getAllGenders()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<GenderInterface[]>) => {
        this.genders.set(response.data);
      });
  }

  private _getNationalities() {
    this.personDataService
      .getAllNationalities()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<NationalityInterface[]>) => {
        this.nationalities.set(response.data);
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

  private _getPhoneTypes() {
    this.personDataService
      .getAllPhoneTypes()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<PhoneTypeInterface[]>) => {
        this.phoneTypes.set(response.data);
      });
  }

  private _getRoles() {
    this.roleService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<RoleInterface[]>) => {
        this.roles.set(response.data);
      });
  }

  private _loadForm() {
    this.userForm = new FormGroup({
      username: new FormControl<string>("", [
        Validators.required,
        Validators.email,
      ]),
      password1: new FormControl<string>("", [Validators.required]),
      password2: new FormControl<string>("", [
        Validators.required,
        this.passwordMatchValidator(),
      ]),
      rolesList: new FormControl<RoleInterface[] | null>(null, [
        Validators.required,
      ]),
      firstName: new FormControl<string>(""),
      lastName: new FormControl<string>(""),
      dniType: new FormControl<DniTypeInterface | null>(null),
      dni: new FormControl<string | null>(""),
      birthDate: new FormControl<Date | null>(null),
      gender: new FormControl<GenderInterface | null>(null),
      nationality: new FormControl<NationalityInterface | null>(null),
      country: new FormControl<CountryInterface | null>(null),
      province: new FormControl<ProvinceInterface | null>(null),
      locality: new FormControl<LocalityInterface | null>(null),
      street: new FormControl<string | null>(""),
      number: new FormControl<number | null>(null),
      floor: new FormControl<string | null>(""),
      apartment: new FormControl<string | null>(""),
      email: new FormControl<string>("", [Validators.email]),
      phoneType: new FormControl<PhoneTypeInterface | null>(null),
      phone: new FormControl<string>(""),
      licenseNumber: new FormControl<string | null>(""),
      dentistSpecialty: new FormControl<DentistSpecialtyInterface | null>(null),
    });
  }

  private passwordMatchValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const password = this.userForm?.get("password1")?.value;
      const confirmPassword = control.value;

      if (!password || !confirmPassword) {
        return null;
      }

      return password === confirmPassword ? null : { passwordsMismatch: true };
    };
  }

  togglePasswordVisibility(): void {
    this.hidePassword.set(!this.hidePassword());
  }
}
