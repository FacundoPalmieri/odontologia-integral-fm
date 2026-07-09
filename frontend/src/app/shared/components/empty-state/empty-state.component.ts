import { ChangeDetectionStrategy, Component, input } from "@angular/core";

@Component({
  selector: "app-empty-state",
  standalone: true,
  template: `
    <div class="py-4 flex flex-col items-center justify-center gap-4 w-full">
      <p
        class="text-sm font-medium text-[var(--mat-sys-on-surface-variant)] bg-[var(--mat-sys-surface-variant)] px-4 py-2 rounded-full"
      >
        {{ message() }}
      </p>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmptyStateComponent {
  readonly message = input.required<string>();
}
