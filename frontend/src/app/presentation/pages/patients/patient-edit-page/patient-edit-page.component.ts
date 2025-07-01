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
import { PatientService } from "../../../../services/patient.service";
import { PatientInterface } from "../../../../domain/interfaces/patient.interface";
import { PatientDtoInterface } from "../../../../domain/dto/patient.dto";
import { mockOdontogram1 } from "../../../../utils/mocks/odontogram.mock";
import { MatTableModule } from "@angular/material/table";
import { CommonModule } from "@angular/common";
import { MatTooltipModule } from "@angular/material/tooltip";

//QUITAR
interface OdontogramInterface {
  id: number;
  creationDate: Date;
  lastModified: Date;
  odontogram: any;
}

@Component({
  selector: "app-patient-edit-page",
  templateUrl: "./patient-edit-page.component.html",
  standalone: true,
  imports: [
    CommonModule,
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
    MatTooltipModule,
  ],
})
export class PatientEditPageComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly snackbarService = inject(SnackbarService);
  private readonly patientService = inject(PatientService);
  private readonly _destroy$ = new Subject<void>();
  private readonly activatedRoute = inject(ActivatedRoute);
  personDataService = inject(PersonDataService);

  patientForm: FormGroup = new FormGroup({});
  patientId: number | null = null;
  patient = signal<PatientInterface>({} as PatientInterface);

  @ViewChild("fileInput") fileInput!: ElementRef<HTMLInputElement>;

  avatarUrl = signal<string | null>(null);
  showAdditionalInfo = signal(false);

  localities = signal<LocalityInterface[]>([]);
  provinces = signal<ProvinceInterface[]>([]);

  displayedColumns: string[] = ["creationDate", "lastModified", "actions"];
  odontogramData: OdontogramInterface[] = [
    {
      id: 1,
      creationDate: new Date("2024-03-12 14:46:00"),
      lastModified: new Date("2025-03-20 18:33:00"),
      odontogram: mockOdontogram1,
    },
  ];
  private selectedAvatarFile: File | null = null;

  constructor() {
    this._loadForm();
    this._getPatientIdFromRoute();
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
    if (!file) return;
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

    const personId = this.patientForm.get("person.id")?.value;
    if (personId) {
      this.personDataService
        .setAvatar(personId, file)
        .pipe(takeUntil(this._destroy$))
        .subscribe({
          next: () => {
            this.snackbarService.openSnackbar(
              "Imagen de perfil actualizada.",
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Success
            );
          },
          error: () => {
            this.snackbarService.openSnackbar(
              "Error al actualizar la imgen de perfil.",
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Error
            );
          },
        });
    }
  }

  removeAvatar(): void {
    this.avatarUrl.set(null);
    this.patientForm.markAsDirty();
  }

  //QUITAR
  openOdontogram() {
    // this.router.navigate([
    //   `patients/${this.patient()!.id}/odontogram/${this.odontogramData[0].id}`,
    // ]);
  }

  save() {
    const patient: PatientInterface = this.patientForm.getRawValue();

    this.patientService
      .update(patient)
      .subscribe((response: ApiResponseInterface<PatientDtoInterface>) => {
        if (this.selectedAvatarFile && patient.person?.id) {
          this.personDataService
            .setAvatar(patient.person.id, this.selectedAvatarFile)
            .subscribe(() => {
              this.selectedAvatarFile = null;
              this.router.navigate([
                "/patients/edit/",
                response.data.person.id,
              ]);
            });
        } else {
          this.snackbarService.openSnackbar(
            "El paciente ha sido modificado.",
            6000,
            "center",
            "bottom",
            SnackbarTypeEnum.Success
          );
          this.router.navigate(["/patients/edit/", response.data.person.id]);
        }
      });
  }

  private _loadForm() {
    this.patientForm = new FormGroup({
      person: new FormGroup({
        id: new FormControl<number>(0, [Validators.required]),
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
        email: new FormControl<string>("", [
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
      .subscribe((response: ApiResponseInterface<PatientInterface>) => {
        this.patient.set(response.data);
        this._populateForm(this.patient());

        if (response.data.person?.id) {
          this.personDataService
            .getAvatar(response.data.person.id)
            .pipe(takeUntil(this._destroy$))
            .subscribe((avatar: string) => {
              this.avatarUrl.set(avatar);
            });
        }
      });
  }

  private _populateForm(patient: PatientInterface) {
    this.patientForm.patchValue({
      person: {
        id: patient.person?.id,
        firstName: patient.person?.firstName,
        lastName: patient.person?.lastName,
        dniType: patient.person?.dniType,
        dni: patient.person?.dni,
        birthDate: patient.person.birthDate
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
      },
      affiliateNumber: patient.affiliateNumber,
      healthPlan: patient.healthPlan,
    });

    this.patientForm.markAsPristine();
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
