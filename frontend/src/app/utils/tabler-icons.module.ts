import { NgModule } from "@angular/core";

import { TablerIconsModule } from "angular-tabler-icons";
import {
  IconCamera,
  IconCameraFilled,
  IconHeart,
  IconHeartFilled,
  IconBrandGithub,
  IconBrandGoogle,
  IconAlertTriangle,
  IconAlertCircle,
  IconCircleCheck,
  IconXboxX,
} from "angular-tabler-icons/icons";

const ICONS = {
  IconCamera,
  IconCameraFilled,
  IconHeart,
  IconHeartFilled,
  IconBrandGithub,
  IconBrandGoogle,
  IconAlertTriangle,
  IconAlertCircle,
  IconCircleCheck,
  IconXboxX,
} as const;

@NgModule({
  imports: [TablerIconsModule.pick(ICONS)],
  exports: [TablerIconsModule],
})
export class IconsModule {}
