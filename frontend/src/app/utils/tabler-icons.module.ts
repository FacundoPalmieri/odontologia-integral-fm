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
  IconSettings,
  IconCalendarWeek,
  IconDashboard,
  IconUserHeart,
  IconFileDollar,
  IconSun,
  IconMoon,
  IconSearch,
  IconChevronDown,
  IconUserCircle,
  IconLogout2,
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
  IconSettings,
  IconCalendarWeek,
  IconDashboard,
  IconUserHeart,
  IconFileDollar,
  IconSun,
  IconMoon,
  IconSearch,
  IconChevronDown,
  IconUserCircle,
  IconLogout2,
} as const;

@NgModule({
  imports: [TablerIconsModule.pick(ICONS)],
  exports: [TablerIconsModule],
})
export class IconsModule {}
