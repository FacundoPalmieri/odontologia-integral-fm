import { Component, inject, Input, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import {
  CountryInterface,
  DniTypeInterface,
  GenderInterface,
  LocalityInterface,
  NationalityInterface,
  PhoneTypeInterface,
  ProvinceInterface,
} from "../../../domain/interfaces/person-data.interface";
import { MatFormFieldModule } from "@angular/material/form-field";
import { PersonInterface } from "../../../domain/interfaces/person.interface";
import { MatSelectModule } from "@angular/material/select";
import { PersonDataService } from "../../../services/person-data.service";
import { MatInputModule } from "@angular/material/input";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { Subject, takeUntil } from "rxjs";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";

@Component({
  selector: "app-person-data-form",
  templateUrl: "./person-data-form.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatDatepickerModule,
  ],
})
export class PersonDataFormComponent implements OnInit {
  private readonly _destroy$ = new Subject<void>();

  private _person!: PersonInterface;

  @Input({ required: true })
  set person(value: PersonInterface) {
    this._person = value;
    if (value) {
      this.personForm.patchValue(value);
    }
  }

  personDataService = inject(PersonDataService);

  personForm: FormGroup = new FormGroup({
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
    apartment: new FormControl<string | null>(null, [Validators.maxLength(2)]),
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
  });
  maxDate = new Date();

  countries = signal<CountryInterface[]>([]);
  localities = signal<LocalityInterface[]>([]);
  provinces = signal<ProvinceInterface[]>([]);

  constructor() {}

  ngOnInit(): void {
    this.personForm
      .get("country")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((country: CountryInterface) => {
        if (country) {
          this._getProvincesByCountryId(country.id);
        } else {
          this.provinces.set([]);
        }
      });

    this.personForm
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

  compare = (
    item1:
      | GenderInterface
      | LocalityInterface
      | NationalityInterface
      | ProvinceInterface
      | CountryInterface
      | DniTypeInterface
      | PhoneTypeInterface
      | null,
    item2:
      | GenderInterface
      | LocalityInterface
      | NationalityInterface
      | ProvinceInterface
      | CountryInterface
      | DniTypeInterface
      | PhoneTypeInterface
      | null
  ): boolean => {
    return item1 && item2 ? item1.id === item2.id : item1 === item2;
  };

  get person(): PersonInterface {
    return this._person;
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
}
