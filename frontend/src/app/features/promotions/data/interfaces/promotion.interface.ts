import { DiscountTypeEnum } from '../enums/discount-type.enum';

export interface PromotionInterface {
  id: string;
  label: string;
  discountType: DiscountTypeEnum;
  value: number;
  startDate: Date;
  endDate: Date;
  finishedAt?: Date | null;
}
