import { Component, EventEmitter, Input, Output } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatMenuModule } from "@angular/material/menu";
import { MatListModule } from "@angular/material/list";
import { TreatmentInterface } from "../../../domain/interfaces/treatment.interface";
import { TreatmentFactory } from "../../../utils/factories/treatment.factory";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";

@Component({
  selector: "app-tooth",
  templateUrl: "./tooth.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatMenuModule,
    MatButtonModule,
    MatTooltipModule,
    MatListModule,
  ],
})
export class ToothComponent {
  @Input() toothNumber: number = 0;
  @Input() topTreatment?: TreatmentInterface;
  @Input() bottomTreatment?: TreatmentInterface;
  @Input() leftTreatment?: TreatmentInterface;
  @Input() rightTreatment?: TreatmentInterface;
  @Input() centerTreatment?: TreatmentInterface;
  @Input() fullToothTreatment?: TreatmentInterface;
  @Output() topTreatmentChange = new EventEmitter<TreatmentInterface>();
  @Output() bottomTreatmentChange = new EventEmitter<TreatmentInterface>();
  @Output() leftTreatmentChange = new EventEmitter<TreatmentInterface>();
  @Output() rightTreatmentChange = new EventEmitter<TreatmentInterface>();
  @Output() centerTreatmentChange = new EventEmitter<TreatmentInterface>();
  @Output() fullToothTreatmentChange = new EventEmitter<TreatmentInterface>();
  @Output() clearFullToothTreatment = new EventEmitter<void>();

  treatments: TreatmentInterface[] = TreatmentFactory.createTreatments();

  constructor() {}

  updateFullTooth(treatment: TreatmentInterface) {
    this.fullToothTreatment = treatment;
    this.topTreatment = undefined;
    this.bottomTreatment = undefined;
    this.leftTreatment = undefined;
    this.rightTreatment = undefined;
    this.centerTreatment = treatment;
    this.fullToothTreatmentChange.emit(treatment);
  }

  updateTopIcon(treatment: TreatmentInterface) {
    this.topTreatment = treatment;
    this.topTreatmentChange.emit(treatment);
  }

  updateBottomIcon(treatment: TreatmentInterface) {
    this.bottomTreatment = treatment;
    this.bottomTreatmentChange.emit(treatment);
  }

  updateLeftIcon(treatment: TreatmentInterface) {
    this.leftTreatment = treatment;
    this.leftTreatmentChange.emit(treatment);
  }

  updateRightIcon(treatment: TreatmentInterface) {
    this.rightTreatment = treatment;
    this.rightTreatmentChange.emit(treatment);
  }

  updateCenterIcon(treatment: TreatmentInterface) {
    this.centerTreatment = treatment;
    this.centerTreatmentChange.emit(treatment);
  }

  clearFullTooth() {
    this.fullToothTreatment = undefined;
    this.centerTreatment = undefined;
    this.bottomTreatment = undefined;
    this.leftTreatment = undefined;
    this.rightTreatment = undefined;
    this.topTreatment = undefined;
    this.bottomTreatment = undefined;
    this.clearFullToothTreatment.emit();
  }

  clearCenterTreatment() {
    this.centerTreatment = undefined;
    this.clearFullToothTreatment.emit();
  }

  clearTopTreatment() {
    this.topTreatment = undefined;
    this.clearFullToothTreatment.emit();
  }

  clearBottomTreatment() {
    this.bottomTreatment = undefined;
    this.clearFullToothTreatment.emit();
  }

  clearRightTreatment() {
    this.rightTreatment = undefined;
    this.clearFullToothTreatment.emit();
  }

  clearLeftTreatment() {
    this.leftTreatment = undefined;
    this.clearFullToothTreatment.emit();
  }

  calculateMargin(index: number, totalIcons: number): string {
    if (totalIcons > 1 && index < totalIcons - 1) {
      return "mr-[-14px]";
    }
    return "";
  }
}
