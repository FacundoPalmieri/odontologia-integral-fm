import { inject, DestroyRef, computed } from "@angular/core";
import {
  patchState,
  signalStore,
  withComputed,
  withMethods,
  withState,
} from "@ngrx/signals";
import { rxMethod } from "@ngrx/signals/rxjs-interop";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { catchError, of, pipe, switchMap, tap } from "rxjs";
import { PersonDataService } from "../../../../shared/services/person-data.service";
import { SnackbarService } from "../../../../shared/services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../shared/utils/enums/snackbar-type.enum";
import { UserDto } from "../dtos/user.dto";
import { initialUserListState, LoadUsersParams } from "./user-list.state";
import { UserService } from "../../services/user.service";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../../shared/interfaces/api-response.interface";

export const UserListStore = signalStore(
  /**
   * Provided in root so the state persists across navigation within the feature.
   * If the state needs to reset when leaving the route, change to
   * `providers: [UserListStore]` at the component or route level instead.
   */
  { providedIn: "root" },

  // ─── State ─────────────────────────────────────────────────────────────────
  // Initializes the store with the default shape defined in user-list.state.ts.
  withState(initialUserListState),

  // ─── Computed (derived signals) ────────────────────────────────────────────
  withComputed((state) => ({
    /** true if there is at least one user in the list */
    hasUsers: computed(() => state.users().length > 0),

    /**
     * true if the store is not loading and the users array is empty.
     * Useful for showing the empty-state UI.
     */
    isEmpty: computed(() => !state.isLoading() && state.users().length === 0),
  })),

  // ─── Methods ───────────────────────────────────────────────────────────────
  withMethods(
    (
      store,
      userService = inject(UserService),
      personDataService = inject(PersonDataService),
      snackbarService = inject(SnackbarService),
      /**
       * DestroyRef is injected here — inside withMethods' factory function —
       * which runs synchronously in an injection context (store initialization).
       * This allows us to pass it to takeUntilDestroyed() later, inside async
       * callbacks (tap, subscribe) where inject() would throw NG0203.
       */
      destroyRef = inject(DestroyRef),
    ) => ({
      /**
       * Updates the sort parameters in the state and resets to the first page.
       * The component should call loadUsers() afterwards to re-fetch with the
       * new sort params.
       */
      updateSort(sortBy: string, direction: string): void {
        patchState(store, { sortBy, sortDirection: direction, pageIndex: 0 });
      },

      /**
       * Fetches the user list from the API.
       *
       * Built with rxMethod, so it accepts an Observable, a Signal, or a plain
       * value as input. Key behaviors:
       *
       * - switchMap: automatically cancels any in-flight HTTP request if a new
       *   one arrives before the previous one completes (e.g. rapid sort changes).
       *
       * - Avatar loading: runs independently and incrementally after the main
       *   list is rendered. Each resolved avatar patches only the affected user
       *   in the state, avoiding a full re-render of the list.
       *
       * - takeUntilDestroyed(destroyRef): prevents memory leaks in avatar
       *   subscriptions if the store is destroyed while requests are in flight.
       *
       * - catchError: catches HTTP errors, shows a snackbar, updates error
       *   state, and returns of(null) to keep the rxMethod stream alive.
       */
      loadUsers: rxMethod<LoadUsersParams>(
        pipe(
          // Set loading flag and clear any previous error before the request.
          tap(() => patchState(store, { isLoading: true, error: null })),

          // switchMap ensures only the latest request is active at any time.
          switchMap(({ page, size, sortBy, direction }) =>
            userService.getAll(page, size, sortBy, direction).pipe(
              tap(
                (
                  response: ApiResponseInterface<PagedDataInterface<UserDto[]>>,
                ) => {
                  const users = response.data?.content ?? [];

                  // Immediately update the list and stop the loading indicator.
                  patchState(store, {
                    users,
                    totalElements: response.data?.totalElements ?? 0,
                    isLoading: false,
                  });

                  // Avatar loading: fire-and-forget per user, independently of
                  // the main list rendering. Only users with a linked person
                  // record will have their avatar fetched.
                  users
                    .filter((user) => !!user.person?.id)
                    .forEach((user) => {
                      personDataService
                        .getAvatar(user.person.id)
                        // takeUntilDestroyed(destroyRef) cancels pending avatar
                        // requests if the store is destroyed before they complete,
                        // preventing memory leaks and state updates after teardown.
                        .pipe(takeUntilDestroyed(destroyRef))
                        .subscribe((avatar: string | null) => {
                          // Patch only the affected user — not the entire array —
                          // to minimize unnecessary re-renders.
                          patchState(store, {
                            users: store.users().map((u) =>
                              u.id === user.id
                                ? {
                                    ...u,
                                    avatarUrl:
                                      avatar ?? "img/doctor-avatar.png",
                                  }
                                : u,
                            ),
                          });
                        });
                    });
                },
              ),

              catchError((err) => {
                console.error("[UserListStore] Failed to load users:", err);
                snackbarService.openSnackbar(
                  "Error al cargar usuarios.",
                  6000,
                  "center",
                  "top",
                  SnackbarTypeEnum.Error,
                );
                // Update state with the error message and stop the loader.
                patchState(store, {
                  isLoading: false,
                  error: "Error al cargar usuarios",
                });
                // Return of(null) to keep the rxMethod stream alive for future calls.
                return of(null);
              }),
            ),
          ),
        ),
      ),
    }),
  ),
);
