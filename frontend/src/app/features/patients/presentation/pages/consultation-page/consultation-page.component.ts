import {
  Component,
  ChangeDetectionStrategy,
  signal,
  computed,
  inject,
  OnInit,
} from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatCardModule } from "@angular/material/card";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { OdontogramComponent } from "../../../../odontogram/presentation/pages/odontogram/odontogram.component";
import { mockOdontogram1 } from "../../../../../shared/utils/mocks/odontogram.mock";
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

/** Mock consultations — replace with real API when available */
const MOCK_CONSULTATIONS: MockConsultation[] = [
  { id: 1, date: new Date(2026, 2, 15), label: "15 de Marzo, 2026 (Actual)" },
  { id: 2, date: new Date(2026, 1, 10), label: "10 de Febrero, 2026" },
  { id: 3, date: new Date(2025, 11, 5), label: "05 de Diciembre, 2025" },
  { id: 4, date: new Date(2025, 9, 20), label: "20 de Octubre, 2025" },
];

/** Mock treatments — replace with real API when available */
const MOCK_TREATMENTS: TreatmentRow[] = [
  {
    tooth: "44",
    procedure: "Limpieza Profunda (Profilaxis)",
    status: "completed",
    cost: 45,
  },
  {
    tooth: "62",
    procedure: "Restauración con Resina Compuesta",
    status: "completed",
    cost: 85,
  },
  {
    tooth: "14",
    procedure: "Aplicación de Sellante",
    status: "in-progress",
    cost: 30,
  },
];

@Component({
  selector: "app-consultation-page",
  templateUrl: "./consultation-page.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatExpansionModule,
    MatCardModule,
    MatDialogModule,
    OdontogramComponent,
    ConsultationPatientHeaderComponent,
    PrestationTableComponent,
    AddPrestationDialogComponent,
    ConsultationSummaryPanelComponent,
    IconsModule,
    CardIconTitleComponent,
  ],
})
export class ConsultationPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly patientService = inject(PatientService);
  private readonly personDataService = inject(PersonDataService);
  private readonly dialog = inject(MatDialog);

  // Patient data (populated from API)
  readonly patient = signal<PatientInterface | null>(null);
  readonly avatarUrl = signal<string>("img/men-avatar.png");

  // Derived display values
  readonly patientName = computed(() => {
    const p = this.patient();
    if (!p) return "Cargando...";
    return `${p.person.firstName} ${p.person.lastName}`;
  });
  readonly lastVisit = signal("10 de Febrero, 2026");
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

  // Consultations — mock until backend supports it
  readonly consultations = signal<MockConsultation[]>(MOCK_CONSULTATIONS);
  readonly selectedConsultation = signal<MockConsultation>(
    MOCK_CONSULTATIONS[0],
  );

  // Odontogram
  readonly odontogram = mockOdontogram1;

  // Treatments — mock until backend supports it
  readonly treatments = signal<TreatmentRow[]>(MOCK_TREATMENTS);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.params["id"]);
    if (!id) return;

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

  openAddPrestationDialog() {
    this.dialog
      .open(AddPrestationDialogComponent, {
        width: "600px",
      })
      .afterClosed()
      .subscribe((result) => {
        if (result) {
        }
      });
  }
}
