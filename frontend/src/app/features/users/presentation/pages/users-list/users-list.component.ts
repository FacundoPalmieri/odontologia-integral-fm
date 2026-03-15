import {
  Component,
  inject,
  effect,
  DestroyRef,
  AfterViewInit,
  ViewChild,
  OnInit,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatCardModule } from "@angular/material/card";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { Router } from "@angular/router";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../../../../shared/components/page-toolbar/page-toolbar.component";
import {
  ActionsEnum,
  PermissionsEnum,
} from "../../../../../shared/utils/enums/permissions.enum";
import { AccessControlService } from "../../../../../core/services/access-control.service";
import { UserDto } from "../../../data/dtos/user.dto";
import { UserListStore } from "../../../data/store/user-list.store";

@Component({
  selector: "app-users-list",
  templateUrl: "./users-list.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatCardModule,
    MatTableModule,
    MatTooltipModule,
    MatButtonModule,
    MatChipsModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
  ],
})
export class UsersListComponent implements OnInit, AfterViewInit {
  private readonly router = inject(Router);
  private readonly accessControlService = inject(AccessControlService);
  private readonly destroyRef = inject(DestroyRef);

  readonly store = inject(UserListStore);

  readonly userFilter = new FormControl("");
  readonly usersDataSource = new MatTableDataSource<UserDto>([]);
  readonly skeletonRows: UserDto[] = Array(5).fill({}) as UserDto[];

  get isTableEmpty(): boolean {
    return (
      !this.store.isLoading() && this.usersDataSource.filteredData.length === 0
    );
  }

  readonly userDisplayedColumns: string[] = [
    "avatar",
    "username",
    "firstName",
    "lastName",
    "rolesList",
    "enabled",
  ];

  canCreate = false;
  canRead = false;
  canUpdate = false;

  @ViewChild(MatPaginator) set paginator(p: MatPaginator) {
    if (p) this.usersDataSource.paginator = p;
  }

  @ViewChild(MatSort) usersSort!: MatSort;

  constructor() {
    effect(() => {
      this.usersDataSource.data = this.store.users();
    });
  }

  ngOnInit(): void {
    this.canCreate = this.accessControlService.can(
      PermissionsEnum.CONFIGURATION,
      ActionsEnum.CREATE,
    );
    this.canRead = this.accessControlService.can(
      PermissionsEnum.CONFIGURATION,
      ActionsEnum.READ,
    );
    this.canUpdate = this.accessControlService.can(
      PermissionsEnum.CONFIGURATION,
      ActionsEnum.UPDATE,
    );

    if (!this.canRead) return;

    this._loadUsers();
    this._setupFilterListener();
  }

  ngAfterViewInit(): void {
    if (!this.usersSort) return;

    this.usersSort.sortChange
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((sort) => {
        this.store.updateSort(sort.active, sort.direction);
        this._loadUsers();
      });
  }

  createUser(): void {
    this.router.navigate(["/configuration/users/create"]);
  }

  editUser(user: UserDto): void {
    this.router.navigate(["/configuration/users/edit", user.id]);
  }

  private _loadUsers(): void {
    this.store.loadUsers({
      page: this.store.pageIndex(),
      size: this.store.pageSize(),
      sortBy: this.store.sortBy(),
      direction: this.store.sortDirection() as "asc" | "desc",
    });
  }

  private _setupFilterListener(): void {
    this.userFilter.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.usersDataSource.filter = value?.trim().toLowerCase() ?? "";
        this.usersDataSource.paginator?.firstPage();
      });
  }
}
