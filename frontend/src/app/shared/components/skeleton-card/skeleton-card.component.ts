import { ChangeDetectionStrategy, Component } from "@angular/core";
import { MatCardModule } from "@angular/material/card";
import { MatDividerModule } from "@angular/material/divider";

@Component({
  selector: "app-skeleton-card",
  standalone: true,
  imports: [MatCardModule, MatDividerModule],
  templateUrl: "./skeleton-card.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SkeletonCardComponent {}
