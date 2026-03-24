import {
  Component,
  ChangeDetectionStrategy,
  input,
  signal,
  computed,
} from "@angular/core";
import { MatPaginatorModule, PageEvent } from "@angular/material/paginator";
import { AppointmentPatientCardComponent } from "../appointment-patient-card/appointment-patient-card.component";

@Component({
  selector: "app-appointments-cards",
  template: `
    <div
      class="mt-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4"
    >
      @for (appointment of pagedAppointments(); track appointment.dni) {
        <app-appointment-patient-card [appointment]="appointment" />
      }
    </div>

    <mat-paginator
      class="mt-4 rounded-[var(--mat-sys-corner-medium)]"
      [length]="appointments().length"
      [pageSize]="12"
      [pageSizeOptions]="[12, 24, 36]"
      showFirstLastButtons="true"
      aria-label="Seleccionar página de turnos"
      (page)="onPage($event)"
    ></mat-paginator>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AppointmentPatientCardComponent, MatPaginatorModule],
})
export class AppointmentsCardsComponent {
  readonly appointments = input.required<any[]>();

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
