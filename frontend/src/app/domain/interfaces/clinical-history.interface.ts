import {
  DisseaseConditionEnum,
  DisseaseEnum,
} from "../../utils/enums/dissease.enum";

export interface DisseaseInterface {
  dissease: DisseaseTypeInterface;
  condition?: DisseaseConditionInterface[];
  type?: string;
  medicament?: string;
  description?: string;
}

export interface DisseaseTypeInterface {
  dissease: DisseaseEnum;
  name: string;
}

export interface DisseaseConditionInterface {
  condition: DisseaseConditionEnum;
  name: string;
}
