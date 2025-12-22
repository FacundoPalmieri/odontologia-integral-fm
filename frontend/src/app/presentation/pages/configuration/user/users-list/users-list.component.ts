import {
  Component,
  inject,
  signal,
  effect,
  OnDestroy,
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
import { Subject, takeUntil } from "rxjs";
import { Router } from "@angular/router";
import { IconsModule } from "../../../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../../../components/page-toolbar/page-toolbar.component";
import { LoaderService } from "../../../../../services/loader.service";
import { AccessControlService } from "../../../../../services/access-control.service";
import { UserService } from "../../../../../services/user.service";
import { PersonDataService } from "../../../../../services/person-data.service";
import { SnackbarService } from "../../../../../services/snackbar.service";
import { UserDtoInterface } from "../../../../../domain/dto/user.dto";
import {
  ActionsEnum,
  PermissionsEnum,
} from "../../../../../utils/enums/permissions.enum";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../../../domain/interfaces/api-response.interface";
import { SnackbarTypeEnum } from "../../../../../utils/enums/snackbar-type.enum";

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
export class UsersListComponent implements OnInit, OnDestroy, AfterViewInit {
  private readonly _destroy$ = new Subject<void>();
  private readonly loaderService = inject(LoaderService);
  private readonly router = inject(Router);
  private readonly accessControlService = inject(AccessControlService);
  private readonly userService = inject(UserService);
  private readonly personDataService = inject(PersonDataService);
  private readonly snackbarService = inject(SnackbarService);

  users = signal<UserDtoInterface[]>([]);

  usersPageSize = signal(1000);
  usersPageIndex = signal(0);
  usersSortBy = signal("username");
  usersSortDirection = signal("asc");
  usersTotalElements = signal(0);

  userFilter = new FormControl("");
  usersDataSource = new MatTableDataSource<UserDtoInterface>([]);

  permissionsReady = signal(false);

  private _usersPaginator: MatPaginator | undefined;
  @ViewChild("usersPaginator")
  set usersPaginator(paginator: MatPaginator | undefined) {
    this._usersPaginator = paginator;
    if (paginator) {
      this.usersDataSource.paginator = paginator;
    }
  }
  get usersPaginator() {
    return this._usersPaginator;
  }
  @ViewChild("usersSort") usersSort!: MatSort;

  userDisplayedColumns: string[] = [
    "avatar",
    "username",
    "rolesList",
    "enabled",
  ];

  canCreate = signal<boolean>(false);
  canRead = signal<boolean>(false);
  canUpdate = signal<boolean>(false);

  constructor() {
    effect(() => {
      if (this.users()) {
        this.usersDataSource.data = this.users();
        if (this.usersPaginator) {
          this.usersPaginator.length = this.usersTotalElements();
        }
      }
    });

    effect(() => {
      if (this.canUpdate()) {
        this.userDisplayedColumns.push("action");
      }
    });
  }

  ngOnInit() {
    this._loadPermissionsFlags();
    this.permissionsReady.set(true);
    if (this.canRead()) {
      this._loadInitialData();
      this._setupFilters();
    }
  }

  ngAfterViewInit(): void {
    if (this.usersSort) {
      const userSort = this.usersSort;
      userSort.sortChange.pipe(takeUntil(this._destroy$)).subscribe((sort) => {
        this.usersPageIndex.set(0);
        this.usersSortBy.set(sort.active);
        this.usersSortDirection.set(sort.direction);
        this._loadUsers(
          this.usersPageIndex(),
          this.usersPageSize(),
          this.usersSortBy(),
          this.usersSortDirection()
        );
      });
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  createUser() {
    this.router.navigate(["/configuration/users/create"]);
  }

  editUser(user: UserDtoInterface) {
    this.router.navigate(["/configuration/users/edit", user.id]);
  }

  private _loadInitialData() {
    this._loadUsers(
      this.usersPageIndex(),
      this.usersPageSize(),
      this.usersSortBy(),
      this.usersSortDirection()
    );
  }

  private _loadPermissionsFlags() {
    this.canCreate.set(
      this.accessControlService.can(
        PermissionsEnum.CONFIGURATION,
        ActionsEnum.CREATE
      )
    );
    this.canRead.set(
      this.accessControlService.can(
        PermissionsEnum.CONFIGURATION,
        ActionsEnum.READ
      )
    );
    this.canUpdate.set(
      this.accessControlService.can(
        PermissionsEnum.CONFIGURATION,
        ActionsEnum.UPDATE
      )
    );
  }

  private _loadUsers(
    page: number,
    size: number,
    sortBy: string,
    direction: string
  ) {
    this.userService
      .getAll(page, size, sortBy, direction)
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (
          response: ApiResponseInterface<PagedDataInterface<UserDtoInterface[]>>
        ) => {
          const users = response.data?.content;
          this.users.set(users);

          if (this.users()?.length > 0) {
            users.forEach((user) => {
              if (user.person?.id) {
                this.personDataService
                  .getAvatar(user.person.id)
                  .subscribe((avatar: string | null) => {
                    if (avatar) {
                      user.avatarUrl = avatar;
                    } else {
                      const gender = user.person?.gender?.toLowerCase();
                      user.avatarUrl =
                        gender === "femenino"
                          ? "img/women-avatar.png"
                          : "img/men-avatar.png";
                    }
                    this.users.set([...this.users()]);
                  });
              }
            });
          }

          this.loaderService.hide();
        },
        (error) => {
          console.error("Error al cargar usuarios:", error);
          this.loaderService.hide();
          this.snackbarService.openSnackbar(
            "Error al cargar usuarios.",
            6000,
            "center",
            "top",
            SnackbarTypeEnum.Error
          );
        }
      );
  }

  private _setupFilters() {
    this.userFilter.valueChanges.subscribe((filterValue) => {
      this.usersDataSource.filter = filterValue?.trim().toLowerCase()!;

      if (this.usersDataSource.paginator) {
        this.usersDataSource.paginator.firstPage();
      }
    });
  }
}
