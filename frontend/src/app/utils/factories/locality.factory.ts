export interface LocalityInterface {
  district: string;
  province: "CABA" | "Buenos Aires";
}

export class LocalityFactory {
  static createLocalities(): LocalityInterface[] {
    return [
      { district: "Ciudad Autónoma de Buenos Aires", province: "CABA" },
      { district: "Almirante Brown", province: "Buenos Aires" },
      { district: "Avellaneda", province: "Buenos Aires" },
      { district: "Berazategui", province: "Buenos Aires" },
      { district: "Esteban Echeverría", province: "Buenos Aires" },
      { district: "Ezeiza", province: "Buenos Aires" },
      { district: "Florencio Varela", province: "Buenos Aires" },
      { district: "General Rodríguez", province: "Buenos Aires" },
      { district: "General San Martín", province: "Buenos Aires" },
      { district: "Hurlingham", province: "Buenos Aires" },
      { district: "Ituzaingó", province: "Buenos Aires" },
      { district: "José C. Paz", province: "Buenos Aires" },
      { district: "La Matanza", province: "Buenos Aires" },
      { district: "Lanús", province: "Buenos Aires" },
      { district: "Lomas de Zamora", province: "Buenos Aires" },
      { district: "Malvinas Argentinas", province: "Buenos Aires" },
      { district: "Marcos Paz", province: "Buenos Aires" },
      { district: "Merlo", province: "Buenos Aires" },
      { district: "Moreno", province: "Buenos Aires" },
      { district: "Morón", province: "Buenos Aires" },
      { district: "Navarro", province: "Buenos Aires" },
      { district: "Pilar", province: "Buenos Aires" },
      { district: "Presidente Perón", province: "Buenos Aires" },
      { district: "Quilmes", province: "Buenos Aires" },
      { district: "San Fernando", province: "Buenos Aires" },
      { district: "San Isidro", province: "Buenos Aires" },
      { district: "San Miguel", province: "Buenos Aires" },
      { district: "San Vicente", province: "Buenos Aires" },
      { district: "Tigre", province: "Buenos Aires" },
      { district: "Tres de Febrero", province: "Buenos Aires" },
      { district: "Vicente López", province: "Buenos Aires" },
    ];
  }
}
