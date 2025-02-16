import { Component } from "@angular/core";
import { MatDialogModule } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";

@Component({
  selector: "app-sample-dialog",
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>My Dialog</h2>
    <mat-dialog-content>
      <p>Some sample content!</p>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-flat-button>Submit</button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      :host {
        display: block;
        padding: 16px;
      }
    `,
  ],
})
export class SampleDialogComponent {}
