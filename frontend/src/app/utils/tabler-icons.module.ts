import { NgModule } from "@angular/core";

import { TablerIconsModule } from "angular-tabler-icons";
import {
  IconBrandGoogle,
  IconAlertTriangle,
  IconAlertCircle,
  IconCircleCheck,
  IconXboxX,
  IconSettings,
  IconCalendarWeek,
  IconUserHeart,
  IconFileDollar,
  IconSun,
  IconMoon,
  IconSearch,
  IconChevronDown,
  IconUserCircle,
  IconLogout2,
  IconDeviceDesktopCog,
  IconPackages,
  IconMenu,
  IconChartBar,
  IconEdit,
} from "angular-tabler-icons/icons";

const ICONS = {
  IconBrandGoogle,
  IconAlertTriangle,
  IconAlertCircle,
  IconCircleCheck,
  IconXboxX,
  IconSettings,
  IconCalendarWeek,
  IconUserHeart,
  IconFileDollar,
  IconSun,
  IconMoon,
  IconSearch,
  IconChevronDown,
  IconUserCircle,
  IconLogout2,
  IconDeviceDesktopCog,
  IconPackages,
  IconMenu,
  IconChartBar,
  IconEdit,
} as const;

@NgModule({
  imports: [TablerIconsModule.pick(ICONS)],
  exports: [TablerIconsModule],
})
export class IconsModule {}
