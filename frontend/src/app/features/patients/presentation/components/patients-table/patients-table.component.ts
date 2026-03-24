import {
  Component,
  input,
  output,
  ViewChild,
  AfterViewInit,
  ChangeDetectionStrategy,
  effect,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatTableModule, MatTableDataSource } from "@angular/material/table";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { PatientDto } from "../../../data/dtos/patient.dto";
import { EmptyStateComponent } from "../../../../../shared/components/empty-state/empty-state.component";

@Component({
  selector: "app-patients-table",
  templateUrl: "./patients-table.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatCardModule,
    MatButtonModule,
    MatTooltipModule,
    IconsModule,
    EmptyStateComponent,
  ],
})
export class PatientsTableComponent implements AfterViewInit {
  readonly patients = input.required<PatientDto[]>();
  readonly isLoading = input<boolean>(false);
  readonly skeletonRows = input<PatientDto[]>(Array(5).fill({}) as PatientDto[]);

  readonly viewProfile = output<PatientDto>();
  readonly openConsultation = output<PatientDto>();

  @ViewChild(MatPaginator) set paginator(paginator: MatPaginator) {
    this.dataSource.paginator = paginator;
  }
  @ViewChild(MatSort) set sort(sort: MatSort) {
    this.dataSource.sort = sort;
  }

  readonly dataSource = new MatTableDataSource<PatientDto>();
  readonly displayedColumns = [
    "avatar",
    "person.firstName",
    "person.lastName",
    "person.contactEmails",
    "person.dni",
    "person.contactPhone",
  ];

  constructor() {
    effect(() => {
      this.dataSource.data = this.patients();
    });
  }

  ngAfterViewInit() {}

  get isTableEmpty(): boolean {
    return !this.isLoading() && this.dataSource.filteredData.length === 0;
  }
}
