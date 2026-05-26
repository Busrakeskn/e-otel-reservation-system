package com.eotel.model;

public enum OdemeDurumu {
    ODENMEDI("Ödenmedi"),
    ON_ODEME("Ön Ödeme"),
    TAM_ODENDI("Tam Ödendi");

    private final String label;

    OdemeDurumu(String label) { this.label = label; }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}
