import {
  Component,
  inject,
  OnDestroy,
  OnInit,
  signal,
  ElementRef,
  ViewChild,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { Subject, takeUntil } from "rxjs";
import { MatCardModule } from "@angular/material/card";
import { PageToolbarComponent } from "../../../components/page-toolbar/page-toolbar.component";
import { Router } from "@angular/router";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { PatientService } from "../../../../services/patient.service";
import { ApiResponseInterface } from "../../../../domain/interfaces/api-response.interface";
import { MatSelectModule } from "@angular/material/select";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatTableModule } from "@angular/material/table";
import { mockOdontogram1 } from "../../../../utils/mocks/odontogram.mock";
import { MatDialog } from "@angular/material/dialog";
import { SnackbarService } from "../../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../utils/enums/snackbar-type.enum";
import { PatientInterface } from "../../../../domain/interfaces/patient.interface";
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
import { PersonDataService } from "../../../../services/person-data.service";

interface OdontogramInterface {
  id: number;
  creationDate: Date;
  lastModified: Date;
  odontogram: any;
}

@Component({
  selector: "app-patient",
  templateUrl: "./patients.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatDatepickerModule,
    MatAutocompleteModule,
    IconsModule,
    MatTooltipModule,
    MatSelectModule,
    PageToolbarComponent,
    MatCardModule,
    MatTableModule,
    MatTooltipModule,
  ],
})
export class PatientComponent implements OnInit, OnDestroy {
  snackbarService = inject(SnackbarService);
  @ViewChild("fileInput") fileInput!: ElementRef<HTMLInputElement>;

  private readonly _destroy$ = new Subject<void>();
  private readonly router = inject(Router);
  private readonly patientService = inject(PatientService);
  private readonly personDataService = inject(PersonDataService);
  private readonly dialog = inject(MatDialog);

  showAdditionalInfo = signal(false);

  patient = signal<PatientInterface | null>(null);
  patientForm: FormGroup = new FormGroup({});

  displayedColumns: string[] = ["creationDate", "lastModified", "actions"];
  odontogramData: OdontogramInterface[] = [
    {
      id: 1,
      creationDate: new Date("2024-03-12 14:46:00"),
      lastModified: new Date("2025-03-20 18:33:00"),
      odontogram: mockOdontogram1,
    },
  ];

  countries = signal<CountryInterface[]>([]);
  localities = signal<LocalityInterface[]>([]);
  provinces = signal<ProvinceInterface[]>([]);
  dniTypes = signal<DniTypeInterface[]>([]);
  genders = signal<GenderInterface[]>([]);
  nationalities = signal<NationalityInterface[]>([]);
  healthPlans = signal<HealthPlanInterface[]>([]);
  phoneTypes = signal<PhoneTypeInterface[]>([
    { id: 1, name: "Celular" },
    { id: 2, name: "Fijo" },
  ]);

  avatarUrl = signal<string | null>(null);

  constructor() {
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras.state) {
      this.patient.set(
        (navigation.extras.state as { patient: PatientInterface }).patient
      );
    }
    this._loadForm();
    this._loadData();
  }

  openOdontogram() {
    // this.router.navigate([
    //   `patients/${this.patient()!.id}/odontogram/${this.odontogramData[0].id}`,
    // ]);
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

  compare = (
    item1:
      | GenderInterface
      | LocalityInterface
      | NationalityInterface
      | ProvinceInterface
      | CountryInterface
      | DniTypeInterface
      | HealthPlanInterface
      | null,
    item2:
      | GenderInterface
      | LocalityInterface
      | NationalityInterface
      | ProvinceInterface
      | CountryInterface
      | DniTypeInterface
      | HealthPlanInterface
      | null
  ): boolean => {
    return item1 && item2 ? item1.id === item2.id : item1 === item2;
  };

  savePatient() {}

  goBack(): void {
    this.router.navigate(["/patients"]);
  }

  toggleAdditionalInfo(): void {
    this.showAdditionalInfo.update((value) => !value);
  }

  triggerFileInput(): void {
    this.fileInput.nativeElement.click();
  }

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

  private _loadForm() {
    this.patientForm = new FormGroup({
      firstName: new FormControl<string>(this.patient()!.firstName, [
        Validators.required,
      ]),
      lastName: new FormControl<string>(this.patient()!.lastName, [
        Validators.required,
      ]),
      dniType: new FormControl<DniTypeInterface | null>(
        this.patient()!.dniType,
        [Validators.required]
      ),
      dni: new FormControl<string | null>(this.patient()!.dni, [
        Validators.required,
      ]),
      birthDate: new FormControl<Date>(new Date(this.patient()!.birthDate), [
        Validators.required,
      ]),
      gender: new FormControl<GenderInterface | null>(this.patient()!.gender),
      nationality: new FormControl<NationalityInterface | null>(
        this.patient()!.nationality
      ),
      country: new FormControl<CountryInterface | null>(
        this.patient()!.country
      ),
      province: new FormControl<ProvinceInterface | null>(
        this.patient()!.province
      ),
      locality: new FormControl<LocalityInterface | null>(
        this.patient()!.locality
      ),
      street: new FormControl<string | null>(this.patient()!.street),
      number: new FormControl<number | null>(this.patient()!.number),
      floor: new FormControl<string | null>(this.patient()!.floor),
      apartment: new FormControl<string | null>(this.patient()!.apartment),
      healthPlan: new FormControl<HealthPlanInterface | null>(
        this.patient()!.healthPlan
      ),
      affiliateNumber: new FormControl<string | null>(
        this.patient()!.affiliateNumber
      ),
      email: new FormControl<string>("", [Validators.email]),
      phoneType: new FormControl<PhoneTypeInterface | null>(
        this.patient()!.phoneType
      ),
      phone: new FormControl<string>(this.patient()!.phone),
      // medicalRisk: new FormArray([]),
    });
  }

  private _loadData() {
    this._getCountries();

    if (this.patient()!.country.id) {
      this.personDataService
        .getProvinceByCountryId(this.patient()!.country.id)
        .pipe(takeUntil(this._destroy$))
        .subscribe((response: ApiResponseInterface<ProvinceInterface[]>) => {
          this.provinces.set(response.data);
        });
    }
    if (this.patient()!.province.id) {
      this.personDataService
        .getLocalityByProvinceId(this.patient()!.province.id)
        .pipe(takeUntil(this._destroy$))
        .subscribe((response: ApiResponseInterface<LocalityInterface[]>) => {
          this.localities.set(response.data);
        });
    }
    this._getDniTypes();
    this._getGenders();
    this._getNationalities();
    this._getHealthPlans();
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

  private _getHealthPlans() {
    this.personDataService
      .getAllHealthPlans()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<HealthPlanInterface[]>) => {
        this.healthPlans.set(response.data);
      });
  }
}
