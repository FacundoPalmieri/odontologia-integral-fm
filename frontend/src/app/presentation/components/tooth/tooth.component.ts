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
  @Output() topTreatmentChange = new EventEmitter<TreatmentInterface>();
  @Output() bottomTreatmentChange = new EventEmitter<TreatmentInterface>();
  @Output() leftTreatmentChange = new EventEmitter<TreatmentInterface>();
  @Output() rightTreatmentChange = new EventEmitter<TreatmentInterface>();
  @Output() centerTreatmentChange = new EventEmitter<TreatmentInterface>();
  @Output() fullToothTreatmentChange = new EventEmitter<TreatmentInterface>();

  treatments: TreatmentInterface[] = TreatmentFactory.createTreatments();

  constructor() {}

  updateFullTooth(treatment: TreatmentInterface) {
    this.topTreatment = treatment;
    this.bottomTreatment = treatment;
    this.leftTreatment = treatment;
    this.rightTreatment = treatment;
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

  calculateMargin(index: number, totalIcons: number): string {
    if (totalIcons > 1 && index < totalIcons - 1) {
      return "mr-[-16px]";
    }
    return "";
  }
}
