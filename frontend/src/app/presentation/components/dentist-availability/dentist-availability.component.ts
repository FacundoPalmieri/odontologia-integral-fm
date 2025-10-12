import {
  Component,
  input,
  effect,
  OnDestroy,
  inject,
  signal,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { MatInputModule } from "@angular/material/input";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatButtonModule } from "@angular/material/button";
import {
  DentistAvailabilityInterface,
  DentistAvailabilitySaveResponseInterface,
} from "../../../domain/interfaces/dentist.interface";
import { Subject, takeUntil } from "rxjs";
import { DentistService } from "../../../services/dentist.service";
import { DayEnum } from "../../../utils/enums/day.enum";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";

@Component({
  selector: "app-dentist-availability",
  templateUrl: "./dentist-availability.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatDatepickerModule,
    MatCheckboxModule,
    MatButtonModule,
  ],
})
export class DentistAvailabilityComponent implements OnDestroy {
  dentistId = input<number | null>(null);

  private readonly dentistService = inject(DentistService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly fb = inject(FormBuilder);

  dentistAvailabilityForm: FormGroup = new FormGroup({
    availability: new FormArray([]),
  });

  dentistAvailability = signal<DentistAvailabilityInterface | null>(null);

  private readonly _destroy$ = new Subject<void>();

  private readonly weekDays = [
    DayEnum.MONDAY,
    DayEnum.TUESDAY,
    DayEnum.WEDNESDAY,
    DayEnum.THURSDAY,
    DayEnum.FRIDAY,
    DayEnum.SATURDAY,
    DayEnum.SUNDAY,
  ];

  constructor() {
    effect(() => {
      const dentistId = this.dentistId();
      if (dentistId) {
        this.dentistService
          .getAvailability(dentistId)
          .pipe(takeUntil(this._destroy$))
          .subscribe((response) => {
            this.dentistAvailability.set(response.data);
            // TO DO: si hay appointmentConflict levantar un popup con el componente de visualización de conflictos
            this._initializeForm();
            this._populateForm();
          });
      }
    });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  get availability(): FormArray {
    return this.dentistAvailabilityForm.get("availability") as FormArray;
  }

  getDayLabel(day: DayEnum): string {
    const dayLabels: Record<DayEnum, string> = {
      [DayEnum.MONDAY]: "Lunes",
      [DayEnum.TUESDAY]: "Martes",
      [DayEnum.WEDNESDAY]: "Miércoles",
      [DayEnum.THURSDAY]: "Jueves",
      [DayEnum.FRIDAY]: "Viernes",
      [DayEnum.SATURDAY]: "Sábado",
      [DayEnum.SUNDAY]: "Domingo",
    };
    return dayLabels[day] || day;
  }

  trackByIndex(index: number): number {
    return index;
  }

  trackByValue(index: number, value: number): number {
    return value;
  }

  private _initializeForm(): void {
    this.availability.clear();

    const workingDays = [
      DayEnum.MONDAY,
      DayEnum.TUESDAY,
      DayEnum.WEDNESDAY,
      DayEnum.THURSDAY,
      DayEnum.FRIDAY,
    ];

    this.weekDays.forEach((day) => {
      const isWorkingDay = workingDays.includes(day);

      const dayGroup = this.fb.group({
        dayName: [day],
        isWorking: [isWorkingDay],
        startTime: this.fb.group({
          hour: [9, Validators.required],
          minute: [0, Validators.required],
        }),
        endTime: this.fb.group({
          hour: [17, Validators.required],
          minute: [0, Validators.required],
        }),
        appointmentDuration: [
          30,
          [Validators.required, Validators.min(5), Validators.max(120)],
        ],
      });

      dayGroup.get("isWorking")?.valueChanges.subscribe((isWorking) => {
        if (isWorking) {
          dayGroup.get("startTime.hour")?.enable();
          dayGroup.get("startTime.minute")?.enable();
          dayGroup.get("endTime.hour")?.enable();
          dayGroup.get("endTime.minute")?.enable();
          dayGroup.get("appointmentDuration")?.enable();

          dayGroup.get("startTime.hour")?.setValidators([Validators.required]);
          dayGroup
            .get("startTime.minute")
            ?.setValidators([Validators.required]);
          dayGroup.get("endTime.hour")?.setValidators([Validators.required]);
          dayGroup.get("endTime.minute")?.setValidators([Validators.required]);
          dayGroup
            .get("appointmentDuration")
            ?.setValidators([
              Validators.required,
              Validators.min(5),
              Validators.max(120),
            ]);
        } else {
          dayGroup.get("startTime.hour")?.disable();
          dayGroup.get("startTime.minute")?.disable();
          dayGroup.get("endTime.hour")?.disable();
          dayGroup.get("endTime.minute")?.disable();
          dayGroup.get("appointmentDuration")?.disable();

          dayGroup.get("startTime.hour")?.clearValidators();
          dayGroup.get("startTime.minute")?.clearValidators();
          dayGroup.get("endTime.hour")?.clearValidators();
          dayGroup.get("endTime.minute")?.clearValidators();
          dayGroup.get("appointmentDuration")?.clearValidators();
        }

        dayGroup.get("startTime.hour")?.updateValueAndValidity();
        dayGroup.get("startTime.minute")?.updateValueAndValidity();
        dayGroup.get("endTime.hour")?.updateValueAndValidity();
        dayGroup.get("endTime.minute")?.updateValueAndValidity();
        dayGroup.get("appointmentDuration")?.updateValueAndValidity();
      });

      if (!isWorkingDay) {
        dayGroup.get("startTime.hour")?.disable();
        dayGroup.get("startTime.minute")?.disable();
        dayGroup.get("endTime.hour")?.disable();
        dayGroup.get("endTime.minute")?.disable();
        dayGroup.get("appointmentDuration")?.disable();
      }

      this.availability.controls.push(dayGroup);
    });
  }

  private _populateForm(): void {
    const availabilityData = this.dentistAvailability();

    if (!availabilityData?.days || availabilityData.days.length === 0) {
      return;
    }

    this.availability.controls.forEach((dayControl) => {
      const dayName = dayControl.get("dayName")?.value;
      const dayData = availabilityData.days.find((d) => d.dayName === dayName);
      if (dayData) {
        dayControl.patchValue({
          isWorking: true,
          startTime: {
            hour: dayData.startTime.hour,
            minute: dayData.startTime.minute,
          },
          endTime: {
            hour: dayData.endTime.hour,
            minute: dayData.endTime.minute,
          },
          appointmentDuration: dayData.appointmentDuration,
        });
      }
    });
  }

  saveAvailability(): void {
    if (!this.dentistAvailabilityForm.valid) {
      console.error("Formulario inválido");
      return;
    }

    const dentistId = this.dentistId();
    if (!dentistId) {
      console.error("No hay ID de dentista");
      return;
    }

    const workingDays = this.availability.controls
      .filter((control) => control.get("isWorking")?.value === true)
      .map((control) => ({
        dayName: control.get("dayName")?.value,
        startTime: {
          hour: control.get("startTime.hour")?.value,
          minute: control.get("startTime.minute")?.value,
        },
        endTime: {
          hour: control.get("endTime.hour")?.value,
          minute: control.get("endTime.minute")?.value,
        },
        appointmentDuration: control.get("appointmentDuration")?.value,
      }));

    this.dentistService
      .saveAvailability(dentistId, workingDays)
      .pipe(takeUntil(this._destroy$))
      .subscribe({
        next: (
          response: ApiResponseInterface<DentistAvailabilitySaveResponseInterface>
        ) => {
          if (response.data.appointmentConflict.length > 0) {
            this.snackbarService.openSnackbar(
              "Hay conflictos de citas",
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Error
            );
          } else {
            this.snackbarService.openSnackbar(
              "Disponibilidad guardada exitosamente",
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Success
            );
          }
        },
      });
  }
}
