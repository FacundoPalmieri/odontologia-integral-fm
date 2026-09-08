import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from "@angular/forms";
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatFormFieldModule } from "@angular/material/form-field";
import { DiscountTypeEnum } from "../../../data/enums/discount-type.enum";
import { PromotionInterface } from "../../../data/interfaces/promotion.interface";

@Component({
  selector: "app-promotion-edit-dialog",
  templateUrl: "./promotion-edit-dialog.component.html",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatFormFieldModule,
  ],
})
export class PromotionEditDialogComponent implements OnInit {
  private readonly dialogRef = inject(MatDialogRef<PromotionEditDialogComponent>);
  private readonly data = inject(MAT_DIALOG_DATA) as { promotion: PromotionInterface };
  private readonly fb = inject(FormBuilder);

  readonly discountTypes = [
    { value: DiscountTypeEnum.PERCENTAGE, label: 'Porcentaje' },
    { value: DiscountTypeEnum.FIXED, label: 'Fijo' }
  ];

  form: FormGroup;

  constructor() {
    this.form = this.fb.group({
      label: ["", [Validators.required, Validators.minLength(3)]],
      discountType: [DiscountTypeEnum.PERCENTAGE, Validators.required],
      value: ["", [Validators.required, Validators.min(0)]],
      startDate: ["", Validators.required],
      endDate: ["", Validators.required],
    }, { validators: this.dateRangeValidator });
  }

  ngOnInit(): void {
    if (this.data.promotion) {
      this.form.patchValue({
        label: this.data.promotion.label,
        discountType: this.data.promotion.discountType,
        value: this.data.promotion.value,
        startDate: this.data.promotion.startDate,
        endDate: this.data.promotion.endDate,
      });
    }
  }

  dateRangeValidator(control: AbstractControl): ValidationErrors | null {
    const start = control.get('startDate')?.value;
    const end = control.get('endDate')?.value;
    if (start && end) {
      const startDate = new Date(start);
      const endDate = new Date(end);
      if (startDate > endDate) {
        return { dateRangeInvalid: true };
      }
    }
    return null;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.dialogRef.close(this.form.value);
    }
  }
}
