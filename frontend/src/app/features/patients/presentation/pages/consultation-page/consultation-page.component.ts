import {
  Component,
  ChangeDetectionStrategy,
  signal,
  computed,
  inject,
  OnInit,
} from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatCardModule } from "@angular/material/card";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { OdontogramComponent } from "../../../../odontogram/presentation/pages/odontogram/odontogram.component";
import {
  ConsultationPatientHeaderComponent,
  MockConsultation,
} from "../../components/consultation-patient-header/consultation-patient-header.component";
import {
  PrestationTableComponent,
  TreatmentRow,
} from "../../components/prestation-table/prestation-table.component";
import { AddPrestationDialogComponent } from "../../components/add-prestation-dialog/add-prestation-dialog.component";
import { ConsultationSummaryPanelComponent } from "../../components/consultation-summary-panel/consultation-summary-panel.component";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { CardIconTitleComponent } from "../../../../../shared/components/card-icon-title/card-icon-title.component";
import { PatientService } from "../../../services/patient.service";
import { PersonDataService } from "../../../../../shared/services/person-data.service";
import { PatientInterface } from "../../../data/interfaces/patient.interface";
import { OdontogramInterface } from "../../../../odontogram/data/interfaces/odontogram.interface";
import { FormControl } from "@angular/forms";
import { ConsultationObservationsPanelComponent } from "../../components/consultation-observations-panel/consultation-observations-panel.component";
import { PrestationDto } from "../../../data/interfaces/prestation.interface";
import {
  PrestationScopeEnum,
  ToothEnum,
  ToothFaceEnum as ApiFace,
  PrestationInstanceStatusEnum,
} from "../../../utils/enums/consultation-instance.enum";
import { OdontogramDialogComponent } from "../../components/odontogram-dialog/odontogram-dialog.component";
import { ToothFaceMapper } from "../../../../odontogram/utils/tooth-face.mapper";
import { TreatmentService } from "../../../../odontogram/services/treatment.service";
import { ConsultationInstanceRequest, OdontogramRequest } from "../../../data/interfaces/consultation-instance.interface";
import { ConsultationInstanceService } from "../../../services/consultation-instance.service";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";

@Component({
  selector: "app-consultation-page",
  templateUrl: "./consultation-page.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatExpansionModule,
    MatCardModule,
    MatDialogModule,
    MatButtonModule,
    MatTooltipModule,
    OdontogramComponent,
    ConsultationPatientHeaderComponent,
    PrestationTableComponent,
    ConsultationSummaryPanelComponent,
    ConsultationObservationsPanelComponent,
    IconsModule,
    CardIconTitleComponent,
  ],
})
export class ConsultationPageComponent implements OnInit {
  readonly observationsControl = new FormControl("");
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly patientService = inject(PatientService);
  private readonly personDataService = inject(PersonDataService);
  private readonly dialog = inject(MatDialog);
  private readonly treatmentService = inject(TreatmentService);
  private readonly consultationInstanceService = inject(ConsultationInstanceService);
  private readonly snackbarService = inject(SnackbarService);

  readonly treatmentNameToIdMap = new Map<string, number>();

  // Patient data (populated from API)
  readonly patient = signal<PatientInterface | null>(null);
  readonly avatarUrl = signal<string>("img/men-avatar.png");

  // Derived display values
  readonly patientName = computed(() => {
    const p = this.patient();
    if (!p) return "Cargando...";
    return `${p.person.firstName} ${p.person.lastName}`;
  });
  readonly lastVisit = signal("");
  readonly patientDni = computed(() => {
    const p = this.patient();
    if (!p) return "—";
    return `${p.person.dniType.dni ?? ""} ${p.person.dni}`.trim();
  });

  readonly patientAge = computed((): number | null => {
    const p = this.patient();
    if (!p?.person.birthDate) return null;
    const birth = new Date(p.person.birthDate);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
    return age;
  });

  // Consultations
  readonly consultations = signal<MockConsultation[]>([]);
  readonly selectedConsultation = signal<MockConsultation | null>(null);

  // Odontogram
  readonly odontogram: OdontogramInterface = {
    upperTeethLeft: [
      { number: 18 }, { number: 17 }, { number: 16 }, { number: 15 },
      { number: 14 }, { number: 13 }, { number: 12 }, { number: 11 },
    ],
    upperTeethRight: [
      { number: 21 }, { number: 22 }, { number: 23 }, { number: 24 },
      { number: 25 }, { number: 26 }, { number: 27 }, { number: 28 },
    ],
    lowerTeethLeft: [
      { number: 48 }, { number: 47 }, { number: 46 }, { number: 45 },
      { number: 44 }, { number: 43 }, { number: 42 }, { number: 41 },
    ],
    lowerTeethRight: [
      { number: 31 }, { number: 32 }, { number: 33 }, { number: 34 },
      { number: 35 }, { number: 36 }, { number: 37 }, { number: 38 },
    ],
    temporaryUpperLeft: [
      { number: 55 }, { number: 54 }, { number: 53 }, { number: 52 }, { number: 51 },
    ],
    temporaryUpperRight: [
      { number: 61 }, { number: 62 }, { number: 63 }, { number: 64 }, { number: 65 },
    ],
    temporaryLowerLeft: [
      { number: 85 }, { number: 84 }, { number: 83 }, { number: 82 }, { number: 81 },
    ],
    temporaryLowerRight: [
      { number: 71 }, { number: 72 }, { number: 73 }, { number: 74 }, { number: 75 },
    ],
  };

