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
import { PatientDto } from "../dtos/patient.dto";
import {
  initialPatientListState,
  LoadPatientsParams,
} from "./patient-list.state";
import { PatientService } from "../../services/patient.service";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../../shared/interfaces/api-response.interface";

export const PatientListStore = signalStore(
  { providedIn: "root" },

  withState(initialPatientListState),

  withComputed((state) => ({
    hasPatients: computed(() => state.patients().length > 0),
    isEmpty: computed(
      () => !state.isLoading() && state.patients().length === 0,
    ),
  })),

  withMethods(
    (
      store,
      patientService = inject(PatientService),
      personDataService = inject(PersonDataService),
      snackbarService = inject(SnackbarService),
      destroyRef = inject(DestroyRef),
    ) => ({
      updateSort(sortBy: string, direction: string): void {
        patchState(store, { sortBy, sortDirection: direction, pageIndex: 0 });
      },

      loadPatients: rxMethod<LoadPatientsParams>(
        pipe(
          tap(() => patchState(store, { isLoading: true, error: null })),

          switchMap(({ page, size, sortBy, direction }) =>
            patientService.getAll(page, size, sortBy, direction).pipe(
              tap(
                (
                  response: ApiResponseInterface<
                    PagedDataInterface<PatientDto[]>
                  >,
                ) => {
                  const patients = response.data?.content ?? [];

                  patchState(store, {
                    patients,
                    totalElements: response.data?.totalElements ?? 0,
                    isLoading: false,
                  });

                  patients
                    .filter((patient) => !!patient.person?.id)
                    .forEach((patient) => {
                      const gender = patient.person?.gender?.toLowerCase();
                      const fallbackAvatar =
                        gender === "femenino"
                          ? "img/women-avatar.png"
                          : "img/men-avatar.png";

                      personDataService
                        .getAvatar(patient.person.id)
                        .pipe(takeUntilDestroyed(destroyRef))
                        .subscribe((avatar: string | null) => {
                          patchState(store, {
                            patients: store.patients().map((p) =>
                              p.person.id === patient.person.id
                                ? {
                                    ...p,
                                    avatarUrl: avatar ?? fallbackAvatar,
                                  }
                                : p,
                            ),
                          });
                        });
                    });
                },
              ),

              catchError((err) => {
                console.error(
                  "[PatientListStore] Failed to load patients:",
                  err,
                );
                snackbarService.openSnackbar(
                  "Error al cargar pacientes.",
                  6000,
                  "center",
                  "top",
                  SnackbarTypeEnum.Error,
                );
                patchState(store, {
                  isLoading: false,
                  error: "Error al cargar pacientes",
                });
                return of(null);
              }),
            ),
          ),
        ),
      ),
    }),
  ),
);
