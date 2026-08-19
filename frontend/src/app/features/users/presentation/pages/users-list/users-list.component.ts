import {
  Component,
  inject,
  effect,
  DestroyRef,
  AfterViewInit,
  OnInit,
  signal,
  computed,
} from "@angular/core";

import { MatCardModule } from "@angular/material/card";
import { MatChipsModule } from "@angular/material/chips";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { Router } from "@angular/router";
import { toSignal } from "@angular/core/rxjs-interop";
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
import { UsersTableComponent } from "../../components/users-table/users-table.component";
import { UsersCardsComponent } from "../../components/users-cards/users-cards.component";

@Component({
  selector: "app-users-list",
  templateUrl: "./users-list.component.html",
  standalone: true,
  imports: [
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonToggleModule,
    UsersTableComponent,
    UsersCardsComponent
],
})
export class UsersListComponent implements OnInit, AfterViewInit {
  private readonly router = inject(Router);
  private readonly accessControlService = inject(AccessControlService);

  readonly store = inject(UserListStore);

  readonly userFilter = new FormControl("");
  readonly filterValue = toSignal(this.userFilter.valueChanges, {
    initialValue: "",
  });

  readonly filteredUsers = computed(() => {
    const filter = (this.filterValue() ?? "").trim().toLowerCase();
    const allUsers = this.store.users();

    if (!filter) return allUsers;

    return allUsers.filter((u) => {
      const term =
        `${u.username} ${u.person.firstName} ${u.person.lastName} ${u.person.dni} ${u.person.contactEmails?.[0] || ""} ${u.person.contactPhone?.[0]?.phone || ""}`.toLowerCase();
      return term.includes(filter);
    });
  });

  readonly tableSkeletonRows: UserDto[] = Array(5).fill({}) as UserDto[];
  readonly cardsSkeletonRows: UserDto[] = Array(6).fill({}) as UserDto[];
  readonly viewMode = signal<"table" | "cards">(
    (localStorage.getItem("usersViewMode") as "table" | "cards") || "table",
  );

  canCreate = false;
  canRead = false;
  canUpdate = false;

  constructor() {
    effect(() => {
      localStorage.setItem("usersViewMode", this.viewMode());
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
  }

  ngAfterViewInit(): void {
    // We could bind table sort changes to store if we had a dedicated sort child.
    // Right now table works locally with paginator/sort.
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
}
