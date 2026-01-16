import { Injectable } from "@angular/core";
import { Observable, of, delay } from "rxjs";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../domain/interfaces/api-response.interface";
import { InsumoDtoInterface } from "../domain/dto/insumo.dto";

@Injectable({
  providedIn: "root",
})
export class InsumoService {
  // Datos mockeados de insumos odontológicos
  private mockInsumos: InsumoDtoInterface[] = [
    {
      id: 1,
      nombre: "Guantes de látex",
      descripcion: "Guantes de látex desechables talla M, caja x100 unidades",
      fechaIngreso: "2026-01-10T10:00:00Z",
      fechaVencimiento: "2027-01-10T10:00:00Z",
      cantidad: 50,
    },
    {
      id: 2,
      nombre: "Anestesia Lidocaína 2%",
      descripcion:
        "Anestésico local con epinefrina 1:100.000, caja x50 carpules",
      fechaIngreso: "2026-01-05T14:30:00Z",
      fechaVencimiento: "2026-12-05T14:30:00Z",
      cantidad: 25,
    },
    {
      id: 3,
      nombre: "Amalgama dental",
      descripcion: "Amalgama de plata para obturaciones, frasco 50g",
      fechaIngreso: "2025-12-20T09:15:00Z",
      fechaVencimiento: "2028-12-20T09:15:00Z",
      cantidad: 15,
    },
    {
      id: 4,
      nombre: "Resina compuesta A2",
      descripcion: "Resina fotopolimerizable color A2, jeringa 4g",
      fechaIngreso: "2026-01-08T11:20:00Z",
      fechaVencimiento: "2027-06-08T11:20:00Z",
      cantidad: 30,
    },
    {
      id: 5,
      nombre: "Agujas dentales 27G",
      descripcion: "Agujas desechables calibre 27G cortas, caja x100 unidades",
      fechaIngreso: "2026-01-12T08:45:00Z",
      fechaVencimiento: "2028-01-12T08:45:00Z",
      cantidad: 80,
    },
    {
      id: 6,
      nombre: "Algodón en rollos",
      descripcion: "Rollos de algodón estériles, paquete x100 unidades",
      fechaIngreso: "2026-01-03T16:00:00Z",
      fechaVencimiento: "2027-01-03T16:00:00Z",
      cantidad: 120,
    },
    {
      id: 7,
      nombre: "Gasas estériles",
      descripcion: "Gasas estériles 10x10cm, caja x100 unidades",
      fechaIngreso: "2026-01-07T13:30:00Z",
      fechaVencimiento: "2027-07-07T13:30:00Z",
      cantidad: 60,
    },
    {
      id: 8,
      nombre: "Cemento dental temporal",
      descripcion: "Cemento temporal para obturaciones provisorias, frasco 30g",
      fechaIngreso: "2025-12-28T10:00:00Z",
      fechaVencimiento: "2026-11-28T10:00:00Z",
      cantidad: 8,
    },
    {
      id: 9,
      nombre: "Fresas diamantadas",
      descripcion: "Set de fresas diamantadas para turbina, kit x10 unidades",
      fechaIngreso: "2026-01-15T09:00:00Z",
      fechaVencimiento: "2029-01-15T09:00:00Z",
      cantidad: 12,
    },
    {
      id: 10,
      nombre: "Hilo dental",
      descripcion: "Hilo dental encerado sabor menta, caja x50 unidades",
      fechaIngreso: "2026-01-11T15:20:00Z",
      fechaVencimiento: "2027-12-11T15:20:00Z",
      cantidad: 45,
    },
    {
      id: 11,
      nombre: "Eyectores de saliva",
      descripcion: "Eyectores de saliva desechables, bolsa x100 unidades",
      fechaIngreso: "2026-01-09T12:00:00Z",
      fechaVencimiento: "2028-01-09T12:00:00Z",
      cantidad: 90,
    },
    {
      id: 12,
      nombre: "Baberos desechables",
      descripcion: "Baberos desechables impermeables, paquete x100 unidades",
      fechaIngreso: "2026-01-06T10:30:00Z",
      fechaVencimiento: "2027-06-06T10:30:00Z",
      cantidad: 75,
    },
    {
      id: 13,
      nombre: "Vasos descartables",
      descripcion: "Vasos plásticos descartables 180ml, paquete x100 unidades",
      fechaIngreso: "2026-01-14T14:00:00Z",
      fechaVencimiento: "2028-01-14T14:00:00Z",
      cantidad: 200,
    },
    {
      id: 14,
      nombre: "Alcohol en gel",
      descripcion: "Alcohol en gel antiséptico 70%, bidón 5 litros",
      fechaIngreso: "2026-01-02T11:00:00Z",
      fechaVencimiento: "2026-10-02T11:00:00Z",
      cantidad: 5,
    },
    {
      id: 15,
      nombre: "Mascarillas quirúrgicas",
      descripcion: "Mascarillas quirúrgicas descartables triple capa, caja x50",
      fechaIngreso: "2026-01-13T09:30:00Z",
      fechaVencimiento: "2027-01-13T09:30:00Z",
      cantidad: 100,
    },
  ];

