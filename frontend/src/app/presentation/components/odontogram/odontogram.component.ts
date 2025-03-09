import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ToothInterface } from "../../../domain/interfaces/tooth.interface";

@Component({
  selector: "app-odontogram",
  templateUrl: "./odontogram.component.html",
  standalone: true,
  imports: [CommonModule],
})
export class OdontogramComponent {
  upperTeeth: ToothInterface[] = [
    { number: 18 },
    { number: 17 },
    { number: 16 },
    { number: 15 },
    { number: 14 },
    { number: 13 },
    { number: 12 },
    { number: 11 },
    { number: 21 },
    { number: 22 },
    { number: 23 },
    { number: 24 },
    { number: 25 },
    { number: 26 },
    { number: 27 },
    { number: 28 },
  ];

  lowerTeeth: ToothInterface[] = [
    { number: 48 },
    { number: 47 },
    { number: 46 },
    { number: 45 },
    { number: 44 },
    { number: 43 },
    { number: 42 },
    { number: 41 },
    { number: 31 },
    { number: 32 },
    { number: 33 },
    { number: 34 },
    { number: 35 },
    { number: 36 },
    { number: 37 },
    { number: 38 },
  ];
  constructor() {}
}
