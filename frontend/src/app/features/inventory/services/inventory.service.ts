import { Injectable } from "@angular/core";
import { Observable, of, delay } from "rxjs";
import { InventoryDto } from "../domain/dtos/inventory.dto";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../shared/interfaces/api-response.interface";

/**
 * Service for managing dental clinic inventory.
 *
 * This service provides CRUD operations for inventory items (supplies/materials).
 * Currently uses mock data for demonstration purposes.
 * Handles pagination, sorting, and filtering of inventory items.
 */
@Injectable({
  providedIn: "root",
})
export class InventoryService {
  /**
   * Mock data array containing sample inventory items.
   * In production, this would be replaced with actual API calls.
   * @private
   */
  private mockInsumos: InventoryDto[] = [
    {
      id: 1,
      name: "Latex gloves",
      description: "Disposable latex gloves size M, box of 100 units",
      entryDate: "2026-01-10T10:00:00Z",
      expirationDate: "2027-01-10T10:00:00Z",
      quantity: 50,
    },
    {
      id: 2,
      name: "Lidocaine 2% Anesthesia",
      description:
        "Local anesthetic with epinephrine 1:100,000, box of 50 carpules",
      entryDate: "2026-01-05T14:30:00Z",
      expirationDate: "2026-12-05T14:30:00Z",
      quantity: 25,
    },
    {
      id: 3,
      name: "Dental amalgam",
      description: "Silver amalgam for fillings, 50g bottle",
      entryDate: "2025-12-20T09:15:00Z",
      expirationDate: "2028-12-20T09:15:00Z",
      quantity: 15,
    },
    {
      id: 4,
      name: "A2 Composite resin",
      description: "Light-cured composite resin color A2, 4g syringe",
      entryDate: "2026-01-08T11:20:00Z",
      expirationDate: "2027-06-08T11:20:00Z",
      quantity: 30,
    },
    {
      id: 5,
      name: "27G dental needles",
      description: "Disposable needles 27G gauge short, box of 100 units",
      entryDate: "2026-01-12T08:45:00Z",
      expirationDate: "2028-01-12T08:45:00Z",
      quantity: 80,
    },
    {
      id: 6,
      name: "Cotton rolls",
      description: "Sterile cotton rolls, pack of 100 units",
      entryDate: "2026-01-03T16:00:00Z",
      expirationDate: "2027-01-03T16:00:00Z",
      quantity: 120,
    },
    {
      id: 7,
      name: "Sterile gauze",
      description: "Sterile gauze 10x10cm, box of 100 units",
      entryDate: "2026-01-07T13:30:00Z",
      expirationDate: "2027-07-07T13:30:00Z",
      quantity: 60,
    },
    {
      id: 8,
      name: "Temporary dental cement",
      description: "Temporary cement for provisional fillings, 30g bottle",
      entryDate: "2025-12-28T10:00:00Z",
      expirationDate: "2026-11-28T10:00:00Z",
      quantity: 8,
    },
    {
      id: 9,
      name: "Diamond burs",
      description: "Diamond bur set for turbine, kit of 10 units",
      entryDate: "2026-01-15T09:00:00Z",
      expirationDate: "2029-01-15T09:00:00Z",
      quantity: 12,
    },
    {
      id: 10,
      name: "Dental floss",
      description: "Waxed dental floss mint flavor, box of 50 units",
      entryDate: "2026-01-11T15:20:00Z",
      expirationDate: "2027-12-11T15:20:00Z",
      quantity: 45,
    },
    {
      id: 11,
      name: "Saliva ejectors",
      description: "Disposable saliva ejectors, bag of 100 units",
      entryDate: "2026-01-09T12:00:00Z",
      expirationDate: "2028-01-09T12:00:00Z",
      quantity: 90,
    },
    {
      id: 12,
      name: "Disposable bibs",
      description: "Waterproof disposable bibs, pack of 100 units",
      entryDate: "2026-01-06T10:30:00Z",
      expirationDate: "2027-06-06T10:30:00Z",
      quantity: 75,
    },
    {
      id: 13,
      name: "Disposable cups",
      description: "Disposable plastic cups 180ml, pack of 100 units",
      entryDate: "2026-01-14T14:00:00Z",
      expirationDate: "2028-01-14T14:00:00Z",
      quantity: 200,
    },
    {
      id: 14,
      name: "Hand sanitizer gel",
      description: "70% antiseptic hand sanitizer gel, 5 liter container",
      entryDate: "2026-01-02T11:00:00Z",
      expirationDate: "2026-10-02T11:00:00Z",
      quantity: 5,
    },
    {
      id: 15,
      name: "Surgical masks",
      description: "Disposable triple-layer surgical masks, box of 50",
      entryDate: "2026-01-13T09:30:00Z",
      expirationDate: "2027-01-13T09:30:00Z",
      quantity: 100,
    },
  ];

