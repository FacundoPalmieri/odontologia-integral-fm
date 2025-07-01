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
  FormArray,
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
} from "../../../../domain/interfaces/person-data.interface";
import {
  MedicalHistoryRiskInterface,
  PatientInterface,
} from "../../../../domain/interfaces/patient.interface";
import { PatientService } from "../../../../services/patient.service";
import { PatientDtoInterface } from "../../../../domain/dto/patient.dto";
import { MatTooltipModule } from "@angular/material/tooltip";

@Component({
  selector: "app-patient-create-page",
  templateUrl: "./patient-create-page.component.html",
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
    MatTooltipModule,
  ],
})
export class PatientCreatePageComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly snackbarService = inject(SnackbarService);
  private readonly patientService = inject(PatientService);
  private readonly _destroy$ = new Subject<void>();
  personDataService = inject(PersonDataService);

  patientForm: FormGroup = new FormGroup({});

  @ViewChild("fileInput") fileInput!: ElementRef<HTMLInputElement>;
  private selectedAvatarFile: File | null = null;
  avatarUrl = signal<string | null>(null);

  localities = signal<LocalityInterface[]>([]);
  provinces = signal<ProvinceInterface[]>([]);

  constructor() {
    this._loadForm();
  }

  ngOnInit() {
    this.patientForm
      .get("person.country")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((country: CountryInterface) => {
        if (country) {
          this._getProvincesByCountryId(country.id);
        } else {
          this.provinces.set([]);
        }
      });

    this.patientForm
      .get("person.province")
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
        this.patientForm.markAsDirty();
      };
      reader.readAsDataURL(file);

      this.selectedAvatarFile = file;
    }
  }

  removeAvatar(): void {
    this.avatarUrl.set(null);
    this.patientForm.markAsDirty();
  }

  create() {
    const patientForm: PatientInterface = this.patientForm.getRawValue();
    const medicalRisks: MedicalHistoryRiskInterface[] =
      this.getMedicalRisksValue();
    const patientValues = { ...patientForm, medicalRisks: medicalRisks };

    this.patientService
      .create(patientValues)
      .subscribe((response: ApiResponseInterface<PatientDtoInterface>) => {
        this.snackbarService.openSnackbar(
          "El paciente ha sido creado con éxito.",
          6000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
        const personId = response.data.person.id;
        if (personId && this.selectedAvatarFile) {
          this.personDataService
            .setAvatar(personId, this.selectedAvatarFile!)
            .pipe(takeUntil(this._destroy$))
            .subscribe({
              next: () => {},
              error: () => {
                this.snackbarService.openSnackbar(
                  "Error al cargar la imagen de perfil.",
                  6000,
                  "center",
                  "bottom",
                  SnackbarTypeEnum.Error
                );
              },
            });
        }
        this.router.navigate(["/patients/edit/", response.data.person.id]);
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

  private _loadForm() {
    this.patientForm = new FormGroup({
      person: new FormGroup({
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
        country: new FormControl<CountryInterface | null>(null, [
          Validators.required,
        ]),
        province: new FormControl<ProvinceInterface | null>(null, [
          Validators.required,
        ]),
        locality: new FormControl<LocalityInterface | null>(null, [
          Validators.required,
        ]),
        street: new FormControl<string | null>("", [Validators.required]),
        number: new FormControl<number | null>(null, [Validators.required]),
        floor: new FormControl<string | null>(null),
        apartment: new FormControl<string | null>(null),
        contactEmails: new FormControl<string>("", [
          Validators.email,
          Validators.required,
        ]),
        phoneType: new FormControl<PhoneTypeInterface | null>(null, [
          Validators.required,
        ]),
        phone: new FormControl<string>("", [Validators.required]),
      }),
      healthPlan: new FormControl<HealthPlanInterface | null>(null),
      affiliateNumber: new FormControl<string | null>(null),
      medicalRisks: new FormArray([]),
    });
  }

  get medicalRisks(): FormArray {
    return this.patientForm.get("medicalRisks") as FormArray;
  }

  addMedicalRisk() {
    this.medicalRisks.push(
      new FormGroup({
        selectedRisk: new FormControl<MedicalHistoryRiskInterface | null>(
          null,
          [Validators.required]
        ),
        observation: new FormControl<string>(""),
      })
    );
  }

  getMedicalRisksValue(): MedicalHistoryRiskInterface[] {
    return this.medicalRisks.controls.map((ctrl) => {
      const risk = ctrl.value.selectedRisk;
      return {
        id: risk.id,
        name: risk.name,
        observation: ctrl.value.observation,
      };
    });
  }

  removeMedicalRisk(index: number) {
    this.medicalRisks.removeAt(index);
  }
}
