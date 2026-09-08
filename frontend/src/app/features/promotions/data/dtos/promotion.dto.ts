import { DiscountTypeEnum } from '../enums/discount-type.enum';

export interface PromotionDtoInterface {
  id?: string;
  label: string;
  discountType: DiscountTypeEnum;
  value: number;
  startDate: string;
  endDate: string;
  finishedAt?: string | null;
}

export interface PromotionCreateDtoInterface {
  label: string;
  discountType: DiscountTypeEnum;
  value: number;
  startDate: string;
  endDate: string;
}

export interface PromotionUpdateDtoInterface {
  id: string;
  label: string;
  discountType: DiscountTypeEnum;
  value: number;
  startDate: string;
  endDate: string;
}
