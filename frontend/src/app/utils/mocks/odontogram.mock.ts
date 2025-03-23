import { OdontogramInterface } from "../../domain/interfaces/odontogram.interface";
import { TreatmentFactory } from "../factories/treatment.factory";

export const mockOdontogram: OdontogramInterface = {
  upperTeethLeft: [
    {
      number: 18,
      centerTreatment: TreatmentFactory.createTreatments()[0],
      topTreatment: TreatmentFactory.createTreatments()[1],
      leftTreatment: TreatmentFactory.createTreatments()[2],
    },
    {
      number: 17,
      topTreatment: TreatmentFactory.createTreatments()[1],
      rightTreatment: TreatmentFactory.createTreatments()[3],
    },
    {
      number: 16,
      bottomTreatment: TreatmentFactory.createTreatments()[2],
      centerTreatment: TreatmentFactory.createTreatments()[4],
    },
    {
      number: 15,
      leftTreatment: TreatmentFactory.createTreatments()[3],
      topTreatment: TreatmentFactory.createTreatments()[5],
    },
    {
      number: 14,
      rightTreatment: TreatmentFactory.createTreatments()[4],
      bottomTreatment: TreatmentFactory.createTreatments()[6],
    },
    {
      number: 13,
      centerTreatment: TreatmentFactory.createTreatments()[5],
      leftTreatment: TreatmentFactory.createTreatments()[7],
    },
    {
      number: 12,
      topTreatment: TreatmentFactory.createTreatments()[6],
      rightTreatment: TreatmentFactory.createTreatments()[8],
    },
    {
      number: 11,
      bottomTreatment: TreatmentFactory.createTreatments()[7],
      centerTreatment: TreatmentFactory.createTreatments()[9],
    },
  ],
  upperTeethRight: [
    {
      number: 21,
      centerTreatment: TreatmentFactory.createTreatments()[8],
      topTreatment: TreatmentFactory.createTreatments()[10],
    },
    {
      number: 22,
      topTreatment: TreatmentFactory.createTreatments()[9],
      bottomTreatment: TreatmentFactory.createTreatments()[11],
    },
    {
      number: 23,
      bottomTreatment: TreatmentFactory.createTreatments()[10],
      leftTreatment: TreatmentFactory.createTreatments()[12],
    },
    {
      number: 24,
      leftTreatment: TreatmentFactory.createTreatments()[11],
      rightTreatment: TreatmentFactory.createTreatments()[13],
    },
    {
      number: 25,
      rightTreatment: TreatmentFactory.createTreatments()[12],
      centerTreatment: TreatmentFactory.createTreatments()[14],
    },
    {
      number: 26,
      centerTreatment: TreatmentFactory.createTreatments()[13],
      topTreatment: TreatmentFactory.createTreatments()[15],
    },
    {
      number: 27,
      topTreatment: TreatmentFactory.createTreatments()[14],
      bottomTreatment: TreatmentFactory.createTreatments()[16],
    },
    {
      number: 28,
      bottomTreatment: TreatmentFactory.createTreatments()[15],
      leftTreatment: TreatmentFactory.createTreatments()[17],
    },
  ],
  lowerTeethLeft: [
    {
      number: 48,
      centerTreatment: TreatmentFactory.createTreatments()[16],
      rightTreatment: TreatmentFactory.createTreatments()[0],
    },
    { number: 47, topTreatment: TreatmentFactory.createTreatments()[1] },
    { number: 46, bottomTreatment: TreatmentFactory.createTreatments()[2] },
    { number: 45, leftTreatment: TreatmentFactory.createTreatments()[3] },
    { number: 44, rightTreatment: TreatmentFactory.createTreatments()[4] },
    { number: 43, centerTreatment: TreatmentFactory.createTreatments()[5] },
    { number: 42, topTreatment: TreatmentFactory.createTreatments()[6] },
    { number: 41, bottomTreatment: TreatmentFactory.createTreatments()[7] },
  ],
  lowerTeethRight: [
    {
      number: 31,
      centerTreatment: TreatmentFactory.createTreatments()[17],
      leftTreatment: TreatmentFactory.createTreatments()[8],
    },
    { number: 32, topTreatment: TreatmentFactory.createTreatments()[9] },
    { number: 33, bottomTreatment: TreatmentFactory.createTreatments()[10] },
    { number: 34, leftTreatment: TreatmentFactory.createTreatments()[11] },
    { number: 35, rightTreatment: TreatmentFactory.createTreatments()[12] },
    { number: 36, centerTreatment: TreatmentFactory.createTreatments()[13] },
    { number: 37, topTreatment: TreatmentFactory.createTreatments()[14] },
    { number: 38, bottomTreatment: TreatmentFactory.createTreatments()[15] },
  ],
  temporaryUpperLeft: [
    { number: 55, topTreatment: TreatmentFactory.createTreatments()[0] },
    { number: 54, bottomTreatment: TreatmentFactory.createTreatments()[1] },
    { number: 53, leftTreatment: TreatmentFactory.createTreatments()[2] },
    { number: 52, rightTreatment: TreatmentFactory.createTreatments()[3] },
    { number: 51, centerTreatment: TreatmentFactory.createTreatments()[4] },
  ],
  temporaryUpperRight: [
    { number: 61, centerTreatment: TreatmentFactory.createTreatments()[5] },
    { number: 62, topTreatment: TreatmentFactory.createTreatments()[6] },
    { number: 63, bottomTreatment: TreatmentFactory.createTreatments()[7] },
    { number: 64, leftTreatment: TreatmentFactory.createTreatments()[8] },
    { number: 65, rightTreatment: TreatmentFactory.createTreatments()[9] },
  ],
  temporaryLowerLeft: [
    { number: 85, rightTreatment: TreatmentFactory.createTreatments()[10] },
    { number: 84, centerTreatment: TreatmentFactory.createTreatments()[11] },
    { number: 83, topTreatment: TreatmentFactory.createTreatments()[12] },
    { number: 82, bottomTreatment: TreatmentFactory.createTreatments()[13] },
    { number: 81, leftTreatment: TreatmentFactory.createTreatments()[14] },
  ],
  temporaryLowerRight: [
    { number: 71, leftTreatment: TreatmentFactory.createTreatments()[15] },
    { number: 72, rightTreatment: TreatmentFactory.createTreatments()[16] },
    { number: 73, centerTreatment: TreatmentFactory.createTreatments()[17] },
    { number: 74, topTreatment: TreatmentFactory.createTreatments()[0] },
    { number: 75, bottomTreatment: TreatmentFactory.createTreatments()[1] },
  ],
};
