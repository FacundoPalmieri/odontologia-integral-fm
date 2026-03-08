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
  // ─── Infrastructure ─────────────────────────────────────────────────────────
  private readonly router = inject(Router);
  private readonly accessControlService = inject(AccessControlService);

  /**
   * DestroyRef is injected here (in the constructor injection context) so it can
   * be passed to takeUntilDestroyed() inside ngAfterViewInit and private methods,
   * which run outside of an active injection context.
   */
  private readonly destroyRef = inject(DestroyRef);

  // ─── Store ──────────────────────────────────────────────────────────────────
  // Single source of truth for the user list state.
  readonly store = inject(UserListStore);

  // ─── Table ──────────────────────────────────────────────────────────────────

  /** Reactive form control bound to the search input for client-side filtering. */
  readonly userFilter = new FormControl("");

  /**
   * MatTableDataSource wraps the user array and handles client-side filtering,
   * sorting, and pagination. The data is kept in sync with the store via effect().
   */
  readonly usersDataSource = new MatTableDataSource<UserDto>([]);

  /**
   * Placeholder rows shown during the initial load (skeleton UI pattern).
   * Array of empty objects — same length as the default page size — so the
   * table renders the correct number of shimmer rows while data is loading.
   */
  readonly skeletonRows: UserDto[] = Array(5).fill({}) as UserDto[];

  /**
   * Returns true when there are no visible rows in the table.
   * Covers two cases:
   *   1. The API returned an empty list (store has no users).
   *   2. The user typed a search term that didn't match any row.
   *
   * Uses filteredData (not users()) so it reflects the active filter state.
   */
  get isTableEmpty(): boolean {
    return (
      !this.store.isLoading() && this.usersDataSource.filteredData.length === 0
    );
  }

  /** Columns rendered by the Material table. Action buttons live inside "enabled" as a hover overlay. */
  readonly userDisplayedColumns: string[] = [
    "avatar",
    "username",
    "rolesList",
    "enabled",
  ];

  // ─── Permissions ────────────────────────────────────────────────────────────
  // Resolved once in ngOnInit from AccessControlService. Plain booleans are
  // enough here — no need for signals since permissions don't change at runtime.
  canCreate = false;
  canRead = false;
  canUpdate = false;

  // ─── ViewChild ──────────────────────────────────────────────────────────────

  /**
   * Paginator setter: assigns the paginator to the DataSource as soon as the
   * view renders it. A setter is used because @if(canRead) in the template
   * delays the paginator's creation past AfterViewInit.
   */
  @ViewChild(MatPaginator) set paginator(p: MatPaginator) {
    if (p) this.usersDataSource.paginator = p;
  }

  @ViewChild(MatSort) usersSort!: MatSort;

  constructor() {
    /**
     * Keeps MatTableDataSource in sync with the store's users signal.
     * effect() re-runs automatically whenever store.users() emits a new value,
     * including incremental avatar updates that happen after the initial load.
     */
    effect(() => {
      this.usersDataSource.data = this.store.users();
    });
  }

  ngOnInit(): void {
    // Resolve permissions synchronously — AccessControlService reads from in-memory state.
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

    // Guard: do not load data or set up listeners if the user has no READ permission.
    if (!this.canRead) return;

    this._loadUsers();
    this._setupFilterListener();
  }

  ngAfterViewInit(): void {
    if (!this.usersSort) return;

    /**
     * Listen to sort changes from Angular Material's MatSort.
     * On each change:
     *   1. Update the sort params in the store (also resets pageIndex to 0).
     *   2. Re-fetch users with the new sort order.
     *
     * takeUntilDestroyed(destroyRef) automatically unsubscribes when the
     * component is destroyed, preventing memory leaks.
     */
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

  // ─── Private ────────────────────────────────────────────────────────────────

  /**
   * Reads the current pagination/sort params from the store and triggers a
   * data fetch. Centralizing this avoids duplicating the cast and the
   * store.pageIndex() / store.sortBy() calls at every call site.
   */
  private _loadUsers(): void {
    this.store.loadUsers({
      page: this.store.pageIndex(),
      size: this.store.pageSize(),
      sortBy: this.store.sortBy(),
      direction: this.store.sortDirection() as "asc" | "desc",
    });
  }

  /**
   * Subscribes to the search input and applies client-side filtering on the
   * MatTableDataSource. Resets to the first page on every new filter value.
   *
   * takeUntilDestroyed(destroyRef) cleans up the subscription on destroy.
   */
  private _setupFilterListener(): void {
    this.userFilter.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.usersDataSource.filter = value?.trim().toLowerCase() ?? "";
        this.usersDataSource.paginator?.firstPage();
      });
  }
}
