import { Injectable } from "@angular/core";
import { InventoryInterface } from "../interfaces/inventory.interface";
import { InventoryDto } from "../dtos/inventory.dto";

@Injectable({
  providedIn: "root",
})
export class InventorySerializer {
  fromDto(dto: InventoryDto): InventoryInterface {
    return {
      id: dto.id,
      name: dto.name,
      description: dto.description,
      entryDate: new Date(dto.entryDate),
      expirationDate: new Date(dto.expirationDate),
      quantity: dto.quantity,
    };
  }

  toDto(insumo: InventoryInterface): InventoryDto {
    return {
      id: insumo.id,
      name: insumo.name,
      description: insumo.description,
      entryDate: insumo.entryDate.toISOString(),
      expirationDate: insumo.expirationDate.toISOString(),
      quantity: insumo.quantity,
    };
  }
}