  getAll(
    page: number,
    size: number,
    sortBy: string,
    direction: string
  ): Observable<
    ApiResponseInterface<PagedDataInterface<InsumoDtoInterface[]>>
  > {
    // Simular ordenamiento
    const sortedInsumos = [...this.mockInsumos].sort((a, b) => {
      let comparison = 0;

      switch (sortBy) {
        case "id":
          comparison = a.id - b.id;
          break;
        case "nombre":
          comparison = a.nombre.localeCompare(b.nombre);
          break;
        case "descripcion":
          comparison = a.descripcion.localeCompare(b.descripcion);
          break;
        case "fechaIngreso":
          comparison =
            new Date(a.fechaIngreso).getTime() -
            new Date(b.fechaIngreso).getTime();
          break;
        case "fechaVencimiento":
          comparison =
            new Date(a.fechaVencimiento).getTime() -
            new Date(b.fechaVencimiento).getTime();
          break;
        case "cantidad":
          comparison = a.cantidad - b.cantidad;
          break;
        default:
          comparison = 0;
      }

      return direction === "desc" ? -comparison : comparison;
    });

    // Simular paginación
    const start = page * size;
    const end = start + size;
    const paginatedInsumos = sortedInsumos.slice(start, end);

    const response: ApiResponseInterface<
      PagedDataInterface<InsumoDtoInterface[]>
    > = {
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

  getById(id: number): Observable<ApiResponseInterface<InsumoDtoInterface>> {
    const insumo = this.mockInsumos.find((i) => i.id === id);

    const response: ApiResponseInterface<InsumoDtoInterface> = {
      success: !!insumo,
      message: insumo ? "Insumo encontrado" : "Insumo no encontrado",
      data: insumo!,
    };

    return of(response).pipe(delay(200));
  }

  create(
    insumo: InsumoDtoInterface
  ): Observable<ApiResponseInterface<InsumoDtoInterface>> {
    const newInsumo = {
      ...insumo,
      id: Math.max(...this.mockInsumos.map((i) => i.id)) + 1,
    };

    this.mockInsumos.push(newInsumo);

    const response: ApiResponseInterface<InsumoDtoInterface> = {
      success: true,
      message: "Insumo creado exitosamente",
      data: newInsumo,
    };

    return of(response).pipe(delay(300));
  }

  update(
    id: number,
    insumo: InsumoDtoInterface
  ): Observable<ApiResponseInterface<InsumoDtoInterface>> {
    const index = this.mockInsumos.findIndex((i) => i.id === id);

    if (index !== -1) {
      this.mockInsumos[index] = { ...insumo, id };

      const response: ApiResponseInterface<InsumoDtoInterface> = {
        success: true,
        message: "Insumo actualizado exitosamente",
        data: this.mockInsumos[index],
      };

      return of(response).pipe(delay(300));
    }

    const response: ApiResponseInterface<InsumoDtoInterface> = {
      success: false,
      message: "Insumo no encontrado",
      data: {} as InsumoDtoInterface,
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