  /**
   * Retrieves a paginated and sorted list of inventory items.
   *
   * @param page - Page number (0-indexed)
   * @param size - Number of items per page
   * @param sortBy - Field name to sort by (id, name, description, entryDate, expirationDate, quantity)
   * @param direction - Sort direction ('asc' or 'desc')
   * @returns Observable with paginated inventory data
   */
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

    // Simulate network delay (300ms)
    return of(response).pipe(delay(300));
  }

  /**
   * Retrieves a single inventory item by its ID.
   *
   * @param id - The ID of the inventory item to retrieve
   * @returns Observable with the inventory item data
   */
  getById(id: number): Observable<ApiResponseInterface<InventoryDto>> {
    const insumo = this.mockInsumos.find((i) => i.id === id);

    const response: ApiResponseInterface<InventoryDto> = {
      success: !!insumo,
      message: insumo ? "Item found" : "Item not found",
      data: insumo!,
    };

    return of(response).pipe(delay(200));
  }

  /**
   * Creates a new inventory item.
   *
   * @param insumo - The inventory item data to create
   * @returns Observable with the created inventory item
   */
  create(insumo: InventoryDto): Observable<ApiResponseInterface<InventoryDto>> {
    const newInsumo = {
      ...insumo,
      id: Math.max(...this.mockInsumos.map((i) => i.id)) + 1,
    };

    this.mockInsumos.push(newInsumo);

    const response: ApiResponseInterface<InventoryDto> = {
      success: true,
      message: "Item created successfully",
      data: newInsumo,
    };

    return of(response).pipe(delay(300));
  }

  /**
   * Updates an existing inventory item.
   *
   * @param id - The ID of the inventory item to update
   * @param insumo - The updated inventory item data
   * @returns Observable with the updated inventory item
   */
  update(
    id: number,
    insumo: InventoryDto,
  ): Observable<ApiResponseInterface<InventoryDto>> {
    const index = this.mockInsumos.findIndex((i) => i.id === id);

    if (index !== -1) {
      this.mockInsumos[index] = { ...insumo, id };

      const response: ApiResponseInterface<InventoryDto> = {
        success: true,
        message: "Item updated successfully",
        data: this.mockInsumos[index],
      };

      return of(response).pipe(delay(300));
    }

    const response: ApiResponseInterface<InventoryDto> = {
      success: false,
      message: "Item not found",
      data: {} as InventoryDto,
    };

    return of(response).pipe(delay(200));
  }

  /**
   * Deletes an inventory item by its ID.
   *
   * @param id - The ID of the inventory item to delete
   * @returns Observable with deletion confirmation
   */
  delete(id: number): Observable<ApiResponseInterface<void>> {
    const index = this.mockInsumos.findIndex((i) => i.id === id);

    if (index !== -1) {
      this.mockInsumos.splice(index, 1);

      const response: ApiResponseInterface<void> = {
        success: true,
        message: "Item deleted successfully",
        data: undefined as any,
      };

      return of(response).pipe(delay(300));
    }

    const response: ApiResponseInterface<void> = {
      success: false,
      message: "Item not found",
      data: undefined as any,
    };

    return of(response).pipe(delay(200));
  }
}
