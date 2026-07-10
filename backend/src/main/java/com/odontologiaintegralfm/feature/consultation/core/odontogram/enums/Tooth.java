package com.odontologiaintegralfm.feature.consultation.core.odontogram.enums;

/**
 * Enum que representa los dientes según nomenclatura FDI.
 */
public enum Tooth {

    /* Cuadrante 1 – Superior derecho */
    T11(11, "Incisivo central superior derecho"),
    T12(12, "Incisivo lateral superior derecho"),
    T13(13, "Canino superior derecho"),
    T14(14, "Primer premolar superior derecho"),
    T15(15, "Segundo premolar superior derecho"),
    T16(16, "Primer molar superior derecho"),
    T17(17, "Segundo molar superior derecho"),
    T18(18, "Tercer molar superior derecho"),

    /* Cuadrante 2 – Superior izquierdo */
    T21(21, "Incisivo central superior izquierdo"),
    T22(22, "Incisivo lateral superior izquierdo"),
    T23(23, "Canino superior izquierdo"),
    T24(24, "Primer premolar superior izquierdo"),
    T25(25, "Segundo premolar superior izquierdo"),
    T26(26, "Primer molar superior izquierdo"),
    T27(27, "Segundo molar superior izquierdo"),
    T28(28, "Tercer molar superior izquierdo"),

    /* Cuadrante 3 – Inferior izquierdo */
    T31(31, "Incisivo central inferior izquierdo"),
    T32(32, "Incisivo lateral inferior izquierdo"),
    T33(33, "Canino inferior izquierdo"),
    T34(34, "Primer premolar inferior izquierdo"),
    T35(35, "Segundo premolar inferior izquierdo"),
    T36(36, "Primer molar inferior izquierdo"),
    T37(37, "Segundo molar inferior izquierdo"),
    T38(38, "Tercer molar inferior izquierdo"),

    /* Cuadrante 4 – Inferior derecho */
    T41(41, "Incisivo central inferior derecho"),
    T42(42, "Incisivo lateral inferior derecho"),
    T43(43, "Canino inferior derecho"),
    T44(44, "Primer premolar inferior derecho"),
    T45(45, "Segundo premolar inferior derecho"),
    T46(46, "Primer molar inferior derecho"),
    T47(47, "Segundo molar inferior derecho"),
    T48(48, "Tercer molar inferior derecho"),

    /**
     * Cuadrante 5 – Superior derecho (temporal)
     */
    T51(51, "Incisivo central temporal superior derecho"),
    T52(52, "Incisivo lateral temporal superior derecho"),
    T53(53, "Canino temporal superior derecho"),
    T54(54, "Primer molar temporal superior derecho"),
    T55(55, "Segundo molar temporal superior derecho"),

    /**
     * Cuadrante 6 – Superior izquierdo (temporal)
     */
    T61(61, "Incisivo central temporal superior izquierdo"),
    T62(62, "Incisivo lateral temporal superior izquierdo"),
    T63(63, "Canino temporal superior izquierdo"),
    T64(64, "Primer molar temporal superior izquierdo"),
    T65(65, "Segundo molar temporal superior izquierdo"),

    /**
     * Cuadrante 7 – Inferior izquierdo (temporal)
     */
    T71(71, "Incisivo central temporal inferior izquierdo"),
    T72(72, "Incisivo lateral temporal inferior izquierdo"),
    T73(73, "Canino temporal inferior izquierdo"),
    T74(74, "Primer molar temporal inferior izquierdo"),
    T75(75, "Segundo molar temporal inferior izquierdo"),

    /**
     * Cuadrante 8 – Inferior derecho (temporal)
     */
    T81(81, "Incisivo central temporal inferior derecho"),
    T82(82, "Incisivo lateral temporal inferior derecho"),
    T83(83, "Canino temporal inferior derecho"),
    T84(84, "Primer molar temporal inferior derecho"),
    T85(85, "Segundo molar temporal inferior derecho");

    private final int code;
    private final String description;

    Tooth(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Permite obtener el enum a partir del código FDI (ej: 11 → T11)
     */
    public static Tooth fromCode(int code) {
        for (Tooth tooth : values()) {
            if (tooth.code == code) {
                return tooth;
            }
        }
        throw new IllegalArgumentException("Código de diente inválido: " + code);
    }
}