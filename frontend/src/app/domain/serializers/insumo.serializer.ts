import { Injectable } from "@angular/core";
import { InsumoInterface } from "../interfaces/insumo.interface";
import { InsumoDtoInterface } from "../dto/insumo.dto";

@Injectable({
  providedIn: "root",
})
export class InsumoSerializer {
  fromDto(dto: InsumoDtoInterface): InsumoInterface {
    return {
      id: dto.id,
      nombre: dto.nombre,
      descripcion: dto.descripcion,
      fechaIngreso: new Date(dto.fechaIngreso),
      fechaVencimiento: new Date(dto.fechaVencimiento),
      cantidad: dto.cantidad,
    };
  }

  toDto(insumo: InsumoInterface): InsumoDtoInterface {
    return {
      id: insumo.id,
      nombre: insumo.nombre,
      descripcion: insumo.descripcion,
      fechaIngreso: insumo.fechaIngreso.toISOString(),
      fechaVencimiento: insumo.fechaVencimiento.toISOString(),
      cantidad: insumo.cantidad,
    };
  }
}
