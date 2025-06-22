import { Component, inject, OnDestroy, OnInit, signal } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule, MatDialogRef } from "@angular/material/dialog";
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
import { MatExpansionModule } from "@angular/material/expansion";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { MatSelectModule } from "@angular/material/select";
import { MatIconModule } from "@angular/material/icon";
import {
  CountryInterface,
  DniTypeInterface,
  GenderInterface,
  HealthPlanInterface,
  LocalityInterface,
  NationalityInterface,
  PhoneTypeInterface,
  ProvinceInterface,
} from "../../../domain/interfaces/person-data.interface";
import { PersonDataService } from "../../../services/person-data.service";

@Component({
  selector: "app-create-patient-dialog",
  templateUrl: "./create-patient-dialog.component.html",
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
    MatExpansionModule,
    MatSelectModule,
    MatExpansionModule,
    MatIconModule,
  ],
})
export class CreatePatientDialogComponent implements OnInit, OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly personDataService = inject(PersonDataService);
  private readonly dialogRef = inject(
    MatDialogRef<CreatePatientDialogComponent>
  );
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
      firstName: new FormControl<string>("", [Validators.required]),
      lastName: new FormControl<string>("", [Validators.required]),
      dniType: new FormControl<DniTypeInterface | null>(null),
      dni: new FormControl<string | null>(null, [Validators.required]),
      birthDate: new FormControl<Date>(new Date(), [Validators.required]),
      gender: new FormControl<GenderInterface | null>(null),
      nationality: new FormControl<NationalityInterface | null>(null),
      country: new FormControl<CountryInterface | null>(null),
      province: new FormControl<ProvinceInterface | null>(null),
      locality: new FormControl<LocalityInterface | null>(null),
      street: new FormControl<string | null>(null),
      number: new FormControl<number | null>(null),
      floor: new FormControl<string | null>(null),
      apartment: new FormControl<string | null>(null),
      healthPlan: new FormControl<HealthPlanInterface | null>(null),
      affiliateNumber: new FormControl<string | null>(null),
      email: new FormControl<string>("", [Validators.email]),
      phoneType: new FormControl<PhoneTypeInterface | null>(null),
      phone: new FormControl<string>(""),
      // medicalRisk: new FormArray([]),
    });
  }

  private _loadData() {
    this._getCountries();
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
