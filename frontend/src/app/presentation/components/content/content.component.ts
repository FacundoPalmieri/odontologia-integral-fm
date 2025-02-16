import { Component, inject, signal } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { FormsModule } from "@angular/forms";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatDialog } from "@angular/material/dialog";
import { MatRadioModule } from "@angular/material/radio";
import { MatToolbarModule } from "@angular/material/toolbar";
import { SampleDialogComponent } from "../dialog/sample-dialog.component";
import { ThemeService } from "../../../services/theme.service";

@Component({
  selector: "app-content",
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    FormsModule,
    MatExpansionModule,
    MatRadioModule,
    MatToolbarModule,
  ],
  template: `
    <mat-toolbar>
      <span>Odontología Integral FM</span>
      <button mat-icon-button (click)="toggleTheme()">
        <!-- <mat-icon>{{ isDarkMode ? "brightness_3" : "wb_sunny" }}</mat-icon> -->
      </button>
    </mat-toolbar>
    <div class="content responsive-grid">
      <div>
        <h3>Buttons</h3>

        <div class="flex-row">
          <button mat-raised-button (click)="openDialog()">Dialog</button>

          <button mat-flat-button>Primary button</button>

          <button mat-stroked-button>Stroked button</button>
        </div>

        <br /><br />

        <h3>Input</h3>

        <div class="responsive-grid">
          <mat-form-field>
            <input matInput placeholder="Enter some information" />
          </mat-form-field>

          <mat-form-field>
            <mat-label>Select an option</mat-label>
            <mat-select>
              <mat-option value="option1">Option 1</mat-option>
              <mat-option value="option2">Option 2</mat-option>
              <mat-option value="option3">Option 3</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field>
            <mat-label>Leave a comment</mat-label>
            <textarea matInput placeholder="Ex. It makes me feel..."></textarea>
          </mat-form-field>
        </div>

        <br /><br />

        <h3>Selection</h3>

        <mat-checkbox>Check this box!</mat-checkbox>

        <br /><br />

        <mat-radio-group>
          <mat-radio-button value="option1">Option 1</mat-radio-button>
          <mat-radio-button value="option2">Option 2</mat-radio-button>
          <mat-radio-button value="option3">Option 3</mat-radio-button>
        </mat-radio-group>

        <br /><br />

        <h3>Expansion panel</h3>

        <mat-expansion-panel hideToggle>
          <mat-expansion-panel-header>
            <mat-panel-title> My Product </mat-panel-title>
            <mat-panel-description> $ 45.00 </mat-panel-description>
          </mat-expansion-panel-header>
          <p>This is the primary content of the panel.</p>
        </mat-expansion-panel>

        <br /><br />
      </div>
      <div>
        <h3>A card</h3>
        <mat-card class="example-card" appearance="outlined">
          <mat-card-header>
            <div mat-card-avatar class="example-header-image"></div>
            <mat-card-title>Shiba Inu</mat-card-title>
            <mat-card-subtitle>Dog Breed</mat-card-subtitle>
          </mat-card-header>
          <img
            mat-card-image
            src="https://material.angular.io/assets/img/examples/shiba2.jpg"
            alt="Photo of a Shiba Inu"
          />
          <mat-card-content>
            <p>
              The Shiba Inu is the smallest of the six original and distinct
              spitz breeds of dog from Japan. A small, agile dog that copes very
              well with mountainous terrain, the Shiba Inu was originally bred
              for hunting.
            </p>
          </mat-card-content>
          <mat-card-actions>
            <button mat-button>LIKE</button>
            <button mat-button>SHARE</button>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [
    `
      .content {
        padding: 32px;
      }

      .responsive-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 20px;
      }

      mat-card {
        width: 400px;
      }

      .flex-row {
        display: flex;
        flex-direction: row;
        gap: 16px;
      }
    `,
  ],
})
export class ContentComponent {
  dialog = inject(MatDialog);
  themeService = inject(ThemeService);
  isDarkMode: boolean = false;

  openDialog() {
    this.dialog.open(SampleDialogComponent, {
      width: "400px",
    });
  }

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    this.themeService.toggleTheme(this.isDarkMode);
  }
}
