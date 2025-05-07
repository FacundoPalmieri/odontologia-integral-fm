import { Component, inject, OnDestroy, OnInit, signal } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { Observable, Subject, takeUntil } from "rxjs";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MedicalRisksComponent } from "../medical-risks/medical-risks.component";
import { MatExpansionModule } from "@angular/material/expansion";
import {
  CountryInterface,
  DniTypeInterface,
  GenderInterface,
  HealthPlanInterface,
  LocalityInterface,
  NationalityInterface,
  PatientInterface,
  PhoneTypeInterface,
  ProvinceInterface,
} from "../../../domain/interfaces/patient.interface";
import { PatientService } from "../../../services/patient.service";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { MatSelectModule } from "@angular/material/select";
import { MatIconModule } from "@angular/material/icon";

@Component({
  selector: "app-edit-patient-dialog",
  templateUrl: "./edit-patient-dialog.component.html",
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatDatepickerModule,
    MatAutocompleteModule,
    IconsModule,
    MatTooltipModule,
    MedicalRisksComponent,
    MatExpansionModule,
    MatSelectModule,
    MatExpansionModule,
    MatIconModule,
  ],
})
export class EditPatientDialogComponent implements OnInit, OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  data: PatientInterface;
  patientService = inject(PatientService);
  dialogRef = inject(MatDialogRef<EditPatientDialogComponent>);
  patientForm: FormGroup = new FormGroup({});

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

  filteredLocalities: Observable<LocalityInterface[]> | undefined;
  localityControl = new FormControl<string | LocalityInterface>("");

  constructor() {
    this.data = inject(MAT_DIALOG_DATA);
    this._loadForm();
    this._loadData();
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

  private _loadForm() {
    this.patientForm = new FormGroup({
      firstName: new FormControl<string>(this.data.firstName, [
        Validators.required,
      ]),
      lastName: new FormControl<string>(this.data.lastName, [
        Validators.required,
      ]),
      dniType: new FormControl<DniTypeInterface | null>(this.data.dniType, [
        Validators.required,
      ]),
      dni: new FormControl<string | null>(this.data.dni, [Validators.required]),
      birthDate: new FormControl<Date>(new Date(this.data.birthDate), [
        Validators.required,
      ]),
      gender: new FormControl<GenderInterface | null>(this.data.gender),
      nationality: new FormControl<NationalityInterface | null>(
        this.data.nationality
      ),
      country: new FormControl<CountryInterface | null>(this.data.country),
      province: new FormControl<ProvinceInterface | null>(this.data.province),
      locality: new FormControl<LocalityInterface | null>(this.data.locality),
      street: new FormControl<string | null>(this.data.street),
      number: new FormControl<number | null>(this.data.number),
      floor: new FormControl<string | null>(this.data.floor),
      apartment: new FormControl<string | null>(this.data.apartment),
      healthPlan: new FormControl<HealthPlanInterface | null>(
        this.data.healthPlan
      ),
      affiliateNumber: new FormControl<string | null>(
        this.data.affiliateNumber
      ),
      email: new FormControl<string>(this.data.email, [Validators.email]),
      phoneType: new FormControl<PhoneTypeInterface | null>(
        this.data.phoneType
      ),
      phone: new FormControl<string>(this.data.phone),
      // medicalRisk: new FormArray([]),
    });
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

  private _loadData() {
    this._getCountries();

    // Cargo las provincias y localidades en base al id country y province (revisar con facu)
    if (this.data.country.id) {
      this.patientService
        .getProvinceByCountryId(this.data.country.id)
        .pipe(takeUntil(this._destroy$))
        .subscribe((response: ApiResponseInterface<ProvinceInterface[]>) => {
          this.provinces.set(response.data);
        });
    }
    if (this.data.province.id) {
      this.patientService
        .getLocalityByProvinceId(this.data.province.id)
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
    this.patientService
      .getAllCountries()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<CountryInterface[]>) => {
        this.countries.set(response.data);
      });
  }

  private _getDniTypes() {
    this.patientService
      .getAllDNITypes()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<DniTypeInterface[]>) => {
        this.dniTypes.set(response.data);
      });
  }

  private _getGenders() {
    this.patientService
      .getAllGenders()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<GenderInterface[]>) => {
        this.genders.set(response.data);
      });
  }

  private _getNationalities() {
    this.patientService
      .getAllNationalities()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<NationalityInterface[]>) => {
        this.nationalities.set(response.data);
      });
  }

  private _getProvincesByCountryId(id: number) {
    this.patientService
      .getProvinceByCountryId(id)
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<ProvinceInterface[]>) => {
        this.provinces.set(response.data);
      });
  }

  private _getLocalitiesByProvinceId(id: number) {
    this.patientService
      .getLocalityByProvinceId(id)
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<LocalityInterface[]>) => {
        this.localities.set(response.data);
      });
  }

  private _getHealthPlans() {
    this.patientService
      .getAllHealthPlans()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<HealthPlanInterface[]>) => {
        this.healthPlans.set(response.data);
      });
  }
}
