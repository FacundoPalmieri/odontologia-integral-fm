import { Directive, ElementRef, HostListener, Input, OnInit, OnChanges, SimpleChanges, Renderer2 } from "@angular/core";

@Directive({
  selector: "[appCurrencyFormat]",
  standalone: true,
})
export class CurrencyFormatDirective implements OnInit, OnChanges {
  @Input("appCurrencyFormat") value?: number | string | null;

  private isInput = false;

  constructor(
    private el: ElementRef,
    private renderer: Renderer2
  ) {
    const tagName = this.el.nativeElement.tagName.toLowerCase();
    this.isInput = tagName === "input" || tagName === "textarea";
  }

  ngOnInit() {
    if (this.isInput) {
      this.formatInputValue();
    } else {
      this.formatTextValue();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes["value"] && !this.isInput) {
      this.formatTextValue();
    }
  }

  @HostListener("blur")
  onBlur() {
    if (this.isInput) {
      this.formatInputValue();
    }
  }

  @HostListener("input", ["$event"])
  onInput(event: Event) {
    if (this.isInput) {
      const input = this.el.nativeElement as HTMLInputElement;
      const rawValue = input.value;
      const cleanValue = rawValue.replace(/[^0-9.,-]/g, "");
      if (cleanValue !== rawValue) {
        input.value = cleanValue;
      }
    }
  }

  private formatTextValue() {
    const formatted = this.formatNumber(this.value);
    this.renderer.setProperty(this.el.nativeElement, "textContent", formatted);
  }

  private formatInputValue() {
    const input = this.el.nativeElement as HTMLInputElement;
    const formatted = this.formatNumber(input.value);
    input.value = formatted;
  }

  private formatNumber(val: number | string | null | undefined): string {
    if (val === null || val === undefined || val === "") return "";

    let num: number;
    if (typeof val === "number") {
      num = val;
    } else {
      // Normalize Spanish-formatted string (1.234,56) into standard float (1234.56)
      const standardStr = val.replace(/\./g, "").replace(/,/g, ".");
      num = parseFloat(standardStr);
    }

    if (isNaN(num)) return "";

    return new Intl.NumberFormat("es-AR", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(num);
  }
}
