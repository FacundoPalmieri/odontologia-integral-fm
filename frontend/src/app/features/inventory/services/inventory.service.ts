import { Injectable } from "@angular/core";
import { Observable, of, delay } from "rxjs";
import { InventoryDto } from "../domain/dtos/inventory.dto";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../shared/interfaces/api-response.interface";

@Injectable({
  providedIn: "root",
})
export class InventoryService {
  private mockInsumos: InventoryDto[] = [
    {
      id: 1,
      name: "Guantes de látex",
      description: "Guantes de látex desechables talla M, caja x100 unidades",
      entryDate: "2026-01-10T10:00:00Z",
      expirationDate: "2027-01-10T10:00:00Z",
      quantity: 50,
    },
    {
      id: 2,
      name: "Anestesia Lidocaína 2%",
      description:
        "Anestésico local con epinefrina 1:100.000, caja x50 carpules",
      entryDate: "2026-01-05T14:30:00Z",
      expirationDate: "2026-12-05T14:30:00Z",
      quantity: 25,
    },
    {
      id: 3,
      name: "Amalgama dental",
      description: "Amalgama de plata para obturaciones, frasco 50g",
      entryDate: "2025-12-20T09:15:00Z",
      expirationDate: "2028-12-20T09:15:00Z",
      quantity: 15,
    },
    {
      id: 4,
      name: "Resina compuesta A2",
      description: "Resina fotopolimerizable color A2, jeringa 4g",
      entryDate: "2026-01-08T11:20:00Z",
      expirationDate: "2027-06-08T11:20:00Z",
      quantity: 30,
    },
    {
      id: 5,
      name: "Agujas dentales 27G",
      description: "Agujas desechables calibre 27G cortas, caja x100 unidades",
      entryDate: "2026-01-12T08:45:00Z",
      expirationDate: "2028-01-12T08:45:00Z",
      quantity: 80,
    },
    {
      id: 6,
      name: "Algodón en rollos",
      description: "Rollos de algodón estériles, paquete x100 unidades",
      entryDate: "2026-01-03T16:00:00Z",
      expirationDate: "2027-01-03T16:00:00Z",
      quantity: 120,
    },
    {
      id: 7,
      name: "Gasas estériles",
      description: "Gasas estériles 10x10cm, caja x100 unidades",
      entryDate: "2026-01-07T13:30:00Z",
      expirationDate: "2027-07-07T13:30:00Z",
      quantity: 60,
    },
    {
      id: 8,
      name: "Cemento dental temporal",
      description: "Cemento temporal para obturaciones provisorias, frasco 30g",
      entryDate: "2025-12-28T10:00:00Z",
      expirationDate: "2026-11-28T10:00:00Z",
      quantity: 8,
    },
    {
      id: 9,
      name: "Fresas diamantadas",
      description: "Set de fresas diamantadas para turbina, kit x10 unidades",
      entryDate: "2026-01-15T09:00:00Z",
      expirationDate: "2029-01-15T09:00:00Z",
      quantity: 12,
    },
    {
      id: 10,
      name: "Hilo dental",
      description: "Hilo dental encerado sabor menta, caja x50 unidades",
      entryDate: "2026-01-11T15:20:00Z",
      expirationDate: "2027-12-11T15:20:00Z",
      quantity: 45,
    },
    {
      id: 11,
      name: "Eyectores de saliva",
      description: "Eyectores de saliva desechables, bolsa x100 unidades",
      entryDate: "2026-01-09T12:00:00Z",
      expirationDate: "2028-01-09T12:00:00Z",
      quantity: 90,
    },
    {
      id: 12,
      name: "Baberos desechables",
      description: "Baberos desechables impermeables, paquete x100 unidades",
      entryDate: "2026-01-06T10:30:00Z",
      expirationDate: "2027-06-06T10:30:00Z",
      quantity: 75,
    },
    {
      id: 13,
      name: "Vasos descartables",
      description: "Vasos plásticos descartables 180ml, paquete x100 unidades",
      entryDate: "2026-01-14T14:00:00Z",
      expirationDate: "2028-01-14T14:00:00Z",
      quantity: 200,
    },
    {
      id: 14,
      name: "Alcohol en gel",
      description: "Alcohol en gel antiséptico 70%, bidón 5 litros",
      entryDate: "2026-01-02T11:00:00Z",
      expirationDate: "2026-10-02T11:00:00Z",
      quantity: 5,
    },
    {
      id: 15,
      name: "Mascarillas quirúrgicas",
      description: "Mascarillas quirúrgicas descartables triple capa, caja x50",
      entryDate: "2026-01-13T09:30:00Z",
      expirationDate: "2027-01-13T09:30:00Z",
      quantity: 100,
    },
  ];

