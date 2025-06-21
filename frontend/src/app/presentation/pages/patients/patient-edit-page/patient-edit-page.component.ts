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
import { UserInterface } from "../../../../domain/interfaces/user.interface";
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
import { UserDtoInterface } from "../../../../domain/dto/user.dto";
import { PatientService } from "../../../../services/patient.service";
import { PatientInterface } from "../../../../domain/interfaces/patient.interface";
import { PatientDtoInterface } from "../../../../domain/dto/patient.dto";

@Component({
  selector: "app-patient-edit-page",
  templateUrl: "./patient-edit-page.component.html",
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
export class PatientEditPageComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly snackbarService = inject(SnackbarService);
  private readonly personDataService = inject(PersonDataService);
  private readonly patientService = inject(PatientService);
  private readonly _destroy$ = new Subject<void>();
  private readonly activatedRoute = inject(ActivatedRoute);

  patientForm: FormGroup = new FormGroup({});
  patientId: number | null = null;

  @ViewChild("fileInput") fileInput!: ElementRef<HTMLInputElement>;

  avatarUrl = signal<string | null>(null);
  showAdditionalInfo = signal(false);

  countries = signal<CountryInterface[]>([]);
  localities = signal<LocalityInterface[]>([]);
  provinces = signal<ProvinceInterface[]>([]);
  dniTypes = signal<DniTypeInterface[]>([]);
  genders = signal<GenderInterface[]>([]);
  nationalities = signal<NationalityInterface[]>([]);
  phoneTypes = signal<PhoneTypeInterface[]>([]);
  healthPlans = signal<HealthPlanInterface[]>([]);

  constructor() {
    this._loadForm();
    this._loadData();
    this._getPatientIdFromRoute();
  }

  ngOnInit() {
    this.patientForm
      .get("country")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((country: CountryInterface) => {
        if (country) {
          this._getProvincesByCountryId(country.id);
        } else {
          this.provinces.set([]);
        }
      });

    this.patientForm
      .get("province")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((province: ProvinceInterface) => {
        if (province) {
          this._getLocalitiesByProvinceId(province.id);
        } else {
          this.localities.set([]);
        }
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  goBack(): void {
    this.router.navigate(["/patients"]);
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
        this.patientForm.markAsDirty();
      };
      reader.readAsDataURL(file);
    }
  }

  removeAvatar(): void {
    this.avatarUrl.set(null);
    this.patientForm.markAsDirty();
  }

  create() {
    const patient: PatientInterface = this.patientForm.getRawValue();

    this.patientService
      .update(patient)
      .subscribe((response: ApiResponseInterface<PatientDtoInterface>) => {
        this.snackbarService.openSnackbar(
          "Paciente modificado correctamente",
          6000,
          "center",
          "bottom",
          SnackbarTypeEnum.Success
        );
        this.router.navigate(["/patients/edit/", response.data.person.id]);
      });
  }

  private _loadData() {
    this._getCountries();
    this._getDniTypes();
    this._getGenders();
    this._getNationalities();
    this._getPhoneTypes();
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

  private _loadForm() {
    this.patientForm = new FormGroup({
      username: new FormControl<string>("", [
        Validators.required,
        Validators.email,
      ]),
      rolesList: new FormControl<RoleInterface[] | null>(null, [
        Validators.required,
      ]),
      firstName: new FormControl<string>("", [Validators.required]),
      lastName: new FormControl<string>("", [Validators.required]),
      dniType: new FormControl<DniTypeInterface | null>(null, [
        Validators.required,
      ]),
      dni: new FormControl<string | null>("", [Validators.required]),
      birthDate: new FormControl<Date | null>(null, [Validators.required]),
      gender: new FormControl<GenderInterface | null>(null, [
        Validators.required,
      ]),
      nationality: new FormControl<NationalityInterface | null>(null, [
        Validators.required,
      ]),
      country: new FormControl<CountryInterface | null>(null),
      province: new FormControl<ProvinceInterface | null>(null),
      locality: new FormControl<LocalityInterface | null>(null),
      street: new FormControl<string | null>(""),
      number: new FormControl<number | null>(null),
      floor: new FormControl<string | null>(""),
      apartment: new FormControl<string | null>(""),
      email: new FormControl<string>("", [
        Validators.email,
        Validators.required,
      ]),
      phoneType: new FormControl<PhoneTypeInterface | null>(null, [
        Validators.required,
      ]),
      phone: new FormControl<string>("", [Validators.required]),
      licenseNumber: new FormControl<string | null>(""),
      dentistSpecialty: new FormControl<DentistSpecialtyInterface | null>(null),
    });
  }

  private _getPatientIdFromRoute() {
    this.activatedRoute.params.subscribe((params) => {
      this.patientId = params["id"];
      if (this.patientId) {
        this._loadPatientData();
      } else {
        this.snackbarService.openSnackbar(
          "El paciente no se pudo cargar correctamente.",
          6000,
          "center",
          "bottom",
          SnackbarTypeEnum.Error
        );
        this.goBack();
      }
    });
  }

  private _loadPatientData() {
    if (!this.patientId) return;

    this.patientService
      .getById(this.patientId)
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<PatientDtoInterface>) => {
        const patient = response.data;
        this._populateForm(patient);
      });
  }

  private _populateForm(patient: PatientDtoInterface) {
    this.patientForm.patchValue({
      firstName: patient.person.firstName,
      lastName: patient.person?.lastName,
      dniType: patient.person?.dniType,
      dni: patient.person?.dni,
      birthDate: patient.person?.birthDate
        ? new Date(patient.person.birthDate)
        : null,
      gender: patient.person?.gender,
      nationality: patient.person?.nationality,
      country: patient.person?.country,
      province: patient.person?.province,
      locality: patient.person?.locality,
      street: patient.person?.street,
      number: patient.person?.number,
      floor: patient.person?.floor,
      apartment: patient.person?.apartment,
      email: patient.person?.contactEmails,
      phoneType: patient.person?.phoneType,
      phone: patient.person?.phone,
    });

    this.patientForm.markAsPristine();
  }
}
