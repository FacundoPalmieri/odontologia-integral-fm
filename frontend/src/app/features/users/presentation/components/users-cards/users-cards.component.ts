import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  signal,
  computed,
} from "@angular/core";
import { MatPaginatorModule, PageEvent } from "@angular/material/paginator";
import { UserDto } from "../../../data/dtos/user.dto";
import { UserCardComponent } from "../user-card/user-card.component";
import { SkeletonCardComponent } from "../../../../../shared/components/skeleton-card/skeleton-card.component";
import { EmptyStateComponent } from "../../../../../shared/components/empty-state/empty-state.component";

@Component({
  selector: "app-users-cards",
  template: `
    @if (!isLoading() && users().length === 0) {
      <app-empty-state [message]="'No se encontraron usuarios'"></app-empty-state>
    } @else {
      <div
        class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4"
      >
        @if (isLoading()) {
          @for (row of skeletonRows(); track $index) {
            <app-skeleton-card />
          }
        } @else {
          @for (user of pagedUsers(); track user.id) {
            <app-user-card
              [user]="user"
              (editUser)="editUser.emit($event)"
            />
          }
        }
      </div>

      @if (!isLoading()) {
        <mat-paginator
          class="mt-4 rounded-[var(--mat-sys-corner-medium)]"
          [length]="users().length"
          [pageSize]="12"
          [pageSizeOptions]="[12, 24, 36]"
          showFirstLastButtons="true"
          aria-label="Seleccionar página de usuarios"
          (page)="onPage($event)"
        ></mat-paginator>
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UserCardComponent, SkeletonCardComponent, MatPaginatorModule, EmptyStateComponent],
})
export class UsersCardsComponent {
  readonly users = input.required<UserDto[]>();
  readonly isLoading = input(false);
  readonly skeletonRows = input<unknown[]>([]);
  readonly editUser = output<UserDto>();

  private readonly pageIndex = signal(0);
  private readonly pageSize = signal(12);

  readonly pagedUsers = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.users().slice(start, start + this.pageSize());
  });

  onPage(event: PageEvent) {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }
}