  getAll(
    page: number,
    size: number,
    sortBy: string,
    direction: string,
  ): Observable<ApiResponseInterface<PagedDataInterface<InventoryDto[]>>> {
    const sortedInsumos = [...this.mockInsumos].sort((a, b) => {
      let comparison = 0;

      switch (sortBy) {
        case "id":
          comparison = a.id - b.id;
          break;
        case "name":
          comparison = a.name.localeCompare(b.name);
          break;
        case "description":
          comparison = a.description.localeCompare(b.description);
          break;
        case "entryDate":
          comparison =
            new Date(a.entryDate).getTime() - new Date(b.entryDate).getTime();
          break;
        case "expirationDate":
          comparison =
            new Date(a.expirationDate).getTime() -
            new Date(b.expirationDate).getTime();
          break;
        case "quantity":
          comparison = a.quantity - b.quantity;
          break;
        default:
          comparison = 0;
      }

      return direction === "desc" ? -comparison : comparison;
    });

    const start = page * size;
    const end = start + size;
    const paginatedInsumos = sortedInsumos.slice(start, end);

    const response: ApiResponseInterface<PagedDataInterface<InventoryDto[]>> = {
      success: true,
      message: "Insumos obtenidos exitosamente",
      data: {
        content: paginatedInsumos,
        totalElements: this.mockInsumos.length,
        totalPages: Math.ceil(this.mockInsumos.length / size),
        size: size,
        number: page,
      },
    };

    // Simular delay de red (300ms)
    return of(response).pipe(delay(300));
  }

  getById(id: number): Observable<ApiResponseInterface<InventoryDto>> {
    const insumo = this.mockInsumos.find((i) => i.id === id);

    const response: ApiResponseInterface<InventoryDto> = {
      success: !!insumo,
      message: insumo ? "Insumo encontrado" : "Insumo no encontrado",
      data: insumo!,
    };

    return of(response).pipe(delay(200));
  }

  create(insumo: InventoryDto): Observable<ApiResponseInterface<InventoryDto>> {
    const newInsumo = {
      ...insumo,
      id: Math.max(...this.mockInsumos.map((i) => i.id)) + 1,
    };

    this.mockInsumos.push(newInsumo);

    const response: ApiResponseInterface<InventoryDto> = {
      success: true,
      message: "Insumo creado exitosamente",
      data: newInsumo,
    };

    return of(response).pipe(delay(300));
  }

  update(
    id: number,
    insumo: InventoryDto,
  ): Observable<ApiResponseInterface<InventoryDto>> {
    const index = this.mockInsumos.findIndex((i) => i.id === id);

    if (index !== -1) {
      this.mockInsumos[index] = { ...insumo, id };

      const response: ApiResponseInterface<InventoryDto> = {
        success: true,
        message: "Insumo actualizado exitosamente",
        data: this.mockInsumos[index],
      };

      return of(response).pipe(delay(300));
    }

    const response: ApiResponseInterface<InventoryDto> = {
      success: false,
      message: "Insumo no encontrado",
      data: {} as InventoryDto,
    };

    return of(response).pipe(delay(200));
  }

  delete(id: number): Observable<ApiResponseInterface<void>> {
    const index = this.mockInsumos.findIndex((i) => i.id === id);

    if (index !== -1) {
      this.mockInsumos.splice(index, 1);

      const response: ApiResponseInterface<void> = {
        success: true,
        message: "Insumo eliminado exitosamente",
        data: undefined as any,
      };

      return of(response).pipe(delay(300));
    }

    const response: ApiResponseInterface<void> = {
      success: false,
      message: "Insumo no encontrado",
      data: undefined as any,
    };

    return of(response).pipe(delay(200));
  }
}
