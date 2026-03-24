import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  signal,
  computed,
} from "@angular/core";
import { MatPaginatorModule, PageEvent } from "@angular/material/paginator";
import { PatientDto } from "../../../data/dtos/patient.dto";
import { PatientCardComponent } from "../patient-card/patient-card.component";
import { SkeletonCardComponent } from "../../../../../shared/components/skeleton-card/skeleton-card.component";
import { EmptyStateComponent } from "../../../../../shared/components/empty-state/empty-state.component";

@Component({
  selector: "app-patients-cards",
  template: `
    @if (!isLoading() && patients().length === 0) {
      <app-empty-state [message]="'No se encontraron pacientes'"></app-empty-state>
    } @else {
      <div
        class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4"
      >
        @if (isLoading()) {
          @for (row of skeletonRows(); track $index) {
            <app-skeleton-card />
          }
        } @else {
          @for (patient of pagedPatients(); track patient.person.id) {
            <app-patient-card
              [patient]="patient"
              (viewProfile)="viewProfile.emit($event)"
              (openConsultation)="openConsultation.emit($event)"
            />
          }
        }
      </div>

      @if (!isLoading()) {
        <mat-paginator
          class="mt-4 rounded-[var(--mat-sys-corner-medium)]"
          [length]="patients().length"
          [pageSize]="12"
          [pageSizeOptions]="[12, 24, 36]"
          showFirstLastButtons="true"
          aria-label="Seleccionar página de pacientes"
          (page)="onPage($event)"
        ></mat-paginator>
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PatientCardComponent, SkeletonCardComponent, MatPaginatorModule, EmptyStateComponent],
})
export class PatientsCardsComponent {
  readonly patients = input.required<PatientDto[]>();
  readonly isLoading = input(false);
  readonly skeletonRows = input<unknown[]>([]);
  readonly viewProfile = output<PatientDto>();
  readonly openConsultation = output<PatientDto>();

  private readonly pageIndex = signal(0);
  private readonly pageSize = signal(12);

  readonly pagedPatients = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.patients().slice(start, start + this.pageSize());
  });

  onPage(event: PageEvent) {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }
}
