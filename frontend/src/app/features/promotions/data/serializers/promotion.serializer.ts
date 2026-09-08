import { PromotionDtoInterface, PromotionCreateDtoInterface, PromotionUpdateDtoInterface } from '../dtos/promotion.dto';
import { PromotionInterface } from '../interfaces/promotion.interface';

export class PromotionSerializer {
  static toView(dto: PromotionDtoInterface): PromotionInterface {
    return {
      id: dto.id || '',
      label: dto.label,
      discountType: dto.discountType,
      value: dto.value,
      startDate: new Date(dto.startDate),
      endDate: new Date(dto.endDate),
      finishedAt: dto.finishedAt ? new Date(dto.finishedAt) : null,
    };
  }

  static toCreateDto(promotion: Omit<PromotionInterface, 'id'>): PromotionCreateDtoInterface {
    return {
      label: promotion.label,
      discountType: promotion.discountType,
      value: promotion.value,
      startDate: new Date(promotion.startDate).toISOString(),
      endDate: new Date(promotion.endDate).toISOString(),
    };
  }

  static toUpdateDto(promotion: PromotionInterface): PromotionUpdateDtoInterface {
    return {
      id: promotion.id,
      label: promotion.label,
      discountType: promotion.discountType,
      value: promotion.value,
      startDate: new Date(promotion.startDate).toISOString(),
      endDate: new Date(promotion.endDate).toISOString(),
    };
  }
}
