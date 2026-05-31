package com.astrabuild.events;

public enum EventType {
    NORMAL("Штатный режим", "Никаких эффектов."),
    ACTIVE_MINING("Активная копка", "Скорость копки +15%: кулдаун 5 тиков."),
    HEAVY_ROCK("Тяжелая порода", "Потребление энергии х2."),
    FUEL_SURPLUS("Профицит", "Любое топливо дает на 30% больше энергии."),
    LOGISTICS_BOOST("Форсаж труб", "Предметы летят со скоростью 2 тика на блок вместо 5."),
    DEEP_SCAN("Чистая руда", "Карьер уничтожает камень, булыжник, землю, гравий, андезит, диорит, гранит без траты энергии и не пускает их в трубы.");

    private final String title;
    private final String description;

    EventType(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
