import {
  Component,
  input,
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
import { AppointmentActionsMenuComponent } from "../appointment-actions-menu/appointment-actions-menu.component";

@Component({
  selector: "app-appointments-table",
  templateUrl: "./appointments-table.component.html",
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
    AppointmentActionsMenuComponent,
  ],
})
export class AppointmentsTableComponent implements AfterViewInit {
  readonly appointments = input.required<any[]>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  readonly dataSource = new MatTableDataSource<any>();
  readonly displayedColumns = ["patient", "schedule", "professional", "status"];

  constructor() {
    effect(() => {
      this.dataSource.data = this.appointments();
      if (this.paginator) this.dataSource.paginator = this.paginator;
      if (this.sort) this.dataSource.sort = this.sort;
    });
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }
}
