import { Component, ChangeDetectionStrategy, input, output, ViewChild, AfterViewInit, effect } from "@angular/core";

import { MatCardModule } from "@angular/material/card";
import { MatTableModule, MatTableDataSource } from "@angular/material/table";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { UserDto } from "../../../data/dtos/user.dto";
import { EmptyStateComponent } from "../../../../../shared/components/empty-state/empty-state.component";

@Component({
  selector: "app-users-table",
  templateUrl: "./users-table.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatCardModule,
    MatTooltipModule,
    MatButtonModule,
    MatChipsModule,
    IconsModule,
    EmptyStateComponent
],
})
export class UsersTableComponent implements AfterViewInit {
  readonly users = input.required<UserDto[]>();
  readonly isLoading = input<boolean>(false);
  readonly skeletonRows = input<unknown[]>([]);
  readonly canUpdate = input<boolean>(false);

  readonly editUser = output<UserDto>();

  @ViewChild(MatPaginator) set paginator(paginator: MatPaginator) {
    this.dataSource.paginator = paginator;
  }
  @ViewChild(MatSort) set sort(sort: MatSort) {
    this.dataSource.sort = sort;
  }

  readonly dataSource = new MatTableDataSource<UserDto>();
  readonly displayedColumns = [
    "avatar",
    "username",
    "firstName",
    "lastName",
    "dni",
    "rolesList",
    "enabled",
  ];

  constructor() {
    effect(() => {
      this.dataSource.data = this.users();
    });
  }

  ngAfterViewInit() {}

  get isTableEmpty(): boolean {
    return !this.isLoading() && this.dataSource.filteredData.length === 0;
  }
}
