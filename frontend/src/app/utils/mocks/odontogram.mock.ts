import { OdontogramInterface } from "../../domain/interfaces/odontogram.interface";
import { TreatmentInterface } from "../../domain/interfaces/treatment.interface";
import { TreatmentFactory } from "../factories/treatment.factory";

const treatments: TreatmentInterface[] = TreatmentFactory.createTreatments();

export const mockOdontogram: OdontogramInterface = {
  upperTeethLeft: [
    {
      number: 18,
      centerTreatment: treatments[0],
      topTreatment: treatments[1],
      leftTreatment: treatments[2],
    },
    {
      number: 17,
      topTreatment: treatments[3],
      rightTreatment: treatments[4],
    },
    {
      number: 16,
      bottomTreatment: treatments[2],
      centerTreatment: treatments[0],
    },
    {
      number: 15,
      leftTreatment: treatments[5],
      topTreatment: treatments[0],
    },
    {
      number: 14,
      rightTreatment: treatments[7],
      bottomTreatment: treatments[2],
    },
    {
      number: 13,
      centerTreatment: treatments[2],
      leftTreatment: treatments[3],
    },
    {
      number: 12,
      topTreatment: treatments[1],
      rightTreatment: treatments[5],
    },
    {
      number: 11,
      fullToothTreatment: treatments[6],
    },
  ],
  upperTeethRight: [
    {
      number: 21,
      fullToothTreatment: treatments[6],
    },
    {
      number: 22,
      topTreatment: treatments[7],
      bottomTreatment: treatments[7],
    },
    {
      number: 23,
      bottomTreatment: treatments[1],
      leftTreatment: treatments[2],
    },
    {
      number: 24,
      leftTreatment: treatments[3],
      rightTreatment: treatments[4],
    },
    {
      number: 25,
      rightTreatment: treatments[5],
      centerTreatment: treatments[6],
    },
    {
      number: 26,
      centerTreatment: treatments[7],
      topTreatment: treatments[0],
    },
    {
      number: 27,
      topTreatment: treatments[1],
      bottomTreatment: treatments[2],
    },
    {
      number: 28,
      bottomTreatment: treatments[3],
      leftTreatment: treatments[4],
    },
  ],
  lowerTeethLeft: [
    {
      number: 48,
      centerTreatment: treatments[5],
      rightTreatment: treatments[6],
    },
    { number: 47, topTreatment: treatments[7] },
    { number: 46, bottomTreatment: treatments[0] },
    { number: 45, leftTreatment: treatments[1] },
    { number: 44, rightTreatment: treatments[2] },
    { number: 43, centerTreatment: treatments[3] },
    { number: 42, topTreatment: treatments[4] },
    { number: 41, bottomTreatment: treatments[5] },
  ],
  lowerTeethRight: [
    {
      number: 31,
      centerTreatment: treatments[6],
      leftTreatment: treatments[7],
    },
    { number: 32, topTreatment: treatments[0] },
    { number: 33, bottomTreatment: treatments[1] },
    { number: 34, leftTreatment: treatments[2] },
    { number: 35, rightTreatment: treatments[3] },
    { number: 36, centerTreatment: treatments[4] },
    { number: 37, topTreatment: treatments[5] },
    { number: 38, bottomTreatment: treatments[6] },
  ],
  temporaryUpperLeft: [
    { number: 55, topTreatment: treatments[7] },
    { number: 54, bottomTreatment: treatments[0] },
    { number: 53, leftTreatment: treatments[1] },
    { number: 52, rightTreatment: treatments[2] },
    { number: 51, centerTreatment: treatments[3] },
  ],
  temporaryUpperRight: [
    { number: 61, centerTreatment: treatments[4] },
    { number: 62, topTreatment: treatments[5] },
    { number: 63, bottomTreatment: treatments[6] },
    { number: 64, leftTreatment: treatments[7] },
    { number: 65, rightTreatment: treatments[0] },
  ],
  temporaryLowerLeft: [
    { number: 85, rightTreatment: treatments[1] },
    { number: 84, centerTreatment: treatments[2] },
    { number: 83, topTreatment: treatments[3] },
    { number: 82, bottomTreatment: treatments[4] },
    { number: 81, leftTreatment: treatments[5] },
  ],
  temporaryLowerRight: [
    { number: 71, leftTreatment: treatments[6] },
    { number: 72, rightTreatment: treatments[7] },
    { number: 73, centerTreatment: treatments[0] },
    { number: 74, topTreatment: treatments[1] },
    { number: 75, bottomTreatment: treatments[2] },
  ],
};