  // Treatments
  readonly treatments = signal<TreatmentRow[]>([]);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.params["id"]);
    if (!id) return;

    this.treatmentService.getAll().subscribe({
      next: (response) => {
        if (response.data) {
          const list = Array.isArray(response.data)
            ? response.data
            : (response.data as any).content || [];
          list.forEach((t: any) => {
            this.treatmentNameToIdMap.set(t.name, t.id);
          });
        }
      },
      error: (err) => {
        console.error("Error loading treatments in ConsultationPage:", err);
      },
    });

    this.patientService.getById(id).subscribe((response) => {
      this.patient.set(response.data);

      const personId = response.data.person?.id;
      if (personId) {
        this.personDataService
          .getAvatar(personId)
          .subscribe((avatar: string | null) => {
            if (avatar) {
              this.avatarUrl.set(avatar);
            } else {
              const gender = response.data.person?.gender?.name?.toLowerCase();
              this.avatarUrl.set(
                gender === "femenino"
                  ? "img/women-avatar.png"
                  : "img/men-avatar.png",
              );
            }
          });
      }
    });
  }

  onConsultationChange(consultation: MockConsultation): void {
    this.selectedConsultation.set(consultation);
  }

  onScopeChange(event: { index: number; scope: PrestationScopeEnum }) {
    this.treatments.update((prev) => {
      const updated = [...prev];
      updated[event.index] = {
        ...updated[event.index],
        selectedScope: event.scope,
      };
      return updated;
    });
  }

  openAddPrestationDialog() {
    this.dialog
      .open(AddPrestationDialogComponent, {
        width: "600px",
      })
      .afterClosed()
      .subscribe((result: PrestationDto | undefined) => {
        if (result) {
          const newTreatment: TreatmentRow = {
            ...result,
            selectedScope: result.allowedScopes && result.allowedScopes.length > 0 ? result.allowedScopes[0] : undefined,
          };
          this.treatments.update((prev) => [...prev, newTreatment]);
        }
      });
  }

  openOdontogramDialog() {
    this.dialog.open(OdontogramDialogComponent, {
      width: "95vw",
      maxWidth: "1400px",
      data: {
        odontogram: this.odontogram,
      },
    });
  }

  finishConsultation() {
    const odontogramRequests: OdontogramRequest[] = [];
    const allSections = [
      this.odontogram.upperTeethLeft,
      this.odontogram.upperTeethRight,
      this.odontogram.lowerTeethLeft,
      this.odontogram.lowerTeethRight,
      this.odontogram.temporaryUpperLeft,
      this.odontogram.temporaryUpperRight,
      this.odontogram.temporaryLowerLeft,
      this.odontogram.temporaryLowerRight,
    ];

    for (const section of allSections) {
      if (!section) continue;
      for (const tooth of section) {
        if (tooth.treatments && tooth.treatments.length > 0) {
          for (const tx of tooth.treatments) {
            const treatmentId = this.treatmentNameToIdMap.get(tx.name) ?? 0;
            const conditionId = Number(tx.treatmentType);

            if (tx.faces && tx.faces.length > 0) {
              for (const face of tx.faces) {
                const apiFace = ToothFaceMapper.toApiFace(tooth.number, face as any);
                odontogramRequests.push({
                  tooth: `T${tooth.number}` as ToothEnum,
                  toothFace: apiFace,
                  treatmentId: treatmentId,
                  treatmentConditionId: conditionId,
                });
              }
            } else {
              odontogramRequests.push({
                tooth: `T${tooth.number}` as ToothEnum,
                toothFace: ApiFace.CENTER,
                treatmentId: treatmentId,
                treatmentConditionId: conditionId,
              });
            }
          }
        }
      }
    }

    const payload: ConsultationInstanceRequest = {
      consultationId: this.selectedConsultation()?.id ?? 0,
      observation: this.observationsControl.value ?? "",
      odontogram: odontogramRequests,
      prestationNew: this.treatments().map((t) => ({
        prestationTypeId: t.id,
        prestationStepId: 0,
        prestationStepStatus: PrestationInstanceStatusEnum.IN_PROGRESS,
        odontogram: [],
        scope: t.selectedScope ?? PrestationScopeEnum.FULL_MOUTH,
        tooth: undefined as any,
        quadrant: undefined as any,
        maxillary: undefined as any,
        promotionId: undefined as any,
        discountType: undefined as any,
        discountValue: 0,
      })),
      stepAdvancements: [],
    };

    this.consultationInstanceService.createConsultationInstance(payload).subscribe({
      next: () => {
        this.snackbarService.openSnackbar(
          "Consulta finalizada con éxito.",
          4000,
          "center",
          "bottom",
          SnackbarTypeEnum.Success
        );
        const patientId = Number(this.route.snapshot.params["id"]);
        if (patientId) {
          this.router.navigate([`/patients/edit/${patientId}`]);
        } else {
          this.router.navigate(["/patients"]);
        }
      },
      error: (err) => {
        console.error("Error creating consultation instance:", err);
        this.snackbarService.openSnackbar(
          "Ocurrió un error al finalizar la consulta.",
          5000,
          "center",
          "bottom",
          SnackbarTypeEnum.Error
        );
      },
    });
  }
}
