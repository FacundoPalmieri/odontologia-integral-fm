import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
} from "@angular/core";
import { MatSelectModule } from "@angular/material/select";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormsModule } from "@angular/forms";
import { NgOptimizedImage } from "@angular/common";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { MatCardModule } from "@angular/material/card";
import { BackButtonComponent } from "../../../../../shared/components/back-button/back-button.component";

export interface MockConsultation {
  id: number;
  date: Date;
  label: string;
}

@Component({
  selector: "app-consultation-patient-header",
  templateUrl: "./consultation-patient-header.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatSelectModule,
    MatFormFieldModule,
    FormsModule,
    NgOptimizedImage,
    IconsModule,
    MatCardModule,
    BackButtonComponent,
  ],
})
export class ConsultationPatientHeaderComponent {
  patientName = input<string>("Paciente");
  patientDni = input<string>("—");
  patientAge = input<number | null>(null);
  avatarUrl = input<string>("img/men-avatar.png");
  lastVisit = input<string | null>(null);
  consultations = input<MockConsultation[]>([]);
  selectedConsultation = input<MockConsultation | null>(null);

  consultationChange = output<MockConsultation>();

  onConsultationChange(consultation: MockConsultation): void {
    this.consultationChange.emit(consultation);
  }

  compareConsultations(a: MockConsultation, b: MockConsultation): boolean {
    return a?.id === b?.id;
  }
}
