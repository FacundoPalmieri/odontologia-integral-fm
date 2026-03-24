import {
  Component,
  ChangeDetectionStrategy,
  input,
  signal,
  computed,
} from "@angular/core";
import { MatPaginatorModule, PageEvent } from "@angular/material/paginator";
import { AppointmentPatientCardComponent } from "../appointment-patient-card/appointment-patient-card.component";
import { SkeletonCardComponent } from "../../../../../shared/components/skeleton-card/skeleton-card.component";
import { EmptyStateComponent } from "../../../../../shared/components/empty-state/empty-state.component";

@Component({
  selector: "app-appointments-cards",
  template: `
    @if (!isLoading() && appointments().length === 0) {
      <app-empty-state [message]="'No se encontraron turnos'"></app-empty-state>
    } @else {
      <div
        class="mt-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4"
      >
        @if (isLoading()) {
          @for (row of skeletonRows(); track $index) {
            <app-skeleton-card />
          }
        } @else {
          @for (appointment of pagedAppointments(); track appointment.dni) {
            <app-appointment-patient-card [appointment]="appointment" />
          }
        }
      </div>

      @if (!isLoading()) {
        <mat-paginator
          class="mt-4 rounded-[var(--mat-sys-corner-medium)]"
          [length]="appointments().length"
          [pageSize]="12"
          [pageSizeOptions]="[12, 24, 36]"
          showFirstLastButtons="true"
          aria-label="Seleccionar página de turnos"
          (page)="onPage($event)"
        ></mat-paginator>
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AppointmentPatientCardComponent, SkeletonCardComponent, MatPaginatorModule, EmptyStateComponent],
})
export class AppointmentsCardsComponent {
  readonly appointments = input.required<any[]>();
  readonly isLoading = input(false);
  readonly skeletonRows = input<any[]>([]);

  private readonly pageIndex = signal(0);
  private readonly pageSize = signal(12);

  readonly pagedAppointments = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.appointments().slice(start, start + this.pageSize());
  });

  onPage(event: PageEvent) {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }
}
